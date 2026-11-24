package com.example.minigame.module.quiz.bootstrap;

import com.example.minigame.module.quiz.entity.Question;
import com.example.minigame.module.quiz.mapper.QuestionMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 题库初始化：表里没题时，把 resources/seed 下的题库文件灌进去。
 *
 * <p>为什么要放在代码里而不是纯 SQL 脚本：
 * 同一份题目要能同时灌进 MySQL 和 H2，用 SQL 就得维护两套 INSERT；
 * 放在 JSON 里一份数据通吃，而且扩充题库只需要改这些文件。
 *
 * <p>题库按分区拆成了多个文件（{@code questions.json} 是最早的一批，
 * {@code questions-extra-*.json} 是后补的大批题），这样一次扩充只动一个文件，
 * 不会因为一个超大 JSON 改坏而整库灌不进去。
 *
 * <p>幂等：按「分区 + 题干」增量补齐，不会覆盖你后来在数据库里加的题。
 *
 * <p>{@code @Order(10)}：必须排在 {@link QuestionAutoFiller}（20）之前，
 * 先把种子题灌进去，自动补题算缺口时才是准的。
 */
@Slf4j
@Component
@Order(10)
@RequiredArgsConstructor
public class QuestionSeeder implements ApplicationRunner {

    /** 题库文件清单：新增一批题就加一行，缺失的文件会跳过并告警。 */
    private static final List<String> SEED_FILES = List.of(
            "seed/questions.json",
            "seed/questions-extra-computer-a.json",
            "seed/questions-extra-computer-b.json",
            "seed/questions-extra-literature-a.json",
            "seed/questions-extra-literature-b.json",
            "seed/questions-extra-geography-a.json",
            "seed/questions-extra-geography-b.json",
            "seed/questions-extra-history-a.json",
            "seed/questions-extra-history-b.json",
            "seed/questions-extra-science-a.json",
            "seed/questions-extra-science-b.json");

    private final QuestionMapper questionMapper;
    private final ObjectMapper objectMapper;

    /** 种子文件的 JSON 结构 */
    private record SeedQuestion(
            String category,
            String question,
            List<String> options,
            Integer answerIndex,
            String explanation,
            Integer difficulty
    ) {
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        // 以「分区 + 题干」为幂等键增量补齐：已存在的题跳过，新题插入。
        // 这样不会覆盖后台改过的题，题库 JSON 扩充后也能自动补进老库（如新增困难题）。
        Set<String> existing = questionMapper.selectList(null).stream()
                .map(q -> key(q.getCategory(), q.getQuestion()))
                .collect(Collectors.toSet());

        int inserted = 0;
        int skipped = 0;
        for (String seedFile : SEED_FILES) {
            ClassPathResource resource = new ClassPathResource(seedFile);
            if (!resource.exists()) {
                log.warn("未找到题库文件 {}，跳过", seedFile);
                continue;
            }
            List<SeedQuestion> seeds;
            try (InputStream in = resource.getInputStream()) {
                seeds = objectMapper.readValue(in, objectMapper.getTypeFactory()
                        .constructCollectionType(List.class, SeedQuestion.class));
            } catch (Exception e) {
                // 单个文件写坏（比如多了一个逗号）不该让应用起不来：记日志继续灌别的文件
                log.error("题库文件 {} 解析失败，已跳过：{}", seedFile, e.getMessage());
                continue;
            }
            int fileInserted = 0;
            int fileSkipped = 0;
            for (SeedQuestion s : seeds) {
                if (s.category() == null || s.question() == null || s.options() == null) {
                    log.warn("跳过字段不全的题目: {}", s.question());
                    continue;
                }
                if (existing.contains(key(s.category(), s.question()))) {
                    fileSkipped++;
                    continue;
                }
                Question q = new Question();
                q.setCategory(s.category());
                q.setQuestion(s.question());
                q.setOptionsJson(objectMapper.writeValueAsString(s.options()));
                q.setAnswerIndex(s.answerIndex() == null ? 0 : s.answerIndex());
                q.setExplanation(s.explanation());
                q.setDifficulty(s.difficulty() == null ? 1 : s.difficulty());
                q.setStatus(1);
                questionMapper.insert(q);
                existing.add(key(s.category(), s.question()));
                fileInserted++;
            }
            log.info("题库文件 {}：新增 {} 道，跳过已存在 {} 道", seedFile, fileInserted, fileSkipped);
            inserted += fileInserted;
            skipped += fileSkipped;
        }
        log.info("题库补齐完成：新增 {} 道，跳过已存在 {} 道，当前共 {} 道",
                inserted, skipped, questionMapper.selectCount(null));
    }

    private static String key(String category, String question) {
        return category + "::" + question;
    }
}
