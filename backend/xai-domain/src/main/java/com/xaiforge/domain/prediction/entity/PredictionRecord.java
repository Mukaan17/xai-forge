package com.xaiforge.domain.prediction.entity;

import com.xaiforge.domain.model.entity.MLModel;
import com.xaiforge.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "prediction_records")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PredictionRecord {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "model_id", nullable = false)
    private MLModel model;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(name = "input_data", columnDefinition = "TEXT")
    private String inputData; // JSON string of input features
    
    @Column(name = "prediction", columnDefinition = "TEXT")
    private String prediction; // JSON string of prediction result
    
    @Column(name = "confidence")
    private Double confidence;
    
    @Column(name = "explanation", columnDefinition = "TEXT")
    private String explanation; // JSON string of XAI explanation
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}

