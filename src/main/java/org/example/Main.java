package org.example;

import com.sun.net.httpserver.HttpServer;
import org.example.api.AdminHandler;
import org.example.api.AuthHandler;
import org.example.config.AppConfig;

import java.net.InetSocketAddress;

public class Main {
    public static void main(String[] args) throws Exception {
        int port = AppConfig.getInt("server.port");

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        AuthHandler authHandler = new AuthHandler();
        AdminHandler adminHandler = new AdminHandler();

        server.createContext("/api/register", authHandler);
        server.createContext("/api/login", authHandler);

        server.createContext("/api/admin/config", adminHandler);
        server.createContext("/api/admin/users", adminHandler);

        server.setExecutor(null);
        server.start();

        System.out.println("OTP Service started on port " + port);
    }
}