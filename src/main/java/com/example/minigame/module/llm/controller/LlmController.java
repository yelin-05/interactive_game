package com.example.minigame.module.llm.controller;

import com.example.minigame.module.llm.service.MockLlmService;
import com.example.minigame.module.llm.vo.LlmResultVO;
import com.example.minigame.module.quiz.QuizCategories;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 大模型生成（占位）。
 *
 * <pre>
 * GET  /llm            页面
 * POST /llm/generate   按分区+主题生成一道题（当前 Mock）
 * </pre>
 */
@Controller
@RequestMapping("/llm")
@RequiredArgsConstructor
public class LlmController {

    private final MockLlmService llmService;

    @GetMapping
    public String page(@RequestParam(required = false) String category,
                       @RequestParam(required = false) String topic,
                       Model model) {
        // 带 topic 进来就直接生成，方便分享链接
        if (topic != null && !topic.isBlank()) {
            return generate(category == null ? "computer" : category, topic, model);
        }
        model.addAttribute("active", "llm");
        model.addAttribute("categories", QuizCategories.ALL);
        return "llm";
    }

    @PostMapping("/generate")
    public String generate(@RequestParam(defaultValue = "computer") String category,
                           @RequestParam(required = false) String topic,
                           Model model) {
        QuizCategories.Def def = QuizCategories.of(category);
        String categoryName = def == null ? "通用" : def.name();

        LlmResultVO result = llmService.generate(topic, categoryName);

        model.addAttribute("active", "llm");
        model.addAttribute("categories", QuizCategories.ALL);
        model.addAttribute("category", category);
        model.addAttribute("topic", topic);
        model.addAttribute("result", result);
        return "llm";
    }
}
