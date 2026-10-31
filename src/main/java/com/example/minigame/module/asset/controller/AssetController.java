package com.example.minigame.module.asset.controller;

import com.example.minigame.common.AuthUtil;
import com.example.minigame.module.asset.service.AssetService;
import com.example.minigame.module.quiz.QuizCategories;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * 素材库：上传 + 浏览。
 *
 * <pre>
 * GET  /assets                 列表（可按分区过滤）+ 上传表单
 * POST /assets/upload          上传图片（需登录）
 * GET  /uploads/**             磁盘文件静态访问（见 WebConfig）
 * </pre>
 */
@Controller
@RequestMapping("/assets")
@RequiredArgsConstructor
public class AssetController {

    private final AssetService assetService;

    @GetMapping
    public String list(@RequestParam(required = false) String category,
                       @RequestParam(required = false) String ok,
                       @RequestParam(required = false) String error,
                       HttpSession session,
                       Model model) {
        model.addAttribute("active", "assets");
        model.addAttribute("assets", assetService.list(category, 60));
        model.addAttribute("categories", QuizCategories.ALL);
        model.addAttribute("category", category);
        model.addAttribute("total", assetService.count());
        model.addAttribute("loggedIn", session.getAttribute(AuthUtil.SESSION_USER_ID) != null);
        model.addAttribute("okMsg", ok);
        model.addAttribute("error", error);
        return "assets";
    }

    @PostMapping("/upload")
    public String upload(@RequestParam("file") MultipartFile file,
                         @RequestParam(required = false) String name,
                         @RequestParam(required = false) String category,
                         HttpSession session) {
        Long userId = (Long) session.getAttribute(AuthUtil.SESSION_USER_ID);
        if (userId == null) {
            return "redirect:/login?redirect=/assets";
        }
        try {
            assetService.save(name, category, userId, file);
            return "redirect:/assets?ok=1";
        } catch (IllegalArgumentException e) {
            return "redirect:/assets?error=" + URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            return "redirect:/assets?error=" + URLEncoder.encode("上传失败：" + e.getMessage(), StandardCharsets.UTF_8);
        }
    }
}
