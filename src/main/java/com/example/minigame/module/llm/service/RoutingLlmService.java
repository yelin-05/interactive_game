package com.example.minigame.module.llm.service;

import com.example.minigame.module.llm.vo.LlmResultVO;
import com.example.minigame.module.llm.vo.LlmStatusVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

/**
 * 按「当前设置」把请求路由到真实通道或占位通道。
 *
 * <p>以前靠 {@code @ConditionalOnProperty(app.llm.mode=real)} 决定创建哪个 Bean，
 * 意味着改通道必须重启；现在两个实现都是常驻 Bean，由这里按 {@link LlmSettings} 的快照路由，
 * 页面保存后下一个请求就走新通道。
 *
 * <p>注意：真实通道自己会在失败时回落 Mock（并记 lastError），
 * 本类只负责「用户选了哪个通道」，不重复做回落。
 */
@Slf4j
@Service
@Primary
@RequiredArgsConstructor
public class RoutingLlmService implements LlmService {

    private final MockLlmService mockLlmService;
    private final RealLlmService realLlmService;
    private final LlmSettings settings;

    @Override
    public LlmResultVO generate(String categoryCode, String categoryName, String topic, int difficulty) {
        if (settings.snapshot().real()) {
            return realLlmService.generate(categoryCode, categoryName, topic, difficulty);
        }
        return mockLlmService.generate(categoryCode, categoryName, topic, difficulty);
    }

    @Override
    public LlmStatusVO status() {
        LlmSettings.Snapshot snapshot = settings.snapshot();
        if (snapshot.real()) {
            return realLlmService.status();
        }
        // mock 通道也把地址/模型带上，页面表单才能回填「下次要接哪儿」
        return new LlmStatusVO("mock", snapshot.baseUrl(), snapshot.model(),
                snapshot.keyConfigured(), snapshot.jsonFormat(),
                0L, 0L, 0L, null, null, null);
    }
}
