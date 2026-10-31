package com.example.minigame.module.quiz.vo;

import java.time.LocalDateTime;

/**
 * 排行榜一行。
 */
public record RankRowVO(
        int rank,
        String nickname,
        int score,
        int correct,
        int total,
        int durationSeconds,
        LocalDateTime createdAt
) {
}
