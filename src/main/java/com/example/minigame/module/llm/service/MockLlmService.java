package com.example.minigame.module.llm.service;

import com.example.minigame.module.llm.vo.LlmResultVO;
import com.example.minigame.module.llm.vo.LlmStatusVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

/**
 * 大模型生成「占位」实现。
 *
 * <p>真实调用需要 API Key，答辩现场网络不可控，所以默认走 Mock：
 * 用模板拼出一道结构正确的题，前端/接口形状跟真实模型完全一致。
 * 真实通道见 {@link RealLlmService}，由 {@link RoutingLlmService} 按当前设置
 * （{@code /llm} 页面可改，或 {@code app.llm.mode=real}）路由过去。
 *
 * <p><b>返回的 mode 恒为 "mock"</b>：自动补题据此拒绝把占位题写进正式题库
 * （除非显式配置 <code>app.quiz.auto-fill.allow-mock=true</code> 做离线演示）。
 */
@Slf4j
@Service
public class MockLlmService implements LlmService {

    private final Random random = new Random();

    @Override
    public LlmResultVO generate(String categoryCode, String categoryName, String topic, int difficulty) {
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
                + "（当前通道=mock，目标难度=" + difficulty + "）";

        log.debug("Mock 通道出题：分区={}，难度={}", categoryName, difficulty);
        return new LlmResultVO("mock", stem, options, 0, explanation, difficulty);
    }

    @Override
    public LlmStatusVO status() {
        return LlmStatusVO.mock();
    }
}
