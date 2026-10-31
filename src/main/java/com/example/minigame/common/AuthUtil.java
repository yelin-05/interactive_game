package com.example.minigame.common;

/**
 * 会话相关的常量与取值工具。
 *
 * <p>登录态直接放在 HttpSession 里（没用 Spring Security 的过滤器链，
 * 课程作业这个体量够用，也更容易讲清楚）。要升级成 Spring Security，
 * 换掉 AuthInterceptor + 这里的读写即可。
 */
public final class AuthUtil {

    /** 登录用户主键 */
    public static final String SESSION_USER_ID = "LOGIN_USER_ID";
    /** 登录用户名 */
    public static final String SESSION_USERNAME = "LOGIN_USERNAME";
    /** 昵称，导航栏直接显示 */
    public static final String SESSION_NICKNAME = "LOGIN_NICKNAME";
    /** 角色：PLAYER / ADMIN */
    public static final String SESSION_ROLE = "LOGIN_ROLE";

    public static final String ROLE_ADMIN = "ADMIN";

    private AuthUtil() {
    }
}
