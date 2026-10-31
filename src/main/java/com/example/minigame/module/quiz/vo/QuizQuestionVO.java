package com.example.minigame.module.quiz.vo;

/**
 * 前端答题用的题目视图。
 *
 * <p>注意：answerIndex 目前直接下发给前端做即时判题，方便离线演示。
 * 要做防作弊（比如排行榜进前十）就得改成服务端判题：下发时不带答案，
 * 提交作答后由后端返回对错。这一步等接排行榜时再做。
 */
public record QuizQuestionVO(
        Long id,
        String category,
        String question,
        java.util.List<String> options,
        Integer answerIndex,
        String explanation,
        Integer difficulty
) {
}
