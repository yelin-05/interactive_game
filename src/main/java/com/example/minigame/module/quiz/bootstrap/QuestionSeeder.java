package com.example.minigame.module.quiz.bootstrap;

import com.example.minigame.module.quiz.entity.Question;
import com.example.minigame.module.quiz.mapper.QuestionMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.List;

/**
 * 题库初始化：表里没题时，把 resources/seed/questions.json 灌进去。
 *
 * <p>为什么要放在代码里而不是纯 SQL 脚本：
 * 同一份题目要能同时灌进 MySQL 和 H2，用 SQL 就得维护两套 INSERT；
 * 放在 JSON 里一份数据通吃，而且扩充题库只需要改这一个文件。
 *
 * <p>幂等：只在 t_question 为空时执行，不会覆盖你后来在数据库里加的题。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class QuestionSeeder implements ApplicationRunner {

    private static final String SEED_FILE = "seed/questions.json";

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
        Long existing = questionMapper.selectCount(null);
        if (existing != null && existing > 0) {
            log.info("题库已存在 {} 道题，跳过初始化", existing);
            return;
        }

        ClassPathResource resource = new ClassPathResource(SEED_FILE);
        if (!resource.exists()) {
            log.warn("未找到题库种子文件 {}，跳过初始化", SEED_FILE);
            return;
        }

        List<SeedQuestion> seeds;
        try (InputStream in = resource.getInputStream()) {
            seeds = objectMapper.readValue(in, objectMapper.getTypeFactory()
                    .constructCollectionType(List.class, SeedQuestion.class));
        }

        int inserted = 0;
        for (SeedQuestion s : seeds) {
            if (s.category() == null || s.question() == null || s.options() == null) {
                log.warn("跳过字段不全的题目: {}", s.question());
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
            inserted++;
        }
        log.info("题库初始化完成，导入 {} 道题（共 {} 条）", inserted, questionMapper.selectCount(null));
    }
}
