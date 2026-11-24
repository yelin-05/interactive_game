package com.example.moshi.web.servlet;

import com.example.moshi.model.QuizMode;
import com.example.moshi.model.QuizRound;
import com.example.moshi.service.QuizService;
import com.example.moshi.util.Categories;
import com.example.moshi.web.SessionKeys;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet("/quiz")
public class QuizServlet extends HttpServlet {

    private final QuizService quiz = new QuizService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        setPage(req);
        if (session != null) {
            try {
                QuizService.Completion finished = quiz.finishIfNeeded(session);
                if (finished != null) {
                    showResult(req, resp, finished.getRound(), finished.getResult());
                    return;
                }
                QuizRound round = quiz.getRound(session);
                if (round != null && !round.isFinished()) {
                    req.setAttribute("round", round);
                    req.setAttribute("item", round.current());
                    req.setAttribute("mode", QuizMode.from(round.getModeCode()));
                    req.getRequestDispatcher("/WEB-INF/views/quiz-question.jsp").forward(req, resp);
                    return;
                }
            } catch (Exception ex) {
                throw new ServletException("读取答题状态失败", ex);
            }
        }
        req.getRequestDispatcher("/WEB-INF/views/quiz-picker.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String action = req.getParameter("action");
        HttpSession session = req.getSession(true);
        if ("start".equals(action)) {
            start(req, resp, session);
        } else if ("answer".equals(action)) {
            answer(req, resp, session);
        } else if ("abandon".equals(action)) {
            quiz.abandon(session);
            resp.sendRedirect(req.getContextPath() + "/quiz");
        } else {
            resp.sendRedirect(req.getContextPath() + "/quiz");
        }
    }

    private void start(HttpServletRequest req, HttpServletResponse resp, HttpSession session)
            throws ServletException, IOException {
        String category = req.getParameter("category");
        QuizMode mode = QuizMode.from(req.getParameter("mode"));
        setPage(req);
        if (!Categories.exists(category)) {
            req.setAttribute("error", "请选择一个有效分区");
            req.getRequestDispatcher("/WEB-INF/views/quiz-picker.jsp").forward(req, resp);
            return;
        }
        try {
            quiz.start(session, category, mode);
            resp.sendRedirect(req.getContextPath() + "/quiz");
        } catch (IllegalArgumentException ex) {
            req.setAttribute("error", ex.getMessage());
            req.getRequestDispatcher("/WEB-INF/views/quiz-picker.jsp").forward(req, resp);
        } catch (Exception ex) {
            throw new ServletException("开局失败", ex);
        }
    }

    private void answer(HttpServletRequest req, HttpServletResponse resp, HttpSession session)
            throws ServletException, IOException {
        QuizRound before = quiz.getRound(session);
        if (before == null || before.isFinished()) {
            resp.sendRedirect(req.getContextPath() + "/quiz");
            return;
        }
        int pick;
        try {
            pick = Integer.parseInt(req.getParameter("pick"));
        } catch (Exception ex) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "选项参数无效");
            return;
        }
        try {
            QuizService.Completion completion = quiz.answer(session, pick);
            if (completion == null) {
                resp.sendRedirect(req.getContextPath() + "/quiz");
                return;
            }
            setPage(req);
            showResult(req, resp, completion.getRound(), completion.getResult());
        } catch (IllegalArgumentException ex) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "选项参数无效");
        } catch (Exception ex) {
            throw new ServletException("提交答案失败", ex);
        }
    }

    private void showResult(HttpServletRequest req, HttpServletResponse resp,
                            QuizRound round, QuizRound.Result result)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        Long userId = session == null ? null : (Long) session.getAttribute(SessionKeys.USER_ID);
        req.setAttribute("result", result);
        req.setAttribute("categoryName", Categories.nameOf(round.getCategory()));
        req.setAttribute("mode", QuizMode.from(round.getModeCode()));
        req.setAttribute("loggedIn", userId != null);
        req.getRequestDispatcher("/WEB-INF/views/quiz-result.jsp").forward(req, resp);
    }

    private void setPage(HttpServletRequest req) {
        req.setAttribute("pageTitle", "答题中心 · 魔识");
        req.setAttribute("active", "quiz");
        req.setAttribute("categories", Categories.ALL);
    }
}
