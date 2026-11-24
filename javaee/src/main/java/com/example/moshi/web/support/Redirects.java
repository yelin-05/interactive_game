package com.example.moshi.web.support;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.net.URISyntaxException;

public final class Redirects {

    private Redirects() {
    }

    public static String localPath(String target, String fallback) {
        if (target == null || target.isBlank() || !target.startsWith("/") || target.startsWith("//")) {
            return fallback;
        }
        try {
            URI uri = new URI(target);
            if (uri.isAbsolute() || uri.getAuthority() != null || uri.getFragment() != null) {
                return fallback;
            }
            return target;
        } catch (URISyntaxException ex) {
            return fallback;
        }
    }

    public static void sendLocal(HttpServletRequest request, javax.servlet.http.HttpServletResponse response,
                                 String target, String fallback) throws java.io.IOException {
        String path = localPath(target, fallback);
        response.sendRedirect(request.getContextPath() + path);
    }
}
