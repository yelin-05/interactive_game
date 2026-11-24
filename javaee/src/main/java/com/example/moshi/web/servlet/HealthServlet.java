package com.example.moshi.web.servlet;

import com.example.moshi.util.DBUtil;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

@WebServlet(name = "health", urlPatterns = "/health", loadOnStartup = 1)
public class HealthServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        boolean databaseUp;
        try (Connection connection = DBUtil.getConnection()) {
            databaseUp = connection.isValid(2);
        } catch (SQLException | RuntimeException | LinkageError ex) {
            databaseUp = false;
        }
        resp.setStatus(databaseUp ? HttpServletResponse.SC_OK : HttpServletResponse.SC_SERVICE_UNAVAILABLE);
        resp.setContentType("application/json;charset=UTF-8");
        resp.setHeader("Cache-Control", "no-store");
        resp.getWriter().write(databaseUp
                ? "{\"status\":\"UP\",\"db\":\"UP\",\"version\":\"1.0.0\"}"
                : "{\"status\":\"DOWN\",\"db\":\"DOWN\",\"version\":\"1.0.0\"}");
    }
}
