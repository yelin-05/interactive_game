package com.example.moshi.service;

import com.example.moshi.dao.UserDao;
import com.example.moshi.entity.User;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.SQLException;

public class AuthService {

    private final UserDao userDao = new UserDao();

    public User register(String username, String password, String password2, String nickname)
            throws SQLException {
        validateRegistration(username, password, password2);
        if (userDao.findByUsername(username) != null) {
            throw new IllegalArgumentException("用户名已被占用");
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(BCrypt.hashpw(password, BCrypt.gensalt()));
        user.setNickname(nickname == null || nickname.isBlank() ? username : nickname.trim());
        user.setRole("PLAYER");
        user.setStatus(1);
        userDao.insert(user);
        return user;
    }

    public User authenticate(String username, String password) throws SQLException {
        if (username == null || username.isBlank() || password == null || password.isEmpty()) {
            return null;
        }
        User user = userDao.findByUsername(username.trim());
        if (user == null || !BCrypt.checkpw(password, user.getPassword())) {
            return null;
        }
        return user.getStatus() != null && user.getStatus() == 0 ? null : user;
    }

    public static void validateRegistration(String username, String password, String password2) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("用户名不能为空");
        }
        if (username.length() < 3 || username.length() > 20) {
            throw new IllegalArgumentException("用户名长度需在 3 到 20 之间");
        }
        if (!username.matches("[A-Za-z0-9_]+")) {
            throw new IllegalArgumentException("用户名只能包含字母、数字和下划线");
        }
        if (password == null || password.length() < 6 || password.length() > 72) {
            throw new IllegalArgumentException("密码长度需在 6 到 72 之间");
        }
        if (!password.equals(password2)) {
            throw new IllegalArgumentException("两次输入的密码不一致");
        }
    }
}
