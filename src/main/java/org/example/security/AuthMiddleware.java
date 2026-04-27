package org.example.security;

import com.sun.net.httpserver.HttpExchange;
import org.example.model.Role;

import java.util.List;

/**
 * Вспомогательный класс для проверки JWT-токена и роли пользователя.
 */
public class AuthMiddleware {
    private static final JwtService jwtService = new JwtService();

    private AuthMiddleware() {
    }

    public static AuthContext requireAuth(HttpExchange exchange) {
        String token = extractToken(exchange);
        return jwtService.validateToken(token);
    }

    public static AuthContext requireRole(HttpExchange exchange, Role requiredRole) {
        AuthContext authContext = requireAuth(exchange);

        if (authContext.getRole() != requiredRole) {
            throw new RuntimeException("Access denied");
        }

        return authContext;
    }

    public static AuthContext requireAdmin(HttpExchange exchange) {
        return requireRole(exchange, Role.ADMIN);
    }

    public static AuthContext requireUser(HttpExchange exchange) {
        return requireRole(exchange, Role.USER);
    }

    private static String extractToken(HttpExchange exchange) {
        List<String> headers = exchange.getRequestHeaders().get("Authorization");

        if (headers == null || headers.isEmpty()) {
            throw new RuntimeException("Authorization header is required");
        }

        String authorizationHeader = headers.get(0);

        if (!authorizationHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Invalid Authorization header format");
        }

        return authorizationHeader.substring("Bearer ".length());
    }
}