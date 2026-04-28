package org.example.dao;

import org.example.config.DatabaseConfig;
import org.example.model.OtpCode;
import org.example.model.OtpStatus;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.Optional;

public class OtpCodeDao {

    public OtpCode create(OtpCode otpCode) {
        String sql = """
                INSERT INTO otp_codes (user_id, operation_id, code, status, expires_at)
                VALUES (?, ?, ?, ?, ?)
                RETURNING id, user_id, operation_id, code, status, created_at, expires_at
                """;

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, otpCode.getUserId());
            statement.setString(2, otpCode.getOperationId());
            statement.setString(3, otpCode.getCode());
            statement.setString(4, otpCode.getStatus().name());
            statement.setTimestamp(5, Timestamp.valueOf(otpCode.getExpiresAt()));

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapRow(resultSet);
                }
            }

            throw new RuntimeException("Failed to create OTP code");

        } catch (SQLException e) {
            throw new RuntimeException("Database error while creating OTP code", e);
        }
    }

    public Optional<OtpCode> findActiveCode(Long userId, String operationId, String code) {
        String sql = """
                SELECT id, user_id, operation_id, code, status, created_at, expires_at
                FROM otp_codes
                WHERE user_id = ?
                  AND operation_id = ?
                  AND code = ?
                  AND status = 'ACTIVE'
                ORDER BY created_at DESC
                LIMIT 1
                """;

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, userId);
            statement.setString(2, operationId);
            statement.setString(3, code);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapRow(resultSet));
                }
            }

            return Optional.empty();

        } catch (SQLException e) {
            throw new RuntimeException("Database error while finding active OTP code", e);
        }
    }

    public void updateStatus(Long id, OtpStatus status) {
        String sql = """
                UPDATE otp_codes
                SET status = ?
                WHERE id = ?
                """;

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, status.name());
            statement.setLong(2, id);
            statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Database error while updating OTP status", e);
        }
    }

    public int expireOldActiveCodes() {
        String sql = """
                UPDATE otp_codes
                SET status = 'EXPIRED'
                WHERE status = 'ACTIVE'
                  AND expires_at < CURRENT_TIMESTAMP
                """;

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            return statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Database error while expiring OTP codes", e);
        }
    }

    private OtpCode mapRow(ResultSet resultSet) throws SQLException {
        LocalDateTime createdAt = resultSet.getTimestamp("created_at").toLocalDateTime();
        LocalDateTime expiresAt = resultSet.getTimestamp("expires_at").toLocalDateTime();

        return new OtpCode(
                resultSet.getLong("id"),
                resultSet.getLong("user_id"),
                resultSet.getString("operation_id"),
                resultSet.getString("code"),
                OtpStatus.valueOf(resultSet.getString("status")),
                createdAt,
                expiresAt
        );
    }

    public Optional<OtpCode> findLatestByUserOperationAndCode(Long userId, String operationId, String code) {
        String sql = """
            SELECT id, user_id, operation_id, code, status, created_at, expires_at
            FROM otp_codes
            WHERE user_id = ?
              AND operation_id = ?
              AND code = ?
            ORDER BY created_at DESC
            LIMIT 1
            """;

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, userId);
            statement.setString(2, operationId);
            statement.setString(3, code);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapRow(resultSet));
                }
            }

            return Optional.empty();

        } catch (SQLException e) {
            throw new RuntimeException("Database error while finding OTP code", e);
        }
    }
}