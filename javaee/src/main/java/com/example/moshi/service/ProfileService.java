package com.example.moshi.service;

import com.example.moshi.dao.ScoreDao;
import com.example.moshi.dao.UserDao;
import com.example.moshi.entity.User;
import com.example.moshi.model.Score;

import java.sql.SQLException;
import java.util.List;

public class ProfileService {

    private final UserDao users = new UserDao();
    private final ScoreDao scores = new ScoreDao();

    public User findActive(long userId) throws SQLException {
        User user = users.findById(userId);
        return user != null && user.getStatus() != null && user.getStatus() == 1 ? user : null;
    }

    public int[] stats(long userId) throws SQLException {
        return scores.statsOf(userId);
    }

    public List<Score> recent(long userId) throws SQLException {
        return scores.recentByUser(userId, 5);
    }
}
