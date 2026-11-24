package com.example.moshi.dao;

import com.example.moshi.model.Task;
import com.example.moshi.util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** 任务定义 DAO。 */
public class TaskDao {

    public List<Task> listActive() throws SQLException {
        String sql = "SELECT id, title, description, category, target_count, reward_points, status "
                + "FROM t_task WHERE status = 1 ORDER BY id";
        List<Task> list = new ArrayList<>();
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(map(rs));
            }
        }
        return list;
    }

    public Task findById(long id) throws SQLException {
        String sql = "SELECT id, title, description, category, target_count, reward_points, status "
                + "FROM t_task WHERE id = ?";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    private Task map(ResultSet rs) throws SQLException {
        Task t = new Task();
        t.setId(rs.getLong("id"));
        t.setTitle(rs.getString("title"));
        t.setDescription(rs.getString("description"));
        t.setCategory(rs.getString("category"));
        t.setTargetCount(rs.getInt("target_count"));
        t.setRewardPoints(rs.getInt("reward_points"));
        t.setStatus(rs.getInt("status"));
        return t;
    }
}
