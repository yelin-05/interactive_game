package com.example.minigame.module.quiz.controller;

import com.example.minigame.common.ApiResult;
import com.example.minigame.common.AuthUtil;
import com.example.minigame.module.quiz.dto.QuizSubmitForm;
import com.example.minigame.module.quiz.service.QuizRoundService;
import com.example.minigame.module.quiz.service.QuizService;
import com.example.minigame.module.quiz.vo.QuizCategoryVO;
import com.example.minigame.module.quiz.vo.QuizResultVO;
import com.example.minigame.module.quiz.vo.QuizStartVO;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 答题接口。
 *
 * <pre>
 * GET  /api/quiz/categories            分区列表 + 题量
 * POST /api/quiz/start?category=xxx    开局：抽题并下发（不含答案）
 * POST /api/quiz/submit                交卷：服务端判分、入库，返回逐题结果
 * </pre>
 *
 * <p>旧的 GET /api/quiz/questions 已删除 —— 它会把正确答案一起发给前端。
 */
@RestController
@RequestMapping("/api/quiz")
@RequiredArgsConstructor
public class QuizApiController {

    private final QuizService quizService;
    private final QuizRoundService roundService;

    @GetMapping("/categories")
    public ApiResult<List<QuizCategoryVO>> categories() {
        return ApiResult.ok(quizService.categories());
    }

    @PostMapping("/start")
    public ApiResult<QuizStartVO> start(@RequestParam String category, HttpSession session) {
        return ApiResult.ok(roundService.start(session, category));
    }

    @PostMapping("/submit")
    public ApiResult<QuizResultVO> submit(@RequestBody QuizSubmitForm form, HttpSession session) {
        Long userId = (Long) session.getAttribute(AuthUtil.SESSION_USER_ID);
        return ApiResult.ok(roundService.submit(session, form, userId));
    }
}
