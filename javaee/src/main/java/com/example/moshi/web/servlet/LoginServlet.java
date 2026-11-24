package com.example.moshi.web.servlet;

import com.example.moshi.entity.User;
import com.example.moshi.service.AuthService;
import com.example.moshi.web.SessionKeys;
import com.example.moshi.web.filter.CsrfFilter;
import com.example.moshi.web.support.Redirects;
import com.example.moshi.web.support.SessionIdentity;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private final AuthService auth = new AuthService();

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

        User user = null;
        try {
            user = auth.authenticate(username, password);
        } catch (Exception ex) {
            req.setAttribute("error", "登录服务暂不可用，请稍后重试");
        }

        if (user == null) {
            if (req.getAttribute("error") == null) {
                req.setAttribute("error", "用户名或密码不正确");
            }
            req.setAttribute("pageTitle", "登录 · 魔识");
            req.setAttribute("active", "login");
            req.setAttribute("username", username);
            req.setAttribute("to", to);
            req.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(req, resp);
            return;
        }

        HttpSession oldSession = req.getSession(false);
        String savedTarget = oldSession == null ? null : (String) oldSession.getAttribute(SessionKeys.REDIRECT);
        if (oldSession != null) {
            oldSession.invalidate();
        }
        HttpSession session = req.getSession(true);
        SessionIdentity.apply(session, user);
        CsrfFilter.rotate(session);

        Redirects.sendLocal(req, resp, savedTarget == null ? to : savedTarget, "/home");
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }
}
