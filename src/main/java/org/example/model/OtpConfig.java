package org.example.model;

/**
 * Модель конфигурации OTP-кодов.
 * Соответствует таблице otp_config.
 */
public class OtpConfig {
    private Long id;
    private int codeLength;
    private int ttlSeconds;

    public OtpConfig() {
    }

    public OtpConfig(Long id, int codeLength, int ttlSeconds) {
        this.id = id;
        this.codeLength = codeLength;
        this.ttlSeconds = ttlSeconds;
    }

    public Long getId() {
        return id;
    }

    public int getCodeLength() {
        return codeLength;
    }

    public int getTtlSeconds() {
        return ttlSeconds;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setCodeLength(int codeLength) {
        this.codeLength = codeLength;
    }

    public void setTtlSeconds(int ttlSeconds) {
        this.ttlSeconds = ttlSeconds;
    }

    @Override
    public String toString() {
        return "OtpConfig{" +
                "id=" + id +
                ", codeLength=" + codeLength +
                ", ttlSeconds=" + ttlSeconds +
                '}';
    }
}