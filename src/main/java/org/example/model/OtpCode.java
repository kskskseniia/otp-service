package org.example.model;

import java.time.LocalDateTime;

/**
 * Модель OTP-кода.
 * Соответствует таблице otp_codes.
 */
public class OtpCode {
    private Long id;
    private Long userId;
    private String operationId;
    private String code;
    private OtpStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;

    public OtpCode() {
    }

    public OtpCode(Long userId, String operationId, String code, OtpStatus status, LocalDateTime expiresAt) {
        this.userId = userId;
        this.operationId = operationId;
        this.code = code;
        this.status = status;
        this.expiresAt = expiresAt;
    }

    public OtpCode(Long id, Long userId, String operationId, String code,
                   OtpStatus status, LocalDateTime createdAt, LocalDateTime expiresAt) {
        this.id = id;
        this.userId = userId;
        this.operationId = operationId;
        this.code = code;
        this.status = status;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public String getOperationId() {
        return operationId;
    }

    public String getCode() {
        return code;
    }

    public OtpStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setOperationId(String operationId) {
        this.operationId = operationId;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setStatus(OtpStatus status) {
        this.status = status;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    @Override
    public String toString() {
        return "OtpCode{" +
                "id=" + id +
                ", userId=" + userId +
                ", operationId='" + operationId + '\'' +
                ", status=" + status +
                ", createdAt=" + createdAt +
                ", expiresAt=" + expiresAt +
                '}';
    }
}