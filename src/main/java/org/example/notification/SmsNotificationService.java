package org.example.notification;

import org.jsmpp.bean.Alphabet;
import org.jsmpp.bean.BindType;
import org.jsmpp.bean.ESMClass;
import org.jsmpp.bean.GeneralDataCoding;
import org.jsmpp.bean.NumberingPlanIndicator;
import org.jsmpp.bean.RegisteredDelivery;
import org.jsmpp.bean.SMSCDeliveryReceipt;
import org.jsmpp.bean.TypeOfNumber;
import org.jsmpp.session.BindParameter;
import org.jsmpp.session.SMPPSession;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class SmsNotificationService implements NotificationService {
    private static final String CONFIG_FILE = "sms.properties";

    private final String host;
    private final int port;
    private final String systemId;
    private final String password;
    private final String systemType;
    private final String sourceAddress;

    public SmsNotificationService() {
        Properties config = loadConfig();

        this.host = config.getProperty("smpp.host").trim();
        this.port = Integer.parseInt(config.getProperty("smpp.port").trim());
        this.systemId = config.getProperty("smpp.system_id").trim();
        this.password = config.getProperty("smpp.password").trim();
        this.systemType = config.getProperty("smpp.system_type").trim();
        this.sourceAddress = config.getProperty("smpp.source_addr").trim();
    }

    @Override
    public void sendCode(String destination, String code) {
        SMPPSession session = new SMPPSession();

        try {
            BindParameter bindParameter = new BindParameter(
                    BindType.BIND_TX,
                    systemId,
                    password,
                    systemType,
                    TypeOfNumber.UNKNOWN,
                    NumberingPlanIndicator.UNKNOWN,
                    sourceAddress
            );

            session.connectAndBind(host, port, bindParameter);

            session.submitShortMessage(
                    systemType,
                    TypeOfNumber.UNKNOWN,
                    NumberingPlanIndicator.UNKNOWN,
                    sourceAddress,
                    TypeOfNumber.UNKNOWN,
                    NumberingPlanIndicator.UNKNOWN,
                    destination,
                    new ESMClass(),
                    (byte) 0,
                    (byte) 1,
                    null,
                    null,
                    new RegisteredDelivery(SMSCDeliveryReceipt.DEFAULT),
                    (byte) 0,
                    new GeneralDataCoding(Alphabet.ALPHA_DEFAULT),
                    (byte) 0,
                    ("Your code: " + code).getBytes(StandardCharsets.UTF_8)
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to send SMS via SMPP: " + e.getMessage(), e);
        } finally {
            try {
                session.unbindAndClose();
            } catch (Exception ignored) {
            }
        }
    }

    private Properties loadConfig() {
        try {
            Properties props = new Properties();
            try (InputStream inputStream = SmsNotificationService.class
                    .getClassLoader()
                    .getResourceAsStream(CONFIG_FILE)) {

                if (inputStream == null) {
                    throw new RuntimeException("SMS configuration file not found: " + CONFIG_FILE);
                }

                props.load(inputStream);
            }
            return props;
        } catch (Exception e) {
            throw new RuntimeException("Failed to load SMS configuration", e);
        }
    }
}