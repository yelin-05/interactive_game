package com.example.minigame.module.quiz.vo;

import java.util.List;

/**
 * 判分用的题目视图：**含正确答案与解析，只允许在服务端内部流转**。
 *
 * <p>下发给浏览器的题目是 {@link QuizItemVO}，那个不带 answerIndex。
 * 本类只出现在 {@code QuizService.byIds()} → {@code QuizRoundService.submit()} 这条判分链路里，
 * 任何 Controller 都不要直接把它序列化返回。
 */
public record QuizQuestionVO(
        Long id,
        String category,
        String question,
        List<String> options,
        Integer answerIndex,
        String explanation,
        Integer difficulty
) {
}
