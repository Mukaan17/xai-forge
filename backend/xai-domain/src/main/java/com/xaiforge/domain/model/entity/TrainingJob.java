package com.xaiforge.domain.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity representing a model training job with progress tracking
 * 
 * @since 1.0.0
 */
@Entity
@Table(name = "training_jobs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrainingJob {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "model_id", nullable = false)
    private Long modelId;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private JobStatus status = JobStatus.PENDING;
    
    @Column(name = "progress", nullable = false)
    private Integer progress = 0; // 0-100
    
    @Column(name = "current_step", length = 500)
    private String currentStep;
    
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
    
    @Column(name = "started_at")
    private LocalDateTime startedAt;
    
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    
    @Column(name = "estimated_completion_seconds")
    private Long estimatedCompletionSeconds;
    
    @PrePersist
    protected void onCreate() {
        if (startedAt == null && status == JobStatus.RUNNING) {
            startedAt = LocalDateTime.now();
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        if (status == JobStatus.COMPLETED || status == JobStatus.FAILED) {
            if (completedAt == null) {
                completedAt = LocalDateTime.now();
            }
        }
    }
    
    public enum JobStatus {
        PENDING,
        RUNNING,
        COMPLETED,
        FAILED,
        CANCELLED
    }
}
