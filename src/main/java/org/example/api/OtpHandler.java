package org.example.api;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.example.model.OtpCode;
import org.example.security.AuthContext;
import org.example.security.AuthMiddleware;
import org.example.service.OtpService;
import org.example.util.HttpUtils;

import java.io.IOException;
import java.util.Map;

public class OtpHandler implements HttpHandler {
    private final OtpService otpService = new OtpService();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();

        try {
            AuthContext authContext = AuthMiddleware.requireUser(exchange);

            if (path.equals("/api/otp/generate") && method.equalsIgnoreCase("POST")) {
                handleGenerate(exchange, authContext);
                return;
            }

            if (path.equals("/api/otp/validate") && method.equalsIgnoreCase("POST")) {
                handleValidate(exchange, authContext);
                return;
            }

            HttpUtils.sendError(exchange, 404, "Endpoint not found");
        } catch (RuntimeException e) {
            HttpUtils.sendError(exchange, 400, e.getMessage());
        } catch (Exception e) {
            HttpUtils.sendError(exchange, 500, "Internal server error");
        }
    }

    private void handleGenerate(HttpExchange exchange, AuthContext authContext) throws IOException {
        GenerateOtpRequest request = HttpUtils.readRequestBody(exchange, GenerateOtpRequest.class);

        OtpCode otpCode = otpService.generateCode(
                authContext.getUserId(),
                request.operationId(),
                request.channel(),
                request.destination()
        );

        HttpUtils.sendJson(exchange, 201, Map.of(
                "message", "OTP code generated successfully",
                "operationId", otpCode.getOperationId(),
                "status", otpCode.getStatus().name(),
                "expiresAt", otpCode.getExpiresAt().toString()
        ));
    }

    private void handleValidate(HttpExchange exchange, AuthContext authContext) throws IOException {
        ValidateOtpRequest request = HttpUtils.readRequestBody(exchange, ValidateOtpRequest.class);

        otpService.validateCode(
                authContext.getUserId(),
                request.operationId(),
                request.code()
        );

        HttpUtils.sendJson(exchange, 200, Map.of(
                "message", "OTP code validated successfully"
        ));
    }

    public record GenerateOtpRequest(String operationId, String channel, String destination) {
    }

    public record ValidateOtpRequest(String operationId, String code) {
    }
}