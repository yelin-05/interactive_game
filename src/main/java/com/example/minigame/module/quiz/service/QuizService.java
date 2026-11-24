package com.example.minigame.module.quiz.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.minigame.module.quiz.QuizCategories;
import com.example.minigame.module.quiz.QuizMode;
import com.example.minigame.module.quiz.entity.Question;
import com.example.minigame.module.quiz.mapper.QuestionMapper;
import com.example.minigame.module.quiz.vo.QuizCategoryVO;
import com.example.minigame.module.quiz.vo.QuizQuestionVO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuizService {

    private final QuestionMapper questionMapper;
    private final ObjectMapper objectMapper;

    /** 全部分区 + 每个分区的可用题量。 */
    public List<QuizCategoryVO> categories() {
        return QuizCategories.ALL.stream()
                .map(d -> new QuizCategoryVO(d.code(), d.name(), d.icon(), d.desc(), countByCategory(d.code())))
                .toList();
    }

    public long countByCategory(String category) {
        return questionMapper.selectCount(Wrappers.<Question>lambdaQuery()
                .eq(Question::getCategory, category)
                .eq(Question::getStatus, 1));
    }

    /** 题库总题数（含已下架），后台统计用。 */
    public long total() {
        return questionMapper.selectCount(null);
    }

    /** 最新入库的若干题，后台列表用。 */
    public List<QuizQuestionVO> latest(int limit) {
        int size = Math.max(1, Math.min(limit, 50));
        return questionMapper.selectList(Wrappers.<Question>lambdaQuery()
                        .orderByDesc(Question::getId)
                        .last("limit " + size))
                .stream()
                .map(this::toVO)
                .toList();
    }

    /** 按分区 + 难度档随机抽题，并按难度递进排列（简单在前、困难在后）。 */
    public List<QuizQuestionVO> randomQuestions(String category, QuizMode mode) {
        if (!QuizCategories.exists(category)) {
            throw new IllegalArgumentException("未知分区: " + category);
        }
        List<Question> pool = new ArrayList<>(questionMapper.selectList(Wrappers.<Question>lambdaQuery()
                .eq(Question::getCategory, category)
                .eq(Question::getStatus, 1)
                .in(Question::getDifficulty, mode.difficulties())));
        Collections.shuffle(pool);

        return pool.stream()
                .limit(mode.count())
                .sorted(Comparator.comparingInt(q -> q.getDifficulty() == null ? 1 : q.getDifficulty()))
                .map(this::toVO)
                .toList();
    }

    /** 按 id 批量取题，保持传入顺序；含答案与解析，只给服务端判分用。 */
    public List<QuizQuestionVO> byIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        Map<Long, Question> map = questionMapper.selectByIds(ids).stream()
                .collect(Collectors.toMap(Question::getId, q -> q));
        return ids.stream()
                .map(map::get)
                .filter(Objects::nonNull)
                .map(this::toVO)
                .toList();
    }

    private QuizQuestionVO toVO(Question q) {
        return new QuizQuestionVO(
                q.getId(),
                q.getCategory(),
                q.getQuestion(),
                parseOptions(q.getOptionsJson()),
                q.getAnswerIndex(),
                q.getExplanation(),
                q.getDifficulty()
        );
    }

    private List<String> parseOptions(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {
            });
        } catch (Exception e) {
            log.warn("题目选项 JSON 解析失败, question 相关字段将为空: {}", json, e);
            return List.of();
        }
    }
}
