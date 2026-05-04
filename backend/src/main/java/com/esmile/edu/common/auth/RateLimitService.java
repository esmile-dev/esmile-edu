package com.esmile.edu.common.auth;

import com.esmile.edu.common.exception.user.VerificationCodeRateLimitException;
import com.esmile.edu.module.auth.RateLimitRequestEntity;
import com.esmile.edu.module.auth.RateLimitRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Service for rate limiting verification code requests.
 * Uses a sliding window approach with database persistence.
 */
@Service
public class RateLimitService {

    private static final Logger log = LoggerFactory.getLogger(RateLimitService.class);

    // Email rate limit: 5 requests per minute
    private static final int EMAIL_RATE_LIMIT = 5;
    private static final int EMAIL_WINDOW_MINUTES = 1;

    // IP rate limit: 10 requests per hour
    private static final int IP_RATE_LIMIT = 10;
    private static final int IP_WINDOW_MINUTES = 60;

    private final RateLimitRepository rateLimitRepository;

    public RateLimitService(RateLimitRepository rateLimitRepository) {
        this.rateLimitRepository = rateLimitRepository;
    }

    /**
     * Checks if the email has exceeded the rate limit for verification codes.
     * Throws VerificationCodeRateLimitException if limit is exceeded.
     * @param email The email address to check
     */
    @Transactional
    public void checkEmailRateLimit(String email) {
        checkRateLimit(email, RateLimitRequestEntity.IdentifierType.EMAIL,
                EMAIL_RATE_LIMIT, EMAIL_WINDOW_MINUTES, "email");
    }

    /**
     * Checks if the IP has exceeded the rate limit for verification codes.
     * Throws VerificationCodeRateLimitException if limit is exceeded.
     * @param ip The IP address to check
     */
    @Transactional
    public void checkIpRateLimit(String ip) {
        checkRateLimit(ip, RateLimitRequestEntity.IdentifierType.IP,
                IP_RATE_LIMIT, IP_WINDOW_MINUTES, "IP");
    }

    private void checkRateLimit(String identifier, RateLimitRequestEntity.IdentifierType type,
                                int limit, int windowMinutes, String typeName) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime windowStart = now.minusMinutes(windowMinutes);
        LocalDateTime windowEnd = now.plusMinutes(windowMinutes);

        RateLimitRequestEntity existingWindow = rateLimitRepository
                .findActiveWindow(identifier, type, now)
                .orElse(null);

        if (existingWindow != null) {
            if (existingWindow.getRequestCount() >= limit) {
                log.warn("Rate limit exceeded for {} {} in {} minute window",
                        typeName, identifier, windowMinutes);
                throw new VerificationCodeRateLimitException();
            }
            // Atomic increment - avoids race condition
            rateLimitRepository.incrementCount(existingWindow.getId(), now);
            log.debug("Rate limit incremented for {} {}: {}/{}",
                    typeName, identifier, existingWindow.getRequestCount() + 1, limit);
        } else {
            // Create new window
            RateLimitRequestEntity newWindow = new RateLimitRequestEntity(
                    identifier, type, windowStart, windowEnd);
            rateLimitRepository.save(newWindow);
            log.debug("New rate limit window created for {} {}: 1/{}",
                    typeName, identifier, limit);
        }
    }
}
