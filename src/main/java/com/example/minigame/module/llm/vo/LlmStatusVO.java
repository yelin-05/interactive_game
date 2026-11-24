package com.example.minigame.module.llm.vo;

import java.time.LocalDateTime;

/**
 * 大模型通道的实时状态：/{@code llm} 页面、{@code GET /api/llm/status} 与排查共用。
 *
 * <p>为什么要这个：真实通道失败时设计上是「回落 mock、不打断业务」，
 * 但这样一来页面上只会看到一道占位题，根本看不出是 Key 没配、网络不通还是模型返回了脏 JSON。
 * 把最近一次失败原因和调用计数暴露出来，才能一眼定位。
 *
 * @param mode           当前 Bean 的通道：real（真实模型）/ mock（占位模板）
 * @param baseUrl        真实通道的请求地址，mock 通道为 null
 * @param model          模型名，mock 通道为 null
 * @param keyConfigured  是否配了 API Key
 * @param jsonFormat     是否强制 {@code response_format=json_object}
 * @param calls          累计调用次数
 * @param successes      成功拿到真实题目的次数
 * @param fallbacks      回落到 mock 的次数
 * @param lastSuccessAt  最近一次成功时间
 * @param lastErrorAt    最近一次失败时间
 * @param lastError      最近一次失败原因（HTTP 状态 + 响应体片段 / 异常信息）
 */
public record LlmStatusVO(
        String mode,
        String baseUrl,
        String model,
        boolean keyConfigured,
        boolean jsonFormat,
        long calls,
        long successes,
        long fallbacks,
        LocalDateTime lastSuccessAt,
        LocalDateTime lastErrorAt,
        String lastError
) {

    /** mock 通道的状态：不联网，没有 Key / 地址 / 模型，也不会有网络错误。 */
    public static LlmStatusVO mock() {
        return new LlmStatusVO("mock", null, null, false, false, 0L, 0L, 0L, null, null, null);
    }
}
