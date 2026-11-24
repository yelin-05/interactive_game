package com.example.moshi.dao;

import com.example.moshi.util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

/** 任务领取记录 DAO。 */
public class UserTaskDao {

    public boolean exists(long userId, long taskId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM t_user_task WHERE user_id = ? AND task_id = ?";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setLong(2, taskId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    public void insert(long userId, long taskId, int rewardPoints) throws SQLException {
        String sql = "INSERT INTO t_user_task (user_id, task_id, reward_points) VALUES (?, ?, ?)";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setLong(2, taskId);
            ps.setInt(3, rewardPoints);
            ps.executeUpdate();
        }
    }

    /** 已领取的任务 id 集合。 */
    public Set<Long> claimedTaskIds(long userId) throws SQLException {
        Set<Long> ids = new HashSet<>();
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT task_id FROM t_user_task WHERE user_id = ?")) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ids.add(rs.getLong(1));
                }
            }
        }
        return ids;
    }

    /** 累计已领取积分。 */
    public int totalReward(long userId) throws SQLException {
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT COALESCE(SUM(reward_points), 0) FROM t_user_task WHERE user_id = ?")) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }
}
