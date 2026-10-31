package com.example.minigame.module.quiz.vo;

/**
 * 个人战绩统计。
 */
public record MyStatsVO(
        long rounds,
        int bestScore,
        int accuracyPercent,
        long globalRank
) {
    public static MyStatsVO empty() {
        return new MyStatsVO(0, 0, 0, 0);
    }
}
