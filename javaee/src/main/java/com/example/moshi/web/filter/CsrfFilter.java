package com.example.moshi.web.filter;

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
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

public class CsrfFilter implements Filter {

    private static final SecureRandom RANDOM = new SecureRandom();

    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        HttpSession session = req.getSession(true);
        String token = (String) session.getAttribute(SessionKeys.CSRF);
        if (token == null) {
            token = generate();
            session.setAttribute(SessionKeys.CSRF, token);
        }
        req.setAttribute("csrfToken", token);

        if ("POST".equalsIgnoreCase(req.getMethod())) {
            String supplied = req.getParameter("_csrf");
            if (!matches(token, supplied)) {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN, "请求校验失败，请刷新页面后重试");
                return;
            }
        }
        chain.doFilter(request, response);
    }

    public static String rotate(HttpSession session) {
        String token = generate();
        session.setAttribute(SessionKeys.CSRF, token);
        return token;
    }

    private static boolean matches(String expected, String supplied) {
        if (supplied == null || supplied.isEmpty()) {
            return false;
        }
        return MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8),
                supplied.getBytes(StandardCharsets.UTF_8));
    }

    private static String generate() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    @Override
    public void destroy() {
    }
}
