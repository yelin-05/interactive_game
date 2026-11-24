package com.example.moshi.web.servlet;

import com.example.moshi.model.Asset;
import com.example.moshi.service.AssetService;
import com.example.moshi.util.UploadPaths;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;

@WebServlet("/uploads/*")
public class UploadFileServlet extends HttpServlet {

    private final AssetService assets = new AssetService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setHeader("X-Content-Type-Options", "nosniff");
        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.length() < 2
                || pathInfo.indexOf('/', 1) >= 0 || pathInfo.contains("\\")) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        String name = pathInfo.substring(1);
        try {
            Asset asset = assets.find(name);
            File file = UploadPaths.resolve(name);
            if (asset == null || !name.equals(asset.getStoredPath())) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            resp.setContentType(asset.getContentType());
            resp.setContentLengthLong(file.length());
            try (OutputStream out = resp.getOutputStream()) {
                Files.copy(file.toPath(), out);
            }
        } catch (IOException ex) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        } catch (Exception ex) {
            resp.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, "素材服务暂不可用");
        }
    }
}
