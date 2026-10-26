package com.foodordering.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/** Opens connections to the MySQL database using environment configuration. */
public final class DatabaseConnection {
    private DatabaseConnection() {}

    public static Connection open() throws SQLException {
        String url = required("FOOD_DB_URL");
        String user = required("FOOD_DB_USER");
        String password = System.getenv("FOOD_DB_PASSWORD");
        if (password == null) password = "";
        return DriverManager.getConnection(url, user, password);
    }

    private static String required(String key) {
        String value = System.getenv(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Set the " + key + " environment variable before starting the app.");
        }
        return value;
    }
}
