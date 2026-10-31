package com.example.minigame.module.quiz.vo;

/**
 * 分区卡片：某个分区下有多少道可用题目。
 */
public record QuizCategoryVO(String code, String name, String icon, String desc, long count) {
}
