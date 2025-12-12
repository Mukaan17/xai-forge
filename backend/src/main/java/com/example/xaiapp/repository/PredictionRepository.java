package com.example.xaiapp.repository;

import com.example.xaiapp.entity.Prediction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Prediction entity operations.
 */
@Repository
public interface PredictionRepository extends JpaRepository<Prediction, Long> {

    /**
     * Find all predictions for a user, ordered by creation date.
     */
    @Query("SELECT p FROM Prediction p WHERE p.user.id = :userId ORDER BY p.createdAt DESC")
    Page<Prediction> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId, Pageable pageable);

    /**
     * Find all predictions for a specific model.
     */
    @Query("SELECT p FROM Prediction p WHERE p.model.id = :modelId ORDER BY p.createdAt DESC")
    Page<Prediction> findByModelIdOrderByCreatedAtDesc(@Param("modelId") Long modelId, Pageable pageable);

    /**
     * Find predictions for a user within a date range.
     */
    @Query("SELECT p FROM Prediction p WHERE p.user.id = :userId " +
           "AND p.createdAt BETWEEN :startDate AND :endDate " +
           "ORDER BY p.createdAt DESC")
    Page<Prediction> findByUserIdAndDateRange(
        @Param("userId") Long userId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        Pageable pageable
    );

    /**
     * Find predictions for a user filtered by model.
     */
    @Query("SELECT p FROM Prediction p WHERE p.user.id = :userId AND p.model.id = :modelId ORDER BY p.createdAt DESC")
    Page<Prediction> findByUserIdAndModelIdOrderByCreatedAtDesc(
        @Param("userId") Long userId, @Param("modelId") Long modelId, Pageable pageable
    );

    /**
     * Find a prediction by ID ensuring it belongs to the user.
     */
    @Query("SELECT p FROM Prediction p WHERE p.id = :id AND p.user.id = :userId")
    Optional<Prediction> findByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    /**
     * Count predictions for a user.
     */
    @Query("SELECT COUNT(p) FROM Prediction p WHERE p.user.id = :userId")
    long countByUserId(@Param("userId") Long userId);

    /**
     * Count predictions for a model.
     */
    @Query("SELECT COUNT(p) FROM Prediction p WHERE p.model.id = :modelId")
    long countByModelId(@Param("modelId") Long modelId);

    /**
     * Count predictions for a user in the last N days.
     */
    @Query("SELECT COUNT(p) FROM Prediction p WHERE p.user.id = :userId " +
           "AND p.createdAt >= :since")
    long countByUserIdSince(@Param("userId") Long userId, @Param("since") LocalDateTime since);

    /**
     * Delete all predictions for a model.
     */
    @Modifying
    @Query("DELETE FROM Prediction p WHERE p.model.id = :modelId")
    void deleteByModelId(@Param("modelId") Long modelId);

    /**
     * Delete predictions by IDs for a user.
     */
    @Modifying
    @Query("DELETE FROM Prediction p WHERE p.id IN :ids AND p.user.id = :userId")
    int deleteByIdInAndUserId(@Param("ids") List<Long> ids, @Param("userId") Long userId);

    /**
     * Get daily prediction counts for a user (for charts).
     */
    @Query(value = "SELECT DATE(p.created_at) as date, COUNT(p) as count " +
           "FROM predictions p WHERE p.user_id = :userId " +
           "AND p.created_at >= :since " +
           "GROUP BY DATE(p.created_at) " +
           "ORDER BY date", nativeQuery = true)
    List<Object[]> getDailyPredictionCounts(
        @Param("userId") Long userId, 
        @Param("since") LocalDateTime since
    );

    /**
     * Get average confidence by model for a user.
     */
    @Query("SELECT p.model.id, AVG(p.confidence) FROM Prediction p " +
           "WHERE p.user.id = :userId GROUP BY p.model.id")
    List<Object[]> getAverageConfidenceByModel(@Param("userId") Long userId);

    /**
     * Find predictions older than a certain date (for cleanup).
     */
    @Query("SELECT p FROM Prediction p WHERE p.user.id = :userId " +
           "AND p.createdAt < :before")
    List<Prediction> findOldPredictions(
        @Param("userId") Long userId, 
        @Param("before") LocalDateTime before
    );
}
