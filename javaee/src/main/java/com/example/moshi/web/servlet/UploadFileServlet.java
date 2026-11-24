package com.example.moshi.web.servlet;

import com.example.moshi.util.UploadPaths;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;

/**
 * 素材文件下载：把 /uploads/xxx.png 映射到磁盘目录。
 *
 * <p>用 getCanonicalPath 做前缀校验，阻断 ../ 穿越。
 */
@WebServlet("/uploads/*")
public class UploadFileServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.length() < 2) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        String name = pathInfo.substring(1);
        File root = UploadPaths.root();
        File file = new File(root, name);
        if (!file.getCanonicalPath().startsWith(root.getCanonicalPath()) || !file.isFile()) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        String contentType = getServletContext().getMimeType(file.getName());
        resp.setContentType(contentType == null ? "application/octet-stream" : contentType);
        resp.setContentLengthLong(file.length());
        try (OutputStream out = resp.getOutputStream()) {
            Files.copy(file.toPath(), out);
        }
    }
}
