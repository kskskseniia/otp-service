package org.example.service;

import org.example.config.AppConfig;
import org.example.dao.OtpCodeDao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class ExpiredOtpScheduler {
    private static final Logger logger = LoggerFactory.getLogger(ExpiredOtpScheduler.class);

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

        logger.info("Expired OTP scheduler started. Interval: {} seconds", intervalSeconds);
    }

    private void expireOldCodes() {
        try {
            int expiredCount = otpCodeDao.expireOldActiveCodes();

            if (expiredCount > 0) {
                logger.info("Expired OTP codes: {}", expiredCount);
            }
        } catch (Exception e) {
            logger.error("Failed to expire OTP codes", e);
        }
    }
}