package com.example.minigame.module.quiz.dto;

import lombok.Data;

/**
 * 逐题作答请求体（即时反馈用）。
 */
@Data
public class QuizAnswerForm {

    /** 开局时拿到的 roundId */
    private String roundId;

    /** 当前题序号（从 0 开始） */
    private Integer index;

    /** 所选选项下标，-1 表示超时未作答 */
    private Integer answer;
}
