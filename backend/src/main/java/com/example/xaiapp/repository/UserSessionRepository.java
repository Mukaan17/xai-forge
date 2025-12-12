package com.example.xaiapp.repository;

import com.example.xaiapp.entity.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for UserSession entity operations.
 */
@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, Long> {

    /**
     * Find all sessions for a user.
     */
    @Query("SELECT s FROM UserSession s WHERE s.user.id = :userId ORDER BY s.createdAt DESC")
    List<UserSession> findByUserId(@Param("userId") Long userId);

    /**
     * Find all active sessions for a user.
     */
    @Query("SELECT s FROM UserSession s WHERE s.user.id = :userId AND s.isActive = true ORDER BY s.lastActiveAt DESC")
    List<UserSession> findByUserIdAndIsActiveTrue(@Param("userId") Long userId);

    /**
     * Find session by token.
     */
    Optional<UserSession> findBySessionToken(String sessionToken);

    /**
     * Find session by ID for a specific user.
     */
    @Query("SELECT s FROM UserSession s WHERE s.id = :id AND s.user.id = :userId")
    Optional<UserSession> findByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    /**
     * Deactivate all sessions for a user except one.
     */
    @Modifying
    @Query("UPDATE UserSession s SET s.isActive = false, s.revokedAt = :now, " +
           "s.revocationReason = 'Revoked by user' WHERE s.user.id = :userId AND s.id != :exceptSessionId")
    void deactivateAllByUserIdExcept(
        @Param("userId") Long userId,
        @Param("exceptSessionId") Long exceptSessionId,
        @Param("now") LocalDateTime now
    );

    /**
     * Deactivate all sessions for a user.
     */
    @Modifying
    @Query("UPDATE UserSession s SET s.isActive = false, s.revokedAt = :now, " +
           "s.revocationReason = 'All sessions revoked' WHERE s.user.id = :userId")
    void deactivateAllByUserId(@Param("userId") Long userId, @Param("now") LocalDateTime now);

    /**
     * Find expired sessions.
     */
    @Query("SELECT s FROM UserSession s WHERE s.isActive = true AND s.expiresAt < :now")
    List<UserSession> findExpiredSessions(@Param("now") LocalDateTime now);

    /**
     * Update last active timestamp.
     */
    @Modifying
    @Query("UPDATE UserSession s SET s.lastActiveAt = :timestamp WHERE s.id = :sessionId")
    void updateLastActiveAt(@Param("sessionId") Long sessionId, @Param("timestamp") LocalDateTime timestamp);
}
