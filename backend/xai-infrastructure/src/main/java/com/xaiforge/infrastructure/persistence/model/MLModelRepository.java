package com.xaiforge.infrastructure.persistence.model;

import com.xaiforge.domain.dataset.entity.Dataset;
import com.xaiforge.domain.model.entity.MLModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MLModelRepository extends JpaRepository<MLModel, Long> {
    
    List<MLModel> findByDatasetOwnerId(Long ownerId);
    
    Page<MLModel> findByDatasetOwnerIdOrderByTrainingDateDesc(Long ownerId, Pageable pageable);
    
    Optional<MLModel> findByIdAndDatasetOwnerId(Long id, Long ownerId);
    
    Optional<MLModel> findByDataset(Dataset dataset);
    
    List<MLModel> findByModelType(MLModel.ModelType modelType);
    
    @Query("SELECT COUNT(m) FROM MLModel m WHERE m.dataset.owner.id = :ownerId")
    long countByOwnerId(Long ownerId);
    
    @Query("SELECT AVG(m.accuracy) FROM MLModel m WHERE m.dataset.owner.id = :ownerId AND m.accuracy IS NOT NULL")
    Double avgAccuracyByOwnerId(Long ownerId);
    
    @Query("SELECT m.modelType, COUNT(m) FROM MLModel m WHERE m.dataset.owner.id = :ownerId GROUP BY m.modelType")
    List<Object[]> countByTypeGrouped(Long ownerId);
}

