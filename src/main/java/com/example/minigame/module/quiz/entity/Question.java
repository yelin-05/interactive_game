package com.example.minigame.module.quiz.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 题目表 t_question。
 *
 * <p>选项用 JSON 数组存（如 ["Tomcat","Jetty","Undertow","Netty"]），
 * 而不是 option_a..option_d 四个字段 —— 选项个数以后要能变（4 选 1 / 6 选 1）。
 */
@Data
@TableName("t_question")
public class Question {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 分区：computer / literature / geography / history / science */
    private String category;

    /** 题干 */
    private String question;

    /** 选项 JSON 数组字符串 */
    private String optionsJson;

    /** 正确选项下标，从 0 开始 */
    private Integer answerIndex;

    /** 答案解析，答错时展示 */
    private String explanation;

    /** 难度 1 简单 2 中等 3 困难 */
    private Integer difficulty;

    /** 1 启用 0 下架 */
    private Integer status;

    private LocalDateTime createdAt;
}
