package com.example.minigame.module.llm.service;

import com.example.minigame.module.llm.vo.LlmResultVO;
import com.example.minigame.module.llm.vo.LlmStatusVO;

/**
 * 大模型出题能力抽象：题库自动补充、/llm 页面都只依赖这个接口。
 *
 * <p>实现有三层，最终由 {@link RoutingLlmService}（{@code @Primary}）统一对外：
 * <ul>
 *   <li>{@link MockLlmService}（默认 mock）：离线模板拼题，答辩现场没网也能演示链路；</li>
 *   <li>{@link RealLlmService}（real）：OpenAI 兼容的 <code>/chat/completions</code> 真实调用；</li>
 *   <li>{@link RoutingLlmService}：读 {@link LlmSettings}，决定这次走 mock 还是 real。</li>
 * </ul>
 *
 * <p>走哪个通道可以在 {@code /llm} 页面直接改（保存即生效），也能用
 * <code>app.llm.mode</code> / <code>LLM_MODE</code> 设初值。
 *
 * <p>返回值里带着 <code>mode</code>，调用方（如自动补题）据此判断
 * 「这是真实模型生成的题，还是占位题」，避免把占位题写进正式题库。
 */
public interface LlmService {

    /**
     * 生成一道四选一单选题。
     *
     * <p>刻意不出批量接口：一次一题，失败只影响这一题，自动补题时容易重试与判废。
     *
     * @param categoryCode 分区 code（computer / literature / geography / history / science）
     * @param categoryName 分区中文名，写进 prompt
     * @param topic        主题提示，可为空（自动补题时不指定，让模型自选知识点）
     * @param difficulty   目标难度 1 简单 / 2 中等 / 3 困难
     */
    LlmResultVO generate(String categoryCode, String categoryName, String topic, int difficulty);

    /**
     * 当前通道状态：有没有配 Key、最近一次失败原因是什么。
     *
     * <p>真实通道失败会自动回落 mock，不抛异常；错误就靠这个方法暴露给页面。
     */
    LlmStatusVO status();
}

