package org.example.api;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.example.model.Role;
import org.example.model.User;
import org.example.service.AuthService;
import org.example.util.HttpUtils;
import org.example.security.JwtService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

public class AuthHandler implements HttpHandler {
    private final AuthService authService = new AuthService();
    private final JwtService jwtService = new JwtService();
    private static final Logger logger = LoggerFactory.getLogger(AuthHandler.class);

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();

        logger.info("Incoming request: {} {}", method, path);

        try {
            if (path.equals("/api/register") && method.equalsIgnoreCase("POST")) {
                handleRegister(exchange);
                logger.info("Request completed: {} {} status={}", method, path, 201);
                return;
            }

            if (path.equals("/api/login") && method.equalsIgnoreCase("POST")) {
                handleLogin(exchange);
                logger.info("Request completed: {} {} status={}", method, path, 200);
                return;
            }

            logger.warn("Endpoint not found: {} {}", method, path);
            HttpUtils.sendError(exchange, 404, "Endpoint not found");
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid request data for {} {}: {}", method, path, e.getMessage());
            HttpUtils.sendError(exchange, 400, "Invalid request data");
        } catch (RuntimeException e) {
            logger.warn("Request failed: {} {} error={}", method, path, e.getMessage());
            HttpUtils.sendError(exchange, 400, e.getMessage());
        } catch (Exception e) {
            logger.error("Internal server error: {} {}", method, path, e);
            HttpUtils.sendError(exchange, 500, "Internal server error");
        }
    }
    private void handleRegister(HttpExchange exchange) throws IOException {
        RegisterRequest request = HttpUtils.readRequestBody(exchange, RegisterRequest.class);

        Role role = Role.valueOf(request.role().toUpperCase());

        User user = authService.register(
                request.username(),
                request.password(),
                role
        );

        logger.info("User registered: username={} role={}", user.getUsername(), user.getRole());
        HttpUtils.sendJson(exchange, 201, new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getRole().name()
        ));
    }

    private void handleLogin(HttpExchange exchange) throws IOException {
        LoginRequest request = HttpUtils.readRequestBody(exchange, LoginRequest.class);

        User user = authService.login(
                request.username(),
                request.password()
        );

        String token = jwtService.generateToken(user);

        logger.info("User logged in: username={} role={}", user.getUsername(), user.getRole());
        HttpUtils.sendJson(exchange, 200, Map.of(
                "token", token,
                "userId", user.getId(),
                "username", user.getUsername(),
                "role", user.getRole().name()
        ));
    }

    public record RegisterRequest(String username, String password, String role) {
    }

    public record LoginRequest(String username, String password) {
    }

    public record UserResponse(Long id, String username, String role) {
    }
}