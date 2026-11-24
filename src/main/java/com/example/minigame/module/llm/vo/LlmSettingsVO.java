package com.example.minigame.module.llm.vo;

/**
 * 「接入大模型」面板要显示的内容：当前生效值 + Key 的掩码 + 配置来源。
 *
 * <p>刻意不带明文 Key：页面只需要知道「配没配 / 是哪一个」，不需要再看到完整密钥。
 *
 * @param mode           当前通道：mock / real
 * @param baseUrl        请求地址，回填到表单
 * @param model          模型名，回填到表单
 * @param keyConfigured  是否已保存 Key
 * @param keyHint        Key 掩码（未配置时为 null）
 * @param jsonFormat     是否强制 JSON 模式
 * @param timeoutSeconds 请求超时（秒）
 * @param source         当前值来源：application.yml / 本地配置文件 / 页面保存
 * @param file           本地配置文件路径（页面提示「保存在哪」）
 */
public record LlmSettingsVO(
        String mode,
        String baseUrl,
        String model,
        boolean keyConfigured,
        String keyHint,
        boolean jsonFormat,
        int timeoutSeconds,
        String source,
        String file
) {
}
