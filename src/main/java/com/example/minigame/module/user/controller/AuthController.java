package com.example.minigame.module.user.controller;

import com.example.minigame.common.AuthUtil;
import com.example.minigame.module.user.entity.User;
import com.example.minigame.module.user.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 登录 / 注册 / 退出。
 *
 * <p>登录态写 HttpSession，键见 {@link AuthUtil}。
 * 登录后跳到 redirect 指定的原页面，没有就回首页。
 */
@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    /* ---------------- 登录 ---------------- */

    @GetMapping("/login")
    public String loginPage(@RequestParam(required = false) String redirect, Model model) {
        model.addAttribute("redirect", redirect);
        model.addAttribute("active", "login");
        return "auth/login";
    }

    @PostMapping("/login")
    public String doLogin(@RequestParam String username,
                          @RequestParam String password,
                          @RequestParam(required = false) String redirect,
                          HttpSession session,
                          Model model) {
        try {
            User user = userService.login(username, password);
            session.setAttribute(AuthUtil.SESSION_USER_ID, user.getId());
            session.setAttribute(AuthUtil.SESSION_USERNAME, user.getUsername());
            session.setAttribute(AuthUtil.SESSION_NICKNAME,
                    user.getNickname() == null ? user.getUsername() : user.getNickname());
            session.setAttribute(AuthUtil.SESSION_ROLE, user.getRole());

            if (redirect != null && redirect.startsWith("/")) {
                return "redirect:" + redirect;
            }
            return "redirect:/";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("username", username);
            model.addAttribute("redirect", redirect);
            model.addAttribute("active", "login");
            return "auth/login";
        }
    }

    /* ---------------- 注册 ---------------- */

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("active", "login");
        return "auth/register";
    }

    @PostMapping("/register")
    public String doRegister(@RequestParam String username,
                             @RequestParam String password,
                             @RequestParam String password2,
                             @RequestParam(required = false) String nickname,
                             Model model) {
        try {
            if (!password.equals(password2)) {
                throw new IllegalArgumentException("两次输入的密码不一致");
            }
            userService.register(username, password, nickname);
            return "redirect:/login?registered=1";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("username", username);
            model.addAttribute("nickname", nickname);
            model.addAttribute("active", "login");
            return "auth/register";
        }
    }

    /* ---------------- 退出 ---------------- */

    @PostMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }
}
