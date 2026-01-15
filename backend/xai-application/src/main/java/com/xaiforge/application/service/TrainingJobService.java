package com.xaiforge.application.service;

import com.xaiforge.common.dto.TrainingProgressDto;
import com.xaiforge.domain.model.entity.TrainingJob;
import com.xaiforge.infrastructure.persistence.model.TrainingJobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service for managing training jobs and progress tracking
 * 
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TrainingJobService {
    
    private final TrainingJobRepository trainingJobRepository;
    
    /**
     * Create a new training job
     */
    @Transactional
    public TrainingJob createJob(Long modelId, Long userId) {
        TrainingJob job = new TrainingJob();
        job.setModelId(modelId);
        job.setUserId(userId);
        job.setStatus(TrainingJob.JobStatus.PENDING);
        job.setProgress(0);
        job.setCurrentStep("Initializing training job...");
        
        TrainingJob saved = trainingJobRepository.save(job);
        log.info("Created training job {} for model {} (user {})", saved.getId(), modelId, userId);
        return saved;
    }
    
    /**
     * Update job progress
     */
    @Transactional
    public void updateProgress(Long jobId, int progress, String currentStep) {
        Optional<TrainingJob> jobOpt = trainingJobRepository.findById(jobId);
        if (jobOpt.isPresent()) {
            TrainingJob job = jobOpt.get();
            job.setProgress(Math.min(100, Math.max(0, progress)));
            job.setCurrentStep(currentStep);
            if (job.getStatus() == TrainingJob.JobStatus.PENDING) {
                job.setStatus(TrainingJob.JobStatus.RUNNING);
                job.setStartedAt(LocalDateTime.now());
            }
            
            // Estimate completion time
            if (job.getStartedAt() != null && progress > 0) {
                Duration elapsed = Duration.between(job.getStartedAt(), LocalDateTime.now());
                long estimatedTotalSeconds = (elapsed.getSeconds() * 100) / progress;
                long remainingSeconds = estimatedTotalSeconds - elapsed.getSeconds();
                job.setEstimatedCompletionSeconds(Math.max(0, remainingSeconds));
            }
            
            trainingJobRepository.save(job);
        }
    }
    
    /**
     * Mark job as completed
     */
    @Transactional
    public void completeJob(Long jobId) {
        Optional<TrainingJob> jobOpt = trainingJobRepository.findById(jobId);
        if (jobOpt.isPresent()) {
            TrainingJob job = jobOpt.get();
            job.setStatus(TrainingJob.JobStatus.COMPLETED);
            job.setProgress(100);
            job.setCurrentStep("Training completed successfully");
            job.setCompletedAt(LocalDateTime.now());
            trainingJobRepository.save(job);
            log.info("Training job {} completed", jobId);
        }
    }
    
    /**
     * Mark job as failed
     */
    @Transactional
    public void failJob(Long jobId, String errorMessage) {
        Optional<TrainingJob> jobOpt = trainingJobRepository.findById(jobId);
        if (jobOpt.isPresent()) {
            TrainingJob job = jobOpt.get();
            job.setStatus(TrainingJob.JobStatus.FAILED);
            job.setErrorMessage(errorMessage);
            job.setCompletedAt(LocalDateTime.now());
            trainingJobRepository.save(job);
            log.error("Training job {} failed: {}", jobId, errorMessage);
        }
    }
    
    /**
     * Cancel a job
     */
    @Transactional
    public void cancelJob(Long jobId, Long userId) {
        Optional<TrainingJob> jobOpt = trainingJobRepository.findByIdAndUserId(jobId, userId);
        if (jobOpt.isPresent()) {
            TrainingJob job = jobOpt.get();
            if (job.getStatus() == TrainingJob.JobStatus.PENDING || 
                job.getStatus() == TrainingJob.JobStatus.RUNNING) {
                job.setStatus(TrainingJob.JobStatus.CANCELLED);
                job.setCompletedAt(LocalDateTime.now());
                trainingJobRepository.save(job);
                log.info("Training job {} cancelled by user {}", jobId, userId);
            }
        }
    }
    
    /**
     * Get job by ID
     */
    @Transactional(readOnly = true)
    public Optional<TrainingJob> getJob(Long jobId) {
        return trainingJobRepository.findById(jobId);
    }
    
    /**
     * Get job by model ID
     */
    @Transactional(readOnly = true)
    public Optional<TrainingJob> getJobByModelId(Long modelId) {
        return trainingJobRepository.findByModelId(modelId);
    }
    
    /**
     * Get all jobs for a user
     */
    @Transactional(readOnly = true)
    public List<TrainingJob> getUserJobs(Long userId) {
        return trainingJobRepository.findByUserIdOrderByStartedAtDesc(userId);
    }
    
    /**
     * Get active jobs for a user
     */
    @Transactional(readOnly = true)
    public List<TrainingJob> getActiveJobs(Long userId) {
        return trainingJobRepository.findByUserIdAndStatus(userId, TrainingJob.JobStatus.RUNNING);
    }
    
    /**
     * Convert TrainingJob to DTO
     */
    public TrainingProgressDto toDto(TrainingJob job) {
        return new TrainingProgressDto(
            job.getId(),
            job.getModelId(),
            job.getStatus().name(),
            job.getProgress(),
            job.getCurrentStep(),
            job.getErrorMessage(),
            job.getStartedAt(),
            job.getCompletedAt(),
            job.getEstimatedCompletionSeconds()
        );
    }
}
