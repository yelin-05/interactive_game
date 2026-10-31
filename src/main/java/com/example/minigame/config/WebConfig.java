package com.example.minigame.config;

import com.example.minigame.module.asset.service.AssetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.IOException;
import java.nio.file.Files;

/**
 * 注册拦截器与静态资源映射。
 *
 * <pre>
 * /profile/**  需要登录
 * /admin/**    需要管理员
 * /uploads/**  → 磁盘 uploads/ 目录（素材库文件）
 * </pre>
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;
    private final AdminInterceptor adminInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/profile", "/profile/**");

        registry.addInterceptor(adminInterceptor)
                .addPathPatterns("/admin", "/admin/**");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        try {
            Files.createDirectories(AssetService.ROOT);
        } catch (IOException e) {
            log.warn("创建 uploads 目录失败: {}", e.getMessage());
        }
        String location = AssetService.ROOT.toUri().toString();
        if (!location.endsWith("/")) {
            location = location + "/";
        }
        registry.addResourceHandler("/uploads/**").addResourceLocations(location);
    }
}
