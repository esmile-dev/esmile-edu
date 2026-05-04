package com.esmile.edu.common.auth;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory verification code storage for MVP stage.
 * Stores codes with email as key and 5-minute expiry.
 */
@Service
public class VerificationCodeService {

    private static final int CODE_LENGTH = 6;
    private static final long EXPIRY_SECONDS = 300; // 5 minutes

    private final Map<String, CodeEntry> codeStore = new ConcurrentHashMap<>();
    private final SecureRandom random = new SecureRandom();

    private record CodeEntry(String code, long expiresAt) {}

    /**
     * Generates and stores a 6-digit verification code for the given email.
     * Any previously stored code for this email is replaced.
     */
    public String generateCode(String email) {
        String code = String.format("%06d", random.nextInt(1000000));
        codeStore.put(email, new CodeEntry(code, Instant.now().getEpochSecond() + EXPIRY_SECONDS));
        return code;
    }

    /**
     * Validates the provided code against the stored code for the email.
     * Removes the code after successful verification.
     * @return true if code is valid and not expired, false otherwise
     */
    public boolean verifyCode(String email, String code) {
        CodeEntry entry = codeStore.get(email);
        if (entry == null) {
            return false;
        }
        if (Instant.now().getEpochSecond() > entry.expiresAt()) {
            codeStore.remove(email);
            return false;
        }
        if (!entry.code().equals(code)) {
            return false;
        }
        codeStore.remove(email);
        return true;
    }

    /**
     * Gets the stored code for testing purposes (MVP only).
     * Should be removed or disabled in production.
     */
    public String getStoredCode(String email) {
        CodeEntry entry = codeStore.get(email);
        if (entry == null) {
            return null;
        }
        if (Instant.now().getEpochSecond() > entry.expiresAt()) {
            codeStore.remove(email);
            return null;
        }
        return entry.code();
    }
}
