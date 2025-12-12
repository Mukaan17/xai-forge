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
public class PredictionDTO {
    private Long id;
    private Long modelId;
    private String modelName;
    private String predictionResult;
    private Double confidence;
    private String inputSummary;
    private LocalDateTime createdAt;
}
