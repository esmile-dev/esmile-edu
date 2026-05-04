package com.esmile.edu.common.auth;

import com.esmile.edu.module.auth.VerificationCodeEntity;
import com.esmile.edu.module.auth.VerificationCodeRepository;
import com.esmile.edu.module.user.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

/**
 * Service for generating and verifying verification codes.
 * Uses database persistence instead of in-memory storage.
 */
@Service
public class VerificationCodeService {

    private static final Logger log = LoggerFactory.getLogger(VerificationCodeService.class);

    private static final int CODE_LENGTH = 6;
    private static final long EXPIRY_MINUTES = 5;

    private final VerificationCodeRepository verificationCodeRepository;
    private final SecureRandom random = new SecureRandom();

    public VerificationCodeService(VerificationCodeRepository verificationCodeRepository) {
        this.verificationCodeRepository = verificationCodeRepository;
    }

    /**
     * Generates and stores a 6-digit verification code for the given email.
     * Any previously stored valid code for this email is replaced.
     */
    @Transactional
    public String generateCode(String email, Role role) {
        String code = String.format("%06d", random.nextInt(1000000));
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(EXPIRY_MINUTES);

        VerificationCodeEntity entity = new VerificationCodeEntity(email, code, role, expiresAt);
        verificationCodeRepository.save(entity);

        log.info("Generated verification code for {} ({}), expires at {}", email, role, expiresAt);
        return code;
    }

    /**
     * Validates the provided code against the stored code for the email.
     * Marks the code as used after successful verification.
     * @return true if code is valid and not expired, false otherwise
     */
    @Transactional
    public boolean verifyCode(String email, String code) {
        LocalDateTime now = LocalDateTime.now();

        VerificationCodeEntity entity = verificationCodeRepository
                .findValidByEmailAndCode(email, code, now)
                .orElse(null);

        if (entity == null) {
            log.debug("Verification code not found or invalid for email: {}", email);
            return false;
        }

        // Mark as used
        verificationCodeRepository.markAsUsed(entity.getId(), LocalDateTime.now());
        log.info("Verification code used successfully for email: {}", email);
        return true;
    }
}
