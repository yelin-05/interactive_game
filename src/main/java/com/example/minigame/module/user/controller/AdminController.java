package com.example.minigame.module.user.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.minigame.module.llm.service.LlmSettings;
import com.example.minigame.module.quiz.service.QuestionAutoFillService;
import com.example.minigame.module.quiz.service.QuizService;
import com.example.minigame.module.quiz.service.ScoreService;
import com.example.minigame.module.quiz.vo.AutoFillReportVO;
import com.example.minigame.module.user.entity.User;
import com.example.minigame.module.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * 管理后台（仅 ADMIN，拦截在 AdminInterceptor）。
 *
 * <p>概览是只读的；写操作目前只有一项「让游戏自己补题」：
 * 调大模型出题 → 校验 → 写 t_question（见 QuestionAutoFillService）。
 * 题目增删改、上下架、用户禁用仍在待办里。
 */
@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;
    private final QuizService quizService;
    private final ScoreService scoreService;
    private final QuestionAutoFillService questionAutoFillService;
    private final LlmSettings llmSettings;

    @Value("${app.quiz.auto-fill.enabled:true}")
    private boolean autoFillEnabled;

    @GetMapping
    public String index(Model model) {
        model.addAttribute("active", "admin");
        model.addAttribute("userCount", userService.count());
        model.addAttribute("questionCount", quizService.total());
        model.addAttribute("scoreCount", scoreService.count());
        model.addAttribute("categories", quizService.categories());
        model.addAttribute("recentQuestions", quizService.latest(6));
        model.addAttribute("recentUsers", userService.list(Wrappers.<User>lambdaQuery()
                .orderByDesc(User::getId)
                .last("limit 5")));
        model.addAttribute("llmMode", llmSettings.snapshot().mode());
        model.addAttribute("autoFillEnabled", autoFillEnabled);
        return "admin/index";
    }

    /**
     * 自动补题：把各分区各难度的缺口补到目标题量。
     *
     * <p>用 flash 属性回显结果，刷新页面不会重复提交。
     */
    @PostMapping("/questions/autofill")
    public String autoFill(RedirectAttributes ra) {
        if (!autoFillEnabled) {
            ra.addFlashAttribute("autofillError", "自动补题已被配置关闭（app.quiz.auto-fill.enabled=false）");
            return "redirect:/admin";
        }
        try {
            AutoFillReportVO report = questionAutoFillService.fillAll();
            ra.addFlashAttribute("autofillReport", report);
        } catch (Exception e) {
            ra.addFlashAttribute("autofillError", "补题失败：" + e.getMessage());
        }
        return "redirect:/admin";
    }
}
