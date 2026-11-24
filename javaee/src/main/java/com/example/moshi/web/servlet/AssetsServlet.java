package com.example.moshi.web.servlet;

import com.example.moshi.service.AssetService;
import com.example.moshi.util.Categories;
import com.example.moshi.web.SessionKeys;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;

@WebServlet("/assets")
@MultipartConfig(maxFileSize = 10 * 1024 * 1024, maxRequestSize = 12 * 1024 * 1024)
public class AssetsServlet extends HttpServlet {

    private final AssetService assets = new AssetService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String category = req.getParameter("category");
        if (category != null && !Categories.exists(category)) {
            category = null;
        }
        try {
            req.setAttribute("assets", assets.list(category, 60));
            req.setAttribute("total", assets.count());
        } catch (Exception ex) {
            req.setAttribute("assets", List.of());
            req.setAttribute("error", "素材服务暂不可用，请稍后重试");
        }
        HttpSession session = req.getSession(false);
        req.setAttribute("pageTitle", "素材库 · 魔识");
        req.setAttribute("active", "assets");
        req.setAttribute("categories", Categories.ALL);
        req.setAttribute("category", category);
        req.setAttribute("loggedIn", session != null && session.getAttribute(SessionKeys.USER_ID) != null);
        if ("1".equals(req.getParameter("ok"))) {
            req.setAttribute("okMsg", Boolean.TRUE);
        }
        if (req.getParameter("error") != null) {
            req.setAttribute("error", req.getParameter("error"));
        }
        req.getRequestDispatcher("/WEB-INF/views/assets.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        Long userId = session == null ? null : (Long) session.getAttribute(SessionKeys.USER_ID);
        if (userId == null) {
            resp.sendRedirect(req.getContextPath() + "/login?need=1&to=/assets");
            return;
        }
        String category = req.getParameter("category");
        if (category != null && !Categories.exists(category)) {
            category = null;
        }
        try {
            assets.upload(userId, req.getPart("file"), req.getParameter("name"), category);
            resp.sendRedirect(req.getContextPath() + "/assets?ok=1");
        } catch (IllegalArgumentException ex) {
            redirect(req, resp, ex.getMessage());
        } catch (IllegalStateException ex) {
            redirect(req, resp, "文件太大（单文件上限 10MB）");
        } catch (Exception ex) {
            redirect(req, resp, "上传失败，请稍后重试");
        }
    }

    private void redirect(HttpServletRequest req, HttpServletResponse resp, String message) throws IOException {
        resp.sendRedirect(req.getContextPath() + "/assets?error=" + URLEncoder.encode(message, "UTF-8"));
    }
}
