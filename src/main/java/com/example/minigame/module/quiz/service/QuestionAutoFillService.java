package com.example.minigame.module.quiz.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.minigame.module.llm.service.LlmService;
import com.example.minigame.module.llm.vo.LlmResultVO;
import com.example.minigame.module.quiz.QuizCategories;
import com.example.minigame.module.quiz.entity.Question;
import com.example.minigame.module.quiz.mapper.QuestionMapper;
import com.example.minigame.module.quiz.vo.AutoFillReportVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 题库自动补充：让游戏自己调大模型出题、校验后写进 t_question。
 *
 * <p>为什么按「难度」补而不是按总数补：三个档位抽题的难度范围不一样
 * （简单档只抽难度 1、普通档抽 1+2、困难档抽 2+3 且一局要 10 题）。
 * 只补总数会出现「困难档题不够、一局只发 8 题」的坑，所以这里按难度分别设目标：
 * 简单 {@code easy}、中等 {@code normal}、困难 {@code hard}
 * （默认 8 / 6 / 6，保证困难档 2+3 共 12 道，抽 10 题还有余量）。
 *
 * <p>四道闸门，防止把垃圾题写进正式题库：
 * <ol>
 *   <li>通道闸：mock 通道生成的是占位题，除非 allow-mock=true 否则一律不写；</li>
 *   <li>格式闸：题干/选项/答案下标/解析逐项校验（见 {@link #validate}）；</li>
 *   <li>重复闸：以「分区 + 题干」为键，和库里已有题目、本次已插入的题目都去重；</li>
 *   <li>预算闸：单次最多 max-per-run 道，避免一次跑太久把请求/启动拖死。</li>
 * </ol>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QuestionAutoFillService {

    /** 各难度的目标题量（每个分区单独计算）。 */
    @Value("${app.quiz.auto-fill.target.easy:8}")
    private int easyTarget;

    @Value("${app.quiz.auto-fill.target.normal:6}")
    private int normalTarget;

    @Value("${app.quiz.auto-fill.target.hard:6}")
    private int hardTarget;

    /** 单次运行最多入库多少道。 */
    @Value("${app.quiz.auto-fill.max-per-run:12}")
    private int maxPerRun;

    /** mock 通道（占位题）是否允许入库，仅供离线演示。 */
    @Value("${app.quiz.auto-fill.allow-mock:false}")
    private boolean allowMock;

    private static final int MAX_QUESTION_LENGTH = 500;
    private static final int MAX_EXPLANATION_LENGTH = 1000;
    private static final int MAX_OPTION_LENGTH = 120;
    private static final int OPTION_COUNT = 4;

    private final QuestionMapper questionMapper;
    private final ObjectMapper objectMapper;
    private final LlmService llmService;

    /* ---------------- 对外入口 ---------------- */

    /** 遍历五个分区，把缺口补到目标题量。 */
    public AutoFillReportVO fillAll() {
        List<String> notes = new ArrayList<>();
        int inserted = 0;
        int rejected = 0;
        String mode = "";

        Set<String> existing = existingKeys();
        for (QuizCategories.Def def : QuizCategories.ALL) {
            int budget = maxPerRun - inserted;
            if (budget <= 0) {
                notes.add("已达单次上限 " + maxPerRun + " 道，本轮剩余分区不再补");
                break;
            }
            FillResult one = fillCategory(def, existing, budget);
            inserted += one.inserted();
            rejected += one.rejected();
            if (one.mode() != null && !one.mode().isBlank()) {
                mode = one.mode();
            }
            notes.add(def.name() + "：" + one.note());
            if (one.aborted()) {
                break;
            }
        }

        log.info("自动补题结束：新增 {} 道，拒收 {} 道，通道={}", inserted, rejected, mode);
        return new AutoFillReportVO(mode, inserted, rejected, notes);
    }

    /* ---------------- 单个分区 ---------------- */

    private FillResult fillCategory(QuizCategories.Def def, Set<String> existing, int budget) {
        Map<Integer, Long> counts = countsByDifficulty(def.code());
        StringBuilder note = new StringBuilder(String.format("现有 简单%d/中等%d/困难%d；",
                counts.getOrDefault(1, 0L), counts.getOrDefault(2, 0L), counts.getOrDefault(3, 0L)));

        int inserted = 0;
        int rejected = 0;
        String mode = "";

        for (int difficulty = 1; difficulty <= 3; difficulty++) {
            int gap = target(difficulty) - counts.getOrDefault(difficulty, 0L).intValue();
            if (gap <= 0) {
                continue;
            }
            int attempts = 0;
            int maxAttempts = gap * 3;          // 生成可能被校验拒掉，留点重试余量
            while (gap > 0 && attempts < maxAttempts && inserted < budget) {
                attempts++;
                LlmResultVO result;
                try {
                    result = llmService.generate(def.code(), def.name(), null, difficulty);
                } catch (Exception e) {
                    note.append("大模型调用失败（").append(e.getMessage()).append("），本轮停止");
                    return new FillResult(inserted, rejected, mode, note.toString(), true);
                }
                if (result == null) {
                    rejected++;
                    continue;
                }
                mode = result.mode() == null ? "" : result.mode();

                // 闸门 1：mock 通道的占位题默认不入库
                if (!"real".equalsIgnoreCase(mode) && !allowMock) {
                    note.append("当前是 mock 通道（未配置 LLM_API_KEY），按配置不写入占位题");
                    return new FillResult(inserted, rejected, mode, note.toString(), true);
                }

                String error = validate(result);
                String key = error == null ? key(def.code(), result.question()) : null;
                if (error == null && existing.contains(key)) {
                    error = "题干与已有题目重复";
                }
                if (error == null) {
                    insert(def.code(), result, difficulty);
                    existing.add(key);
                    inserted++;
                    gap--;
                    continue;
                }
                rejected++;
                note.append("拒收（难度").append(difficulty).append("）").append(error).append("；");
            }
            if (gap > 0) {
                note.append("难度").append(difficulty).append(" 仍差 ").append(gap).append(" 道；");
            }
        }

        if (inserted > 0) {
            note.append("新增 ").append(inserted).append(" 道");
        } else if (rejected == 0 && note.indexOf("拒收") < 0) {
            note.append("题量已达标");
        }
        return new FillResult(inserted, rejected, mode, note.toString(), false);
    }

    private int target(int difficulty) {
        return switch (difficulty) {
            case 1 -> easyTarget;
            case 2 -> normalTarget;
            default -> hardTarget;
        };
    }

    private Map<Integer, Long> countsByDifficulty(String category) {
        return questionMapper.selectList(Wrappers.<Question>lambdaQuery()
                        .eq(Question::getCategory, category)
                        .eq(Question::getStatus, 1))
                .stream()
                .collect(Collectors.groupingBy(
                        q -> q.getDifficulty() == null ? 1 : q.getDifficulty(),
                        Collectors.counting()));
    }

    private Set<String> existingKeys() {
        return questionMapper.selectList(null).stream()
                .map(q -> key(q.getCategory(), q.getQuestion()))
                .collect(Collectors.toCollection(HashSet::new));
    }

    /* ---------------- 校验与入库 ---------------- */

    /**
     * 校验一道生成出来的题，返回 null 表示合格。
     *
     * <p>这里拦的是「结构上就不能用」的题；事实正确性模型自己保证不了，
     * 所以配置里留了 allow-mock 之外的余量 —— 真实通道的题也建议先看后台日志。
     */
    private String validate(LlmResultVO r) {
        String stem = r.question() == null ? "" : r.question().trim();
        if (stem.isEmpty()) {
            return "题干为空";
        }
        if (stem.length() > MAX_QUESTION_LENGTH) {
            return "题干超过 " + MAX_QUESTION_LENGTH + " 字";
        }
        List<String> options = r.options();
        if (options == null || options.size() != OPTION_COUNT) {
            return "选项不是 " + OPTION_COUNT + " 个";
        }
        Set<String> seen = new HashSet<>();
        for (String raw : options) {
            String opt = raw == null ? "" : raw.trim();
            if (opt.isEmpty()) {
                return "存在空选项";
            }
            if (opt.length() > MAX_OPTION_LENGTH) {
                return "选项超过 " + MAX_OPTION_LENGTH + " 字";
            }
            if (!seen.add(opt.toLowerCase())) {
                return "选项重复：" + opt;
            }
        }
        int idx = r.answerIndex();
        if (idx < 0 || idx >= options.size()) {
            return "答案下标越界：" + idx;
        }
        String exp = r.explanation() == null ? "" : r.explanation().trim();
        if (exp.isEmpty()) {
            return "解析为空";
        }
        if (exp.length() > MAX_EXPLANATION_LENGTH) {
            return "解析超过 " + MAX_EXPLANATION_LENGTH + " 字";
        }
        return null;
    }

    private void insert(String category, LlmResultVO r, int difficulty) {
        List<String> options = r.options().stream().map(String::trim).toList();
        Question q = new Question();
        q.setCategory(category);
        q.setQuestion(r.question().trim());
        try {
            q.setOptionsJson(objectMapper.writeValueAsString(options));
        } catch (Exception e) {
            // 走到这里说明选项里有无法序列化的内容，直接抛出由上层记日志
            throw new IllegalStateException("选项序列化失败", e);
        }
        q.setAnswerIndex(r.answerIndex());
        q.setExplanation(r.explanation().trim());
        q.setDifficulty(difficulty);
        q.setStatus(1);
        questionMapper.insert(q);
        log.debug("自动补题入库：分区={}，难度={}，题干={}", category, difficulty, q.getQuestion());
    }

    private static String key(String category, String question) {
        return category + "::" + (question == null ? "" : question.trim());
    }

    /** 单个分区的执行结果。aborted=true 表示该分区遇到不可继续的情况（mock 通道 / 调用异常）。 */
    private record FillResult(int inserted, int rejected, String mode, String note, boolean aborted) {
    }
}
