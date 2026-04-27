package org.example.util;

import com.sun.net.httpserver.HttpExchange;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class HttpUtils {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private HttpUtils() {
    }

    public static <T> T readRequestBody(HttpExchange exchange, Class<T> clazz) throws IOException {
        try (InputStream inputStream = exchange.getRequestBody()) {
            return objectMapper.readValue(inputStream, clazz);
        }
    }

    public static void sendJson(HttpExchange exchange, int statusCode, Object response) throws IOException {
        byte[] responseBytes = objectMapper.writeValueAsBytes(response);

        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, responseBytes.length);

        try (OutputStream outputStream = exchange.getResponseBody()) {
            outputStream.write(responseBytes);
        }
    }

    public static void sendError(HttpExchange exchange, int statusCode, String message) throws IOException {
        sendJson(exchange, statusCode, new ErrorResponse(message));
    }

    public record ErrorResponse(String error) {
    }
}