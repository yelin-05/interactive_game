package com.example.moshi.web.filter;

import com.example.moshi.entity.User;
import com.example.moshi.service.CurrentUserService;
import com.example.moshi.web.SessionKeys;

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

public class AdminFilter implements Filter {

    private final CurrentUserService currentUsers = new CurrentUserService();

    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        HttpSession session = req.getSession(false);
        Object userId = session == null ? null : session.getAttribute(SessionKeys.USER_ID);
        if (!(userId instanceof Long)) {
            resp.sendRedirect(req.getContextPath() + "/login?need=1");
            return;
        }
        try {
            User user = currentUsers.findActive((Long) userId);
            if (user == null || !"ADMIN".equals(user.getRole())) {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN, "只有管理员可以访问后台");
                return;
            }
            chain.doFilter(request, response);
        } catch (Exception ex) {
            throw new ServletException("权限校验失败", ex);
        }
    }

    @Override
    public void destroy() {
    }
}
