package com.xaiforge.application.service;

import com.xaiforge.domain.dataset.entity.Dataset;
import com.xaiforge.domain.model.entity.MLModel;
import com.xaiforge.domain.prediction.entity.PredictionRecord;
import com.xaiforge.infrastructure.persistence.dataset.DatasetRepository;
import com.xaiforge.infrastructure.persistence.model.MLModelRepository;
import com.xaiforge.infrastructure.persistence.prediction.PredictionRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service for global search across datasets, models, and predictions
 * 
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class GlobalSearchService {
    
    private final DatasetRepository datasetRepository;
    private final MLModelRepository modelRepository;
    private final PredictionRecordRepository predictionRepository;
    
    /**
     * Search across all user resources
     */
    public Map<String, Object> search(Long userId, String query, int limit) {
        if (query == null || query.trim().isEmpty()) {
            return createEmptyResults();
        }
        
        String searchTerm = query.trim().toLowerCase();
        Map<String, Object> results = new HashMap<>();
        
        // Search datasets
        List<Map<String, Object>> datasets = searchDatasets(userId, searchTerm, limit);
        results.put("datasets", datasets);
        
        // Search models
        List<Map<String, Object>> models = searchModels(userId, searchTerm, limit);
        results.put("models", models);
        
        // Search predictions
        List<Map<String, Object>> predictions = searchPredictions(userId, searchTerm, limit);
        results.put("predictions", predictions);
        
        // Total count
        int totalCount = datasets.size() + models.size() + predictions.size();
        results.put("totalCount", totalCount);
        results.put("query", query);
        
        return results;
    }
    
    /**
     * Search datasets by name
     */
    private List<Map<String, Object>> searchDatasets(Long userId, String searchTerm, int limit) {
        try {
            List<Dataset> datasets = datasetRepository.findByOwnerId(userId);
            
            return datasets.stream()
                .filter(dataset -> {
                    String fileName = dataset.getFileName() != null ? dataset.getFileName().toLowerCase() : "";
                    return fileName.contains(searchTerm);
                })
                .limit(limit)
                .map(dataset -> {
                    Map<String, Object> result = new HashMap<>();
                    result.put("id", dataset.getId());
                    result.put("name", dataset.getFileName());
                    result.put("type", "dataset");
                    result.put("url", "/datasets");
                    result.put("description", "Dataset with " + (dataset.getRowCount() != null ? dataset.getRowCount() : 0) + " rows");
                    return result;
                })
                .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error searching datasets: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Search models by name and type
     */
    private List<Map<String, Object>> searchModels(Long userId, String searchTerm, int limit) {
        try {
            List<MLModel> models = modelRepository.findByDatasetOwnerId(userId);
            
            return models.stream()
                .filter(model -> {
                    String modelName = model.getModelName() != null ? model.getModelName().toLowerCase() : "";
                    String modelType = model.getModelType() != null ? model.getModelType().name().toLowerCase() : "";
                    return modelName.contains(searchTerm) || modelType.contains(searchTerm);
                })
                .limit(limit)
                .map(model -> {
                    Map<String, Object> result = new HashMap<>();
                    result.put("id", model.getId());
                    result.put("name", model.getModelName());
                    result.put("type", "model");
                    result.put("url", "/models/" + model.getId());
                    result.put("description", model.getModelType().name() + " model" + 
                        (model.getAccuracy() != null ? " (" + String.format("%.1f%%", model.getAccuracy() * 100) + " accuracy)" : ""));
                    return result;
                })
                .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error searching models: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Search predictions by model name and prediction value
     */
    private List<Map<String, Object>> searchPredictions(Long userId, String searchTerm, int limit) {
        try {
            List<PredictionRecord> predictions = predictionRepository.findByUserIdOrderByCreatedAtDesc(
                userId, 
                org.springframework.data.domain.PageRequest.of(0, 1000)
            ).getContent();
            
            return predictions.stream()
                .filter(prediction -> {
                    String modelName = prediction.getModel().getModelName() != null ? 
                        prediction.getModel().getModelName().toLowerCase() : "";
                    String predictionValue = prediction.getPrediction() != null ? 
                        prediction.getPrediction().toLowerCase() : "";
                    return modelName.contains(searchTerm) || predictionValue.contains(searchTerm);
                })
                .limit(limit)
                .map(prediction -> {
                    Map<String, Object> result = new HashMap<>();
                    result.put("id", prediction.getId());
                    result.put("name", "Prediction from " + prediction.getModel().getModelName());
                    result.put("type", "prediction");
                    result.put("url", "/predictions/history");
                    result.put("description", "Prediction: " + 
                        (prediction.getPrediction() != null ? prediction.getPrediction() : "N/A"));
                    return result;
                })
                .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error searching predictions: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }
    
    private Map<String, Object> createEmptyResults() {
        Map<String, Object> results = new HashMap<>();
        results.put("datasets", new ArrayList<>());
        results.put("models", new ArrayList<>());
        results.put("predictions", new ArrayList<>());
        results.put("totalCount", 0);
        results.put("query", "");
        return results;
    }
}
