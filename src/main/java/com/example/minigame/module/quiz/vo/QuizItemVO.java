package com.example.minigame.module.quiz.vo;

import java.util.List;

/**
 * 下发给前端的题目：**不含正确答案**。
 *
 * <p>早先的版本把 answerIndex 一起发下去，前端自己判题，打开控制台就能看答案。
 * 现在答案只留在服务端（见 QuizRoundService），提交后再由后端判分。
 */
public record QuizItemVO(
        Long id,
        String question,
        List<String> options,
        Integer difficulty
) {
}
