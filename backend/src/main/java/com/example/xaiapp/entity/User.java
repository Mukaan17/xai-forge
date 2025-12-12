/**
 * @Author: Mukhil Sundararaj
 * @Date:   2025-09-04 16:05:25
 * @Last Modified by:   Mukhil Sundararaj
 * @Last Modified time: 2025-10-24 18:38:51
 */
package com.example.xaiapp.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

/**
 * User entity representing a registered user of XAI-Forge.
 * 
 * UPDATED: Added profile fields, relationships to new entities,
 * 2FA support, and additional metadata.
 */
@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_user_email", columnList = "email", unique = true)
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User implements UserDetails {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // ==================== AUTHENTICATION FIELDS ====================
    
    /**
     * Username for backward compatibility (can be same as email)
     */
    @Column(name = "username", unique = true, nullable = false, length = 255)
    private String username;
    
    /**
     * User's email address. Used for login and notifications.
     * Must be unique across all users.
     */
    @Column(name = "email", unique = true, nullable = false, length = 255)
    private String email;
    
    /**
     * BCrypt hashed password.
     */
    @Column(name = "password", nullable = false, length = 100)
    private String password;
    
    /**
     * Whether the email has been verified.
     */
    @Column(name = "email_verified", nullable = false)
    @Builder.Default
    private Boolean emailVerified = false;
    
    /**
     * Email verification token (null after verification).
     */
    @Column(name = "email_verification_token", length = 100)
    private String emailVerificationToken;
    
    /**
     * When the verification token expires.
     */
    @Column(name = "email_verification_expires")
    private LocalDateTime emailVerificationExpires;
    
    // ==================== TWO-FACTOR AUTHENTICATION ====================
    
    /**
     * Whether 2FA is enabled for this user.
     */
    @Column(name = "two_factor_enabled", nullable = false)
    @Builder.Default
    private Boolean twoFactorEnabled = false;
    
    /**
     * Encrypted TOTP secret for 2FA.
     */
    @Column(name = "two_factor_secret", length = 100)
    private String twoFactorSecret;
    
    /**
     * Backup codes for 2FA recovery (comma-separated, hashed).
     */
    @Column(name = "two_factor_backup_codes", length = 500)
    private String twoFactorBackupCodes;
    
    // ==================== PROFILE FIELDS ====================
    
    /**
     * User's first name.
     */
    @Column(name = "first_name", length = 100)
    private String firstName;
    
    /**
     * User's last name.
     */
    @Column(name = "last_name", length = 100)
    private String lastName;
    
    /**
     * Organization or company name.
     */
    @Column(name = "organization", length = 200)
    private String organization;
    
    /**
     * Job title or role.
     */
    @Column(name = "role", length = 100)
    private String role;
    
    /**
     * Location (City, Country or similar).
     */
    @Column(name = "location", length = 200)
    private String location;
    
    /**
     * Short bio or description.
     */
    @Column(name = "bio", columnDefinition = "TEXT")
    private String bio;
    
    /**
     * URL to profile image.
     */
    @Column(name = "profile_image_url", length = 500)
    private String profileImageUrl;
    
    // ==================== ACCOUNT STATUS ====================
    
    /**
     * Whether the account is active.
     */
    @Column(name = "active", nullable = false)
    @Builder.Default
    private Boolean active = true;
    
    /**
     * Whether the account is locked (due to failed login attempts).
     */
    @Column(name = "account_locked", nullable = false)
    @Builder.Default
    private Boolean accountLocked = false;
    
    /**
     * When the account lock expires.
     */
    @Column(name = "lock_expires_at")
    private LocalDateTime lockExpiresAt;
    
    /**
     * Number of consecutive failed login attempts.
     */
    @Column(name = "failed_login_attempts", nullable = false)
    @Builder.Default
    private Integer failedLoginAttempts = 0;
    
    // ==================== TIMESTAMPS ====================
    
    /**
     * When the user registered.
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    /**
     * When the user profile was last updated.
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    /**
     * When the user last logged in.
     */
    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;
    
    /**
     * When the password was last changed.
     */
    @Column(name = "password_changed_at")
    private LocalDateTime passwordChangedAt;
    
    // ==================== RELATIONSHIPS ====================
    
    /**
     * User's datasets.
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Dataset> datasets = new ArrayList<>();
    
    /**
     * User's trained models.
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<MLModel> models = new ArrayList<>();
    
    /**
     * User's predictions.
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Prediction> predictions = new ArrayList<>();
    
    /**
     * User's API keys.
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ApiKey> apiKeys = new ArrayList<>();
    
    /**
     * User's sessions.
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<UserSession> sessions = new ArrayList<>();
    
    /**
     * User's notifications.
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Notification> notifications = new ArrayList<>();
    
    /**
     * User's preferences (one-to-one).
     */
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private UserPreferences preferences;
    
    /**
     * User's activity logs.
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ActivityLog> activityLogs = new ArrayList<>();
    
    /**
     * User's webhooks.
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Webhook> webhooks = new ArrayList<>();
    
    /**
     * User's export jobs.
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ExportJob> exportJobs = new ArrayList<>();
    
    // ==================== UserDetails IMPLEMENTATION ====================
    
    @Override
    public String getUsername() {
        return username != null ? username : email;
    }
    
    @Override
    public String getPassword() {
        return password;
    }
    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
    }
    
    @Override
    public boolean isAccountNonExpired() {
        return active != null && active;
    }
    
    @Override
    public boolean isAccountNonLocked() {
        return !isCurrentlyLocked();
    }
    
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
    
    @Override
    public boolean isEnabled() {
        return active != null && active;
    }
    
    // ==================== HELPER METHODS ====================
    
    /**
     * Get full name.
     */
    public String getFullName() {
        if (firstName == null && lastName == null) return email;
        if (firstName == null) return lastName;
        if (lastName == null) return firstName;
        return firstName + " " + lastName;
    }
    
    /**
     * Get display name (full name or email).
     */
    public String getDisplayName() {
        String fullName = getFullName();
        return fullName.equals(email) ? email.split("@")[0] : fullName;
    }
    
    /**
     * Record a failed login attempt.
     */
    public void recordFailedLogin() {
        this.failedLoginAttempts++;
        if (this.failedLoginAttempts >= 5) {
            this.accountLocked = true;
            this.lockExpiresAt = LocalDateTime.now().plusMinutes(30);
        }
    }
    
    /**
     * Record a successful login.
     */
    public void recordSuccessfulLogin() {
        this.failedLoginAttempts = 0;
        this.accountLocked = false;
        this.lockExpiresAt = null;
        this.lastLoginAt = LocalDateTime.now();
    }
    
    /**
     * Check if account is currently locked.
     */
    public boolean isCurrentlyLocked() {
        if (!accountLocked) return false;
        if (lockExpiresAt != null && lockExpiresAt.isBefore(LocalDateTime.now())) {
            // Lock has expired
            this.accountLocked = false;
            this.lockExpiresAt = null;
            this.failedLoginAttempts = 0;
            return false;
        }
        return true;
    }
}
