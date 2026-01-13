package com.xaiforge.infrastructure.cache;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class RateLimitService {
    
    private final RedisTemplate<String, Object> redisTemplate;
    private final ValueOperations<String, Object> valueOps;
    
    private static final int DEFAULT_LIMIT = 100;  // requests
    private static final int DEFAULT_WINDOW = 60;  // seconds
    
    public RateLimitService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.valueOps = redisTemplate.opsForValue();
    }
    
    public boolean isAllowed(String clientId, String endpoint) {
        String key = String.format("ratelimit:%s:%s", clientId, endpoint);
        
        try {
            Long currentCount = valueOps.increment(key);
            
            if (currentCount == 1) {
                redisTemplate.expire(key, Duration.ofSeconds(DEFAULT_WINDOW));
            }
            
            return currentCount <= DEFAULT_LIMIT;
        } catch (Exception e) {
            // If Redis fails, allow the request (fail-open for availability)
            org.slf4j.LoggerFactory.getLogger(RateLimitService.class)
                .warn("Rate limit check failed, allowing request: {}", e.getMessage());
            return true;
        }
    }
    
    public RateLimitInfo getRateLimitInfo(String clientId, String endpoint) {
        String key = String.format("ratelimit:%s:%s", clientId, endpoint);
        
        try {
            Object countObj = valueOps.get(key);
            Long ttl = redisTemplate.getExpire(key);
            
            int current = countObj != null ? Integer.parseInt(countObj.toString()) : 0;
            int remaining = Math.max(0, DEFAULT_LIMIT - current);
            long resetTime = ttl != null && ttl > 0 ? ttl : DEFAULT_WINDOW;
            
            return new RateLimitInfo(DEFAULT_LIMIT, remaining, resetTime);
        } catch (Exception e) {
            return new RateLimitInfo(DEFAULT_LIMIT, DEFAULT_LIMIT, DEFAULT_WINDOW);
        }
    }
    
    public record RateLimitInfo(int limit, int remaining, long resetAfterSeconds) {}
}

