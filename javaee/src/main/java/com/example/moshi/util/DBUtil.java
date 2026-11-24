package com.example.moshi.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * JDBC 连接工具。
 *
 * <p>课程要求原生 JDBC，这里用 DriverManager 直接取连接，
 * 用完必须由调用方在 try-with-resources 里关闭。
 *
 * <p>连接参数优先读环境变量/系统属性，读不到用默认值，
 * 这样换一台机器只要改环境变量，不用改代码。
 */
public final class DBUtil {

    private static final String DEFAULT_URL =
            "jdbc:mysql://localhost:3306/minigame"
                    + "?useUnicode=true&characterEncoding=UTF-8"
                    + "&serverTimezone=Asia/Shanghai&useSSL=false"
                    + "&allowPublicKeyRetrieval=true";

    private static final String URL = env("MOSHI_DB_URL", DEFAULT_URL);
    private static final String USER = env("MOSHI_DB_USER", "root");
    private static final String PASSWORD = env("MOSHI_DB_PASSWORD", "123456");

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("MySQL 驱动未加载：" + e.getMessage(), e);
        }
    }

    private DBUtil() {
    }

    private static String env(String key, String fallback) {
        String v = System.getenv(key);
        if (v == null || v.isBlank()) {
            v = System.getProperty(key);
        }
        return (v == null || v.isBlank()) ? fallback : v;
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    /** 启动时探一次库，方便在日志里看出配置对不对。 */
    public static boolean ping() {
        try (Connection c = getConnection()) {
            return c != null && !c.isClosed();
        } catch (SQLException e) {
            System.err.println("[DBUtil] 数据库连接失败: " + e.getMessage());
            return false;
        }
    }
}
