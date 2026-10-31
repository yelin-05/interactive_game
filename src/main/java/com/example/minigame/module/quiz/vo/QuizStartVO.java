package com.example.minigame.module.quiz.vo;

import java.util.List;

/**
 * 开局响应。
 *
 * @param roundId           本局唯一编号，提交时带上
 * @param category          分区 code
 * @param categoryName      分区中文名
 * @param secondsPerQuestion 每题限时秒数（前端倒计时用）
 * @param questions         题目列表（不含答案）
 */
public record QuizStartVO(
        String roundId,
        String category,
        String categoryName,
        int secondsPerQuestion,
        List<QuizItemVO> questions
) {
}
