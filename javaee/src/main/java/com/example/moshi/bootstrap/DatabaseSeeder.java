package com.example.moshi.bootstrap;

import com.example.moshi.util.DBUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

@WebListener
public class DatabaseSeeder implements ServletContextListener {

    private static final ObjectMapper JSON = new ObjectMapper();
    private static final List<String> QUESTION_FILES = List.of(
            "questions.json",
            "questions-extra-computer-a.json", "questions-extra-computer-b.json",
            "questions-extra-literature-a.json", "questions-extra-literature-b.json",
            "questions-extra-geography-a.json", "questions-extra-geography-b.json",
            "questions-extra-history-a.json", "questions-extra-history-b.json",
            "questions-extra-science-a.json", "questions-extra-science-b.json");

    @Override
    public void contextInitialized(ServletContextEvent event) {
        if ("false".equalsIgnoreCase(env("MOSHI_DB_SEED", "true"))) {
            return;
        }
        try (Connection connection = DBUtil.getConnection()) {
            connection.setAutoCommit(false);
            try {
                seedQuestions(connection);
                seedTasks(connection);
                connection.commit();
            } catch (Exception ex) {
                connection.rollback();
                throw new IllegalStateException("数据库种子初始化失败", ex);
            }
        } catch (Exception ex) {
            throw new IllegalStateException("应用启动失败：请先执行 schema-mysql.sql 并检查数据库配置", ex);
        }
    }

    private void seedQuestions(Connection connection) throws Exception {
        if (count(connection, "t_question") > 0) {
            return;
        }
        String sql = "INSERT INTO t_question (category, question, options_json, answer_index, explanation, difficulty, status) "
                + "VALUES (?, ?, ?, ?, ?, ?, 1)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            for (String file : QUESTION_FILES) {
                try (InputStream input = getClass().getClassLoader().getResourceAsStream("db/" + file)) {
                    if (input == null) {
                        throw new IllegalStateException("缺少种子文件：" + file);
                    }
                    for (JsonNode node : JSON.readTree(input)) {
                        ps.setString(1, node.path("category").asText());
                        ps.setString(2, node.path("question").asText());
                        ps.setString(3, JSON.writeValueAsString(node.path("options")));
                        ps.setInt(4, node.path("answerIndex").asInt());
                        ps.setString(5, node.path("explanation").asText());
                        ps.setInt(6, node.path("difficulty").asInt());
                        ps.addBatch();
                    }
                }
            }
            ps.executeBatch();
        }
    }

    private void seedTasks(Connection connection) throws Exception {
        if (count(connection, "t_task") > 0) {
            return;
        }
        String sql = "INSERT INTO t_task (title, description, category, target_count, reward_points, status) "
                + "VALUES (?, ?, ?, ?, ?, 1)";
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("db/tasks.json");
             PreparedStatement ps = connection.prepareStatement(sql)) {
            if (input == null) {
                throw new IllegalStateException("缺少任务种子文件");
            }
            for (JsonNode node : JSON.readTree(input)) {
                ps.setString(1, node.path("title").asText());
                ps.setString(2, node.path("description").asText());
                ps.setString(3, node.path("category").isNull() ? null : node.path("category").asText());
                ps.setInt(4, node.path("targetCount").asInt());
                ps.setInt(5, node.path("rewardPoints").asInt());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private int count(Connection connection, String table) throws Exception {
        try (Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery("SELECT COUNT(*) FROM " + table)) {
            return result.next() ? result.getInt(1) : 0;
        }
    }

    private static String env(String key, String fallback) {
        String value = System.getenv(key);
        if (value == null || value.isBlank()) {
            value = System.getProperty(key);
        }
        return value == null || value.isBlank() ? fallback : value;
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
    }
}
