package com.example.minigame.module.quiz.dto;

import lombok.Data;

import java.util.List;

/**
 * 交卷请求体。
 *
 * <p>只上报「每道题选了哪个」，不上报分数和用时 ——
 * 分数、对错、耗时全部由服务端计算，前端改不了。
 */
@Data
public class QuizSubmitForm {

    /** 开局时拿到的 roundId */
    private String roundId;

    /** 每题所选下标，未作答可用 -1 或省略 */
    private List<Integer> answers;
}
