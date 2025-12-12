/**
 * @Author: Mukhil Sundararaj
 * @Date:   2025-09-04 16:05:50
 * @Last Modified by:   Mukhil Sundararaj
 * @Last Modified time: 2025-10-24 18:38:26
 */
package com.example.xaiapp.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.example.xaiapp.entity.Dataset;
import com.example.xaiapp.entity.MLModel;

@Repository
public interface MLModelRepository extends JpaRepository<MLModel, Long> {
    
    // Backward compatibility methods
    @Query("SELECT m FROM MLModel m JOIN FETCH m.dataset d WHERE d.user.id = :ownerId")
    List<MLModel> findByDatasetOwnerId(@Param("ownerId") Long ownerId);
    
    @Query("SELECT m FROM MLModel m JOIN FETCH m.dataset d WHERE m.id = :id AND d.user.id = :ownerId")
    Optional<MLModel> findByIdAndDatasetOwnerId(@Param("id") Long id, @Param("ownerId") Long ownerId);
    
    Optional<MLModel> findByDataset(Dataset dataset);
    
    List<MLModel> findByModelType(MLModel.ModelType modelType);
    
    // New methods for enhanced MLModel entity
    @Query("SELECT COUNT(m) FROM MLModel m WHERE m.user.id = :userId AND m.status != :status")
    long countByUserIdAndStatusNot(@Param("userId") Long userId, @Param("status") MLModel.ModelStatus status);
    
    @Query("SELECT COUNT(m) FROM MLModel m WHERE m.user.id = :userId AND m.status = :status")
    long countByUserIdAndStatus(@Param("userId") Long userId, @Param("status") MLModel.ModelStatus status);
    
    @Query("SELECT COUNT(m) FROM MLModel m WHERE m.user.id = :userId AND m.status IN :statuses")
    long countByUserIdAndStatusIn(@Param("userId") Long userId, @Param("statuses") List<MLModel.ModelStatus> statuses);
    
    @Query("SELECT COUNT(m) FROM MLModel m WHERE m.user.id = :userId AND m.modelType = :modelType")
    long countByUserIdAndModelType(@Param("userId") Long userId, @Param("modelType") MLModel.ModelType modelType);
    
    @Query("SELECT COUNT(m) FROM MLModel m WHERE m.user.id = :userId AND m.createdAt >= :after")
    long countByUserIdAndCreatedAtAfter(@Param("userId") Long userId, @Param("after") LocalDateTime after);
    
    @Query("SELECT m FROM MLModel m WHERE m.user.id = :userId AND m.status IN :statuses ORDER BY m.createdAt DESC")
    List<MLModel> findByUserIdAndStatusIn(@Param("userId") Long userId, @Param("statuses") List<MLModel.ModelStatus> statuses);
    
    @Query("SELECT m FROM MLModel m WHERE m.user.id = :userId ORDER BY m.createdAt DESC")
    Optional<MLModel> findTopByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId);
    
    @Query("SELECT m FROM MLModel m WHERE m.user.id = :userId ORDER BY m.createdAt DESC")
    Page<MLModel> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId, Pageable pageable);
    
    @Query("SELECT m FROM MLModel m WHERE m.user.id = :userId")
    List<MLModel> findByUserId(@Param("userId") Long userId);
    
    @Query("SELECT AVG(m.accuracy) FROM MLModel m WHERE m.user.id = :userId AND m.status = 'READY' AND m.accuracy IS NOT NULL")
    Double getAverageAccuracyByUserId(@Param("userId") Long userId);
    
    @Query("SELECT COALESCE(SUM(m.modelSizeBytes), 0) FROM MLModel m WHERE m.user.id = :userId")
    Long getTotalModelSizeByUserId(@Param("userId") Long userId);
    
    @Query("SELECT m FROM MLModel m WHERE m.user.id = :userId AND m.baseName = :baseName ORDER BY m.version DESC")
    List<MLModel> findByUserIdAndBaseNameOrderByVersionDesc(@Param("userId") Long userId, @Param("baseName") String baseName);
    
    @Query("SELECT m FROM MLModel m WHERE m.user.id = :userId AND m.baseName = :baseName ORDER BY m.version ASC")
    List<MLModel> findByUserIdAndBaseNameOrderByVersionAsc(@Param("userId") Long userId, @Param("baseName") String baseName);
    
    @Query("SELECT m FROM MLModel m WHERE m.id = :id AND m.user.id = :userId")
    Optional<MLModel> findByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);
}
