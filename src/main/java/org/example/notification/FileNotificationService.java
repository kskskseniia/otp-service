package org.example.notification;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;

public class FileNotificationService implements NotificationService {
    private static final String FILE_NAME = "otp_codes.txt";

    @Override
    public void sendCode(String destination, String code) {
        try (FileWriter writer = new FileWriter(FILE_NAME, true)) {
            writer.write("Time: " + LocalDateTime.now() + System.lineSeparator());
            writer.write("Destination: " + destination + System.lineSeparator());
            writer.write("OTP code: " + code + System.lineSeparator());
            writer.write("------------------------" + System.lineSeparator());
        } catch (IOException e) {
            throw new RuntimeException("Failed to save OTP code to file", e);
        }
    }
}