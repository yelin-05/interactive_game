package com.example.moshi;

import org.h2.tools.RunScript;
import org.junit.jupiter.api.BeforeAll;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Properties;

public abstract class TestDatabase {

    @BeforeAll
    public static void configureDatabase() throws Exception {
        Properties properties = new Properties();
        properties.setProperty("MOSHI_DB_URL", "jdbc:h2:mem:moshi;MODE=MySQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE");
        properties.setProperty("MOSHI_DB_USER", "sa");
        properties.setProperty("MOSHI_DB_PASSWORD", "sa");
        System.setProperty("MOSHI_DB_URL", properties.getProperty("MOSHI_DB_URL"));
        System.setProperty("MOSHI_DB_USER", "sa");
        System.setProperty("MOSHI_DB_PASSWORD", "sa");
        try (Connection connection = DriverManager.getConnection(
                properties.getProperty("MOSHI_DB_URL"), "sa", "sa");
             InputStream resource = TestDatabase.class.getClassLoader().getResourceAsStream("schema-h2.sql")) {
            RunScript.execute(connection, new InputStreamReader(resource, StandardCharsets.UTF_8));
        }
    }

    protected static void clearData() throws Exception {
        try (Connection connection = DriverManager.getConnection(
                System.getProperty("MOSHI_DB_URL"), "sa", "sa");
             Statement statement = connection.createStatement()) {
            statement.execute("DELETE FROM t_user_task");
            statement.execute("DELETE FROM t_score");
            statement.execute("DELETE FROM t_task");
            statement.execute("DELETE FROM t_question");
            statement.execute("DELETE FROM t_asset");
            statement.execute("DELETE FROM t_user");
        }
    }
}
