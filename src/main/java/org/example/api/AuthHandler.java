package org.example.api;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.example.model.Role;
import org.example.model.User;
import org.example.service.AuthService;
import org.example.util.HttpUtils;
import org.example.security.JwtService;

import java.io.IOException;
import java.util.Map;

public class AuthHandler implements HttpHandler {
    private final AuthService authService = new AuthService();
    private final JwtService jwtService = new JwtService();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();

        try {
            if (path.equals("/api/register") && method.equalsIgnoreCase("POST")) {
                handleRegister(exchange);
                return;
            }

            if (path.equals("/api/login") && method.equalsIgnoreCase("POST")) {
                handleLogin(exchange);
                return;
            }

            HttpUtils.sendError(exchange, 404, "Endpoint not found");
        } catch (RuntimeException e) {
            HttpUtils.sendError(exchange, 400, e.getMessage());
        } catch (Exception e) {
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