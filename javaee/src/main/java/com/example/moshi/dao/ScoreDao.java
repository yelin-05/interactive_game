package com.example.moshi.dao;

import com.example.moshi.model.RankRow;
import com.example.moshi.model.Score;
import com.example.moshi.util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/** 成绩 DAO：写入、排行榜、个人统计。 */
public class ScoreDao {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("MM-dd HH:mm");

    public void insert(Score s) throws SQLException {
        String sql = "INSERT INTO t_score (user_id, category, score, correct_count, total_count, duration_ms) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, s.getUserId());
            ps.setString(2, s.getCategory());
            ps.setInt(3, s.getScore());
            ps.setInt(4, s.getCorrectCount());
            ps.setInt(5, s.getTotalCount());
            ps.setInt(6, s.getDurationMs());
            ps.executeUpdate();
        }
    }

    /** 排行榜：两表连接一次取完，避免 N+1。 */
    public List<RankRow> top(String category, int limit) throws SQLException {
        String sql = "SELECT s.score, s.correct_count, s.total_count, s.duration_ms, s.created_at, "
                + "       u.nickname, u.username "
                + "FROM t_score s LEFT JOIN t_user u ON u.id = s.user_id "
                + (category == null || category.isBlank() ? "" : "WHERE s.category = ? ")
                + "ORDER BY s.score DESC, s.duration_ms ASC LIMIT ?";
        List<RankRow> rows = new ArrayList<>();
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            int idx = 1;
            if (category != null && !category.isBlank()) {
                ps.setString(idx++, category);
            }
            ps.setInt(idx, limit);
            try (ResultSet rs = ps.executeQuery()) {
                int rank = 1;
                while (rs.next()) {
                    RankRow r = new RankRow();
                    r.setRank(rank++);
                    String nickname = rs.getString("nickname");
                    r.setNickname(nickname == null || nickname.isBlank() ? rs.getString("username") : nickname);
                    r.setScore(rs.getInt("score"));
                    r.setCorrect(rs.getInt("correct_count"));
                    r.setTotal(rs.getInt("total_count"));
                    r.setDurationSeconds(rs.getInt("duration_ms") / 1000);
                    Timestamp ts = rs.getTimestamp("created_at");
                    r.setCreatedAt(ts == null ? "" : ts.toLocalDateTime().format(FMT));
                    rows.add(r);
                }
            }
        }
        return rows;
    }

    public List<Score> recentByUser(long userId, int limit) throws SQLException {
        String sql = "SELECT id, user_id, category, score, correct_count, total_count, duration_ms "
                + "FROM t_score WHERE user_id = ? ORDER BY id DESC LIMIT ?";
        List<Score> list = new ArrayList<>();
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setInt(2, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Score s = new Score();
                    s.setId(rs.getLong("id"));
                    s.setUserId(rs.getLong("user_id"));
                    s.setCategory(rs.getString("category"));
                    s.setScore(rs.getInt("score"));
                    s.setCorrectCount(rs.getInt("correct_count"));
                    s.setTotalCount(rs.getInt("total_count"));
                    s.setDurationMs(rs.getInt("duration_ms"));
                    list.add(s);
                }
            }
        }
        return list;
    }

    /** 个人统计：[局数, 最高分, 正确率%, 总榜名次] */
    public int[] statsOf(long userId) throws SQLException {
        String sql = "SELECT COUNT(*) AS rounds, COALESCE(MAX(score), 0) AS best, "
                + "       COALESCE(SUM(correct_count), 0) AS cs, COALESCE(SUM(total_count), 0) AS ts "
                + "FROM t_score WHERE user_id = ?";
        int rounds = 0;
        int best = 0;
        int accuracy = 0;
        int rank = 0;
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    rounds = rs.getInt("rounds");
                    best = rs.getInt("best");
                    int cs = rs.getInt("cs");
                    int ts = rs.getInt("ts");
                    accuracy = ts == 0 ? 0 : Math.round(cs * 100f / ts);
                }
            }
        }
        if (rounds > 0) {
            try (Connection c = DBUtil.getConnection();
                 PreparedStatement ps = c.prepareStatement(
                         "SELECT COUNT(*) FROM t_score WHERE score > ?")) {
                ps.setInt(1, best);
                try (ResultSet rs = ps.executeQuery()) {
                    rank = rs.next() ? rs.getInt(1) + 1 : 1;
                }
            }
        }
        return new int[]{rounds, best, accuracy, rank};
    }

    /** 某人在某分区（category 为 null 表示任意）完成的局数，任务进度用。 */
    public int countRounds(long userId, String category) throws SQLException {
        String sql = "SELECT COUNT(*) FROM t_score WHERE user_id = ?"
                + (category == null || category.isBlank() ? "" : " AND category = ?");
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, userId);
            if (category != null && !category.isBlank()) {
                ps.setString(2, category);
            }
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    public long count() throws SQLException {
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM t_score");
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getLong(1) : 0L;
        }
    }
}
