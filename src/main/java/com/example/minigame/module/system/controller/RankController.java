package com.example.minigame.module.system.controller;

import com.example.minigame.module.quiz.QuizCategories;
import com.example.minigame.module.quiz.service.ScoreService;
import com.example.minigame.module.quiz.vo.RankRowVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 排行榜：总榜 + 按分区，取分数降序、同分按耗时升序的前 10。
 */
@Controller
@RequiredArgsConstructor
public class RankController {

    private static final int TOP_N = 10;

    private final ScoreService scoreService;

    @GetMapping("/rank")
    public String rank(@RequestParam(required = false) String category, Model model) {
        String cat = (category == null || category.isBlank()) ? null : category;
        List<RankRowVO> rows = scoreService.top(cat, TOP_N);

        model.addAttribute("active", "rank");
        model.addAttribute("rows", rows);
        model.addAttribute("category", cat);
        model.addAttribute("categories", QuizCategories.ALL);
        model.addAttribute("totalRecords", scoreService.count());
        model.addAttribute("categoryName", cat == null ? "总榜" : QuizCategories.of(cat).name());
        model.addAttribute("timeFmt", java.time.format.DateTimeFormatter.ofPattern("MM-dd HH:mm"));
        return "rank";
    }
}
