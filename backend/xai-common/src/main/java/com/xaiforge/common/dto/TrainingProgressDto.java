package com.xaiforge.common.dto;

import java.time.LocalDateTime;

/**
 * DTO for training progress information
 * 
 * @since 1.0.0
 */
public record TrainingProgressDto(
    Long jobId,
    Long modelId,
    String status,
    Integer progress,
    String currentStep,
    String errorMessage,
    LocalDateTime startedAt,
    LocalDateTime completedAt,
    Long estimatedCompletionSeconds
) {}
