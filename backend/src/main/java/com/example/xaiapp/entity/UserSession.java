package com.example.xaiapp.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Entity representing an active user session.
 * Used for session management, security monitoring, and "active sessions" display.
 * 
 * A new session is created on each successful login.
 * Sessions can be revoked individually or in bulk.
 */
@Entity
@Table(name = "user_sessions", indexes = {
    @Index(name = "idx_session_user_id", columnList = "user_id"),
    @Index(name = "idx_session_token", columnList = "session_token", unique = true),
    @Index(name = "idx_session_active", columnList = "user_id, is_active")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * User who owns this session.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Unique session token (JWT jti claim or separate token).
     * Used to identify and revoke specific sessions.
     */
    @Column(name = "session_token", nullable = false, unique = true, length = 100)
    private String sessionToken;

    /**
     * Hash of the refresh token if using refresh token rotation.
     */
    @Column(name = "refresh_token_hash", length = 64)
    private String refreshTokenHash;

    /**
     * Device/browser information from User-Agent header.
     * Parsed and formatted for display (e.g., "Chrome on MacOS")
     */
    @Column(name = "device_info", length = 200)
    private String deviceInfo;

    /**
     * Raw User-Agent string for detailed analysis.
     */
    @Column(name = "user_agent", length = 500)
    private String userAgent;

    /**
     * IP address from which the session was created.
     * Supports both IPv4 and IPv6.
     */
    @Column(name = "ip_address", nullable = false, length = 45)
    private String ipAddress;

    /**
     * Approximate geographic location based on IP.
     * Format: "City, Country" (e.g., "New York, US")
     */
    @Column(name = "location", length = 200)
    private String location;

    /**
     * Country code for the location (e.g., "US", "GB")
     */
    @Column(name = "country_code", length = 2)
    private String countryCode;

    /**
     * Whether this session is currently active.
     */
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    /**
     * Whether this is the session making the current request.
     * This is a transient field, not stored in database.
     */
    @Transient
    private Boolean isCurrentSession;

    /**
     * Timestamp of the last activity in this session.
     * Updated on each authenticated request.
     */
    @Column(name = "last_active_at", nullable = false)
    private LocalDateTime lastActiveAt;

    /**
     * When the session was created (login time).
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * When the session expires.
     * Based on JWT expiration or session timeout policy.
     */
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    /**
     * When the session was revoked (if revoked).
     */
    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;

    /**
     * Reason for revocation if revoked.
     */
    @Column(name = "revocation_reason", length = 100)
    private String revocationReason;

    /**
     * Check if session is valid (active and not expired).
     */
    public boolean isValid() {
        if (!isActive) return false;
        if (expiresAt != null && expiresAt.isBefore(LocalDateTime.now())) return false;
        return true;
    }

    /**
     * Revoke this session.
     */
    public void revoke(String reason) {
        this.isActive = false;
        this.revokedAt = LocalDateTime.now();
        this.revocationReason = reason;
    }
}
