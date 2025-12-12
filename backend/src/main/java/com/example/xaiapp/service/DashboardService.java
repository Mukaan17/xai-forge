package com.example.xaiapp.service;

import com.example.xaiapp.entity.ActivityLog;
import com.example.xaiapp.entity.MLModel;
import com.example.xaiapp.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for dashboard data aggregation.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class DashboardService {

    private final DatasetRepository datasetRepository;
    private final MLModelRepository modelRepository;
    private final PredictionRepository predictionRepository;
    private final ActivityLogRepository activityLogRepository;

    /**
     * Get dashboard summary with all KPIs.
     */
    public Map<String, Object> getDashboardSummary(Long userId) {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);

        long datasetCount = datasetRepository.countByUserIdAndDeletedFalse(userId);
        long modelCount = modelRepository.countByUserIdAndStatusIn(userId, 
            List.of(MLModel.ModelStatus.READY, MLModel.ModelStatus.TRAINING));
        long predictionCount = predictionRepository.countByUserId(userId);
        long datasetsThisWeek = datasetRepository.countByUserIdAndCreatedAtAfter(userId, sevenDaysAgo);
        long modelsThisWeek = modelRepository.countByUserIdAndCreatedAtAfter(userId, sevenDaysAgo);
        long predictionsThisMonth = predictionRepository.countByUserIdSince(userId, thirtyDaysAgo);
        Double avgAccuracy = modelRepository.getAverageAccuracyByUserId(userId);
        long activeModels = modelRepository.countByUserIdAndStatus(userId, MLModel.ModelStatus.READY);

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalDatasets", datasetCount);
        summary.put("totalModels", modelCount);
        summary.put("totalPredictions", predictionCount);
        summary.put("averageModelAccuracy", avgAccuracy != null ? avgAccuracy : 0.0);
        summary.put("datasetsThisWeek", datasetsThisWeek);
        summary.put("modelsThisWeek", modelsThisWeek);
        summary.put("predictionsLast30Days", predictionsThisMonth);
        summary.put("activeModels", activeModels);
        return summary;
    }

    /**
     * Get recent activity feed for dashboard.
     */
    public List<Map<String, Object>> getRecentActivity(Long userId, int limit) {
        List<ActivityLog> activities = activityLogRepository.findRecentByUserId(userId, PageRequest.of(0, limit));
        return activities.stream()
            .map(log -> {
                Map<String, Object> item = new HashMap<>();
                item.put("id", log.getId());
                item.put("type", mapActionToFeedType(log.getAction()));
                item.put("icon", mapActionToIcon(log.getAction()));
                item.put("title", formatActivityTitle(log));
                item.put("subtitle", log.getResourceName());
                item.put("timestamp", log.getCreatedAt());
                item.put("actionUrl", buildActionUrl(log));
                return item;
            })
            .collect(Collectors.toList());
    }

    /**
     * Get model distribution by type.
     */
    public Map<String, Long> getModelsByType(Long userId) {
        long classification = modelRepository.countByUserIdAndModelType(userId, MLModel.ModelType.CLASSIFICATION);
        long regression = modelRepository.countByUserIdAndModelType(userId, MLModel.ModelType.REGRESSION);
        return Map.of("classification", classification, "regression", regression);
    }

    /**
     * Get usage trend over time.
     */
    public List<Map<String, Object>> getUsageTrend(Long userId, int days) {
        LocalDateTime startDate = LocalDateTime.now().minusDays(days);
        List<Object[]> predictionData = predictionRepository.getDailyPredictionCounts(userId, startDate);
        Map<String, Long> predictionsByDate = new HashMap<>();
        for (Object[] row : predictionData) {
            predictionsByDate.put(row[0].toString(), ((Number) row[1]).longValue());
        }

        List<Map<String, Object>> trend = new ArrayList<>();
        for (int i = days - 1; i >= 0; i--) {
            LocalDateTime date = LocalDateTime.now().minusDays(i);
            String dateStr = date.toLocalDate().toString();
            Map<String, Object> point = new HashMap<>();
            point.put("date", dateStr);
            point.put("predictions", predictionsByDate.getOrDefault(dateStr, 0L));
            trend.add(point);
        }
        return trend;
    }

    /**
     * Get recent models for dashboard table.
     */
    public List<Map<String, Object>> getRecentModels(Long userId, int limit) {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(0, limit);
        return modelRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable).getContent().stream()
            .map(model -> {
                Map<String, Object> dto = new HashMap<>();
                dto.put("id", model.getId());
                dto.put("name", model.getName());
                dto.put("type", model.getModelType().name());
                dto.put("algorithm", model.getAlgorithm());
                dto.put("accuracy", model.getAccuracy());
                dto.put("status", model.getStatus().name());
                dto.put("createdAt", model.getCreatedAt());
                dto.put("datasetName", model.getDataset() != null ? model.getDataset().getName() : null);
                dto.put("predictionCount", model.getPredictionCount());
                return dto;
            })
            .collect(Collectors.toList());
    }

    /**
     * Get quick stats for sidebar.
     */
    public Map<String, Object> getQuickStats(Long userId) {
        LocalDateTime today = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        long predictionsToday = predictionRepository.countByUserIdSince(userId, today);
        long modelsInTraining = modelRepository.countByUserIdAndStatus(userId, MLModel.ModelStatus.TRAINING);
        long totalDatasetSize = datasetRepository.getTotalFileSizeByUserId(userId);
        long totalModelSize = modelRepository.getTotalModelSizeByUserId(userId);

        Map<String, Object> stats = new HashMap<>();
        stats.put("predictionsToday", predictionsToday);
        stats.put("modelsInTraining", modelsInTraining);
        stats.put("storageUsedBytes", totalDatasetSize + totalModelSize);
        return stats;
    }

    // ==================== PRIVATE HELPER METHODS ====================

    private String mapActionToFeedType(ActivityLog.ActionType action) {
        return switch (action) {
            case MODEL_TRAINING_COMPLETED, MODEL_TRAINING_STARTED -> "model";
            case DATASET_UPLOADED -> "dataset";
            case PREDICTION_MADE -> "prediction";
            case LOGIN_SUCCESS -> "security";
            default -> "activity";
        };
    }

    private String mapActionToIcon(ActivityLog.ActionType action) {
        return switch (action) {
            case MODEL_TRAINING_COMPLETED -> "🤖";
            case MODEL_TRAINING_FAILED -> "⚠️";
            case DATASET_UPLOADED -> "📁";
            case PREDICTION_MADE -> "🔮";
            case LOGIN_SUCCESS -> "🔐";
            case API_KEY_CREATED -> "🔑";
            default -> "📋";
        };
    }

    private String formatActivityTitle(ActivityLog log) {
        return switch (log.getAction()) {
            case MODEL_TRAINING_COMPLETED -> "Model Training Complete";
            case MODEL_TRAINING_FAILED -> "Training Failed";
            case MODEL_TRAINING_STARTED -> "Model Training Started";
            case DATASET_UPLOADED -> "Dataset Uploaded";
            case PREDICTION_MADE -> "Prediction Made";
            case LOGIN_SUCCESS -> "Successful Login";
            case API_KEY_CREATED -> "API Key Created";
            default -> log.getAction().name().replace("_", " ");
        };
    }

    private String buildActionUrl(ActivityLog log) {
        if (log.getResourceType() == null || log.getResourceId() == null) {
            return null;
        }
        return switch (log.getResourceType()) {
            case "MODEL" -> "/models/" + log.getResourceId();
            case "DATASET" -> "/datasets/" + log.getResourceId();
            case "PREDICTION" -> "/predictions/" + log.getResourceId();
            default -> null;
        };
    }
}
