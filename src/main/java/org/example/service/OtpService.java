package org.example.service;

import org.example.dao.OtpCodeDao;
import org.example.dao.OtpConfigDao;
import org.example.model.OtpCode;
import org.example.model.OtpConfig;
import org.example.model.OtpStatus;
import org.example.notification.FileNotificationService;
import org.example.notification.NotificationService;
import org.example.notification.EmailNotificationService;
import org.example.notification.TelegramNotificationService;

import java.security.SecureRandom;
import java.time.LocalDateTime;

public class OtpService {
    private final OtpCodeDao otpCodeDao;
    private final OtpConfigDao otpConfigDao;
    private final SecureRandom secureRandom;

    public OtpService() {
        this.otpCodeDao = new OtpCodeDao();
        this.otpConfigDao = new OtpConfigDao();
        this.secureRandom = new SecureRandom();
    }

    public OtpCode generateCode(Long userId, String operationId, String channel, String destination) {
        validateGenerateRequest(operationId, channel, destination);

        OtpConfig config = otpConfigDao.getConfig();

        String code = generateNumericCode(config.getCodeLength());
        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(config.getTtlSeconds());

        OtpCode otpCode = new OtpCode(
                userId,
                operationId,
                code,
                OtpStatus.ACTIVE,
                expiresAt
        );

        OtpCode savedCode = otpCodeDao.create(otpCode);

        NotificationService notificationService = resolveNotificationService(channel);
        notificationService.sendCode(destination, code);

        return savedCode;
    }

    public void validateCode(Long userId, String operationId, String code) {
        if (operationId == null || operationId.isBlank()) {
            throw new RuntimeException("Operation ID is required");
        }

        if (code == null || code.isBlank()) {
            throw new RuntimeException("OTP code is required");
        }

        OtpCode otpCode = otpCodeDao.findLatestByUserOperationAndCode(userId, operationId, code)
                .orElseThrow(() -> new RuntimeException("Invalid OTP code"));

        if (otpCode.getStatus() == OtpStatus.USED) {
            throw new RuntimeException("OTP code has already been used");
        }

        if (otpCode.getStatus() == OtpStatus.EXPIRED) {
            throw new RuntimeException("OTP code expired");
        }

        if (otpCode.getExpiresAt().isBefore(LocalDateTime.now())) {
            otpCodeDao.updateStatus(otpCode.getId(), OtpStatus.EXPIRED);
            throw new RuntimeException("OTP code expired");
        }

        otpCodeDao.updateStatus(otpCode.getId(), OtpStatus.USED);
    }

    private String generateNumericCode(int length) {
        StringBuilder code = new StringBuilder();

        for (int i = 0; i < length; i++) {
            int digit = secureRandom.nextInt(10);
            code.append(digit);
        }

        return code.toString();
    }

    private NotificationService resolveNotificationService(String channel) {
        if (channel == null) {
            throw new RuntimeException("Channel is required");
        }

        return switch (channel.toUpperCase()) {
            case "FILE" -> new FileNotificationService();
            case "EMAIL" -> new EmailNotificationService();
            case "TELEGRAM" -> new TelegramNotificationService();
            default -> throw new RuntimeException("Unsupported notification channel: " + channel);
        };
    }

    private void validateGenerateRequest(String operationId, String channel, String destination) {
        if (operationId == null || operationId.isBlank()) {
            throw new RuntimeException("Operation ID is required");
        }

        if (channel == null || channel.isBlank()) {
            throw new RuntimeException("Channel is required");
        }

        if (destination == null || destination.isBlank()) {
            throw new RuntimeException("Destination is required");
        }
    }
}