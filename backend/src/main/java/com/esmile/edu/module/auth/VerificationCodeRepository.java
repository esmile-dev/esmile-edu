package com.esmile.edu.module.auth;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Repository for verification codes.
 */
@Repository
public interface VerificationCodeRepository extends JpaRepository<VerificationCodeEntity, Long> {

    /**
     * Finds the latest valid (not used, not expired) verification code for an email.
     */
    @Query("SELECT v FROM VerificationCodeEntity v WHERE v.email = :email " +
           "AND v.usedAt IS NULL AND v.expiresAt > :now " +
           "ORDER BY v.createdAt DESC LIMIT 1")
    Optional<VerificationCodeEntity> findLatestValidByEmail(
            @Param("email") String email,
            @Param("now") LocalDateTime now);

    /**
     * Finds a valid verification code by email and code.
     */
    @Query("SELECT v FROM VerificationCodeEntity v WHERE v.email = :email AND v.code = :code " +
           "AND v.usedAt IS NULL AND v.expiresAt > :now")
    Optional<VerificationCodeEntity> findValidByEmailAndCode(
            @Param("email") String email,
            @Param("code") String code,
            @Param("now") LocalDateTime now);

    /**
     * Marks a verification code as used by setting usedAt timestamp.
     */
    @Modifying
    @Query("UPDATE VerificationCodeEntity v SET v.usedAt = :usedAt WHERE v.id = :id")
    void markAsUsed(@Param("id") Long id, @Param("usedAt") LocalDateTime usedAt);

    /**
     * Deletes all expired verification codes (cleanup).
     */
    @Modifying
    @Query("DELETE FROM VerificationCodeEntity v WHERE v.expiresAt < :now")
    void deleteExpiredCodes(@Param("now") LocalDateTime now);
}
