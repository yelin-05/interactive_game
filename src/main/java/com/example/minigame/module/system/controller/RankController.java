package com.example.minigame.module.system.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 排行榜页面。
 *
 * <p>成绩还没入库（t_score 未做），这里先给一个诚实的占位页，
 * 而不是把导航项做成死链。做完成绩入库后，这个页面会换成真实榜单。
 */
@Controller
public class RankController {

    @GetMapping("/rank")
    public String rank(Model model) {
        model.addAttribute("active", "rank");
        return "rank";
    }
}
