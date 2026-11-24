package com.example.minigame.module.quiz.vo;

/**
 * 难度档位卡片，前端难度选择用。
 */
public record QuizModeVO(
        String code,
        String name,
        String icon,
        String desc,
        int count,
        int secondsPerQuestion
) {
}
