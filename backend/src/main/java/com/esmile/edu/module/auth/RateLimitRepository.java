package com.esmile.edu.module.auth;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Repository for rate limit tracking.
 */
@Repository
public interface RateLimitRepository extends JpaRepository<RateLimitRequestEntity, Long> {

    /**
     * Finds an active rate limit window for the given identifier and type.
     * An active window is one where the current time is between windowStart and windowEnd.
     */
    @Query("SELECT r FROM RateLimitRequestEntity r WHERE r.identifier = :identifier " +
           "AND r.identifierType = :identifierType " +
           "AND r.windowStart <= :now AND r.windowEnd >= :now")
    Optional<RateLimitRequestEntity> findActiveWindow(
            @Param("identifier") String identifier,
            @Param("identifierType") RateLimitRequestEntity.IdentifierType identifierType,
            @Param("now") LocalDateTime now);

    /**
     * Atomically increments the request count for an active window.
     * Returns the new count, or empty if no active window exists.
     */
    @Modifying
    @Query("UPDATE RateLimitRequestEntity r SET r.requestCount = r.requestCount + 1 " +
           "WHERE r.id = :id AND r.windowEnd >= :now")
    void incrementCount(@Param("id") Long id, @Param("now") LocalDateTime now);

    /**
     * Deletes all expired rate limit records (cleanup).
     */
    @Modifying
    @Query("DELETE FROM RateLimitRequestEntity r WHERE r.windowEnd < :now")
    void deleteExpiredWindows(@Param("now") LocalDateTime now);
}
