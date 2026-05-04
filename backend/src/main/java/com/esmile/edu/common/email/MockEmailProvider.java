package com.esmile.edu.common.email;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Mock email provider for development environment.
 * Logs emails to console instead of sending them.
 * Stores last sent code per email for testing purposes.
 */
@Component
@Profile("dev")
public class MockEmailProvider implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(MockEmailProvider.class);

    // Stores the last sent code per email for testing
    private final Map<String, String> sentCodes = new ConcurrentHashMap<>();

    @Override
    public boolean sendVerificationCode(String to, String code) {
        sentCodes.put(to, code);
        log.info("========================================");
        log.info("MOCK EMAIL - Verification Code");
        log.info("To: {}", to);
        log.info("Code: {}", code);
        log.info("========================================");
        return true;
    }

    @Override
    public String getProviderName() {
        return "mock";
    }

    /**
     * Gets the last sent code for the given email (for testing).
     */
    public String getLastSentCode(String email) {
        return sentCodes.get(email);
    }

    /**
     * Clears all stored codes (for test cleanup).
     */
    public void clearCodes() {
        sentCodes.clear();
    }
}
