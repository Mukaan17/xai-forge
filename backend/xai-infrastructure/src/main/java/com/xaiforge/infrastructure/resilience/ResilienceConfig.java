package com.xaiforge.infrastructure.resilience;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.TransientDataAccessException;

import java.time.Duration;
import java.util.Map;

@Configuration
public class ResilienceConfig {
    
    @Bean
    public CircuitBreakerRegistry circuitBreakerRegistry() {
        CircuitBreakerConfig redisConfig = CircuitBreakerConfig.custom()
            .failureRateThreshold(50)
            .waitDurationInOpenState(Duration.ofSeconds(30))
            .permittedNumberOfCallsInHalfOpenState(3)
            .slidingWindowSize(10)
            .build();
            
        CircuitBreakerConfig mlEngineConfig = CircuitBreakerConfig.custom()
            .failureRateThreshold(30)
            .waitDurationInOpenState(Duration.ofMinutes(1))
            .permittedNumberOfCallsInHalfOpenState(2)
            .slidingWindowSize(5)
            .build();
            
        return CircuitBreakerRegistry.of(Map.of(
            "redis", redisConfig,
            "mlEngine", mlEngineConfig
        ));
    }
    
    @Bean
    public RetryRegistry retryRegistry() {
        RetryConfig defaultConfig = RetryConfig.custom()
            .maxAttempts(3)
            .waitDuration(Duration.ofMillis(500))
            .retryExceptions(TransientDataAccessException.class,
                             CannotAcquireLockException.class)
            .build();
            
        return RetryRegistry.of(defaultConfig);
    }
}

