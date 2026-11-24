package com.example.moshi.web.servlet;

import com.example.moshi.dao.AssetDao;
import com.example.moshi.dao.QuestionDao;
import com.example.moshi.dao.ScoreDao;
import com.example.moshi.dao.UserDao;
import com.example.moshi.model.Question;
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

    private final UserDao userDao = new UserDao();
    private final QuestionDao questionDao = new QuestionDao();
    private final ScoreDao scoreDao = new ScoreDao();
    private final AssetDao assetDao = new AssetDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            req.setAttribute("userCount", userDao.count());
            req.setAttribute("questionCount", questionDao.count());
            req.setAttribute("scoreCount", scoreDao.count());
            req.setAttribute("assetCount", assetDao.count());

            List<Question> latest = questionDao.latest(6);
            req.setAttribute("recentQuestions", latest);
            req.setAttribute("categoryStats", questionDao.countByCategory());
        } catch (Exception e) {
            req.setAttribute("error", "读取统计失败：" + e.getMessage());
        }
        req.setAttribute("pageTitle", "管理后台 · 魔识");
        req.setAttribute("active", "admin");
        req.setAttribute("categories", Categories.ALL);
        req.getRequestDispatcher("/WEB-INF/views/admin.jsp").forward(req, resp);
    }
}
