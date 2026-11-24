package com.example.moshi.web.filter;

import com.example.moshi.web.SessionKeys;
import com.example.moshi.web.support.Redirects;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

public class AuthFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        String path = req.getRequestURI().substring(req.getContextPath().length());
        boolean protectedPath = path.equals("/profile") || path.startsWith("/profile/");
        boolean protectedMutation = "POST".equalsIgnoreCase(req.getMethod())
                && (path.equals("/tasks") || path.equals("/assets") || path.equals("/llm"));
        if (!protectedPath && !protectedMutation) {
            chain.doFilter(request, response);
            return;
        }

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute(SessionKeys.USER_ID) == null) {
            String target = req.getRequestURI().substring(req.getContextPath().length());
            if (req.getQueryString() != null && !req.getQueryString().isEmpty()) {
                target += "?" + req.getQueryString();
            }
            req.getSession(true).setAttribute(SessionKeys.REDIRECT, Redirects.localPath(target, "/home"));
            resp.sendRedirect(req.getContextPath() + "/login?need=1");
            return;
        }
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
    }
}
