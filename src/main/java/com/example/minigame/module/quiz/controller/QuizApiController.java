package com.example.minigame.module.quiz.controller;

import com.example.minigame.common.ApiResult;
import com.example.minigame.module.quiz.service.QuizService;
import com.example.minigame.module.quiz.vo.QuizCategoryVO;
import com.example.minigame.module.quiz.vo.QuizQuestionVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 题库接口。
 *
 * <pre>
 * GET /api/quiz/categories                          分区列表 + 题量
 * GET /api/quiz/questions?category=history&limit=5  按分区随机抽题
 * </pre>
 */
@RestController
@RequestMapping("/api/quiz")
@RequiredArgsConstructor
public class QuizApiController {

    private final QuizService quizService;

    @GetMapping("/categories")
    public ApiResult<List<QuizCategoryVO>> categories() {
        return ApiResult.ok(quizService.categories());
    }

    @GetMapping("/questions")
    public ApiResult<List<QuizQuestionVO>> questions(
            @RequestParam String category,
            @RequestParam(defaultValue = "5") int limit) {
        return ApiResult.ok(quizService.randomQuestions(category, limit));
    }
}
