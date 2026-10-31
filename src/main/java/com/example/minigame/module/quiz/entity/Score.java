package com.example.minigame.module.quiz.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 答题成绩 t_score，一局一行。
 *
 * <p>耗时由服务端从「开局」到「提交」计时，不信任前端上报的数字。
 */
@Data
@TableName("t_score")
public class Score {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    /** 分区 code */
    private String category;

    private Integer score;

    private Integer correctCount;

    private Integer totalCount;

    /** 整局耗时，毫秒 */
    private Integer durationMs;

    private LocalDateTime createdAt;
}
