package com.example.xaiapp.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserStatisticsDTO {
    private Long totalDatasets;
    private Long totalModels;
    private Long totalPredictions;
    private Double averageModelAccuracy;
    private LocalDateTime accountCreatedAt;
    private LocalDateTime lastLoginAt;
    private Long activeApiKeys;
    private Long activeSessions;
}
