package com.example.moshi.web.servlet;

import com.example.moshi.dao.QuestionDao;
import com.example.moshi.dao.ScoreDao;
import com.example.moshi.model.Question;
import com.example.moshi.model.QuizMode;
import com.example.moshi.model.QuizRound;
import com.example.moshi.model.Score;
import com.example.moshi.util.Categories;
import com.example.moshi.web.SessionKeys;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 答题中心：选分区/难度 → 逐题作答 → 服务端判分 → 结果。
 *
 * <p>整局状态放在 Session 的 quizRound 里，正确答案不出服务端。
 */
@WebServlet("/quiz")
public class QuizServlet extends HttpServlet {

    private static final String ATTR_ROUND = "quizRound";

    private final QuestionDao questionDao = new QuestionDao();
    private final ScoreDao scoreDao = new ScoreDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        QuizRound round = session == null ? null : (QuizRound) session.getAttribute(ATTR_ROUND);

        req.setAttribute("pageTitle", "答题中心 · 魔识");
        req.setAttribute("active", "quiz");
        req.setAttribute("categories", Categories.ALL);

        if (round != null && !round.isFinished()) {
            req.setAttribute("round", round);
            req.setAttribute("item", round.current());
            req.setAttribute("mode", QuizMode.from(round.getModeCode()));
            req.getRequestDispatcher("/WEB-INF/views/quiz-question.jsp").forward(req, resp);
            return;
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
            session.removeAttribute(ATTR_ROUND);
            resp.sendRedirect(req.getContextPath() + "/quiz");
        } else {
            resp.sendRedirect(req.getContextPath() + "/quiz");
        }
    }

    private void start(HttpServletRequest req, HttpServletResponse resp, HttpSession session)
            throws ServletException, IOException {
        String category = req.getParameter("category");
        QuizMode mode = QuizMode.from(req.getParameter("mode"));

        if (!Categories.exists(category)) {
            req.setAttribute("pageTitle", "答题中心 · 魔识");
            req.setAttribute("active", "quiz");
            req.setAttribute("categories", Categories.ALL);
            req.setAttribute("error", "请选择一个有效分区");
            req.getRequestDispatcher("/WEB-INF/views/quiz-picker.jsp").forward(req, resp);
            return;
        }

        try {
            List<Question> picked = questionDao.pick(category, mode, mode.getCount());
            if (picked.isEmpty()) {
                req.setAttribute("pageTitle", "答题中心 · 魔识");
                req.setAttribute("active", "quiz");
                req.setAttribute("categories", Categories.ALL);
                req.setAttribute("error", "这个分区在当前难度下还没有题目");
                req.getRequestDispatcher("/WEB-INF/views/quiz-picker.jsp").forward(req, resp);
                return;
            }
            List<QuizRound.Item> items = new ArrayList<>();
            for (Question q : picked) {
                items.add(new QuizRound.Item(q.getId(), q.getQuestion(), q.getOptions(),
                        q.getDifficulty(), q.getAnswerIndex(), q.getExplanation()));
            }
            session.setAttribute(ATTR_ROUND, new QuizRound(category, mode, items, System.currentTimeMillis()));
            resp.sendRedirect(req.getContextPath() + "/quiz");
        } catch (Exception e) {
            throw new ServletException("开局失败：" + e.getMessage(), e);
        }
    }

    private void answer(HttpServletRequest req, HttpServletResponse resp, HttpSession session)
            throws ServletException, IOException {
        QuizRound round = (QuizRound) session.getAttribute(ATTR_ROUND);
        if (round == null || round.isFinished()) {
            resp.sendRedirect(req.getContextPath() + "/quiz");
            return;
        }

        int pick = parseInt(req.getParameter("pick"), -1);
        round.answer(pick);

        if (!round.isFinished()) {
            resp.sendRedirect(req.getContextPath() + "/quiz");
            return;
        }

        Long userId = (Long) session.getAttribute(SessionKeys.USER_ID);
        boolean saved = false;
        if (userId != null) {
            try {
                QuizRound.Result preview = round.grade(true);
                Score s = new Score();
                s.setUserId(userId);
                s.setCategory(round.getCategory());
                s.setScore(preview.getScore());
                s.setCorrectCount(preview.getCorrect());
                s.setTotalCount(preview.getTotal());
                s.setDurationMs(preview.getDurationMs());
                scoreDao.insert(s);
                saved = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        QuizRound.Result result = round.grade(saved);
        session.removeAttribute(ATTR_ROUND);
        req.setAttribute("pageTitle", "本局结算 · 魔识");
        req.setAttribute("active", "quiz");
        req.setAttribute("result", result);
        req.setAttribute("categoryName", Categories.nameOf(round.getCategory()));
        req.setAttribute("mode", QuizMode.from(round.getModeCode()));
        req.setAttribute("loggedIn", userId != null);
        req.getRequestDispatcher("/WEB-INF/views/quiz-result.jsp").forward(req, resp);
    }

    private int parseInt(String s, int fallback) {
        try {
            return Integer.parseInt(s);
        } catch (Exception e) {
            return fallback;
        }
    }
}
