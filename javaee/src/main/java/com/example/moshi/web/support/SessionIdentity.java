package com.example.moshi.web.support;

import com.example.moshi.entity.User;
import com.example.moshi.web.SessionKeys;

import javax.servlet.http.HttpSession;

public final class SessionIdentity {

    private SessionIdentity() {
    }

    public static void apply(HttpSession session, User user) {
        session.setAttribute(SessionKeys.USER_ID, user.getId());
        session.setAttribute(SessionKeys.USERNAME, user.getUsername());
        session.setAttribute(SessionKeys.NICKNAME, user.getDisplayName());
        session.setAttribute(SessionKeys.ROLE, user.getRole());
    }

    public static void clear(HttpSession session) {
        session.removeAttribute(SessionKeys.USER_ID);
        session.removeAttribute(SessionKeys.USERNAME);
        session.removeAttribute(SessionKeys.NICKNAME);
        session.removeAttribute(SessionKeys.ROLE);
        session.removeAttribute(SessionKeys.REDIRECT);
        session.removeAttribute("quizRound");
    }
}
