package com.example.moshi.web;

/** Session 里用到的键名，集中定义避免拼错。 */
public final class SessionKeys {

    public static final String USER_ID = "loginUserId";
    public static final String USERNAME = "loginUsername";
    public static final String NICKNAME = "loginNickname";
    public static final String ROLE = "loginRole";

    /** 登录后要跳回的原始地址 */
    public static final String REDIRECT = "loginRedirect";

    private SessionKeys() {
    }

    public static boolean isAdmin(javax.servlet.http.HttpSession session) {
        return session != null && "ADMIN".equals(session.getAttribute(ROLE));
    }
}
