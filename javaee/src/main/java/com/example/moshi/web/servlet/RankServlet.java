package com.example.moshi.web.servlet;

import com.example.moshi.dao.ScoreDao;
import com.example.moshi.model.RankRow;
import com.example.moshi.util.Categories;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/** 排行榜：总榜 + 五个分区榜，取前 10。 */
@WebServlet("/rank")
public class RankServlet extends HttpServlet {

    private final ScoreDao scoreDao = new ScoreDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String category = req.getParameter("category");
        if (category != null && !Categories.exists(category)) {
            category = null;
        }
        try {
            List<RankRow> rows = scoreDao.top(category, 10);
            req.setAttribute("rows", rows);
            req.setAttribute("totalRecords", scoreDao.count());
        } catch (Exception e) {
            req.setAttribute("rows", List.of());
            req.setAttribute("error", "查询失败：" + e.getMessage());
        }
        req.setAttribute("pageTitle", "排行榜 · 魔识");
        req.setAttribute("active", "rank");
        req.setAttribute("categories", Categories.ALL);
        req.setAttribute("category", category);
        req.setAttribute("categoryName", category == null ? "总榜" : Categories.nameOf(category));
        req.getRequestDispatcher("/WEB-INF/views/rank.jsp").forward(req, resp);
    }
}
