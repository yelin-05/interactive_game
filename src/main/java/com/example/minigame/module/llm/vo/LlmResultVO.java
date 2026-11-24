package com.example.minigame.module.llm.vo;

import java.util.List;

/**
 * 生成结果（当前是 Mock 模板拼装，接口形状按真实模型设计）。
 *
 * @param mode       生成通道：mock / real —— 自动补题靠它判断能不能入库
 * @param difficulty 目标难度 1/2/3（由调用方指定，模型不负责决定）
 */
public record LlmResultVO(
        String mode,
        String question,
        List<String> options,
        int answerIndex,
        String explanation,
        int difficulty
) {
}
