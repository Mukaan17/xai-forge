package com.xaiforge.infrastructure.persistence.model;

import com.xaiforge.domain.model.entity.TrainingJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for TrainingJob entities
 * 
 * @since 1.0.0
 */
@Repository
public interface TrainingJobRepository extends JpaRepository<TrainingJob, Long> {
    
    Optional<TrainingJob> findByModelId(Long modelId);
    
    List<TrainingJob> findByUserIdOrderByStartedAtDesc(Long userId);
    
    List<TrainingJob> findByUserIdAndStatus(Long userId, TrainingJob.JobStatus status);
    
    Optional<TrainingJob> findByIdAndUserId(Long id, Long userId);
}
