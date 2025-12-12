package com.example.xaiapp.repository;

import com.example.xaiapp.entity.ApiKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for ApiKey entity operations.
 */
@Repository
public interface ApiKeyRepository extends JpaRepository<ApiKey, Long> {

    /**
     * Find an API key by its hash.
     */
    Optional<ApiKey> findByKeyHash(String keyHash);

    /**
     * Find all API keys for a user.
     */
    @Query("SELECT k FROM ApiKey k WHERE k.user.id = :userId ORDER BY k.createdAt DESC")
    List<ApiKey> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId);

    /**
     * Find all active API keys for a user.
     */
    @Query("SELECT k FROM ApiKey k WHERE k.user.id = :userId AND k.active = true")
    List<ApiKey> findByUserIdAndActiveTrue(@Param("userId") Long userId);

    /**
     * Find an API key by ID for a specific user.
     */
    @Query("SELECT k FROM ApiKey k WHERE k.id = :id AND k.user.id = :userId")
    Optional<ApiKey> findByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    /**
     * Count active API keys for a user.
     */
    @Query("SELECT COUNT(k) FROM ApiKey k WHERE k.user.id = :userId AND k.active = true")
    long countByUserIdAndActiveTrue(@Param("userId") Long userId);

    /**
     * Check if a key hash exists.
     */
    boolean existsByKeyHash(String keyHash);

    /**
     * Deactivate all keys for a user.
     */
    @Modifying
    @Query("UPDATE ApiKey k SET k.active = false WHERE k.user.id = :userId")
    void deactivateAllByUserId(@Param("userId") Long userId);

    /**
     * Update last used timestamp and increment usage count.
     */
    @Modifying
    @Query("UPDATE ApiKey k SET k.lastUsedAt = :timestamp, k.lastUsedIp = :ip, " +
           "k.usageCount = k.usageCount + 1 WHERE k.id = :keyId")
    void updateLastUsed(
        @Param("keyId") Long keyId,
        @Param("timestamp") LocalDateTime timestamp,
        @Param("ip") String ip
    );

    /**
     * Find expired keys that are still active.
     */
    @Query("SELECT k FROM ApiKey k WHERE k.active = true " +
           "AND k.expiresAt IS NOT NULL AND k.expiresAt < :now")
    List<ApiKey> findExpiredKeys(@Param("now") LocalDateTime now);
}
