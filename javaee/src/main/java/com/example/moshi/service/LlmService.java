package com.example.moshi.service;

import com.example.moshi.util.Categories;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class LlmService {

    private static final ObjectMapper JSON = new ObjectMapper();
    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    private final String mode = env("MOSHI_LLM_MODE", "mock").toLowerCase(Locale.ROOT);
    private final String baseUrl = env("MOSHI_LLM_BASE_URL", "https://api.deepseek.com/v1");
    private final String apiKey = env("MOSHI_LLM_API_KEY", "");
    private final String model = env("MOSHI_LLM_MODEL", "deepseek-chat");
    private final int timeoutSeconds = positiveInt(env("MOSHI_LLM_TIMEOUT_SECONDS", "30"), 30);

    public String getMode() {
        return mode;
    }

    public Result generate(String category, String topic) throws Exception {
        if (!Categories.exists(category)) {
            throw new IllegalArgumentException("请选择有效分区");
        }
        String subject = topic == null || topic.isBlank() ? Categories.nameOf(category) : topic.trim();
        if (subject.length() > 100) {
            throw new IllegalArgumentException("主题不能超过 100 个字符");
        }
        if ("real".equals(mode)) {
            return real(category, subject);
        }
        return mock(category, subject);
    }

    private Result mock(String category, String subject) {
        String stem = "关于「" + subject + "」，下列说法是正确的？";
        List<String> options = List.of(
                "与「" + subject + "」直接相关的核心概念",
                "与「" + subject + "」相关但适用范围不同的概念",
                "常见的混淆概念",
                "与「" + subject + "」无关的概念");
        return new Result("mock", stem, options, 0,
                "这是 Mock 通道生成的结构化示例，仅用于验证页面与数据契约，不代表真实模型推理结果。");
    }

    private Result real(String category, String subject) throws Exception {
        if (apiKey.isBlank()) {
            throw new IllegalStateException("真实大模型通道未配置 API Key");
        }
        String prompt = "你是严谨的命题老师。请围绕分类「" + Categories.nameOf(category)
                + "」和主题「" + subject + "」生成一道单选题。只返回 JSON，不要 Markdown。"
                + "格式：{\"question\":\"题干\",\"options\":[\"A\",\"B\",\"C\",\"D\"],"
                + "\"answerIndex\":0,\"explanation\":\"解析\"}。answerIndex 从 0 开始。";

        ObjectNode root = JSON.createObjectNode();
        root.put("model", model);
        root.put("temperature", 0.7);
        ArrayNode messages = root.putArray("messages");
        messages.addObject().put("role", "system").put("content", "你只输出合法 JSON。");
        messages.addObject().put("role", "user").put("content", prompt);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl.replaceAll("/+$", "") + "/chat/completions"))
                .timeout(Duration.ofSeconds(timeoutSeconds))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(JSON.writeValueAsString(root)))
                .build();
        HttpResponse<String> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IllegalStateException("大模型服务返回异常状态");
        }
        JsonNode content = JSON.readTree(response.body()).path("choices").path(0).path("message").path("content");
        if (!content.isTextual()) {
            throw new IllegalStateException("大模型响应格式不正确");
        }
        return parse(content.asText().replaceAll("(?s)^```(?:json)?\\s*", "").replaceAll("(?s)\\s*```$", ""));
    }

    private Result parse(String content) throws Exception {
        JsonNode node = JSON.readTree(content);
        String question = bounded(node.path("question").asText(""), 500);
        JsonNode optionsNode = node.path("options");
        if (question.isBlank() || !optionsNode.isArray() || optionsNode.size() != 4) {
            throw new IllegalStateException("大模型返回的题目结构不完整");
        }
        List<String> options = new ArrayList<>();
        for (JsonNode option : optionsNode) {
            String text = bounded(option.asText(""), 200);
            if (text.isBlank()) {
                throw new IllegalStateException("大模型返回的选项为空");
            }
            options.add(text);
        }
        int answerIndex = node.path("answerIndex").asInt(-1);
        String explanation = bounded(node.path("explanation").asText(""), 1000);
        if (answerIndex < 0 || answerIndex > 3 || explanation.isBlank()) {
            throw new IllegalStateException("大模型返回的答案或解析无效");
        }
        return new Result("real", question, options, answerIndex, explanation);
    }

    private String bounded(String value, int max) {
        String text = value == null ? "" : value.trim();
        return text.length() <= max ? text : text.substring(0, max);
    }

    private static String env(String key, String fallback) {
        String value = System.getenv(key);
        if (value == null || value.isBlank()) {
            value = System.getProperty(key);
        }
        return value == null || value.isBlank() ? fallback : value;
    }

    private static int positiveInt(String value, int fallback) {
        try {
            int parsed = Integer.parseInt(value);
            return parsed > 0 && parsed <= 120 ? parsed : fallback;
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }

    public static final class Result {
        private final String mode;
        private final String question;
        private final List<String> options;
        private final int answerIndex;
        private final String explanation;

        private Result(String mode, String question, List<String> options, int answerIndex, String explanation) {
            this.mode = mode;
            this.question = question;
            this.options = List.copyOf(options);
            this.answerIndex = answerIndex;
            this.explanation = explanation;
        }

        public String getMode() { return mode; }
        public String getQuestion() { return question; }
        public List<String> getOptions() { return options; }
        public int getAnswerIndex() { return answerIndex; }
        public String getExplanation() { return explanation; }
    }
}
