package org.example;

import org.example.config.DatabaseConfig;

import java.sql.Connection;

public class Main {
    public static void main(String[] args) {
        System.out.println("OTP Service started");

        try (Connection connection = DatabaseConfig.getConnection()) {
            System.out.println("Database connection successful");
        } catch (Exception e) {
            System.err.println("Database connection failed");
            e.printStackTrace();
        }
    }
}