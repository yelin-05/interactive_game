package com.example.moshi.web.filter;

import com.example.moshi.web.SessionKeys;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URLEncoder;

/**
 * 登录过滤器：未登录访问受保护路径时跳登录页，并把原地址记进 Session。
 */
@WebFilter(filterName = "authFilter", urlPatterns = {"/profile", "/profile/*"})
public class AuthFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) resp;
        HttpSession session = request.getSession(false);

        if (session != null && session.getAttribute(SessionKeys.USER_ID) != null) {
            chain.doFilter(req, resp);
            return;
        }

        String target = request.getRequestURI().substring(request.getContextPath().length());
        String query = request.getQueryString();
        if (query != null && !query.isEmpty()) {
            target = target + "?" + query;
        }
        request.getSession(true).setAttribute(SessionKeys.REDIRECT, target);
        response.sendRedirect(request.getContextPath() + "/login?need=1&to="
                + URLEncoder.encode(target, "UTF-8"));
    }

    @Override
    public void destroy() {
    }
}
