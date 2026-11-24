package com.example.minigame.module.llm.service;

import com.example.minigame.module.llm.vo.LlmResultVO;
import com.example.minigame.module.llm.vo.LlmStatusVO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 大模型真实通道（OpenAI 兼容协议）。
 *
 * <p>按 OpenAI 兼容协议调 <code>/chat/completions</code>，默认指向 DeepSeek；
 * 换通义、智谱、或者本机 Ollama 只要在 <code>/llm</code> 页面上改地址与模型
 * （保存后立即生效，见 {@link LlmSettings}），代码不用动。
 *
 * <p><b>这个 Bean 常驻</b>（不再用 {@code @ConditionalOnProperty} 控制创建）：
 * 走哪个通道由 {@link RoutingLlmService} 按当前设置路由，这样页面改完通道不用重启。
 *
 * <p><b>失败绝不抛给调用方</b>：没配 Key、超时、返回不是合法 JSON —— 一律回落
 * {@link MockLlmService}，且回落结果的 mode 是 "mock"，自动补题会据此拒绝入库，
 * 保证「网络不稳」不会把垃圾题写进 t_question。失败原因记在 {@link #status()} 的 lastError 里，
 * 由 /llm 页面与 <code>GET /api/llm/status</code> 显示出来。
 */
@Slf4j
@Service
public class RealLlmService implements LlmService {

    /** 出题约束：只要 JSON，不要解释性文字，答案必须唯一确定。 */
    private static final String SYSTEM_PROMPT = """
            你是一位中文出题老师。只输出一个 JSON 对象，不要输出任何解释性文字，不要用 markdown 代码块。
            JSON 字段固定为：
            {"question": "题干", "options": ["选项A", "选项B", "选项C", "选项D"],
             "answerIndex": 0, "explanation": "解析"}
            要求：题干不超过 100 字；4 个选项互不相同、长度接近、只有一个是正确的；
            answerIndex 是正确选项的下标（0-3）；解析说明为什么选它、干扰项错在哪，不超过 200 字。
            只出有唯一确定答案的客观事实题；不要出「以上都对」这类题；不要涉及会随时间变化的数据。
            """;

    private final MockLlmService fallback;
    private final ObjectMapper objectMapper;
    private final LlmSettings settings;

    /* ---- 状态：失败会被回落吞掉，这里留痕给页面/接口看 ---- */
    private final AtomicLong calls = new AtomicLong();
    private final AtomicLong successes = new AtomicLong();
    private final AtomicLong fallbacks = new AtomicLong();
    private volatile LocalDateTime lastSuccessAt;
    private volatile LocalDateTime lastErrorAt;
    private volatile String lastError;

    public RealLlmService(MockLlmService fallback, ObjectMapper objectMapper, LlmSettings settings) {
        this.fallback = fallback;
        this.objectMapper = objectMapper;
        this.settings = settings;
        LlmSettings.Snapshot snapshot = settings.snapshot();
        if (snapshot.real() && !snapshot.keyConfigured()) {
            log.warn("当前通道是 real 但还没有 API Key，所有生成都会回落 Mock 占位题；可到 /llm 页面直接填写");
        } else {
            log.info("真实大模型通道就绪：model={}，url={}，jsonFormat={}，keyConfigured={}",
                    snapshot.model(), snapshot.baseUrl(), snapshot.jsonFormat(), snapshot.keyConfigured());
        }
    }

    @Override
    public LlmResultVO generate(String categoryCode, String categoryName, String topic, int difficulty) {
        calls.incrementAndGet();
        LlmSettings.Snapshot snapshot = settings.snapshot();
        if (!snapshot.keyConfigured()) {
            return mockFallback(categoryCode, categoryName, topic, difficulty,
                    "还没有配置 API Key：打开 /llm 页面，在「接入大模型」里填 Key 并保存（立即生效，不用重启）");
        }

        try {
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("model", snapshot.model());
            payload.put("temperature", 0.8);
            if (snapshot.jsonFormat()) {
                payload.put("response_format", Map.of("type", "json_object"));
            }
            payload.put("messages", List.of(
                    Map.of("role", "system", "content", SYSTEM_PROMPT),
                    Map.of("role", "user", "content", buildUserPrompt(categoryName, topic, difficulty))
            ));

            String raw = client(snapshot.timeoutSeconds()).post()
                    .uri(snapshot.baseUrl())
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + snapshot.apiKey())
                    .body(payload)
                    .retrieve()
                    .body(String.class);

            JsonNode node = objectMapper.readTree(extractContent(raw));
            List<String> options = new ArrayList<>();
            node.path("options").forEach(o -> options.add(o.asText("")));

            LlmResultVO result = new LlmResultVO(
                    "real",
                    node.path("question").asText(""),
                    options,
                    node.path("answerIndex").asInt(-1),
                    node.path("explanation").asText(""),
                    difficulty);
            successes.incrementAndGet();
            lastSuccessAt = LocalDateTime.now();
            log.debug("真实通道出题成功：分区={}，难度={}，题干={}", categoryName, difficulty, result.question());
            return result;
        } catch (HttpStatusCodeException e) {
            // 401 Key 无效 / 402 余额不足 / 404 地址写错 / 429 限流 / 5xx 服务端故障，都从这里出去
            return mockFallback(categoryCode, categoryName, topic, difficulty, httpError(e));
        } catch (Exception e) {
            return mockFallback(categoryCode, categoryName, topic, difficulty,
                    e.getClass().getSimpleName() + "：" + shortText(e.getMessage()));
        }
    }

    @Override
    public LlmStatusVO status() {
        LlmSettings.Snapshot snapshot = settings.snapshot();
        return new LlmStatusVO("real", snapshot.baseUrl(), snapshot.model(),
                snapshot.keyConfigured(), snapshot.jsonFormat(),
                calls.get(), successes.get(), fallbacks.get(),
                lastSuccessAt, lastErrorAt, lastError);
    }

    /**
     * 按当前超时新建一个 RestClient。
     *
     * <p>页面随时可能把超时改掉，所以不缓存客户端（创建成本很低），
     * 每次调用都读一次最新配置。
     */
    private RestClient client(int timeoutSeconds) {
        Duration timeout = Duration.ofSeconds(Math.max(3, timeoutSeconds));
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(timeout);
        factory.setReadTimeout(timeout);
        return RestClient.builder().requestFactory(factory).build();
    }

    private String buildUserPrompt(String categoryName, String topic, int difficulty) {
        String level = switch (difficulty) {
            case 3 -> "困难（需要一点专业积累，但不许偏题怪题）";
            case 2 -> "中等（认真学过相关内容就该会）";
            default -> "简单（大众常识水平）";
        };
        String theme = (topic == null || topic.isBlank())
                ? "自行挑选该分区里一个常见的知识点"
                : "围绕「" + topic + "」";
        return "分区：" + categoryName + "；难度：" + level + "；主题：" + theme
                + "。请出 1 道四选一单选题，严格按约定 JSON 输出。";
    }

    /**
     * 从 chat/completions 响应里取出题目 JSON。
     *
     * <p>三种情况都要兼容：标准响应（choices[0].message.content）、
     * 某些代理直接返回题目对象、以及模型把 JSON 套在 markdown 代码块里。
     */
    private String extractContent(String raw) throws Exception {
        if (raw == null || raw.isBlank()) {
            throw new IllegalStateException("大模型返回为空");
        }
        JsonNode root = objectMapper.readTree(raw);
        if (root.has("question")) {
            return raw;
        }
        String text = root.path("choices").path(0).path("message").path("content").asText("");
        if (text.isBlank()) {
            throw new IllegalStateException("大模型返回里没有 content");
        }
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        return (start >= 0 && end > start) ? text.substring(start, end + 1) : text;
    }

    /** 回落成 Mock 时把 mode 明确改成 mock，自动补题据此拒绝入库；同时把原因留痕给页面。 */
    private LlmResultVO mockFallback(String categoryCode, String categoryName, String topic, int difficulty,
                                     String reason) {
        fallbacks.incrementAndGet();
        lastError = reason;
        lastErrorAt = LocalDateTime.now();
        log.warn("真实通道不可用，本次回落到 Mock 占位题：{}", reason);
        return asMock(fallback.generate(categoryCode, categoryName, topic, difficulty));
    }

    private LlmResultVO asMock(LlmResultVO r) {
        return new LlmResultVO("mock", r.question(), r.options(), r.answerIndex(), r.explanation(), r.difficulty());
    }

    /** 错误摘要截断：HTTP 响应体经常几百字，整段塞进页面和日志都不好读。 */
    private static String shortText(String text) {
        if (text == null || text.isBlank()) {
            return "（无响应内容）";
        }
        String oneLine = text.replaceAll("\\s+", " ").trim();
        return oneLine.length() <= 200 ? oneLine : oneLine.substring(0, 200) + "…";
    }

    /**
     * 把 HTTP 错误拼成一句人话。
     *
     * <p>有的厂商 401 只回空 body（`Content-Length: 0`），这时只能退回状态文本，
     * 否则页面上只剩「HTTP 401：（无响应内容）」，看不出到底是 Key 无效还是别的。
     */
    private static String httpError(HttpStatusCodeException e) {
        String body = e.getResponseBodyAsString();
        String detail = (body == null || body.isBlank()) ? e.getStatusText() : shortText(body);
        return "HTTP " + e.getStatusCode().value() + "：" + detail;
    }
}
