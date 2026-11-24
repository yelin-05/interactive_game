package com.example.moshi.service;

import com.example.moshi.dao.UserDao;
import com.example.moshi.util.DBUtil;

import java.sql.SQLException;

public class HomeService {

    private final UserDao users = new UserDao();

    public boolean databaseAvailable() {
        return DBUtil.ping();
    }

    public long userCount() throws SQLException {
        return users.count();
    }
}
