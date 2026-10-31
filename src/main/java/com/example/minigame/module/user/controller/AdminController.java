package com.example.minigame.module.user.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.minigame.module.quiz.service.QuizService;
import com.example.minigame.module.user.entity.User;
import com.example.minigame.module.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 管理后台（仅 ADMIN，拦截在 AdminInterceptor）。
 *
 * <p>这一轮先做只读概览：用户数、题库统计、最新题目、最近注册用户。
 * 增删改题目、禁用账号等写操作等题库编辑那一轮再加。
 */
@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;
    private final QuizService quizService;

    @GetMapping
    public String index(Model model) {
        model.addAttribute("active", "admin");
        model.addAttribute("userCount", userService.count());
        model.addAttribute("questionCount", quizService.total());
        model.addAttribute("categories", quizService.categories());
        model.addAttribute("recentQuestions", quizService.latest(6));
        model.addAttribute("recentUsers", userService.list(Wrappers.<User>lambdaQuery()
                .orderByDesc(User::getId)
                .last("limit 5")));
        return "admin/index";
    }
}
