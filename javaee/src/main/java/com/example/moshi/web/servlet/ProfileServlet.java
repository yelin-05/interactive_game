package com.example.moshi.web.servlet;

import com.example.moshi.dao.ScoreDao;
import com.example.moshi.dao.UserDao;
import com.example.moshi.entity.User;
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
import java.util.List;

/** 个人中心：当前登录用户 + 真实战绩（AuthFilter 已保证已登录）。 */
@WebServlet("/profile")
public class ProfileServlet extends HttpServlet {

    private final UserDao userDao = new UserDao();
    private final ScoreDao scoreDao = new ScoreDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        Long userId = session == null ? null : (Long) session.getAttribute(SessionKeys.USER_ID);

        User user = null;
        try {
            if (userId != null) {
                user = userDao.findById(userId);
            }
        } catch (Exception e) {
            req.setAttribute("error", "读取用户失败：" + e.getMessage());
        }
        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/login?need=1");
            return;
        }

        try {
            req.setAttribute("stats", scoreDao.statsOf(userId));
            List<Score> recent = scoreDao.recentByUser(userId, 5);
            req.setAttribute("recent", recent);
        } catch (Exception e) {
            req.setAttribute("error", "读取战绩失败：" + e.getMessage());
        }

        String displayName = user.getDisplayName();
        req.setAttribute("user", user);
        req.setAttribute("initial", displayName.isEmpty() ? "?" : displayName.substring(0, 1));
        req.setAttribute("roleLabel", user.isAdmin() ? "管理员" : "玩家");
        req.setAttribute("pageTitle", "个人中心 · 魔识");
        req.setAttribute("active", "profile");
        req.setAttribute("categories", Categories.ALL);
        req.getRequestDispatcher("/WEB-INF/views/profile.jsp").forward(req, resp);
    }
}
