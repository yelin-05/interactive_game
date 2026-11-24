package com.example.moshi.dao;

import com.example.moshi.model.Question;
import com.example.moshi.model.QuizMode;
import com.example.moshi.util.DBUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 题目 DAO。选项存在 options_json 里，这里用 Jackson 解析成 List。
 */
public class QuestionDao {

    private static final ObjectMapper JSON = new ObjectMapper();

    /** 按分区 + 难度档位取题池，再在 Java 里打乱取前 N 道。 */
    public List<Question> pick(String category, QuizMode mode, int limit) throws SQLException {
        int[] diffs = mode.getDifficulties();
        StringBuilder in = new StringBuilder();
        for (int i = 0; i < diffs.length; i++) {
            in.append(i == 0 ? "?" : ",?");
        }
        String sql = "SELECT id, category, question, options_json, answer_index, explanation, difficulty "
                + "FROM t_question WHERE category = ? AND status = 1 AND difficulty IN (" + in + ")";

        List<Question> pool = new ArrayList<>();
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, category);
            for (int i = 0; i < diffs.length; i++) {
                ps.setInt(i + 2, diffs[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    pool.add(map(rs));
                }
            }
        }
        Collections.shuffle(pool);
        return pool.size() <= limit ? pool : pool.subList(0, limit);
    }

    public List<Question> latest(int limit) throws SQLException {
        String sql = "SELECT id, category, question, options_json, answer_index, explanation, difficulty "
                + "FROM t_question ORDER BY id DESC LIMIT ?";
        List<Question> list = new ArrayList<>();
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        }
        return list;
    }

    /** 各分区题量统计：返回 [code, count, ...]。 */
    public List<Object[]> countByCategory() throws SQLException {
        String sql = "SELECT category, COUNT(*) FROM t_question WHERE status = 1 GROUP BY category";
        List<Object[]> rows = new ArrayList<>();
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                rows.add(new Object[]{rs.getString(1), rs.getInt(2)});
            }
        }
        return rows;
    }

    public long count() throws SQLException {
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM t_question");
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getLong(1) : 0L;
        }
    }

    private Question map(ResultSet rs) throws SQLException {
        Question q = new Question();
        q.setId(rs.getLong("id"));
        q.setCategory(rs.getString("category"));
        q.setQuestion(rs.getString("question"));
        q.setOptions(parseOptions(rs.getString("options_json")));
        q.setAnswerIndex(rs.getInt("answer_index"));
        q.setExplanation(rs.getString("explanation"));
        q.setDifficulty(rs.getInt("difficulty"));
        return q;
    }

    private List<String> parseOptions(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            return JSON.readValue(json, new TypeReference<List<String>>() {
            });
        } catch (Exception e) {
            System.err.println("[QuestionDao] options_json 解析失败: " + e.getMessage());
            return List.of();
        }
    }
}
