package com.example.moshi.service;

import com.example.moshi.dao.UserDao;
import com.example.moshi.entity.User;

import java.sql.SQLException;

public class CurrentUserService {

    private final UserDao userDao = new UserDao();

    public User findActive(long userId) throws SQLException {
        User user = userDao.findById(userId);
        return user != null && user.getStatus() != null && user.getStatus() == 1 ? user : null;
    }
}
