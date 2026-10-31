package com.example.minigame.config;

import com.example.minigame.common.AuthUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 管理员拦截：必须是已登录的 ADMIN，否则 403。
 */
@Component
public class AdminInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute(AuthUtil.SESSION_USER_ID) == null) {
            response.sendRedirect("/login?redirect=/admin");
            return false;
        }
        if (!AuthUtil.ROLE_ADMIN.equals(session.getAttribute(AuthUtil.SESSION_ROLE))) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("text/plain;charset=UTF-8");
            response.getWriter().write("403 只有管理员可以访问后台");
            return false;
        }
        return true;
    }
}
