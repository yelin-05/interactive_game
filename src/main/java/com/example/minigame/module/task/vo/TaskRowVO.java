package com.example.minigame.module.task.vo;

/**
 * 任务列表的一行：任务定义 + 当前用户的进度与领取状态。
 *
 * @param progress   已完成局数（截断到 targetCount 以内，方便画进度条）
 * @param claimable  已完成且未领取
 * @param claimed    已领取过
 */
public record TaskRowVO(
        Long id,
        String title,
        String description,
        String category,
        String categoryName,
        int targetCount,
        int progress,
        int rewardPoints,
        boolean claimable,
        boolean claimed
) {
}
