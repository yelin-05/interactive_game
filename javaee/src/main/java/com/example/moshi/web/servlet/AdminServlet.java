package com.example.moshi.web.servlet;

import com.example.moshi.model.Question;
import com.example.moshi.service.DashboardService;
import com.example.moshi.util.Categories;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/** 管理后台（只读概览，AdminFilter 已限制为 ADMIN）。 */
@WebServlet("/admin")
public class AdminServlet extends HttpServlet {

    private final DashboardService dashboard = new DashboardService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            req.setAttribute("userCount", dashboard.userCount());
            req.setAttribute("questionCount", dashboard.questionCount());
            req.setAttribute("scoreCount", dashboard.scoreCount());
            req.setAttribute("assetCount", dashboard.assetCount());

            List<Question> latest = dashboard.latestQuestions();
            req.setAttribute("recentQuestions", latest);
            req.setAttribute("categoryStats", dashboard.categoryStats());
        } catch (Exception e) {
            req.setAttribute("error", "统计服务暂不可用，请稍后重试");
        }
        req.setAttribute("pageTitle", "管理后台 · 魔识");
        req.setAttribute("active", "admin");
        req.setAttribute("categories", Categories.ALL);
        req.getRequestDispatcher("/WEB-INF/views/admin.jsp").forward(req, resp);
    }
}
