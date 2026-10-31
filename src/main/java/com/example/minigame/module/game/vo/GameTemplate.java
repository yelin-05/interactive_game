package com.example.minigame.module.game.vo;

/**
 * 游戏模板的介绍信息，用在 /games 选型页上。
 *
 * @param code       路由片段，如 reaction
 * @param name       模板名
 * @param tag        类型标签
 * @param desc       一句话玩法
 * @param duration   单局时长
 * @param difficulty 上手难度
 * @param modules    跟哪个业务模块强相关
 */
public record GameTemplate(
        String code,
        String name,
        String tag,
        String desc,
        String duration,
        String difficulty,
        String modules
) {
}
