package com.example.minigame.module.llm.dto;

import lombok.Data;

/**
 * {@code /llm} 页面「接入大模型」表单的绑定对象。
 *
 * <p>字段名与模板里的 {@code name} 一一对应；{@code apiKey} 留空表示不修改已保存的 Key，
 * {@code clearKey} 勾上才真的清空（见 {@code LlmSettings#apply}）。
 */
@Data
public class LlmSettingsForm {

    /** mock / real */
    private String mode = "mock";

    /** OpenAI 兼容的 chat/completions 地址 */
    private String baseUrl;

    /** 模型名，如 deepseek-chat */
    private String model;

    /** 新 Key；留空 = 保持原值 */
    private String apiKey;

    /** 是否发送 response_format=json_object（Ollama 等不支持要关掉） */
    private Boolean jsonFormat;

    /** 勾上 = 清掉已保存的 Key */
    private Boolean clearKey;

    /** 请求超时（秒） */
    private Integer timeoutSeconds;
}
