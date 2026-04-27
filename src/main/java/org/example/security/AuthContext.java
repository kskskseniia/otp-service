package org.example.security;

import org.example.model.Role;

/**
 * Данные пользователя, полученные из JWT-токена.
 */
public class AuthContext {
    private final Long userId;
    private final String username;
    private final Role role;

    public AuthContext(Long userId, String username, Role role) {
        this.userId = userId;
        this.username = username;
        this.role = role;
    }

    public Long getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public Role getRole() {
        return role;
    }

    public boolean isAdmin() {
        return role == Role.ADMIN;
    }

    public boolean isUser() {
        return role == Role.USER;
    }
}