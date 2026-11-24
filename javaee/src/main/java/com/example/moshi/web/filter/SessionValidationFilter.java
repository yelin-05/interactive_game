package com.example.moshi.web.filter;

import com.example.moshi.entity.User;
import com.example.moshi.service.CurrentUserService;
import com.example.moshi.web.SessionKeys;
import com.example.moshi.web.support.SessionIdentity;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;

public class SessionValidationFilter implements Filter {

    private final CurrentUserService currentUsers = new CurrentUserService();

    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpSession session = req.getSession(false);
        if (session != null) {
            Object id = session.getAttribute(SessionKeys.USER_ID);
            if (id instanceof Long) {
                try {
                    User user = currentUsers.findActive((Long) id);
                    if (user == null) {
                        SessionIdentity.clear(session);
                    } else {
                        SessionIdentity.apply(session, user);
                    }
                } catch (Exception ex) {
                    throw new ServletException("会话校验失败", ex);
                }
            }
        }
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
    }
}
