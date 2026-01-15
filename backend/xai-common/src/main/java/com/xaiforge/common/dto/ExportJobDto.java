package com.xaiforge.common.dto;

import java.time.LocalDateTime;

public record ExportJobDto(
    Long id,
    String status,
    int progress,
    String currentStep,
    String errorMessage,
    Long fileSize,
    LocalDateTime createdAt,
    LocalDateTime completedAt,
    LocalDateTime expiresAt
) {}
