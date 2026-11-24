package com.example.moshi.web.support;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RedirectsTest {

    @Test
    void acceptsOnlyLocalPaths() {
        assertEquals("/home", Redirects.localPath("/home", "/"));
        assertEquals("/rank?category=history", Redirects.localPath("/rank?category=history", "/"));
        assertEquals("/", Redirects.localPath("//evil.example", "/"));
        assertEquals("/", Redirects.localPath("https://evil.example", "/"));
        assertEquals("/", Redirects.localPath("/\\evil.example", "/"));
    }
}
