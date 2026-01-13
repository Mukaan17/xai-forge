package com.xaiforge.application.service;

import com.xaiforge.common.dto.ExplanationResponse;
import com.xaiforge.common.dto.PredictionResponse;
import com.xaiforge.common.exception.ModelNotFoundException;
import com.xaiforge.domain.model.entity.MLModel;
import com.xaiforge.infrastructure.ml.XaiService;
import com.xaiforge.infrastructure.persistence.model.MLModelRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@Transactional(readOnly = true)
public class PredictionApplicationService {
    
    private final MLModelRepository modelRepository;
    private final XaiService xaiService;
    
    public PredictionApplicationService(MLModelRepository modelRepository, XaiService xaiService) {
        this.modelRepository = modelRepository;
        this.xaiService = xaiService;
    }
    
    public PredictionResponse predict(Long modelId, Map<String, String> inputData, Long userId) {
        // Verify model exists and belongs to user
        MLModel model = modelRepository.findByIdAndDatasetOwnerId(modelId, userId)
            .orElseThrow(() -> new ModelNotFoundException(modelId));
        
        return xaiService.predict(modelId, inputData, userId);
    }
    
    public ExplanationResponse explain(Long modelId, Map<String, String> inputData, Long userId) {
        // Verify model exists and belongs to user
        MLModel model = modelRepository.findByIdAndDatasetOwnerId(modelId, userId)
            .orElseThrow(() -> new ModelNotFoundException(modelId));
        
        return xaiService.explain(modelId, inputData, userId);
    }
}

