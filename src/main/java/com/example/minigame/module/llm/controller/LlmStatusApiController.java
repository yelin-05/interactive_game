package com.example.minigame.module.llm.controller;

import com.example.minigame.common.ApiResult;
import com.example.minigame.module.llm.service.LlmService;
import com.example.minigame.module.llm.vo.LlmResultVO;
import com.example.minigame.module.llm.vo.LlmStatusVO;
import com.example.minigame.module.quiz.QuizCategories;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 大模型通道的自检接口。
 *
 * <pre>
 * GET  /api/llm/status  当前通道（real/mock）、是否配了 Key、最近一次失败原因
 * POST /api/llm/test    真的调一次模型，验 Key + 网络 + 返回格式是否通
 * </pre>
 *
 * <p>真实通道失败会回落 Mock、不抛异常，所以「生成出来的还是占位题」时先看这两个接口：
 * status 里 lastError 会直接写出是 401 Key 无效、404 地址写错还是返回不是 JSON。
 */
@RestController
@RequestMapping("/api/llm")
@RequiredArgsConstructor
public class LlmStatusApiController {

    private final LlmService llmService;

    @GetMapping("/status")
    public ApiResult<LlmStatusVO> status() {
        return ApiResult.ok(llmService.status());
    }

    /** 测试连接：跑一次真实出题，返回的 mode 是 real 说明通了，mock 说明回落了（原因看 /status）。 */
    @PostMapping("/test")
    public ApiResult<LlmResultVO> test(@RequestParam(defaultValue = "computer") String category,
                                       @RequestParam(required = false) String topic,
                                       @RequestParam(defaultValue = "1") int difficulty) {
        QuizCategories.Def def = QuizCategories.of(category);
        String categoryName = def == null ? "通用" : def.name();
        return ApiResult.ok(llmService.generate(category, categoryName, topic, difficulty));
    }
}
