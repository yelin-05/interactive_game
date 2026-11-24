package com.example.moshi.web.servlet;

import com.example.moshi.service.AuthService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/register")
public class RegisterServlet extends HttpServlet {

    private final AuthService auth = new AuthService();

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
        String username = trim(req.getParameter("username"));
        String password = req.getParameter("password");
        String password2 = req.getParameter("password2");
        String nickname = req.getParameter("nickname");

        try {
            auth.register(username, password, password2, nickname);
            resp.sendRedirect(req.getContextPath() + "/login?registered=1");
        } catch (IllegalArgumentException ex) {
            showForm(req, resp, ex.getMessage(), username, nickname);
        } catch (Exception ex) {
            showForm(req, resp, "注册服务暂不可用，请稍后重试", username, nickname);
        }
    }

    private void showForm(HttpServletRequest req, HttpServletResponse resp,
                          String error, String username, String nickname)
            throws ServletException, IOException {
        req.setAttribute("pageTitle", "注册 · 魔识");
        req.setAttribute("active", "login");
        req.setAttribute("error", error);
        req.setAttribute("username", username);
        req.setAttribute("nickname", nickname);
        req.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(req, resp);
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }
}
