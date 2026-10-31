package com.example.minigame.module.game.controller;

import com.example.minigame.module.game.vo.GameTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * 小游戏模板预览。
 *
 * <p>这一层现在只负责渲染页面，成绩还没入库；定下模板后再接
 * 排行榜（写 t_score）+ 任务（每局算一次任务进度）。
 */
@Controller
@RequestMapping("/games")
public class GameController {

    /** 三个候选模板，改这个列表就能增删选项。 */
    private static final List<GameTemplate> TEMPLATES = List.of(
            new GameTemplate(
                    "reaction",
                    "A · 打地鼠",
                    "反应力",
                    "30 秒内点中随机冒头的地鼠，连击越高单次得分越多。",
                    "30 秒",
                    "低",
                    "排行榜 · 任务"
            ),
            new GameTemplate(
                    "memory",
                    "B · 记忆翻牌",
                    "记忆力",
                    "16 张牌 8 对图案，全部配对成功即通关，步数和用时越少分越高。",
                    "1-2 分钟",
                    "中",
                    "素材库 · 排行榜"
            ),
            new GameTemplate(
                    "quiz",
                    "C · 知识闯关",
                    "答题",
                    "5 道单选题，每题限时 15 秒，答对得基础分，答得快还有时间加成。",
                    "2-3 分钟",
                    "中",
                    "大模型生成 · 任务"
            )
    );

    @GetMapping
    public String index(Model model) {
        model.addAttribute("templates", TEMPLATES);
        return "games/index";
    }

    @GetMapping("/reaction")
    public String reaction() {
        return "games/reaction";
    }

    @GetMapping("/memory")
    public String memory() {
        return "games/memory";
    }

    @GetMapping("/quiz")
    public String quiz() {
        return "games/quiz";
    }
}
