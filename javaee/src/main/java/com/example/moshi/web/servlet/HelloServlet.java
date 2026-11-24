package com.example.moshi.web.servlet;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Logger;

@WebServlet(name = "hello", urlPatterns = "/hello", loadOnStartup = 1)
public class HelloServlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(HelloServlet.class.getName());
    private static final long serialVersionUID = 1L;

    @Override
    public void init() throws ServletException {
        LOG.info("[lifecycle] init() 容器=" + getServletContext().getServerInfo());
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        LOG.info("[lifecycle] service() method=" + req.getMethod());
        super.service(req, resp);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String name = req.getParameter("name");
        if (name == null || name.trim().isEmpty()) {
            name = "同学";
        } else if (name.length() > 50) {
            name = name.substring(0, 50);
        }
        req.setAttribute("name", name);
        req.setAttribute("serverTime",
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        req.getRequestDispatcher("/WEB-INF/views/hello.jsp").forward(req, resp);
    }

    @Override
    public void destroy() {
        LOG.info("[lifecycle] destroy()");
    }
}
