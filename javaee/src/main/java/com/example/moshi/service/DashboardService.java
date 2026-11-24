package com.example.moshi.service;

import com.example.moshi.dao.AssetDao;
import com.example.moshi.dao.QuestionDao;
import com.example.moshi.dao.ScoreDao;
import com.example.moshi.dao.UserDao;
import com.example.moshi.model.Question;

import java.sql.SQLException;
import java.util.List;

public class DashboardService {

    private final UserDao users = new UserDao();
    private final QuestionDao questions = new QuestionDao();
    private final ScoreDao scores = new ScoreDao();
    private final AssetDao assets = new AssetDao();

    public long userCount() throws SQLException { return users.count(); }
    public long questionCount() throws SQLException { return questions.count(); }
    public long scoreCount() throws SQLException { return scores.count(); }
    public long assetCount() throws SQLException { return assets.count(); }
    public List<Question> latestQuestions() throws SQLException { return questions.latest(6); }
    public List<Object[]> categoryStats() throws SQLException { return questions.countByCategory(); }
}
