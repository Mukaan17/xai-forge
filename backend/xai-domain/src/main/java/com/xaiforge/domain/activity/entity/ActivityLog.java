package com.xaiforge.domain.activity.entity;

import com.xaiforge.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "activity_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActivityLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false)
    private EventType eventType;
    
    @Column(name = "details", columnDefinition = "TEXT")
    private String details; // JSON string with event details
    
    @Column(name = "ip_address")
    private String ipAddress;
    
    @Column(name = "user_agent")
    private String userAgent;
    
    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;
    
    @PrePersist
    protected void onCreate() {
        timestamp = LocalDateTime.now();
    }
    
    public enum EventType {
        LOGIN_SUCCESS,
        LOGIN_FAILED,
        DATASET_UPLOADED,
        DATASET_DELETED,
        MODEL_TRAINED,
        MODEL_DELETED,
        PREDICTION_MADE,
        API_KEY_GENERATED,
        API_KEY_REVOKED,
        PROFILE_UPDATED,
        PASSWORD_CHANGED,
        TWO_FACTOR_ENABLED,
        TWO_FACTOR_DISABLED
    }
}

