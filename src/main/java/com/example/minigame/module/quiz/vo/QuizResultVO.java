package com.example.minigame.module.quiz.vo;

import java.util.List;

/**
 * 交卷后的判分结果（服务端算出来的）。
 *
 * @param saved 是否已记入 t_score（未登录时为 false）
 */
public record QuizResultVO(
        int score,
        int baseScore,
        int speedBonus,
        int correct,
        int total,
        int durationMs,
        boolean saved,
        List<Detail> details
) {

    /**
     * 单题结果。
     *
     * @param correctAnswer 正确选项下标
     * @param yourAnswer    你的选择，-1 表示未作答/超时
     */
    public record Detail(
            Long questionId,
            String question,
            int correctAnswer,
            int yourAnswer,
            boolean correct,
            String explanation
    ) {
    }
}
