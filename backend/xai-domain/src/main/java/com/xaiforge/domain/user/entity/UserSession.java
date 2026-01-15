package com.xaiforge.domain.user.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity representing a user session (JWT token).
 * Tracks active sessions for security and session management.
 */
@Entity
@Table(name = "user_sessions", indexes = {
    @Index(name = "idx_user_sessions_user_id", columnList = "user_id"),
    @Index(name = "idx_user_sessions_token", columnList = "session_token", unique = true)
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSession {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(name = "session_token", nullable = false, unique = true, length = 500)
    private String sessionToken; // JWT token
    
    @Column(name = "device_info", length = 255)
    private String deviceInfo; // e.g., "Chrome on Windows", "Safari on macOS"
    
    @Column(name = "ip_address", length = 45)
    private String ipAddress;
    
    @Column(name = "location", length = 255)
    private String location; // e.g., "New York, US"
    
    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "last_active_at", nullable = false)
    private LocalDateTime lastActiveAt;
    
    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;
    
    @Column(name = "revoked_reason", length = 255)
    private String revokedReason;
    
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        lastActiveAt = now;
    }
    
    public void revoke(String reason) {
        this.isActive = false;
        this.revokedAt = LocalDateTime.now();
        this.revokedReason = reason;
    }
    
    public void updateLastActive() {
        this.lastActiveAt = LocalDateTime.now();
    }
}
