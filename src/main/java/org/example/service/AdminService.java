package org.example.service;

import org.example.dao.OtpConfigDao;
import org.example.dao.UserDao;
import org.example.model.OtpConfig;
import org.example.model.User;

import java.util.List;

public class AdminService {
    private final UserDao userDao;
    private final OtpConfigDao otpConfigDao;

    public AdminService() {
        this.userDao = new UserDao();
        this.otpConfigDao = new OtpConfigDao();
    }

    public OtpConfig getOtpConfig() {
        return otpConfigDao.getConfig();
    }

    public OtpConfig updateOtpConfig(int codeLength, int ttlSeconds) {
        validateOtpConfig(codeLength, ttlSeconds);
        return otpConfigDao.updateConfig(codeLength, ttlSeconds);
    }

    public List<User> getAllNonAdminUsers() {
        return userDao.findAllNonAdmins();
    }

    public void deleteUser(Long userId) {
        boolean deleted = userDao.deleteById(userId);

        if (!deleted) {
            throw new RuntimeException("User not found or cannot delete admin");
        }
    }

    private void validateOtpConfig(int codeLength, int ttlSeconds) {
        if (codeLength < 4 || codeLength > 10) {
            throw new RuntimeException("OTP code length must be between 4 and 10");
        }

        if (ttlSeconds < 30 || ttlSeconds > 3600) {
            throw new RuntimeException("OTP TTL must be between 30 and 3600 seconds");
        }
    }
}