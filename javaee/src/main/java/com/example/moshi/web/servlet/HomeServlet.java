package com.example.moshi.web.servlet;

import com.example.moshi.service.HomeService;

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

    private final HomeService home = new HomeService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setAttribute("pageTitle", "魔识 · 互动答题与素材管理");
        req.setAttribute("active", "home");
        req.setAttribute("dbOk", home.databaseAvailable());
        try {
            req.setAttribute("userCount", home.userCount());
        } catch (SQLException e) {
            req.setAttribute("userCount", 0L);
        }
        req.getRequestDispatcher("/WEB-INF/views/home.jsp").forward(req, resp);
    }
}
