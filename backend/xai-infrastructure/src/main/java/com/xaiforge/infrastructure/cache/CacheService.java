package com.xaiforge.infrastructure.cache;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

@Service
public class CacheService {
    
    private static final Logger log = LoggerFactory.getLogger(CacheService.class);
    
    private final RedisTemplate<String, Object> redisTemplate;
    private final CircuitBreaker circuitBreaker;
    
    public CacheService(RedisTemplate<String, Object> redisTemplate,
                        CircuitBreakerRegistry registry) {
        this.redisTemplate = redisTemplate;
        this.circuitBreaker = registry.circuitBreaker("redis");
    }
    
    public <T> Optional<T> get(String key, Class<T> type) {
        return circuitBreaker.executeSupplier(() -> {
            try {
                Object value = redisTemplate.opsForValue().get(key);
                if (value != null && type.isInstance(value)) {
                    log.debug("Cache HIT for key: {}", key);
                    return Optional.of(type.cast(value));
                }
                log.debug("Cache MISS for key: {}", key);
                return Optional.<T>empty();
            } catch (Exception e) {
                log.warn("Redis GET failed for key {}: {}", key, e.getMessage());
                throw e;
            }
        });
    }
    
    public <T> void set(String key, T value, Duration ttl) {
        // Non-blocking, failures logged but not thrown
        CompletableFuture.runAsync(() -> {
            try {
                circuitBreaker.executeRunnable(() -> {
                    redisTemplate.opsForValue().set(key, value, ttl);
                    log.debug("Cache SET for key: {} with TTL: {}", key, ttl);
                });
            } catch (Exception e) {
                log.warn("Redis SET failed for key {}: {}", key, e.getMessage());
                // Don't throw - cache write failures shouldn't break the app
            }
        });
    }
    
    public void evict(String key) {
        circuitBreaker.executeRunnable(() -> {
            try {
                redisTemplate.delete(key);
                log.debug("Cache EVICT for key: {}", key);
            } catch (Exception e) {
                log.warn("Redis EVICT failed for key {}: {}", key, e.getMessage());
            }
        });
    }
    
    public void evictPattern(String pattern) {
        circuitBreaker.executeRunnable(() -> {
            try {
                Set<String> keys = redisTemplate.keys(pattern);
                if (keys != null && !keys.isEmpty()) {
                    redisTemplate.delete(keys);
                    log.debug("Cache EVICT pattern {} removed {} keys", pattern, keys.size());
                }
            } catch (Exception e) {
                log.warn("Redis EVICT pattern failed for {}: {}", pattern, e.getMessage());
            }
        });
    }
}

