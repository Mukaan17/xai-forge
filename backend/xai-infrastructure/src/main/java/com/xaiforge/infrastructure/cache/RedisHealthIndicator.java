package com.xaiforge.infrastructure.cache;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.stereotype.Component;

@Component
public class RedisHealthIndicator implements HealthIndicator {
    
    private final RedisConnectionFactory connectionFactory;
    
    public RedisHealthIndicator(RedisConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }
    
    @Override
    public Health health() {
        try {
            long startTime = System.currentTimeMillis();
            connectionFactory.getConnection().ping();
            long responseTime = System.currentTimeMillis() - startTime;
            
            return Health.up()
                .withDetail("responseTime", responseTime + "ms")
                .withDetail("status", "Connected")
                .build();
        } catch (Exception e) {
            return Health.down()
                .withException(e)
                .withDetail("status", "Disconnected")
                .build();
        }
    }
}

