package com.esmile.edu.module.auth;

import com.esmile.edu.common.BaseEntity;
import com.esmile.edu.module.user.Role;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity for storing verification codes.
 * Codes are 6 digits, expire after 5 minutes, and are single-use.
 */
@Entity
@Table(name = "verification_codes")
public class VerificationCodeEntity extends BaseEntity {

    @Column(nullable = false)
    private String email;

    @Column(nullable = false, length = 6)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "used_at")
    private LocalDateTime usedAt;

    // Constructors
    public VerificationCodeEntity() {}

    public VerificationCodeEntity(String email, String code, Role role, LocalDateTime expiresAt) {
        this.email = email;
        this.code = code;
        this.role = role;
        this.expiresAt = expiresAt;
        this.usedAt = null;
    }

    // Business methods
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean isUsed() {
        return usedAt != null;
    }

    public boolean isValid() {
        return !isUsed() && !isExpired();
    }

    public void markAsUsed() {
        this.usedAt = LocalDateTime.now();
    }

    // Getters
    public String getEmail() { return email; }
    public String getCode() { return code; }
    public Role getRole() { return role; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public LocalDateTime getUsedAt() { return usedAt; }
}
