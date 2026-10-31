package com.example.minigame.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 只引入 spring-security-crypto 做密码摘要，不启用 Spring Security 的过滤器链。
 *
 * <p>好处：不改变现有 URL 行为，也不用写 SecurityFilterChain 配置；
 * 代价：拦截逻辑要自己写（见 AuthInterceptor）。
 */
@Configuration
public class PasswordConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
