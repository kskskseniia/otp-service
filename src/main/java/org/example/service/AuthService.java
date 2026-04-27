package org.example.service;

import org.example.dao.UserDao;
import org.example.model.Role;
import org.example.model.User;
import org.example.security.PasswordHasher;

import java.util.Optional;

public class AuthService {
    private final UserDao userDao;

    public AuthService() {
        this.userDao = new UserDao();
    }

    public User register(String username, String password, Role role) {
        validateRegisterData(username, password, role);

        Optional<User> existingUser = userDao.findByUsername(username);
        if (existingUser.isPresent()) {
            throw new RuntimeException("User with this username already exists");
        }

        if (role == Role.ADMIN && userDao.adminExists()) {
            throw new RuntimeException("Admin already exists");
        }

        String passwordHash = PasswordHasher.hashPassword(password);

        User user = new User(username, passwordHash, role);
        return userDao.create(user);
    }

    public User login(String username, String password) {
        if (username == null || username.isBlank()) {
            throw new RuntimeException("Username is required");
        }

        if (password == null || password.isBlank()) {
            throw new RuntimeException("Password is required");
        }

        User user = userDao.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Invalid username or password"));

        boolean passwordMatches = PasswordHasher.checkPassword(password, user.getPasswordHash());

        if (!passwordMatches) {
            throw new RuntimeException("Invalid username or password");
        }

        return user;
    }

    private void validateRegisterData(String username, String password, Role role) {
        if (username == null || username.isBlank()) {
            throw new RuntimeException("Username is required");
        }

        if (password == null || password.isBlank()) {
            throw new RuntimeException("Password is required");
        }

        if (password.length() < 4) {
            throw new RuntimeException("Password must contain at least 4 characters");
        }

        if (role == null) {
            throw new RuntimeException("Role is required");
        }
    }
}