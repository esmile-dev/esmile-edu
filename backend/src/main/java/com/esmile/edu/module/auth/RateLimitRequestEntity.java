package com.esmile.edu.module.auth;

import com.esmile.edu.common.BaseEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity for tracking rate limit requests using sliding window approach.
 */
@Entity
@Table(name = "rate_limit_requests")
public class RateLimitRequestEntity extends BaseEntity {

    public enum IdentifierType {
        EMAIL, IP
    }

    @Column(nullable = false)
    private String identifier;

    @Enumerated(EnumType.STRING)
    @Column(name = "identifier_type", nullable = false)
    private IdentifierType identifierType;

    @Column(name = "request_count", nullable = false)
    private Integer requestCount;

    @Column(name = "window_start", nullable = false)
    private LocalDateTime windowStart;

    @Column(name = "window_end", nullable = false)
    private LocalDateTime windowEnd;

    // Constructors
    public RateLimitRequestEntity() {}

    public RateLimitRequestEntity(String identifier, IdentifierType identifierType,
                                  LocalDateTime windowStart, LocalDateTime windowEnd) {
        this.identifier = identifier;
        this.identifierType = identifierType;
        this.requestCount = 1;
        this.windowStart = windowStart;
        this.windowEnd = windowEnd;
    }

    // Business methods
    public boolean isWindowActive() {
        LocalDateTime now = LocalDateTime.now();
        return !now.isBefore(windowStart) && !now.isAfter(windowEnd);
    }

    // Getters
    public String getIdentifier() { return identifier; }
    public IdentifierType getIdentifierType() { return identifierType; }
    public Integer getRequestCount() { return requestCount; }
    public LocalDateTime getWindowStart() { return windowStart; }
    public LocalDateTime getWindowEnd() { return windowEnd; }

    // Setters
    public void setIdentifier(String identifier) { this.identifier = identifier; }
    public void setIdentifierType(IdentifierType identifierType) { this.identifierType = identifierType; }
    public void setRequestCount(Integer requestCount) { this.requestCount = requestCount; }
    public void setWindowStart(LocalDateTime windowStart) { this.windowStart = windowStart; }
    public void setWindowEnd(LocalDateTime windowEnd) { this.windowEnd = windowEnd; }
}
