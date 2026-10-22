package com.example.minigame.module.system.vo;

import java.time.LocalDateTime;

/**
 * 系统自检结果。答辩演示时打开首页点「系统自检」，
 * 一眼就能看出数据库连没连上、用的什么库。
 */
public record PingVO(
        String app,
        String version,
        String javaVersion,
        LocalDateTime serverTime,
        /** UP / DOWN */
        String dbStatus,
        String dbProduct,
        String dbVersion,
        Long userCount,
        String dbError
) {
}
