package com.example.moshi.web.servlet;

import com.example.moshi.dao.UserDao;
import com.example.moshi.entity.User;
import org.mindrot.jbcrypt.BCrypt;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/** 注册：用户名唯一、密码 BCrypt 加盐摘要。 */
@WebServlet("/register")
public class RegisterServlet extends HttpServlet {

    private final UserDao userDao = new UserDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setAttribute("pageTitle", "注册 · 魔识");
        req.setAttribute("active", "login");
        req.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String username = req.getParameter("username") == null ? "" : req.getParameter("username").trim();
        String password = req.getParameter("password");
        String password2 = req.getParameter("password2");
        String nickname = req.getParameter("nickname");

        String error = validate(username, password, password2);
        if (error == null) {
            try {
                if (userDao.findByUsername(username) != null) {
                    error = "用户名「" + username + "」已被占用";
                } else {
                    User u = new User();
                    u.setUsername(username);
                    u.setPassword(BCrypt.hashpw(password, BCrypt.gensalt()));
                    u.setNickname(nickname == null || nickname.isBlank() ? username : nickname.trim());
                    u.setRole("PLAYER");
                    u.setStatus(1);
                    userDao.insert(u);
                    resp.sendRedirect(req.getContextPath() + "/login?registered=1");
                    return;
                }
            } catch (Exception e) {
                error = "注册失败：" + e.getMessage();
            }
        }

        req.setAttribute("pageTitle", "注册 · 魔识");
        req.setAttribute("active", "login");
        req.setAttribute("error", error);
        req.setAttribute("username", username);
        req.setAttribute("nickname", nickname);
        req.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(req, resp);
    }

    private String validate(String username, String password, String password2) {
        if (username.isEmpty()) {
            return "用户名不能为空";
        }
        if (username.length() < 3 || username.length() > 20) {
            return "用户名长度需在 3 到 20 之间";
        }
        if (!username.matches("[A-Za-z0-9_]+")) {
            return "用户名只能包含字母、数字和下划线";
        }
        if (password == null || password.length() < 6) {
            return "密码至少 6 位";
        }
        if (!password.equals(password2)) {
            return "两次输入的密码不一致";
        }
        return null;
    }
}
