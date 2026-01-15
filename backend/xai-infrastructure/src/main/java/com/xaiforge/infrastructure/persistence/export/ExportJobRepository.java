package com.xaiforge.infrastructure.persistence.export;

import com.xaiforge.domain.export.entity.ExportJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExportJobRepository extends JpaRepository<ExportJob, Long> {
    
    List<ExportJob> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    Optional<ExportJob> findByIdAndUserId(Long id, Long userId);
    
    List<ExportJob> findByUserIdAndStatus(Long userId, ExportJob.ExportStatus status);
    
    List<ExportJob> findByStatusAndExpiresAtBefore(ExportJob.ExportStatus status, java.time.LocalDateTime expiresAt);
}
