package org.example.api;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.example.model.OtpConfig;
import org.example.model.User;
import org.example.security.AuthMiddleware;
import org.example.service.AdminService;
import org.example.util.HttpUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class AdminHandler implements HttpHandler {
    private final AdminService adminService = new AdminService();

    private static final Logger logger = LoggerFactory.getLogger(AdminHandler.class);

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();

        logger.info("Incoming admin request: {} {}", method, path);

        try {
            AuthMiddleware.requireAdmin(exchange);

            if (path.equals("/api/admin/config") && method.equalsIgnoreCase("GET")) {
                handleGetConfig(exchange);
                logger.info("Admin request completed: {} {} status={}", method, path, 200);
                return;
            }

            if (path.equals("/api/admin/config") && method.equalsIgnoreCase("PUT")) {
                handleUpdateConfig(exchange);
                logger.info("Admin request completed: {} {} status={}", method, path, 200);
                return;
            }

            if (path.equals("/api/admin/users") && method.equalsIgnoreCase("GET")) {
                handleGetUsers(exchange);
                logger.info("Admin request completed: {} {} status={}", method, path, 200);
                return;
            }

            if (path.startsWith("/api/admin/users/") && method.equalsIgnoreCase("DELETE")) {
                handleDeleteUser(exchange, path);
                logger.info("Admin request completed: {} {} status={}", method, path, 200);
                return;
            }

            logger.warn("Admin endpoint not found: {} {}", method, path);
            HttpUtils.sendError(exchange, 404, "Endpoint not found");
        } catch (RuntimeException e) {
            logger.warn("Admin request failed: {} {} error={}", method, path, e.getMessage());
            HttpUtils.sendError(exchange, 400, e.getMessage());
        } catch (Exception e) {
            logger.error("Internal server error in admin request: {} {}", method, path, e);
            HttpUtils.sendError(exchange, 500, "Internal server error");
        }
    }
    private void handleGetConfig(HttpExchange exchange) throws IOException {
        OtpConfig config = adminService.getOtpConfig();

        HttpUtils.sendJson(exchange, 200, new OtpConfigResponse(
                config.getCodeLength(),
                config.getTtlSeconds()
        ));
    }

    private void handleUpdateConfig(HttpExchange exchange) throws IOException {
        UpdateOtpConfigRequest request = HttpUtils.readRequestBody(exchange, UpdateOtpConfigRequest.class);

        OtpConfig config = adminService.updateOtpConfig(
                request.codeLength(),
                request.ttlSeconds()
        );

        logger.info("OTP config updated: codeLength={} ttlSeconds={}",
                config.getCodeLength(), config.getTtlSeconds());

        HttpUtils.sendJson(exchange, 200, new OtpConfigResponse(
                config.getCodeLength(),
                config.getTtlSeconds()
        ));
    }

    private void handleGetUsers(HttpExchange exchange) throws IOException {
        List<User> users = adminService.getAllNonAdminUsers();

        List<UserResponse> response = users.stream()
                .map(user -> new UserResponse(
                        user.getId(),
                        user.getUsername(),
                        user.getRole().name(),
                        user.getCreatedAt().toString()
                ))
                .toList();

        HttpUtils.sendJson(exchange, 200, response);
    }

    private void handleDeleteUser(HttpExchange exchange, String path) throws IOException {
        String idPart = path.substring("/api/admin/users/".length());
        Long userId = Long.parseLong(idPart);

        adminService.deleteUser(userId);

        logger.info("User deleted by admin: userId={}", userId);
        HttpUtils.sendJson(exchange, 200, Map.of(
                "message", "User deleted successfully"
        ));
    }

    public record UpdateOtpConfigRequest(int codeLength, int ttlSeconds) {
    }

    public record OtpConfigResponse(int codeLength, int ttlSeconds) {
    }

    public record UserResponse(Long id, String username, String role, String createdAt) {
    }
}