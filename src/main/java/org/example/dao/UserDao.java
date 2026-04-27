package org.example.dao;

import org.example.config.DatabaseConfig;
import org.example.model.Role;
import org.example.model.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserDao {

    public User create(User user) {
        String sql = """
                INSERT INTO users (username, password_hash, role)
                VALUES (?, ?, ?)
                RETURNING id, username, password_hash, role, created_at
                """;

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, user.getUsername());
            statement.setString(2, user.getPasswordHash());
            statement.setString(3, user.getRole().name());

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapRow(resultSet);
                }
            }

            throw new RuntimeException("Failed to create user");

        } catch (SQLException e) {
            throw new RuntimeException("Database error while creating user", e);
        }
    }

    public Optional<User> findByUsername(String username) {
        String sql = """
                SELECT id, username, password_hash, role, created_at
                FROM users
                WHERE username = ?
                """;

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, username);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapRow(resultSet));
                }
            }

            return Optional.empty();

        } catch (SQLException e) {
            throw new RuntimeException("Database error while finding user by username", e);
        }
    }

    public boolean adminExists() {
        String sql = """
                SELECT COUNT(*)
                FROM users
                WHERE role = 'ADMIN'
                """;

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            resultSet.next();
            return resultSet.getLong(1) > 0;

        } catch (SQLException e) {
            throw new RuntimeException("Database error while checking admin existence", e);
        }
    }

    public List<User> findAllNonAdmins() {
        String sql = """
                SELECT id, username, password_hash, role, created_at
                FROM users
                WHERE role <> 'ADMIN'
                ORDER BY id
                """;

        List<User> users = new ArrayList<>();

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                users.add(mapRow(resultSet));
            }

            return users;

        } catch (SQLException e) {
            throw new RuntimeException("Database error while finding non-admin users", e);
        }
    }

    public boolean deleteById(Long id) {
        String sql = """
                DELETE FROM users
                WHERE id = ? AND role <> 'ADMIN'
                """;

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, id);
            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("Database error while deleting user", e);
        }
    }

    private User mapRow(ResultSet resultSet) throws SQLException {
        return new User(
                resultSet.getLong("id"),
                resultSet.getString("username"),
                resultSet.getString("password_hash"),
                Role.valueOf(resultSet.getString("role")),
                resultSet.getTimestamp("created_at").toLocalDateTime()
        );
    }
}