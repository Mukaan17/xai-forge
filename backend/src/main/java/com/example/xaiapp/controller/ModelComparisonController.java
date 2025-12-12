package com.example.xaiapp.controller;

import com.example.xaiapp.dto.response.*;
import com.example.xaiapp.security.CurrentUser;
import com.example.xaiapp.security.UserPrincipal;
import com.example.xaiapp.service.ModelComparisonService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST controller for model comparison.
 */
@RestController
@RequestMapping("/api/models")
@RequiredArgsConstructor
@Tag(name = "Model Comparison", description = "Model comparison endpoints")
public class ModelComparisonController {

    private final ModelComparisonService modelComparisonService;

    @PostMapping("/compare")
    @Operation(summary = "Compare multiple models")
    public ResponseEntity<ModelComparisonDTO> compareModels(
            @CurrentUser UserPrincipal currentUser,
            @RequestBody List<Long> modelIds) {
        Map<String, Object> comparisonMap = modelComparisonService.compareModels(currentUser.getId(), modelIds);
        ModelComparisonDTO comparison = mapToModelComparisonDTO(comparisonMap);
        return ResponseEntity.ok(comparison);
    }

    @GetMapping("/{id}/versions")
    @Operation(summary = "Get all versions of a model")
    public ResponseEntity<List<ModelSummaryDTO>> getModelVersions(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable Long id,
            @RequestParam String baseName) {
        List<Map<String, Object>> versionsMap = modelComparisonService.getModelVersions(currentUser.getId(), baseName);
        List<ModelSummaryDTO> versions = versionsMap.stream()
            .map(this::mapToModelSummaryDTO)
            .collect(Collectors.toList());
        return ResponseEntity.ok(versions);
    }

    @GetMapping("/{id}/trend")
    @Operation(summary = "Get performance trend for model versions")
    public ResponseEntity<Map<String, Object>> getPerformanceTrend(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable Long id) {
        Map<String, Object> trend = modelComparisonService.getPerformanceTrend(currentUser.getId(), id);
        return ResponseEntity.ok(trend);
    }

    private ModelComparisonDTO mapToModelComparisonDTO(Map<String, Object> map) {
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> modelsList = (List<Map<String, Object>>) map.get("models");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> metricsList = (List<Map<String, Object>>) map.get("metricsComparison");
        
        return ModelComparisonDTO.builder()
            .models(modelsList != null ? modelsList.stream()
                .map(this::mapToModelSummaryDTO)
                .collect(Collectors.toList()) : null)
            .metricsComparison(metricsList != null ? metricsList.stream()
                .map(this::mapToMetricComparisonDTO)
                .collect(Collectors.toList()) : null)
            .featureImportanceComparison((Map<String, Map<Long, Double>>) map.get("featureImportanceComparison"))
            .bestModelId(getLong(map, "bestModelId"))
            .recommendations((List<String>) map.get("recommendations"))
            .modelType((String) map.get("modelType"))
            .build();
    }

    private ModelSummaryDTO mapToModelSummaryDTO(Map<String, Object> map) {
        return ModelSummaryDTO.builder()
            .id(getLong(map, "id"))
            .name((String) map.get("name"))
            .version((String) map.get("version"))
            .algorithm((String) map.get("algorithm"))
            .accuracy(getDouble(map, "accuracy"))
            .trainedAt((java.time.LocalDateTime) map.get("trainedAt"))
            .featureCount(getInteger(map, "featureCount"))
            .build();
    }

    private MetricComparisonDTO mapToMetricComparisonDTO(Map<String, Object> map) {
        return MetricComparisonDTO.builder()
            .metricName((String) map.get("metricName"))
            .values((Map<Long, Double>) map.get("values"))
            .bestModelId(getLong(map, "bestModelId"))
            .higherIsBetter((Boolean) map.get("higherIsBetter"))
            .build();
    }

    private Long getLong(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return null;
        if (value instanceof Long) return (Long) value;
        if (value instanceof Number) return ((Number) value).longValue();
        return null;
    }

    private Double getDouble(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return null;
        if (value instanceof Double) return (Double) value;
        if (value instanceof Number) return ((Number) value).doubleValue();
        return null;
    }

    private Integer getInteger(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return null;
        if (value instanceof Integer) return (Integer) value;
        if (value instanceof Number) return ((Number) value).intValue();
        return null;
    }
}
