package com.example.moshi.dao;

import com.example.moshi.util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;

public class TaskClaimDao {

    public enum Status { CLAIMED, ALREADY_CLAIMED, NOT_FINISHED, NOT_FOUND }

    public static final class Result {
        private final Status status;
        private final int rewardPoints;
        private final int remaining;

        private Result(Status status, int rewardPoints, int remaining) {
            this.status = status;
            this.rewardPoints = rewardPoints;
            this.remaining = remaining;
        }

        public Status getStatus() { return status; }
        public int getRewardPoints() { return rewardPoints; }
        public int getRemaining() { return remaining; }
    }

    public Result claim(long userId, long taskId) throws SQLException {
        try (Connection connection = DBUtil.getConnection()) {
            connection.setAutoCommit(false);
            try {
                int target;
                int reward;
                String category;
                String sql = "SELECT target_count, reward_points, category, status "
                        + "FROM t_task WHERE id = ? FOR UPDATE";
                try (PreparedStatement ps = connection.prepareStatement(sql)) {
                    ps.setLong(1, taskId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            connection.rollback();
                            return new Result(Status.NOT_FOUND, 0, 0);
                        }
                        if (rs.getInt("status") != 1) {
                            connection.rollback();
                            return new Result(Status.NOT_FOUND, 0, 0);
                        }
                        target = rs.getInt("target_count");
                        reward = rs.getInt("reward_points");
                        category = rs.getString("category");
                    }
                }

                String countSql = "SELECT COUNT(*) FROM t_score WHERE user_id = ?";
                if (category != null && !category.isBlank()) {
                    countSql += " AND category = ?";
                }
                int done;
                try (PreparedStatement ps = connection.prepareStatement(countSql)) {
                    ps.setLong(1, userId);
                    if (category != null && !category.isBlank()) {
                        ps.setString(2, category);
                    }
                    try (ResultSet rs = ps.executeQuery()) {
                        done = rs.next() ? rs.getInt(1) : 0;
                    }
                }
                if (done < target) {
                    connection.rollback();
                    return new Result(Status.NOT_FINISHED, reward, target - done);
                }

                try (PreparedStatement ps = connection.prepareStatement(
                        "INSERT INTO t_user_task (user_id, task_id, reward_points) VALUES (?, ?, ?)")) {
                    ps.setLong(1, userId);
                    ps.setLong(2, taskId);
                    ps.setInt(3, reward);
                    ps.executeUpdate();
                } catch (SQLIntegrityConstraintViolationException ex) {
                    connection.rollback();
                    return new Result(Status.ALREADY_CLAIMED, reward, 0);
                }
                connection.commit();
                return new Result(Status.CLAIMED, reward, 0);
            } catch (SQLException ex) {
                connection.rollback();
                throw ex;
            }
        }
    }
}
