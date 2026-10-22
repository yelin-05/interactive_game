package com.example.minigame.module.system.controller;

import com.example.minigame.common.ApiResult;
import com.example.minigame.module.system.vo.PingVO;
import com.example.minigame.module.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.time.LocalDateTime;

/**
 * 系统自检接口：GET /api/ping
 *
 * <p>这一条链路打通说明：Web 容器 → Controller → Service → MyBatis-Plus → 数据源 全部正常。
 * 数据库挂了不会抛 500，而是返回 dbStatus=DOWN，方便前端展示「自检未通过」。
 */
@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PingController {

    private final UserService userService;
    private final DataSource dataSource;

    @Value("${app.name}")
    private String appName;

    @Value("${app.version}")
    private String appVersion;

    @GetMapping("/ping")
    public ApiResult<PingVO> ping() {
        String dbStatus = "DOWN";
        String dbProduct = "-";
        String dbVersion = "-";
        String dbError = null;
        Long userCount = null;

        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData meta = conn.getMetaData();
            dbProduct = meta.getDatabaseProductName();
            dbVersion = meta.getDatabaseProductVersion();
            userCount = userService.count();
            dbStatus = "UP";
        } catch (Exception e) {
            dbError = e.getMessage();
            log.warn("数据库自检失败: {}", e.getMessage());
        }

        PingVO vo = new PingVO(
                appName,
                appVersion,
                System.getProperty("java.version"),
                LocalDateTime.now(),
                dbStatus,
                dbProduct,
                dbVersion,
                userCount,
                dbError
        );
        return ApiResult.ok("system ping", vo);
    }
}
