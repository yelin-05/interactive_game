package com.example.minigame.module.quiz.vo;

/**
 * 逐题作答的即时反馈（服务端判出）。
 *
 * @param combo         答对后当前的连击数（答错为 0）
 * @param gained        本题得分（答对=基础分+连击加成，答错/超时=-5）
 */
public record QuizAnswerVO(
        boolean correct,
        int correctAnswer,
        int yourAnswer,
        String explanation,
        int combo,
        int gained
) {
}
