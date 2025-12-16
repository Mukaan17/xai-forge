package com.example.xaiapp.config;

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
    
    /**
     * ML Prediction Executor
     * 
     * Thread pool for model prediction operations. Optimized for
     * quick response times and high throughput.
     */
    @Bean(name = "mlPredictionExecutor")
    public Executor mlPredictionExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize * 2); // More threads for predictions
        executor.setMaxPoolSize(maxPoolSize * 2);
        executor.setQueueCapacity(queueCapacity * 2);
        executor.setThreadNamePrefix(threadNamePrefix + "prediction-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();
        return executor;
    }
    
    /**
     * File Processing Executor
     * 
     * Thread pool for file upload and processing operations.
     * Handles CSV parsing, validation, and metadata extraction.
     */
    @Bean(name = "fileProcessingExecutor")
    public Executor fileProcessingExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(3);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix(threadNamePrefix + "file-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(45);
        executor.initialize();
        return executor;
    }
}
