package com.example.minigame.module.game.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 小游戏入口。
 *
 * <p>模板选型阶段已结束，最终选定「知识闯关」，所以 A（打地鼠）/ B（记忆翻牌）
 * 两个候选模板的页面、脚本和路由都已从工程移除 —— 需要的话可以在 git 历史里找回。
 *
 * <p>/games 保留为入口并直接跳到答题页，避免外部存的旧链接 404。
 */
@Controller
@RequestMapping("/games")
public class GameController {

    @GetMapping
    public String index() {
        return "redirect:/games/quiz";
    }

    @GetMapping("/quiz")
    public String quiz() {
        return "games/quiz";
    }
}
