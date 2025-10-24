package com.example.xaiapp.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.xaiapp.entity.Dataset;
import com.example.xaiapp.entity.MLModel;

@Repository
public interface MLModelRepository extends JpaRepository<MLModel, Long> {
    
    List<MLModel> findByDatasetOwnerId(Long ownerId);
    
    Optional<MLModel> findByIdAndDatasetOwnerId(Long id, Long ownerId);
    
    Optional<MLModel> findByDataset(Dataset dataset);
    
    List<MLModel> findByModelType(MLModel.ModelType modelType);
}
