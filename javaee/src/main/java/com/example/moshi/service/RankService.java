package com.example.moshi.service;

import com.example.moshi.dao.ScoreDao;
import com.example.moshi.model.RankRow;

import java.sql.SQLException;
import java.util.List;

public class RankService {

    private final ScoreDao scores = new ScoreDao();

    public List<RankRow> top(String category, int limit) throws SQLException {
        return scores.top(category, limit);
    }

    public long count() throws SQLException {
        return scores.count();
    }
}
