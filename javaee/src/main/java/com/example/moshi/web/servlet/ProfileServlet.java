package com.example.moshi.web.servlet;

import com.example.moshi.entity.User;
import com.example.moshi.model.Score;
import com.example.moshi.service.ProfileService;
import com.example.moshi.util.Categories;
import com.example.moshi.web.SessionKeys;
import com.example.moshi.web.support.SessionIdentity;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

@WebServlet("/profile")
public class ProfileServlet extends HttpServlet {

    private final ProfileService profiles = new ProfileService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        Long userId = session == null ? null : (Long) session.getAttribute(SessionKeys.USER_ID);
        if (userId == null) {
            resp.sendRedirect(req.getContextPath() + "/login?need=1");
            return;
        }
        try {
            User user = profiles.findActive(userId);
            if (user == null) {
                SessionIdentity.clear(session);
                resp.sendRedirect(req.getContextPath() + "/login?need=1");
                return;
            }
            req.setAttribute("stats", profiles.stats(userId));
            List<Score> recent = profiles.recent(userId);
            req.setAttribute("recent", recent);
            String displayName = user.getDisplayName();
            req.setAttribute("user", user);
            req.setAttribute("initial", displayName.isEmpty() ? "?" : displayName.substring(0, 1));
            req.setAttribute("roleLabel", user.isAdmin() ? "管理员" : "玩家");
        } catch (Exception ex) {
            req.setAttribute("error", "个人中心服务暂不可用，请稍后重试");
        }
        req.setAttribute("pageTitle", "个人中心 · 魔识");
        req.setAttribute("active", "profile");
        req.setAttribute("categories", Categories.ALL);
        req.getRequestDispatcher("/WEB-INF/views/profile.jsp").forward(req, resp);
    }
}
