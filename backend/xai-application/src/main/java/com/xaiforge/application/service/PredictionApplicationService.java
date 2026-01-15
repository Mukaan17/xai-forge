package com.xaiforge.application.service;

import com.xaiforge.common.dto.BatchPredictionResult;
import com.xaiforge.common.dto.ExplanationResponse;
import com.xaiforge.common.dto.PredictionResponse;
import com.xaiforge.common.exception.ModelNotFoundException;
import com.xaiforge.domain.model.entity.MLModel;
import com.xaiforge.infrastructure.file.CsvParser;
import com.xaiforge.infrastructure.ml.XaiService;
import com.xaiforge.infrastructure.persistence.model.MLModelRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
@Slf4j
public class PredictionApplicationService {
    
    private final MLModelRepository modelRepository;
    private final XaiService xaiService;
    private final CsvParser csvParser;
    
    public PredictionApplicationService(
            MLModelRepository modelRepository, 
            XaiService xaiService,
            CsvParser csvParser) {
        this.modelRepository = modelRepository;
        this.xaiService = xaiService;
        this.csvParser = csvParser;
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
    
    /**
     * Process batch predictions from CSV file
     */
    public BatchPredictionResult batchPredict(
            Long modelId, 
            MultipartFile file, 
            Long userId,
            boolean includeExplanations) {
        
        // Verify model exists and belongs to user
        MLModel model = modelRepository.findByIdAndDatasetOwnerId(modelId, userId)
            .orElseThrow(() -> new ModelNotFoundException(modelId));
        
        log.info("Starting batch prediction for model {} with file {}", modelId, file.getOriginalFilename());
        
        List<BatchPredictionResult.PredictionRow> results = new ArrayList<>();
        List<BatchPredictionResult.ErrorRow> errors = new ArrayList<>();
        
        try {
            // Parse CSV file
            List<Map<String, String>> rows = csvParser.parseCsv(file.getInputStream());
            log.info("Parsed {} rows from CSV", rows.size());
            
            // Process each row
            int rowNumber = 1; // Start from 1 (header is row 0)
            for (Map<String, String> row : rows) {
                rowNumber++;
                
                try {
                    // Make prediction
                    PredictionResponse predictionResponse = xaiService.predict(modelId, row, userId);
                    
                    // Generate explanation if requested
                    ExplanationResponse explanation = null;
                    if (includeExplanations) {
                        try {
                            explanation = xaiService.explain(modelId, row, userId);
                        } catch (Exception e) {
                            log.warn("Failed to generate explanation for row {}: {}", rowNumber, e.getMessage());
                        }
                    }
                    
                    // Create result row
                    results.add(new BatchPredictionResult.PredictionRow(
                        rowNumber,
                        new LinkedHashMap<>(row),
                        predictionResponse.prediction(),
                        predictionResponse.confidence(),
                        predictionResponse.featureImportance(), // Use featureImportance as probabilities
                        explanation
                    ));
                    
                } catch (Exception e) {
                    log.warn("Failed to process row {}: {}", rowNumber, e.getMessage());
                    errors.add(new BatchPredictionResult.ErrorRow(
                        rowNumber,
                        new LinkedHashMap<>(row),
                        e.getMessage()
                    ));
                }
            }
            
            log.info("Batch prediction completed: {} successful, {} failed", results.size(), errors.size());
            
        } catch (IOException e) {
            log.error("Error parsing CSV file: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to parse CSV file: " + e.getMessage(), e);
        }
        
        return new BatchPredictionResult(
            (long) (results.size() + errors.size()),
            (long) results.size(),
            (long) errors.size(),
            results,
            errors
        );
    }
}

