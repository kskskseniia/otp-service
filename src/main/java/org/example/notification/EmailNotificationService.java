package org.example.notification;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.Properties;

public class EmailNotificationService implements NotificationService {
    private String username;
    private String password;
    private String fromEmail;
    private Session session;

    private Properties loadConfig() {
        try {
            Properties props = new Properties();
            props.load(EmailNotificationService.class.getClassLoader()
                    .getResourceAsStream("email.properties"));
            return props;
        } catch (Exception e) {
            throw new RuntimeException("Failed to load email configuration", e);
        }
    }

    public EmailNotificationService() {
        Properties config = loadConfig();

        this.username = config.getProperty("email.username").trim();
        this.password = config.getProperty("email.password").trim();
        this.fromEmail = config.getProperty("email.from").trim();

        this.session = Session.getInstance(config, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });
    }

    @Override
    public void sendCode(String toEmail, String code) {
        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromEmail));
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(toEmail));
            message.setSubject("Your OTP Code");
            message.setText("Your verification code is: " + code);

            Transport.send(message);
        } catch (MessagingException e) {
            //e.printStackTrace();
            throw new RuntimeException("Failed to send email: " + e.getMessage(), e);
        }
    }
}