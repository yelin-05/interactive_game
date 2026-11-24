package com.example.minigame.module.llm.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * 大模型通道的「运行时可改」配置。
 *
 * <p>以前接大模型只能设环境变量再重启应用，答辩现场演示一遍很不方便；
 * 现在把可调项收在这里，页面（{@code /llm}）保存后立即生效：
 * <ol>
 *   <li>启动时先读 {@code application.yml}（或 {@code LLM_*} 环境变量）作为初值；</li>
 *   <li>若本地配置文件存在（默认 {@code config/llm-settings.local.json}，已在 .gitignore），用它覆盖初值；</li>
 *   <li>页面保存 → 改内存 + 写回该文件，重启后依然有效。</li>
 * </ol>
 *
 * <p>API Key 只存在内存和这个本地文件里：不进仓库、页面只回显掩码。
 */
@Slf4j
@Component
public class LlmSettings {

    /** 只接受这两个通道值，避免手滑写成 real1 之后静默走 mock。 */
    private static final Set<String> MODES = Set.of("mock", "real");
    private static final int MIN_TIMEOUT = 3;
    private static final int MAX_TIMEOUT = 120;

    /** 某一时刻的只读快照：RealLlmService / 页面 / 接口都读它，避免读到半更新的状态。 */
    public record Snapshot(String mode, String baseUrl, String model, String apiKey,
                           boolean jsonFormat, int timeoutSeconds, String source, String file) {

        public boolean real() {
            return "real".equalsIgnoreCase(mode);
        }

        public boolean keyConfigured() {
            return apiKey != null && !apiKey.isBlank();
        }

        /** 页面回显用的掩码：只露前 3 位与后 4 位，短 Key 直接整体隐藏。 */
        public String keyHint() {
            if (!keyConfigured()) {
                return null;
            }
            String key = apiKey.trim();
            if (key.length() <= 10) {
                return "已保存（" + key.length() + " 位，出于安全不回显）";
            }
            return key.substring(0, 3) + "…" + key.substring(key.length() - 4);
        }
    }

    private final ObjectMapper objectMapper;
    private final Path file;

    private volatile String mode;
    private volatile String baseUrl;
    private volatile String model;
    private volatile String apiKey;
    private volatile boolean jsonFormat;
    private volatile int timeoutSeconds;
    /** 当前值来自哪里：application.yml / 本地配置文件 / 页面保存。 */
    private volatile String source = "application.yml";
    /** 最近一次写文件失败的原因——配了但没落盘时要提示「重启会丢」。 */
    private volatile String persistError;

    public LlmSettings(ObjectMapper objectMapper,
                       @Value("${app.llm.settings-file:config/llm-settings.local.json}") String settingsFile,
                       @Value("${app.llm.mode:mock}") String mode,
                       @Value("${app.llm.base-url:https://api.deepseek.com/v1/chat/completions}") String baseUrl,
                       @Value("${app.llm.model:deepseek-chat}") String model,
                       @Value("${app.llm.api-key:}") String apiKey,
                       @Value("${app.llm.json-format:true}") boolean jsonFormat,
                       @Value("${app.llm.timeout-seconds:20}") int timeoutSeconds) {
        this.objectMapper = objectMapper;
        this.file = Paths.get(settingsFile).toAbsolutePath().normalize();
        this.mode = normalizeMode(mode);
        this.baseUrl = baseUrl == null ? "" : baseUrl.trim();
        this.model = model == null ? "" : model.trim();
        this.apiKey = apiKey == null ? "" : apiKey.trim();
        this.jsonFormat = jsonFormat;
        this.timeoutSeconds = clampTimeout(timeoutSeconds);
        loadFromFile();
    }

    /* ---------------- 读 ---------------- */

    public Snapshot snapshot() {
        return new Snapshot(mode, baseUrl, model, apiKey, jsonFormat, timeoutSeconds, source, file.toString());
    }

    public String persistError() {
        return persistError;
    }

    /* ---------------- 写 ---------------- */

    /**
     * 页面保存入口：校验 → 改内存 → 写本地文件。
     *
     * <p>apiKey 留空表示「保持原 Key 不变」，只有勾了 clearKey 才是真的清空 ——
     * 否则每次改个模型名都得把 Key 重新粘一遍。
     *
     * @throws IllegalArgumentException 参数不合法，错误信息直接展示给用户
     */
    public synchronized Snapshot apply(String mode, String baseUrl, String model, String apiKey,
                                       boolean jsonFormat, int timeoutSeconds, boolean clearKey) {
        String nextMode = normalizeMode(mode);
        String nextUrl = baseUrl == null ? "" : baseUrl.trim();
        if (!isHttpUrl(nextUrl)) {
            throw new IllegalArgumentException(
                    "请求地址必须是 http(s):// 开头的完整地址，例如 https://api.deepseek.com/v1/chat/completions");
        }
        String nextModel = model == null ? "" : model.trim();
        if (nextModel.isEmpty()) {
            throw new IllegalArgumentException("模型名不能为空，例如 deepseek-chat / qwen-plus / glm-4-flash");
        }
        String typedKey = apiKey == null ? "" : apiKey.trim();
        String nextKey = clearKey ? "" : (typedKey.isEmpty() ? this.apiKey : typedKey);
        if ("real".equals(nextMode) && nextKey.isEmpty()) {
            throw new IllegalArgumentException("切到 real 通道必须填 API Key：Key 只写到本机配置文件，不会进仓库");
        }

        this.mode = nextMode;
        this.baseUrl = nextUrl;
        this.model = nextModel;
        this.apiKey = nextKey;
        this.jsonFormat = jsonFormat;
        this.timeoutSeconds = clampTimeout(timeoutSeconds);
        this.source = "页面保存";
        persist();
        log.info("大模型配置已更新：mode={} model={} url={} keyConfigured={} jsonFormat={} timeout={}s (来源={})",
                nextMode, nextModel, nextUrl, !nextKey.isEmpty(), jsonFormat, this.timeoutSeconds, source);
        return snapshot();
    }

    /* ---------------- 内部实现 ---------------- */

    private void loadFromFile() {
        if (!Files.isRegularFile(file)) {
            return;
        }
        try {
            JsonNode node = objectMapper.readTree(Files.readString(file));
            if (node.hasNonNull("mode")) {
                this.mode = normalizeMode(node.path("mode").asText());
            }
            if (node.hasNonNull("baseUrl")) {
                this.baseUrl = node.path("baseUrl").asText(this.baseUrl);
            }
            if (node.hasNonNull("model")) {
                this.model = node.path("model").asText(this.model);
            }
            if (node.has("apiKey")) {
                this.apiKey = node.path("apiKey").asText("");
            }
            if (node.has("jsonFormat")) {
                this.jsonFormat = node.path("jsonFormat").asBoolean(this.jsonFormat);
            }
            if (node.has("timeoutSeconds")) {
                this.timeoutSeconds = clampTimeout(node.path("timeoutSeconds").asInt(this.timeoutSeconds));
            }
            this.source = "本地配置文件";
            log.info("已加载本地大模型配置 {}：mode={} model={} url={} keyConfigured={}",
                    file, mode, model, baseUrl, !apiKey.isEmpty());
        } catch (IOException | RuntimeException e) {
            this.persistError = "读取本地配置文件失败：" + e.getMessage();
            log.warn("读取本地大模型配置失败，继续使用 application.yml 里的值：{}", file, e);
        }
    }

    private void persist() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("mode", mode);
        data.put("baseUrl", baseUrl);
        data.put("model", model);
        data.put("apiKey", apiKey);
        data.put("jsonFormat", jsonFormat);
        data.put("timeoutSeconds", timeoutSeconds);
        try {
            Path parent = file.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(file.toFile(), data);
            persistError = null;
        } catch (IOException e) {
            persistError = e.getMessage();
            log.warn("写入本地大模型配置失败（本次修改已生效，但重启会丢）：{}", file, e);
        }
    }

    private static String normalizeMode(String raw) {
        String value = raw == null ? "" : raw.trim().toLowerCase();
        if (value.isEmpty()) {
            return "mock";
        }
        if (!MODES.contains(value)) {
            throw new IllegalArgumentException("通道只能是 mock 或 real，收到：" + raw);
        }
        return value;
    }

    private static boolean isHttpUrl(String url) {
        if (url == null || url.isBlank()) {
            return false;
        }
        try {
            URI uri = URI.create(url);
            String scheme = uri.getScheme();
            return uri.getHost() != null
                    && ("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme));
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static int clampTimeout(int seconds) {
        return Math.max(MIN_TIMEOUT, Math.min(MAX_TIMEOUT, seconds));
    }
}
