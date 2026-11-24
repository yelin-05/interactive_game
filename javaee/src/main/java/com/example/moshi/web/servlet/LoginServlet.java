package com.example.moshi.web.servlet;

import com.example.moshi.dao.UserDao;
import com.example.moshi.entity.User;
import com.example.moshi.web.SessionKeys;
import org.mindrot.jbcrypt.BCrypt;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/** 登录：GET 显示页面，POST 校验并写入 Session。 */
@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private final UserDao userDao = new UserDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setAttribute("pageTitle", "登录 · 魔识");
        req.setAttribute("active", "login");
        req.setAttribute("to", req.getParameter("to"));
        req.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String username = trim(req.getParameter("username"));
        String password = req.getParameter("password");
        String to = req.getParameter("to");

        String error = null;
        User user = null;
        try {
            if (username.isEmpty() || password == null || password.isEmpty()) {
                error = "请输入用户名和密码";
            } else {
                user = userDao.findByUsername(username);
                // 用户名不存在与密码错误返回同一句，避免被人枚举账号
                if (user == null || !BCrypt.checkpw(password, user.getPassword())) {
                    error = "用户名或密码不正确";
                } else if (user.getStatus() != null && user.getStatus() == 0) {
                    error = "该账号已被禁用";
                }
            }
        } catch (Exception e) {
            error = "登录失败：" + e.getMessage();
        }

        if (error != null) {
            req.setAttribute("pageTitle", "登录 · 魔识");
            req.setAttribute("active", "login");
            req.setAttribute("error", error);
            req.setAttribute("username", username);
            req.setAttribute("to", to);
            req.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(req, resp);
            return;
        }

        HttpSession session = req.getSession(true);
        session.setAttribute(SessionKeys.USER_ID, user.getId());
        session.setAttribute(SessionKeys.USERNAME, user.getUsername());
        session.setAttribute(SessionKeys.NICKNAME, user.getDisplayName());
        session.setAttribute(SessionKeys.ROLE, user.getRole());

        String target = (to != null && to.startsWith("/")) ? to : "/home";
        resp.sendRedirect(req.getContextPath() + target);
    }

    private String trim(String s) {
        return s == null ? "" : s.trim();
    }
}
