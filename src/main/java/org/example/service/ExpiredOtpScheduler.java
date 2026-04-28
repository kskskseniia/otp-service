package org.example.service;

import org.example.config.AppConfig;
import org.example.dao.OtpCodeDao;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ExpiredOtpScheduler {
    private final OtpCodeDao otpCodeDao;
    private final ScheduledExecutorService scheduler;

    public ExpiredOtpScheduler() {
        this.otpCodeDao = new OtpCodeDao();
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
    }

    public void start() {
        int intervalSeconds = AppConfig.getInt("otp.expiration.check.interval.seconds");

        scheduler.scheduleAtFixedRate(
                this::expireOldCodes,
                intervalSeconds,
                intervalSeconds,
                TimeUnit.SECONDS
        );

        System.out.println("Expired OTP scheduler started. Interval: " + intervalSeconds + " seconds");
    }

    private void expireOldCodes() {
        try {
            int expiredCount = otpCodeDao.expireOldActiveCodes();

            if (expiredCount > 0) {
                System.out.println("Expired OTP codes: " + expiredCount);
            }
        } catch (Exception e) {
            System.err.println("Failed to expire OTP codes");
            e.printStackTrace();
        }
    }
}