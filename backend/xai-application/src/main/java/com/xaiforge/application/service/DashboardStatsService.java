package com.xaiforge.application.service;

import com.xaiforge.application.query.GetDashboardStatsQuery;
import com.xaiforge.infrastructure.cache.CacheService;
import com.xaiforge.infrastructure.persistence.activity.ActivityLogRepository;
import com.xaiforge.infrastructure.persistence.dataset.DatasetRepository;
import com.xaiforge.infrastructure.persistence.model.MLModelRepository;
import com.xaiforge.infrastructure.persistence.prediction.PredictionRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class DashboardStatsService {
    
    private static final String CACHE_KEY_PREFIX = "dashboard:stats:user:";
    private static final Duration CACHE_TTL = Duration.ofMinutes(5);
    
    private final DatasetRepository datasetRepository;
    private final MLModelRepository modelRepository;
    private final PredictionRecordRepository predictionRepository;
    private final ActivityLogRepository activityRepository;
    private final CacheService cacheService;
    
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
        
        // Dataset sizes (placeholder for now)
        Map<String, Long> datasetSizes = new HashMap<>();
        
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
    
    public void invalidateStats(Long userId) {
        cacheService.evict(CACHE_KEY_PREFIX + userId);
    }
}

