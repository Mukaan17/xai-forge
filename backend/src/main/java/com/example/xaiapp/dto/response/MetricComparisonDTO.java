package com.example.xaiapp.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MetricComparisonDTO {
    private String metricName;
    private Map<Long, Double> values; // modelId -> value
    private Long bestModelId;
    private Boolean higherIsBetter;
}
