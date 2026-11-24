package com.example.moshi.web.servlet;

import com.example.moshi.dao.UserDao;
import com.example.moshi.util.DBUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

/**
 * 首页。
 *
 * <p>映射到空字符串，即上下文根 "/moshi" 本身。
 */
@WebServlet(name = "homeServlet", urlPatterns = {"", "/home"})
public class HomeServlet extends HttpServlet {

    private final UserDao userDao = new UserDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setAttribute("pageTitle", "魔识 · 互动答题与素材管理");
        req.setAttribute("active", "home");
        req.setAttribute("dbOk", DBUtil.ping());
        try {
            req.setAttribute("userCount", userDao.count());
        } catch (SQLException e) {
            req.setAttribute("userCount", 0L);
            req.setAttribute("dbError", e.getMessage());
        }
        req.getRequestDispatcher("/WEB-INF/views/home.jsp").forward(req, resp);
    }
}
