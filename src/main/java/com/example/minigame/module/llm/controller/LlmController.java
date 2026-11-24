package com.example.minigame.module.llm.controller;

import com.example.minigame.common.AuthUtil;
import com.example.minigame.module.llm.dto.LlmSettingsForm;
import com.example.minigame.module.llm.service.LlmService;
import com.example.minigame.module.llm.service.LlmSettings;
import com.example.minigame.module.llm.vo.LlmResultVO;
import com.example.minigame.module.llm.vo.LlmSettingsVO;
import com.example.minigame.module.llm.vo.LlmStatusVO;
import com.example.minigame.module.quiz.QuizCategories;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * 大模型生成页面。
 *
 * <pre>
 * GET  /llm             页面（含「接入大模型」配置表单）
 * POST /llm/generate    按分区+主题生成一道题（mock 占位 / real 真实模型）
 * POST /llm/settings    保存通道/地址/模型/Key —— 内存 + 本地文件，立即生效，仅 ADMIN
 * </pre>
 *
 * <p>页面同时会显示通道状态：真实通道失败会静默回落 mock，
 * 不把原因显示出来的话，看到占位选项根本不知道是 Key 没配还是网络不通。
 */
@Controller
@RequestMapping("/llm")
@RequiredArgsConstructor
public class LlmController {

    private final LlmService llmService;
    private final LlmSettings llmSettings;

    @GetMapping
    public String page(@RequestParam(required = false) String category,
                       @RequestParam(required = false) String topic,
                       Model model) {
        // 带 topic 进来就直接生成，方便分享链接
        if (topic != null && !topic.isBlank()) {
            return generate(category == null ? "computer" : category, topic, model);
        }
        fillCommon(model);
        return "llm";
    }

    @PostMapping("/generate")
    public String generate(@RequestParam(defaultValue = "computer") String category,
                           @RequestParam(required = false) String topic,
                           Model model) {
        QuizCategories.Def def = QuizCategories.of(category);
        String categoryName = def == null ? "通用" : def.name();

        LlmResultVO result = llmService.generate(category, categoryName, topic, 1);

        fillCommon(model);
        model.addAttribute("category", category);
        model.addAttribute("topic", topic);
        model.addAttribute("result", result);
        return "llm";
    }

    /**
     * 保存大模型配置。
     *
     * <p>只有 ADMIN 能改：这里存的是别人的 API Key，公网部署时不能让任意访客覆盖。
     * 演示账号见登录页（admin / password）。
     */
    @PostMapping("/settings")
    public String saveSettings(@ModelAttribute LlmSettingsForm form,
                               HttpSession session,
                               RedirectAttributes ra) {
        if (!AuthUtil.ROLE_ADMIN.equals(session.getAttribute(AuthUtil.SESSION_ROLE))) {
            ra.addFlashAttribute("settingsError",
                    "只有管理员能修改大模型配置：请用管理员账号登录（演示账号 admin / password）后再保存。");
            return "redirect:/llm";
        }
        try {
            LlmSettings.Snapshot saved = llmSettings.apply(
                    form.getMode(),
                    form.getBaseUrl(),
                    form.getModel(),
                    form.getApiKey(),
                    Boolean.TRUE.equals(form.getJsonFormat()),
                    form.getTimeoutSeconds() == null ? 20 : form.getTimeoutSeconds(),
                    Boolean.TRUE.equals(form.getClearKey()));
            ra.addFlashAttribute("settingsOk",
                    "已保存并立即生效：通道=" + saved.mode() + "，模型=" + saved.model()
                            + "。点下面「生成题目」验证连通性。");
            if (llmSettings.persistError() != null) {
                ra.addFlashAttribute("settingsError",
                        "配置已生效，但写入本地文件失败（重启后会丢）：" + llmSettings.persistError());
            }
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("settingsError", e.getMessage());
        }
        return "redirect:/llm";
    }

    private void fillCommon(Model model) {
        LlmStatusVO status = llmService.status();
        LlmSettings.Snapshot snapshot = llmSettings.snapshot();
        model.addAttribute("active", "llm");
        model.addAttribute("categories", QuizCategories.ALL);
        model.addAttribute("llmStatus", status);
        model.addAttribute("configuredMode", snapshot.mode());
        model.addAttribute("llmSettings", new LlmSettingsVO(
                snapshot.mode(), snapshot.baseUrl(), snapshot.model(),
                snapshot.keyConfigured(), snapshot.keyHint(), snapshot.jsonFormat(),
                snapshot.timeoutSeconds(), snapshot.source(), snapshot.file()));
        model.addAttribute("persistError", llmSettings.persistError());
        // 配了 real 但一个 Key 都没有 —— 生成的必然是占位题，页面要顶一条警告
        model.addAttribute("llmDegraded", snapshot.real() && !snapshot.keyConfigured());
    }
}

