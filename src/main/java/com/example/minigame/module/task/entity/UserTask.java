package com.example.minigame.module.task.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 任务领取记录 t_user_task，(user_id, task_id) 唯一，防止重复领奖。
 */
@Data
@TableName("t_user_task")
public class UserTask {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long taskId;

    /** 领取时的奖励快照，后续调任务奖励不影响历史 */
    private Integer rewardPoints;

    private LocalDateTime createdAt;
}
