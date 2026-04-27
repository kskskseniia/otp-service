package org.example.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConfig {
    private DatabaseConfig() {
    }

    public static Connection getConnection() throws SQLException {
        String url = AppConfig.get("db.url");
        String username = AppConfig.get("db.username");
        String password = AppConfig.get("db.password");

        return DriverManager.getConnection(url, username, password);
    }
}