package com.example.moshi.web.servlet;

import com.example.moshi.dao.AssetDao;
import com.example.moshi.model.Asset;
import com.example.moshi.util.Categories;
import com.example.moshi.util.UploadPaths;
import com.example.moshi.web.SessionKeys;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;
import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/**
 * 素材库：浏览 + 上传。
 *
 * <p>安全点：只放行图片后缀；落盘文件名用 UUID 重新生成（防 ../ 路径穿越）；
 * 大小上限由 @MultipartConfig 控制。
 */
@WebServlet("/assets")
@MultipartConfig(maxFileSize = 10 * 1024 * 1024, maxRequestSize = 12 * 1024 * 1024)
public class AssetsServlet extends HttpServlet {

    private static final Set<String> ALLOWED = Set.of("png", "jpg", "jpeg", "gif", "webp");

    private final AssetDao assetDao = new AssetDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String category = req.getParameter("category");
        if (category != null && !Categories.exists(category)) {
            category = null;
        }
        try {
            List<Asset> list = assetDao.list(category, 60);
            req.setAttribute("assets", list);
            req.setAttribute("total", assetDao.count());
        } catch (Exception e) {
            req.setAttribute("assets", List.of());
            req.setAttribute("error", "读取素材失败：" + e.getMessage());
        }

        HttpSession session = req.getSession(false);
        req.setAttribute("pageTitle", "素材库 · 魔识");
        req.setAttribute("active", "assets");
        req.setAttribute("categories", Categories.ALL);
        req.setAttribute("category", category);
        req.setAttribute("loggedIn", session != null && session.getAttribute(SessionKeys.USER_ID) != null);
        req.setAttribute("okMsg", req.getParameter("ok"));
        if (req.getParameter("error") != null) {
            req.setAttribute("error", req.getParameter("error"));
        }
        req.getRequestDispatcher("/WEB-INF/views/assets.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute(SessionKeys.USER_ID) == null) {
            resp.sendRedirect(req.getContextPath() + "/login?need=1&to=/assets");
            return;
        }

        try {
            Part filePart = req.getPart("file");
            if (filePart == null || filePart.getSize() == 0) {
                redirect(req, resp, "请选择要上传的文件");
                return;
            }
            String submitted = filePart.getSubmittedFileName();
            String ext = extension(submitted == null ? "" : submitted);
            if (!ALLOWED.contains(ext)) {
                redirect(req, resp, "只支持 png / jpg / jpeg / gif / webp 图片");
                return;
            }

            String stored = UUID.randomUUID().toString().replace("-", "") + "." + ext;
            File target = new File(UploadPaths.root(), stored);
            filePart.write(target.getAbsolutePath());

            String name = req.getParameter("name");
            Asset a = new Asset();
            a.setName(name == null || name.isBlank()
                    ? (submitted == null ? stored : submitted) : name.trim());
            a.setOriginalName(submitted);
            a.setContentType(filePart.getContentType());
            a.setSizeBytes(filePart.getSize());
            a.setCategory(req.getParameter("category"));
            a.setStoredPath(stored);
            assetDao.insert(a);

            resp.sendRedirect(req.getContextPath() + "/assets?ok=1");
        } catch (IllegalStateException e) {
            redirect(req, resp, "文件太大（单文件上限 10MB）");
        } catch (Exception e) {
            redirect(req, resp, "上传失败：" + e.getMessage());
        }
    }

    private void redirect(HttpServletRequest req, HttpServletResponse resp, String error) throws IOException {
        resp.sendRedirect(req.getContextPath() + "/assets?error="
                + URLEncoder.encode(error, "UTF-8"));
    }

    private String extension(String filename) {
        int dot = filename.lastIndexOf('.');
        if (dot < 0 || dot == filename.length() - 1) {
            return "";
        }
        return filename.substring(dot + 1).toLowerCase(Locale.ROOT);
    }
}
