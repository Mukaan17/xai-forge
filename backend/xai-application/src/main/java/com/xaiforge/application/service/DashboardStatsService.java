package com.xaiforge.application.service;

import com.xaiforge.application.query.GetDashboardStatsQuery;
import com.xaiforge.domain.dataset.entity.Dataset;
import com.xaiforge.infrastructure.cache.CacheService;
import com.xaiforge.infrastructure.persistence.activity.ActivityLogRepository;
import com.xaiforge.infrastructure.persistence.dataset.DatasetRepository;
import com.xaiforge.infrastructure.persistence.model.MLModelRepository;
import com.xaiforge.infrastructure.persistence.prediction.PredictionRecordRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@Slf4j
public class DashboardStatsService {
    
    private static final String CACHE_KEY_PREFIX = "dashboard:stats:user:";
    private static final Duration CACHE_TTL = Duration.ofMinutes(5);
    
    private final DatasetRepository datasetRepository;
    private final MLModelRepository modelRepository;
    private final PredictionRecordRepository predictionRepository;
    private final ActivityLogRepository activityRepository;
    private final CacheService cacheService;
    
    @Value("${app.file.upload-dir:uploads}")
    private String uploadDir;
    
    public DashboardStatsService(
            DatasetRepository datasetRepository,
            MLModelRepository modelRepository,
            PredictionRecordRepository predictionRepository,
            ActivityLogRepository activityRepository,
            CacheService cacheService) {
        this.datasetRepository = datasetRepository;
        this.modelRepository = modelRepository;
        this.predictionRepository = predictionRepository;
        this.activityRepository = activityRepository;
        this.cacheService = cacheService;
    }
    
    public GetDashboardStatsQuery.DashboardStats getStats(Long userId) {
        String cacheKey = CACHE_KEY_PREFIX + userId;
        
        // Try cache first
        var cached = cacheService.get(cacheKey, GetDashboardStatsQuery.DashboardStats.class);
        if (cached.isPresent()) {
            return cached.get();
        }
        
        // Cache miss - compute from database
        GetDashboardStatsQuery.DashboardStats stats = computeStats(userId);
        
        // Store in cache (non-blocking)
        cacheService.set(cacheKey, stats, CACHE_TTL);
        
        return stats;
    }
    
    private GetDashboardStatsQuery.DashboardStats computeStats(Long userId) {
        long totalDatasets = datasetRepository.countByOwnerId(userId);
        long totalModels = modelRepository.countByOwnerId(userId);
        long totalPredictions = predictionRepository.countByUserId(userId);
        Double avgAccuracy = modelRepository.avgAccuracyByOwnerId(userId);
        
        // Recent activity (last 5)
        var recentActivities = activityRepository
            .findByUserIdAndTimestampAfterOrderByTimestampDesc(
                userId, 
                LocalDateTime.now().minusDays(7),
                org.springframework.data.domain.PageRequest.of(0, 5)
            )
            .stream()
            .map(activity -> new GetDashboardStatsQuery.RecentActivity(
                activity.getEventType().name(),
                activity.getDetails(),
                activity.getTimestamp().toString()
            ))
            .collect(Collectors.toList());
        
        // Models by type
        Map<String, Long> modelsByType = new HashMap<>();
        var typeCounts = modelRepository.countByTypeGrouped(userId);
        for (Object[] row : typeCounts) {
            modelsByType.put(row[0].toString(), ((Number) row[1]).longValue());
        }
        
        // Weekly usage (last 7 days)
        var weeklyUsage = predictionRepository
            .countByDayLastWeek(userId, LocalDateTime.now().minusDays(7))
            .stream()
            .map(row -> new GetDashboardStatsQuery.WeeklyUsage(
                row[0].toString(),
                ((Number) row[1]).longValue()
            ))
            .collect(Collectors.toList());
        
        // Dataset sizes - calculate actual file sizes
        Map<String, Long> datasetSizes = calculateDatasetSizes(userId);
        
        return new GetDashboardStatsQuery.DashboardStats(
            totalDatasets,
            totalModels,
            totalPredictions,
            avgAccuracy != null ? avgAccuracy : 0.0,
            recentActivities,
            modelsByType,
            weeklyUsage,
            datasetSizes
        );
    }
    
    /**
     * Calculate actual file sizes for user's datasets
     */
    private Map<String, Long> calculateDatasetSizes(Long userId) {
        Map<String, Long> sizes = new HashMap<>();
        
        try {
            List<Dataset> datasets = datasetRepository.findByOwnerId(userId);
            
            for (Dataset dataset : datasets) {
                try {
                    String filePath = dataset.getFilePath();
                    if (filePath == null || filePath.isEmpty()) {
                        log.debug("Dataset {} has no file path", dataset.getId());
                        continue;
                    }
                    
                    // Handle both absolute and relative paths
                    Path path;
                    if (Paths.get(filePath).isAbsolute()) {
                        path = Paths.get(filePath);
                    } else {
                        // Relative path - prepend upload directory
                        path = Paths.get(uploadDir, filePath);
                    }
                    
                    if (Files.exists(path)) {
                        long fileSize = Files.size(path);
                        String datasetName = dataset.getFileName() != null ? dataset.getFileName() : "dataset_" + dataset.getId();
                        sizes.put(datasetName, fileSize);
                    } else {
                        log.debug("File not found for dataset {}: {}", dataset.getId(), path);
                    }
                } catch (Exception e) {
                    log.warn("Error calculating size for dataset {}: {}", dataset.getId(), e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("Error calculating dataset sizes: {}", e.getMessage(), e);
        }
        
        return sizes;
    }
    
    public void invalidateStats(Long userId) {
        cacheService.evict(CACHE_KEY_PREFIX + userId);
    }
}

