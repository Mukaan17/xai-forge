package com.xaiforge.infrastructure.persistence.prediction;

import com.xaiforge.domain.prediction.entity.PredictionRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PredictionRecordRepository extends JpaRepository<PredictionRecord, Long> {
    
    Page<PredictionRecord> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    
    List<PredictionRecord> findByUserIdAndCreatedAtAfter(Long userId, LocalDateTime after);
    
    List<PredictionRecord> findByUserIdAndModelIdOrderByCreatedAtDesc(Long userId, Long modelId);
    
    List<PredictionRecord> findByUserIdAndModelIdAndCreatedAtBetweenOrderByCreatedAtDesc(
        Long userId, Long modelId, LocalDateTime startDate, LocalDateTime endDate);
    
    Page<PredictionRecord> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    
    @Query("SELECT COUNT(p) FROM PredictionRecord p WHERE p.user.id = :userId")
    long countByUserId(Long userId);
    
    @Query("SELECT DATE(p.createdAt), COUNT(p) FROM PredictionRecord p " +
           "WHERE p.user.id = :userId AND p.createdAt >= :startDate " +
           "GROUP BY DATE(p.createdAt) ORDER BY DATE(p.createdAt) DESC")
    List<Object[]> countByDayLastWeek(Long userId, LocalDateTime startDate);
}

