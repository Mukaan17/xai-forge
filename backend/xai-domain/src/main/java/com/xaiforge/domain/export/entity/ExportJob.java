package com.xaiforge.domain.export.entity;

import com.xaiforge.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Entity representing a data export job for GDPR compliance
 * 
 * @since 1.0.0
 */
@Entity
@Table(name = "export_jobs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExportJob {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ExportStatus status = ExportStatus.PENDING;
    
    @Column(name = "progress", nullable = false)
    private int progress = 0; // 0-100
    
    @Column(name = "current_step", length = 255)
    private String currentStep;
    
    @Column(name = "file_path", length = 500)
    private String filePath;
    
    @Column(name = "file_size")
    private Long fileSize; // in bytes
    
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    
    @Column(name = "expires_at")
    private LocalDateTime expiresAt; // Auto-delete after this date
    
    public enum ExportStatus {
        PENDING,
        PROCESSING,
        COMPLETED,
        FAILED,
        EXPIRED
    }
    
    public void startProcessing() {
        this.status = ExportStatus.PROCESSING;
        this.progress = 0;
    }
    
    public void updateProgress(int progress, String currentStep) {
        this.progress = Math.min(100, Math.max(0, progress));
        this.currentStep = currentStep;
    }
    
    public void complete(String filePath, Long fileSize) {
        this.status = ExportStatus.COMPLETED;
        this.progress = 100;
        this.filePath = filePath;
        this.fileSize = fileSize;
        this.completedAt = LocalDateTime.now();
        // Set expiration to 7 days from now
        this.expiresAt = LocalDateTime.now().plusDays(7);
    }
    
    public void fail(String errorMessage) {
        this.status = ExportStatus.FAILED;
        this.errorMessage = errorMessage;
        this.completedAt = LocalDateTime.now();
    }
    
    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }
}
