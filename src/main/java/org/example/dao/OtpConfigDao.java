package org.example.dao;

import org.example.config.DatabaseConfig;
import org.example.model.OtpConfig;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class OtpConfigDao {

    public OtpConfig getConfig() {
        String sql = """
                SELECT id, code_length, ttl_seconds
                FROM otp_config
                WHERE id = 1
                """;

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            if (resultSet.next()) {
                return mapRow(resultSet);
            }

            throw new RuntimeException("OTP config not found");

        } catch (SQLException e) {
            throw new RuntimeException("Database error while getting OTP config", e);
        }
    }

    public OtpConfig updateConfig(int codeLength, int ttlSeconds) {
        String sql = """
                UPDATE otp_config
                SET code_length = ?, ttl_seconds = ?
                WHERE id = 1
                RETURNING id, code_length, ttl_seconds
                """;

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, codeLength);
            statement.setInt(2, ttlSeconds);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapRow(resultSet);
                }
            }

            throw new RuntimeException("Failed to update OTP config");

        } catch (SQLException e) {
            throw new RuntimeException("Database error while updating OTP config", e);
        }
    }

    private OtpConfig mapRow(ResultSet resultSet) throws SQLException {
        return new OtpConfig(
                resultSet.getLong("id"),
                resultSet.getInt("code_length"),
                resultSet.getInt("ttl_seconds")
        );
    }
}