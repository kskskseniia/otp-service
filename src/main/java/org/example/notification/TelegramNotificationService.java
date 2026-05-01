package org.example.notification;

import java.io.InputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class TelegramNotificationService implements NotificationService {
    private static final String CONFIG_FILE = "telegram.properties";

    private final String botToken;
    private final String chatId;
    private final String telegramApiUrl;
    private final HttpClient httpClient;

    public TelegramNotificationService() {
        Properties config = loadConfig();

        this.botToken = config.getProperty("telegram.bot.token").trim();
        this.chatId = config.getProperty("telegram.chat.id").trim();
        this.telegramApiUrl = config.getProperty("telegram.api.url").trim();
        this.httpClient = HttpClient.newHttpClient();
    }

    @Override
    public void sendCode(String destination, String code) {
        String message = String.format("%s, your confirmation code is: %s", destination, code);

        String url = String.format(
                "%s%s/sendMessage?chat_id=%s&text=%s",
                telegramApiUrl,
                botToken,
                urlEncode(chatId),
                urlEncode(message)
        );

        sendTelegramRequest(url);
    }

    private void sendTelegramRequest(String url) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        try {
            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            int statusCode = response.statusCode();

            if (statusCode != 200) {
                throw new RuntimeException("Telegram API error. Status code: "
                        + statusCode + ", body: " + response.body());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Telegram request was interrupted", e);
        } catch (IOException e) {
            throw new RuntimeException("Failed to send Telegram message", e);
        }
    }

    private Properties loadConfig() {
        try (InputStream inputStream = TelegramNotificationService.class
                .getClassLoader()
                .getResourceAsStream(CONFIG_FILE)) {

            if (inputStream == null) {
                throw new RuntimeException("Telegram configuration file not found: " + CONFIG_FILE);
            }

            Properties props = new Properties();
            props.load(inputStream);
            return props;
        } catch (Exception e) {
            throw new RuntimeException("Failed to load Telegram configuration", e);
        }
    }

    private static String urlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}