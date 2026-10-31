package com.example.minigame.module.user.controller;

import com.example.minigame.module.user.entity.User;
import com.example.minigame.module.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.format.DateTimeFormatter;

/**
 * 个人中心。
 *
 * <p>登录鉴权还没做，所以这里固定展示演示账号 player01；
 * 战绩统计要等 t_score 表落地后再接真实数据，现在显示占位。
 */
@Controller
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final UserService userService;

    @GetMapping
    public String profile(Model model) {
        User user = userService.getByUsername("player01");
        if (user == null) {
            user = new User();
            user.setUsername("-");
            user.setNickname("未登录");
        }

        String nickname = user.getNickname() == null || user.getNickname().isBlank()
                ? user.getUsername()
                : user.getNickname();

        model.addAttribute("user", user);
        model.addAttribute("nickname", nickname);
        model.addAttribute("initial", nickname.isEmpty() ? "?" : nickname.substring(0, 1));
        model.addAttribute("roleLabel", "ADMIN".equals(user.getRole()) ? "管理员" : "玩家");
        model.addAttribute("joinedAt", user.getCreatedAt() == null ? "—" : user.getCreatedAt().format(DATE));
        return "profile";
    }
}
