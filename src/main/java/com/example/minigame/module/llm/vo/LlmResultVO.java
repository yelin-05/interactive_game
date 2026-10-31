package com.example.minigame.module.llm.vo;

import java.util.List;

/**
 * 生成结果（当前是 Mock 模板拼装，接口形状按真实模型设计）。
 *
 * @param mode 生成通道：mock / real
 */
public record LlmResultVO(
        String mode,
        String question,
        List<String> options,
        int answerIndex,
        String explanation
) {
}
