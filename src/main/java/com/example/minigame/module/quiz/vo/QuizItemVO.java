package com.example.minigame.module.quiz.vo;

import java.util.List;

/**
 * 下发给前端的题目：**不含正确答案**。
 *
 * <p>早先的版本把 answerIndex 一起发下去，前端自己判题，打开控制台就能看答案。
 * 现在答案只留在服务端（见 QuizRoundService），作答时后端即时判对错、交卷后再结算。
 *
 * @param baseScore 该题答对的基础分（按难度分档：简单 10 / 中等 20 / 困难 30）
 */
public record QuizItemVO(
        Long id,
        String question,
        List<String> options,
        Integer difficulty,
        int baseScore
) {
}
