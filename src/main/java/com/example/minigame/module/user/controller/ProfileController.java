package com.example.minigame.module.user.controller;

import com.example.minigame.common.AuthUtil;
import com.example.minigame.module.quiz.QuizCategories;
import com.example.minigame.module.quiz.entity.Score;
import com.example.minigame.module.quiz.service.ScoreService;
import com.example.minigame.module.user.entity.User;
import com.example.minigame.module.user.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 个人中心。
 *
 * <p>展示当前登录用户（来自 session）+ 真实战绩（来自 t_score）。
 */
@Controller
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME = DateTimeFormatter.ofPattern("MM-dd HH:mm");

    private final UserService userService;
    private final ScoreService scoreService;

    @GetMapping
    public String profile(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute(AuthUtil.SESSION_USER_ID);
        User user = userId == null ? null : userService.getById(userId);
        if (user == null) {
            user = new User();
            user.setUsername("-");
            user.setNickname("未登录");
        }

        String nickname = user.getNickname() == null || user.getNickname().isBlank()
                ? user.getUsername()
                : user.getNickname();

        List<Score> recent = scoreService.recent(userId, 5);
        Map<String, String> categoryNames = new HashMap<>();
        for (QuizCategories.Def d : QuizCategories.ALL) {
            categoryNames.put(d.code(), d.icon() + " " + d.name());
        }

        model.addAttribute("active", "profile");
        model.addAttribute("user", user);
        model.addAttribute("nickname", nickname);
        model.addAttribute("initial", nickname.isEmpty() ? "?" : nickname.substring(0, 1));
        model.addAttribute("roleLabel", AuthUtil.ROLE_ADMIN.equals(user.getRole()) ? "管理员" : "玩家");
        model.addAttribute("joinedAt", user.getCreatedAt() == null ? "—" : user.getCreatedAt().format(DATE));
        model.addAttribute("stats", scoreService.statsOf(userId));
        model.addAttribute("recent", recent);
        model.addAttribute("recentTimeFmt", DATETIME);
        model.addAttribute("categoryNames", categoryNames);
        return "profile";
    }
}
