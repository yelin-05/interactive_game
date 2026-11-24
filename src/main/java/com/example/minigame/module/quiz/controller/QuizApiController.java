package com.example.minigame.module.quiz.controller;

import com.example.minigame.common.ApiResult;
import com.example.minigame.common.AuthUtil;
import com.example.minigame.module.quiz.QuizMode;
import com.example.minigame.module.quiz.dto.QuizAnswerForm;
import com.example.minigame.module.quiz.dto.QuizSubmitForm;
import com.example.minigame.module.quiz.service.QuizRoundService;
import com.example.minigame.module.quiz.service.QuizService;
import com.example.minigame.module.quiz.vo.QuizAnswerVO;
import com.example.minigame.module.quiz.vo.QuizCategoryVO;
import com.example.minigame.module.quiz.vo.QuizModeVO;
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
 * GET  /api/quiz/modes                 难度档列表
 * GET  /api/quiz/categories            分区列表 + 题量
 * POST /api/quiz/start?category=&mode=  开局：按难度抽题下发（不含答案）
 * POST /api/quiz/answer                逐题作答：返回对错/连击/本题得分（即时反馈）
 * POST /api/quiz/submit                交卷：服务端结算、入库，返回逐题结果
 * </pre>
 *
 * <p>答案始终只留在服务端 Session 的 Round 里，从不下发到浏览器。
 */
@RestController
@RequestMapping("/api/quiz")
@RequiredArgsConstructor
public class QuizApiController {

    private final QuizService quizService;
    private final QuizRoundService roundService;

    @GetMapping("/modes")
    public ApiResult<List<QuizModeVO>> modes() {
        return ApiResult.ok(QuizMode.all().stream()
                .map(m -> new QuizModeVO(m.code(), m.label(), m.icon(), m.desc(),
                        m.count(), m.secondsPerQuestion()))
                .toList());
    }

    @GetMapping("/categories")
    public ApiResult<List<QuizCategoryVO>> categories() {
        return ApiResult.ok(quizService.categories());
    }

    @PostMapping("/start")
    public ApiResult<QuizStartVO> start(@RequestParam String category,
                                        @RequestParam(required = false) String mode,
                                        HttpSession session) {
        return ApiResult.ok(roundService.start(session, category, mode));
    }

    @PostMapping("/answer")
    public ApiResult<QuizAnswerVO> answer(@RequestBody QuizAnswerForm form, HttpSession session) {
        return ApiResult.ok(roundService.answer(session, form));
    }

    @PostMapping("/submit")
    public ApiResult<QuizResultVO> submit(@RequestBody QuizSubmitForm form, HttpSession session) {
        Long userId = (Long) session.getAttribute(AuthUtil.SESSION_USER_ID);
        return ApiResult.ok(roundService.submit(session, form, userId));
    }
}
