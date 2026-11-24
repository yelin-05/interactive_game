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
        int comboBonus,
        int penalty,
        int speedBonus,
        int maxCombo,
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
     * @param gained        本题得分（答对=基础分+连击，答错/超时=-5）
     */
    public record Detail(
            Long questionId,
            String question,
            int correctAnswer,
            int yourAnswer,
            boolean correct,
            String explanation,
            int difficulty,
            int gained
    ) {
    }
}
