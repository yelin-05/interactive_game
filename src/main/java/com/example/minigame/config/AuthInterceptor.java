package com.example.minigame.config;

import com.example.minigame.common.AuthUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * 登录拦截：未登录访问受保护页面时跳到登录页，并记住原本要去哪。
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute(AuthUtil.SESSION_USER_ID) != null) {
            return true;
        }
        String target = request.getRequestURI();
        String query = request.getQueryString();
        if (query != null && !query.isEmpty()) {
            target = target + "?" + query;
        }
        response.sendRedirect("/login?redirect=" + URLEncoder.encode(target, StandardCharsets.UTF_8));
        return false;
    }
}
