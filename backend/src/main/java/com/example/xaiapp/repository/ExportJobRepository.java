package com.example.xaiapp.repository;

import com.example.xaiapp.entity.ExportJob;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for ExportJob entity operations.
 */
@Repository
public interface ExportJobRepository extends JpaRepository<ExportJob, Long> {

    /**
     * Find all export jobs for a user.
     */
    @Query("SELECT e FROM ExportJob e WHERE e.user.id = :userId ORDER BY e.createdAt DESC")
    Page<ExportJob> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId, Pageable pageable);

    /**
     * Find export job by ID for a specific user.
     */
    @Query("SELECT e FROM ExportJob e WHERE e.id = :id AND e.user.id = :userId")
    Optional<ExportJob> findByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    /**
     * Find pending export jobs.
     */
    @Query("SELECT e FROM ExportJob e WHERE e.status = 'PENDING' ORDER BY e.createdAt ASC")
    List<ExportJob> findPendingJobs();

    /**
     * Find expired export jobs.
     */
    @Query("SELECT e FROM ExportJob e WHERE e.status = 'COMPLETED' AND e.expiresAt < :now")
    List<ExportJob> findExpiredJobs(@Param("now") LocalDateTime now);

    /**
     * Find export jobs by status for a user.
     */
    @Query("SELECT e FROM ExportJob e WHERE e.user.id = :userId AND e.status = :status ORDER BY e.createdAt DESC")
    List<ExportJob> findByUserIdAndStatus(
        @Param("userId") Long userId,
        @Param("status") ExportJob.ExportStatus status
    );
}
