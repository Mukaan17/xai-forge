package com.example.xaiapp.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * Entity representing an API key for programmatic access to XAI-Forge.
 * 
 * Security considerations:
 * - The actual key is NEVER stored; only a SHA-256 hash is stored
 * - The key prefix (first 8 chars) is stored for identification in UI
 * - Full key is shown ONLY once at creation time
 * 
 * Key format: xai_{env}_sk_{32_random_chars}
 * Example: xai_live_sk_a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6
 */
@Entity
@Table(name = "api_keys", indexes = {
    @Index(name = "idx_api_key_user_id", columnList = "user_id"),
    @Index(name = "idx_api_key_hash", columnList = "key_hash", unique = true),
    @Index(name = "idx_api_key_active", columnList = "user_id, active")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiKey {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * User who owns this API key.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Human-readable name for the key (e.g., "Production Server", "Development")
     */
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    /**
     * SHA-256 hash of the full API key.
     * Used for validation during API requests.
     */
    @Column(name = "key_hash", nullable = false, unique = true, length = 64)
    private String keyHash;

    /**
     * First 12 characters of the key for display purposes.
     * Format: "xai_live_sk_" or "xai_test_sk_"
     * This allows users to identify which key is which without exposing the full key.
     */
    @Column(name = "key_prefix", nullable = false, length = 20)
    private String keyPrefix;

    /**
     * Last 4 characters of the key for additional identification.
     */
    @Column(name = "key_suffix", nullable = false, length = 4)
    private String keySuffix;

    /**
     * Environment this key is intended for.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "environment", nullable = false, length = 20)
    private ApiKeyEnvironment environment;

    /**
     * Set of permissions granted to this key.
     * Stored as JSON array: ["datasets:read", "datasets:write", "models:read", ...]
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "permissions", columnDefinition = "jsonb", nullable = false)
    private Set<String> permissions;

    /**
     * Whether this key is currently active.
     * Revoked keys have active = false.
     */
    @Column(name = "active", nullable = false)
    @Builder.Default
    private Boolean active = true;

    /**
     * Optional expiration date for the key.
     * Null means the key never expires.
     */
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    /**
     * Timestamp of the last time this key was used.
     * Updated on each successful API request.
     */
    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;

    /**
     * IP address from which the key was last used.
     */
    @Column(name = "last_used_ip", length = 45)
    private String lastUsedIp;

    /**
     * Number of times this key has been used.
     * Useful for monitoring and analytics.
     */
    @Column(name = "usage_count", nullable = false)
    @Builder.Default
    private Long usageCount = 0L;

    /**
     * Creation timestamp.
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Optional description of what this key is used for.
     */
    @Column(name = "description", length = 500)
    private String description;

    /**
     * Check if this key has a specific permission.
     */
    public boolean hasPermission(String permission) {
        if (permissions == null) return false;
        // Check for exact match or wildcard
        return permissions.contains(permission) || 
               permissions.contains("*") ||
               permissions.contains(permission.split(":")[0] + ":*");
    }

    /**
     * Check if this key is valid (active and not expired).
     */
    public boolean isValid() {
        if (!active) return false;
        if (expiresAt != null && expiresAt.isBefore(LocalDateTime.now())) return false;
        return true;
    }

    /**
     * API Key environment enum.
     */
    public enum ApiKeyEnvironment {
        PRODUCTION,
        DEVELOPMENT,
        STAGING
    }
}
