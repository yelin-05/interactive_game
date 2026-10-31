package com.example.minigame.module.quiz.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.minigame.module.quiz.QuizCategories;
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
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuizService {

    private static final int MAX_LIMIT = 20;

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

    /** 按分区随机抽题。题量不够时有多少给多少。 */
    public List<QuizQuestionVO> randomQuestions(String category, int limit) {
        if (!QuizCategories.exists(category)) {
            throw new IllegalArgumentException("未知分区: " + category);
        }
        int size = Math.max(1, Math.min(limit, MAX_LIMIT));

        List<Question> pool = new ArrayList<>(questionMapper.selectList(Wrappers.<Question>lambdaQuery()
                .eq(Question::getCategory, category)
                .eq(Question::getStatus, 1)));
        Collections.shuffle(pool);

        return pool.stream().limit(size).map(this::toVO).toList();
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
