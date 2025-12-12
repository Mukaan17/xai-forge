package com.example.xaiapp.controller;

import com.example.xaiapp.dto.response.*;
import com.example.xaiapp.security.CurrentUser;
import com.example.xaiapp.security.UserPrincipal;
import com.example.xaiapp.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST controller for dashboard data.
 */
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "Dashboard data endpoints")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/summary")
    @Operation(summary = "Get dashboard summary")
    public ResponseEntity<DashboardSummaryDTO> getSummary(@CurrentUser UserPrincipal currentUser) {
        Map<String, Object> summaryMap = dashboardService.getDashboardSummary(currentUser.getId());
        DashboardSummaryDTO summary = mapToDashboardSummaryDTO(summaryMap);
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/recent-activity")
    @Operation(summary = "Get recent activity")
    public ResponseEntity<List<ActivityFeedItemDTO>> getRecentActivity(
            @CurrentUser UserPrincipal currentUser,
            @RequestParam(defaultValue = "10") int limit) {
        List<Map<String, Object>> activityMap = dashboardService.getRecentActivity(currentUser.getId(), limit);
        List<ActivityFeedItemDTO> activity = activityMap.stream()
            .map(this::mapToActivityFeedItemDTO)
            .collect(Collectors.toList());
        return ResponseEntity.ok(activity);
    }

    @GetMapping("/models-by-type")
    @Operation(summary = "Get model distribution by type")
    public ResponseEntity<Map<String, Long>> getModelsByType(@CurrentUser UserPrincipal currentUser) {
        return ResponseEntity.ok(dashboardService.getModelsByType(currentUser.getId()));
    }

    @GetMapping("/usage-trend")
    @Operation(summary = "Get usage trend")
    public ResponseEntity<List<UsageTrendDTO>> getUsageTrend(
            @CurrentUser UserPrincipal currentUser,
            @RequestParam(defaultValue = "30") int days) {
        List<Map<String, Object>> trendMap = dashboardService.getUsageTrend(currentUser.getId(), days);
        List<UsageTrendDTO> trend = trendMap.stream()
            .map(this::mapToUsageTrendDTO)
            .collect(Collectors.toList());
        return ResponseEntity.ok(trend);
    }

    @GetMapping("/recent-models")
    @Operation(summary = "Get recent models")
    public ResponseEntity<List<RecentModelDTO>> getRecentModels(
            @CurrentUser UserPrincipal currentUser,
            @RequestParam(defaultValue = "5") int limit) {
        List<Map<String, Object>> modelsMap = dashboardService.getRecentModels(currentUser.getId(), limit);
        List<RecentModelDTO> models = modelsMap.stream()
            .map(this::mapToRecentModelDTO)
            .collect(Collectors.toList());
        return ResponseEntity.ok(models);
    }

    @GetMapping("/quick-stats")
    @Operation(summary = "Get quick stats")
    public ResponseEntity<QuickStatsDTO> getQuickStats(@CurrentUser UserPrincipal currentUser) {
        Map<String, Object> statsMap = dashboardService.getQuickStats(currentUser.getId());
        QuickStatsDTO stats = mapToQuickStatsDTO(statsMap);
        return ResponseEntity.ok(stats);
    }

    // Helper mapping methods
    private DashboardSummaryDTO mapToDashboardSummaryDTO(Map<String, Object> map) {
        return DashboardSummaryDTO.builder()
            .totalDatasets(getLong(map, "totalDatasets"))
            .totalModels(getLong(map, "totalModels"))
            .totalPredictions(getLong(map, "totalPredictions"))
            .averageModelAccuracy(getDouble(map, "averageModelAccuracy"))
            .datasetsThisWeek(getLong(map, "datasetsThisWeek"))
            .modelsThisWeek(getLong(map, "modelsThisWeek"))
            .predictionsLast30Days(getLong(map, "predictionsLast30Days"))
            .activeModels(getLong(map, "activeModels"))
            .build();
    }

    private ActivityFeedItemDTO mapToActivityFeedItemDTO(Map<String, Object> map) {
        return ActivityFeedItemDTO.builder()
            .id(getLong(map, "id"))
            .type((String) map.get("type"))
            .icon((String) map.get("icon"))
            .title((String) map.get("title"))
            .subtitle((String) map.get("subtitle"))
            .timestamp((java.time.LocalDateTime) map.get("timestamp"))
            .actionUrl((String) map.get("actionUrl"))
            .build();
    }

    private UsageTrendDTO mapToUsageTrendDTO(Map<String, Object> map) {
        return UsageTrendDTO.builder()
            .date((String) map.get("date"))
            .predictions(getLong(map, "predictions"))
            .build();
    }

    private RecentModelDTO mapToRecentModelDTO(Map<String, Object> map) {
        return RecentModelDTO.builder()
            .id(getLong(map, "id"))
            .name((String) map.get("name"))
            .type((String) map.get("type"))
            .algorithm((String) map.get("algorithm"))
            .accuracy(getDouble(map, "accuracy"))
            .status((String) map.get("status"))
            .createdAt((java.time.LocalDateTime) map.get("createdAt"))
            .datasetName((String) map.get("datasetName"))
            .predictionCount(getLong(map, "predictionCount"))
            .build();
    }

    private QuickStatsDTO mapToQuickStatsDTO(Map<String, Object> map) {
        return QuickStatsDTO.builder()
            .predictionsToday(getLong(map, "predictionsToday"))
            .modelsInTraining(getLong(map, "modelsInTraining"))
            .storageUsedBytes(getLong(map, "storageUsedBytes"))
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
}
