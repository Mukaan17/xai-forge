package com.example.xaiapp.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSummaryDTO {
    private Long totalDatasets;
    private Long totalModels;
    private Long totalPredictions;
    private Double averageModelAccuracy;
    private Long datasetsThisWeek;
    private Long modelsThisWeek;
    private Long predictionsLast30Days;
    private Long activeModels;
}
