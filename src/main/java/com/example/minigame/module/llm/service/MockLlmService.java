package com.example.minigame.module.llm.service;

import com.example.minigame.module.llm.vo.LlmResultVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

/**
 * 大模型生成「占位」实现。
 *
 * <p>真实调用需要 API Key，答辩现场网络不可控，所以默认走 Mock：
 * 用模板拼出一道结构正确的题，前端/接口形状跟真实模型完全一致，
 * 之后把 {@link #generate} 换成 HTTP 调用即可，调用方不用改。
 *
 * <p>切真实通道：配置 <code>app.llm.mode=real</code> 并注入 LLM_API_KEY
 * （真实调用的代码留在 TODO 处，未启用）。
 */
@Slf4j
@Service
public class MockLlmService {

    private final Random random = new Random();

    @Value("${app.llm.mode:mock}")
    private String mode;

    @Value("${app.llm.model:deepseek-chat}")
    private String model;

    @Value("${app.llm.api-key:}")
    private String apiKey;

    public LlmResultVO generate(String topic, String categoryName) {
        String t = (topic == null || topic.isBlank()) ? categoryName : topic.trim();

        List<String> stems = List.of(
                "关于「%s」，下列说法正确的是？",
                "下列哪一项与「%s」无关？",
                "「%s」最核心的特征是什么？"
        );
        String stem = String.format(stems.get(random.nextInt(stems.size())), t);

        List<String> options = List.of(
                "与主题直接相关的表述（占位选项 A）",
                "看似合理但答非所问的表述（占位选项 B）",
                "常见的混淆项（占位选项 C）",
                "完全无关的干扰项（占位选项 D）"
        );

        String explanation = "这是一条 Mock 生成的占位解析：接入真实模型后，这里会是"
                + "针对「" + t + "」的具体讲解，并由模型给出正确答案。"

                + "（当前通道=" + mode + "，模型=" + model + "）";

        if ("real".equalsIgnoreCase(mode) && !apiKey.isBlank()) {
            // TODO 真实调用：POST https://api.deepseek.com/v1/chat/completions
            // 组装 system+user prompt，解析 JSON 后映射成 LlmResultVO。
            log.warn("app.llm.mode=real 但真实调用尚未启用，本次仍返回 Mock 结果");
        }

        return new LlmResultVO(mode, stem, options, 0, explanation);
    }
}
