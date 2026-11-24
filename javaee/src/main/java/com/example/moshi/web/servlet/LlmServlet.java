package com.example.moshi.web.servlet;

import com.example.moshi.service.LlmService;
import com.example.moshi.util.Categories;
import com.example.moshi.web.SessionKeys;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet("/llm")
public class LlmServlet extends HttpServlet {

    private static final String LAST_GENERATION = "llmLastGeneration";
    private final LlmService llm = new LlmService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        setPage(req);
        req.getRequestDispatcher("/WEB-INF/views/llm.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        Long userId = session == null ? null : (Long) session.getAttribute(SessionKeys.USER_ID);
        if (userId == null) {
            resp.sendRedirect(req.getContextPath() + "/login?need=1&to=/llm");
            return;
        }
        setPage(req);
        long now = System.currentTimeMillis();
        Long last = (Long) session.getAttribute(LAST_GENERATION);
        if (last != null && now - last < 2000L) {
            req.setAttribute("error", "请求过于频繁，请稍后再试");
            req.getRequestDispatcher("/WEB-INF/views/llm.jsp").forward(req, resp);
            return;
        }
        session.setAttribute(LAST_GENERATION, now);
        try {
            req.setAttribute("result", llm.generate(req.getParameter("category"), req.getParameter("topic")));
        } catch (IllegalArgumentException ex) {
            req.setAttribute("error", ex.getMessage());
        } catch (Exception ex) {
            req.setAttribute("error", "生成失败，请检查大模型配置或稍后重试");
        }
        req.getRequestDispatcher("/WEB-INF/views/llm.jsp").forward(req, resp);
    }

    private void setPage(HttpServletRequest req) {
        req.setAttribute("pageTitle", "大模型生成 · 魔识");
        req.setAttribute("active", "llm");
        req.setAttribute("categories", Categories.ALL);
        req.setAttribute("category", req.getParameter("category"));
        req.setAttribute("topic", req.getParameter("topic"));
        req.setAttribute("llmMode", llm.getMode());
    }
}
