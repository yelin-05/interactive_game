package com.example.moshi.dao;

import com.example.moshi.TestDatabase;
import com.example.moshi.util.DBUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TaskClaimDaoTest extends TestDatabase {

    private final TaskClaimDao claims = new TaskClaimDao();

    @BeforeEach
    void cleanAndSeed() throws Exception {
        clearData();
        try (Connection connection = DBUtil.getConnection(); Statement statement = connection.createStatement()) {
            statement.execute("INSERT INTO t_user (username, password, role, status) "
                    + "VALUES ('student01', 'hash', 'PLAYER', 1)");
            statement.execute("INSERT INTO t_task (title, category, target_count, reward_points, status) "
                    + "VALUES ('历史新手', 'history', 1, 50, 1)");
            statement.execute("INSERT INTO t_score (user_id, category, score, correct_count, total_count, duration_ms) "
                    + "VALUES (1, 'history', 10, 1, 1, 1000)");
        }
    }

    @Test
    void rewardCanOnlyBeClaimedOnce() throws Exception {
        assertEquals(TaskClaimDao.Status.CLAIMED, claims.claim(1L, 1L).getStatus());
        assertEquals(TaskClaimDao.Status.ALREADY_CLAIMED, claims.claim(1L, 1L).getStatus());
        try (Connection connection = DBUtil.getConnection();
             Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery("SELECT COUNT(*) FROM t_user_task")) {
            result.next();
            assertEquals(1, result.getInt(1));
        }
    }
}
