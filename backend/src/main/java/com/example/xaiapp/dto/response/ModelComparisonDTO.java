package com.example.xaiapp.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModelComparisonDTO {
    private List<ModelSummaryDTO> models;
    private List<MetricComparisonDTO> metricsComparison;
    private Map<String, Map<Long, Double>> featureImportanceComparison;
    private Long bestModelId;
    private List<String> recommendations;
    private String modelType;
}
