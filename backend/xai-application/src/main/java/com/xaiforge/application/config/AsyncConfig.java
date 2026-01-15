package com.xaiforge.application.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Async Configuration for ML Training Operations
 * 
 * This configuration provides thread pool executors for asynchronous
 * machine learning operations, ensuring non-blocking model training
 * and prediction generation.
 * 
 * @since 1.0.0
 */
@Configuration
@EnableAsync
public class AsyncConfig {
    
    @Value("${app.async.core-pool-size:5}")
    private int corePoolSize;
    
    @Value("${app.async.max-pool-size:20}")
    private int maxPoolSize;
    
    @Value("${app.async.queue-capacity:100}")
    private int queueCapacity;
    
    @Value("${app.async.thread-name-prefix:xai-async-}")
    private String threadNamePrefix;
    
    /**
     * ML Training Executor
     * 
     * Dedicated thread pool for machine learning model training operations.
     * Uses CallerRunsPolicy to prevent task rejection and ensure all
     * training requests are processed.
     */
    @Bean(name = "mlTrainingExecutor")
    public Executor mlTrainingExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix(threadNamePrefix + "training-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        return executor;
    }
}
