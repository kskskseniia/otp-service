package org.example;

import com.sun.net.httpserver.HttpServer;
import org.example.api.AdminHandler;
import org.example.api.AuthHandler;
import org.example.api.OtpHandler;
import org.example.config.AppConfig;
import org.example.service.ExpiredOtpScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws Exception {
        int port = AppConfig.getInt("server.port");

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        AuthHandler authHandler = new AuthHandler();
        AdminHandler adminHandler = new AdminHandler();
        OtpHandler otpHandler = new OtpHandler();

        server.createContext("/api/register", authHandler);
        server.createContext("/api/login", authHandler);

        server.createContext("/api/admin/config", adminHandler);
        server.createContext("/api/admin/users", adminHandler);

        server.createContext("/api/otp/generate", otpHandler);
        server.createContext("/api/otp/validate", otpHandler);

        server.setExecutor(null);
        server.start();

        ExpiredOtpScheduler expiredOtpScheduler = new ExpiredOtpScheduler();
        expiredOtpScheduler.start();

        logger.info("OTP Service started on port {}", port);
    }
}