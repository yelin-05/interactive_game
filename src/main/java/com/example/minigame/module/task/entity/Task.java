package com.example.minigame.module.task.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 任务定义 t_task。
 *
 * <p>进度不单独建表 —— 直接按成绩表里「符合条件的局数」实时算出来，
 * 这样任务和成绩永远一致，也不会出现"进度表没同步"的脏数据。
 */
@Data
@TableName("t_task")
public class Task {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    private String description;

    /** 限定分区，null 表示任意分区都算 */
    private String category;

    /** 需要完成的局数 */
    private Integer targetCount;

    /** 完成后奖励积分 */
    private Integer rewardPoints;

    /** 1 上架 0 下架 */
    private Integer status;

    private LocalDateTime createdAt;
}
