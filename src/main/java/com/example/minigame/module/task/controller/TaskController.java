package com.example.minigame.module.task.controller;

import com.example.minigame.common.AuthUtil;
import com.example.minigame.module.task.service.TaskService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 任务中心。
 *
 * <pre>
 * GET  /tasks                任务列表 + 我的进度
 * POST /tasks/{id}/claim     领取奖励（需登录且已完成）
 * </pre>
 */
@Controller
@RequestMapping("/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @GetMapping
    public String list(HttpSession session,
                       @RequestParam(required = false) Integer claimed,
                       @RequestParam(required = false) String error,
                       Model model) {
        Long userId = (Long) session.getAttribute(AuthUtil.SESSION_USER_ID);
        model.addAttribute("active", "tasks");
        model.addAttribute("rows", taskService.listFor(userId));
        model.addAttribute("totalReward", taskService.totalReward(userId));
        model.addAttribute("loggedIn", userId != null);
        model.addAttribute("claimedPoints", claimed);
        model.addAttribute("error", error);
        return "tasks";
    }

    @PostMapping("/{id}/claim")
    public String claim(@PathVariable Long id, HttpSession session) {
        Long userId = (Long) session.getAttribute(AuthUtil.SESSION_USER_ID);
        if (userId == null) {
            return "redirect:/login?redirect=/tasks";
        }
        try {
            int points = taskService.claim(userId, id);
            return "redirect:/tasks?claimed=" + points;
        } catch (IllegalArgumentException e) {
            return "redirect:/tasks?error=" + java.net.URLEncoder.encode(
                    e.getMessage(), java.nio.charset.StandardCharsets.UTF_8);
        }
    }
}
