package com.xaiforge.application.service;

import com.xaiforge.common.dto.DatasetDto;
import com.xaiforge.common.dto.DatasetPreviewDto;
import com.xaiforge.common.dto.DatasetStatisticsDto;
import com.xaiforge.infrastructure.cache.CacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Optional;

/**
 * Caching wrapper for dataset service operations
 * Provides caching for frequently accessed dataset data
 * 
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CachedDatasetService {
    
    private static final String DATASET_CACHE_PREFIX = "dataset:";
    private static final String PREVIEW_CACHE_PREFIX = "dataset:preview:";
    private static final String STATS_CACHE_PREFIX = "dataset:stats:";
    private static final Duration DATASET_CACHE_TTL = Duration.ofMinutes(30);
    private static final Duration PREVIEW_CACHE_TTL = Duration.ofHours(1);
    private static final Duration STATS_CACHE_TTL = Duration.ofHours(2);
    
    private final DatasetApplicationService datasetService;
    private final CacheService cacheService;
    
    /**
     * Get dataset by ID with caching
     */
    public Optional<DatasetDto> getDataset(Long datasetId, Long userId) {
        String cacheKey = DATASET_CACHE_PREFIX + datasetId + ":user:" + userId;
        
        var cached = cacheService.get(cacheKey, DatasetDto.class);
        if (cached.isPresent()) {
            log.debug("Cache HIT for dataset: {}", datasetId);
            return cached;
        }
        
        log.debug("Cache MISS for dataset: {}", datasetId);
        Optional<DatasetDto> dataset = datasetService.getDataset(datasetId, userId);
        
        if (dataset.isPresent()) {
            cacheService.set(cacheKey, dataset.get(), DATASET_CACHE_TTL);
        }
        
        return dataset;
    }
    
    /**
     * Get dataset preview with caching
     */
    public DatasetPreviewDto previewDataset(Long datasetId, Long userId, int rows, int offset) {
        String cacheKey = PREVIEW_CACHE_PREFIX + datasetId + ":rows:" + rows + ":offset:" + offset + ":user:" + userId;
        
        var cached = cacheService.get(cacheKey, DatasetPreviewDto.class);
        if (cached.isPresent()) {
            log.debug("Cache HIT for dataset preview: {}", datasetId);
            return cached.get();
        }
        
        log.debug("Cache MISS for dataset preview: {}", datasetId);
        DatasetPreviewDto preview = datasetService.previewDataset(datasetId, userId, rows, offset);
        
        cacheService.set(cacheKey, preview, PREVIEW_CACHE_TTL);
        
        return preview;
    }
    
    /**
     * Get dataset statistics with caching
     */
    public DatasetStatisticsDto getDatasetStatistics(Long datasetId, Long userId) {
        String cacheKey = STATS_CACHE_PREFIX + datasetId + ":user:" + userId;
        
        var cached = cacheService.get(cacheKey, DatasetStatisticsDto.class);
        if (cached.isPresent()) {
            log.debug("Cache HIT for dataset statistics: {}", datasetId);
            return cached.get();
        }
        
        log.debug("Cache MISS for dataset statistics: {}", datasetId);
        DatasetStatisticsDto statistics = datasetService.getDatasetStatistics(datasetId, userId);
        
        cacheService.set(cacheKey, statistics, STATS_CACHE_TTL);
        
        return statistics;
    }
    
    /**
     * Invalidate dataset cache
     */
    public void invalidateDataset(Long datasetId, Long userId) {
        String datasetKey = DATASET_CACHE_PREFIX + datasetId + ":user:" + userId;
        String previewPattern = PREVIEW_CACHE_PREFIX + datasetId + ":*:user:" + userId;
        String statsKey = STATS_CACHE_PREFIX + datasetId + ":user:" + userId;
        
        cacheService.evict(datasetKey);
        cacheService.evictPattern(previewPattern);
        cacheService.evict(statsKey);
        
        log.debug("Invalidated cache for dataset: {}", datasetId);
    }
    
    /**
     * Invalidate all dataset caches for a user
     */
    public void invalidateUserDatasets(Long userId) {
        cacheService.evictPattern(DATASET_CACHE_PREFIX + "*:user:" + userId);
        cacheService.evictPattern(PREVIEW_CACHE_PREFIX + "*:user:" + userId);
        cacheService.evictPattern(STATS_CACHE_PREFIX + "*:user:" + userId);
        log.debug("Invalidated all dataset caches for user: {}", userId);
    }
}
