package com.xaiforge.application.service;

import com.xaiforge.common.dto.ExtendedMetricsDto;
import com.xaiforge.common.dto.ModelDto;
import com.xaiforge.domain.model.entity.MLModel;
import com.xaiforge.infrastructure.cache.CacheService;
import com.xaiforge.infrastructure.persistence.model.MLModelRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Optional;

/**
 * Caching wrapper for model service operations
 * Provides caching for frequently accessed model data
 * 
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CachedModelService {
    
    private static final String MODEL_CACHE_PREFIX = "model:";
    private static final String METRICS_CACHE_PREFIX = "model:metrics:";
    private static final Duration MODEL_CACHE_TTL = Duration.ofMinutes(30);
    private static final Duration METRICS_CACHE_TTL = Duration.ofHours(1);
    
    private final MLModelRepository modelRepository;
    private final CacheService cacheService;
    private final ModelApplicationService modelService;
    
    /**
     * Get model by ID with caching
     */
    public Optional<ModelDto> getModel(Long modelId, Long userId) {
        String cacheKey = MODEL_CACHE_PREFIX + modelId + ":user:" + userId;
        
        var cached = cacheService.get(cacheKey, ModelDto.class);
        if (cached.isPresent()) {
            log.debug("Cache HIT for model: {}", modelId);
            return cached;
        }
        
        log.debug("Cache MISS for model: {}", modelId);
        ModelDto model = modelService.getModel(modelId, userId);
        cacheService.set(cacheKey, model, MODEL_CACHE_TTL);
        return Optional.of(model);
    }
    
    /**
     * Get extended metrics with caching
     */
    public Optional<ExtendedMetricsDto> getExtendedMetrics(Long modelId, Long userId) {
        String cacheKey = METRICS_CACHE_PREFIX + modelId + ":user:" + userId;
        
        var cached = cacheService.get(cacheKey, ExtendedMetricsDto.class);
        if (cached.isPresent()) {
            log.debug("Cache HIT for model metrics: {}", modelId);
            return cached;
        }
        
        log.debug("Cache MISS for model metrics: {}", modelId);
        ExtendedMetricsDto metrics = modelService.getExtendedMetrics(modelId, userId);
        cacheService.set(cacheKey, metrics, METRICS_CACHE_TTL);
        return Optional.of(metrics);
    }
    
    /**
     * Invalidate model cache
     */
    public void invalidateModel(Long modelId, Long userId) {
        String modelKey = MODEL_CACHE_PREFIX + modelId + ":user:" + userId;
        String metricsKey = METRICS_CACHE_PREFIX + modelId + ":user:" + userId;
        
        cacheService.evict(modelKey);
        cacheService.evict(metricsKey);
        
        log.debug("Invalidated cache for model: {}", modelId);
    }
    
    /**
     * Invalidate all model caches for a user
     */
    public void invalidateUserModels(Long userId) {
        cacheService.evictPattern(MODEL_CACHE_PREFIX + "*:user:" + userId);
        cacheService.evictPattern(METRICS_CACHE_PREFIX + "*:user:" + userId);
        log.debug("Invalidated all model caches for user: {}", userId);
    }
}
