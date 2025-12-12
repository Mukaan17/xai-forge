package com.example.xaiapp.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PredictionDetailDTO {
    private Long id;
    private Long modelId;
    private String modelName;
    private String modelType;
    private Map<String, Object> inputData;
    private String predictionResult;
    private Double confidence;
    private Map<String, Object> explanation;
    private String explanationSummary;
    private Long predictionTimeMs;
    private Long explanationTimeMs;
    private LocalDateTime createdAt;
}
