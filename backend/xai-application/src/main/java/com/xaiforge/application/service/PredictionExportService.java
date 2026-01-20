package com.xaiforge.application.service;

import com.xaiforge.domain.prediction.entity.PredictionRecord;
import com.xaiforge.infrastructure.file.CsvParser;
import com.xaiforge.infrastructure.persistence.prediction.PredictionRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for exporting predictions and generating analytics
 * 
 * @since 1.0.0
 */
@Service
@Slf4j
@Transactional(readOnly = true)
public class PredictionExportService {
    
    private final PredictionRecordRepository predictionRepository;
    private final CsvParser csvParser;
    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper;
    
    public PredictionExportService(
            PredictionRecordRepository predictionRepository,
            CsvParser csvParser,
            com.fasterxml.jackson.databind.ObjectMapper objectMapper) {
        this.predictionRepository = predictionRepository;
        this.csvParser = csvParser;
        this.objectMapper = objectMapper != null ? objectMapper : new com.fasterxml.jackson.databind.ObjectMapper();
    }
    
    /**
     * Export predictions to CSV format
     */
    public String exportToCsv(Long userId, Long modelId, LocalDateTime startDate, LocalDateTime endDate) {
        List<PredictionRecord> predictions = getPredictions(userId, modelId, startDate, endDate);
        
        // Build CSV headers
        List<String> headers = new ArrayList<>();
        headers.add("id");
        headers.add("model_id");
        headers.add("model_name");
        headers.add("prediction");
        headers.add("confidence");
        headers.add("created_at");
        
            // Extract unique input feature names from all predictions
        Set<String> inputFeatures = new LinkedHashSet<>();
        for (PredictionRecord record : predictions) {
            try {
                if (record.getInputData() != null) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> inputData = (Map<String, Object>) parseJson(record.getInputData(), Map.class);
                    if (inputData != null) {
                        inputFeatures.addAll(inputData.keySet());
                    }
                }
            } catch (Exception e) {
                log.debug("Could not parse input data for record {}: {}", record.getId(), e.getMessage());
            }
        }
        headers.addAll(inputFeatures);
        
        // Build CSV rows
        List<Map<String, String>> rows = new ArrayList<>();
        for (PredictionRecord record : predictions) {
            Map<String, String> row = new LinkedHashMap<>();
            row.put("id", String.valueOf(record.getId()));
            row.put("model_id", String.valueOf(record.getModel().getId()));
            row.put("model_name", record.getModel().getModelName());
            
            // Parse prediction
            String predictionValue = extractPredictionValue(record.getPrediction());
            row.put("prediction", predictionValue);
            row.put("confidence", record.getConfidence() != null ? String.valueOf(record.getConfidence()) : "");
            row.put("created_at", record.getCreatedAt() != null ? record.getCreatedAt().toString() : "");
            
            // Add input features
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> inputData = (Map<String, Object>) parseJson(record.getInputData(), Map.class);
                if (inputData != null) {
                    for (String feature : inputFeatures) {
                        Object value = inputData.get(feature);
                        row.put(feature, value != null ? String.valueOf(value) : "");
                    }
                } else {
                    for (String feature : inputFeatures) {
                        row.put(feature, "");
                    }
                }
            } catch (Exception e) {
                for (String feature : inputFeatures) {
                    row.put(feature, "");
                }
            }
            
            rows.add(row);
        }
        
        return csvParser.writeCsv(headers, rows);
    }
    
    /**
     * Export predictions to JSON format
     */
    public List<Map<String, Object>> exportToJson(Long userId, Long modelId, LocalDateTime startDate, LocalDateTime endDate) {
        List<PredictionRecord> predictions = getPredictions(userId, modelId, startDate, endDate);
        
        return predictions.stream().map(record -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", record.getId());
            map.put("modelId", record.getModel().getId());
            map.put("modelName", record.getModel().getModelName());
            map.put("prediction", extractPredictionValue(record.getPrediction()));
            map.put("confidence", record.getConfidence());
            map.put("createdAt", record.getCreatedAt());
            
            // Parse input data
            try {
                map.put("inputData", parseJson(record.getInputData(), Map.class));
            } catch (Exception e) {
                map.put("inputData", new HashMap<>());
            }
            
            // Parse explanation
            try {
                map.put("explanation", parseJson(record.getExplanation(), Map.class));
            } catch (Exception e) {
                map.put("explanation", null);
            }
            
            return map;
        }).collect(Collectors.toList());
    }
    
    /**
     * Get prediction analytics
     */
    public Map<String, Object> getAnalytics(Long userId, Long modelId, LocalDateTime startDate, LocalDateTime endDate) {
        List<PredictionRecord> predictions = getPredictions(userId, modelId, startDate, endDate);
        
        Map<String, Object> analytics = new LinkedHashMap<>();
        analytics.put("totalPredictions", predictions.size());
        
        if (predictions.isEmpty()) {
            return analytics;
        }
        
        // Model distribution
        Map<String, Long> modelDistribution = predictions.stream()
            .collect(Collectors.groupingBy(
                p -> p.getModel().getModelName(),
                Collectors.counting()
            ));
        analytics.put("modelDistribution", modelDistribution);
        
        // Average confidence
        OptionalDouble avgConfidence = predictions.stream()
            .filter(p -> p.getConfidence() != null)
            .mapToDouble(PredictionRecord::getConfidence)
            .average();
        analytics.put("averageConfidence", avgConfidence.isPresent() ? avgConfidence.getAsDouble() : 0.0);
        
        // Confidence distribution
        Map<String, Long> confidenceRanges = new LinkedHashMap<>();
        confidenceRanges.put("high (>=0.8)", predictions.stream()
            .filter(p -> p.getConfidence() != null && p.getConfidence() >= 0.8)
            .count());
        confidenceRanges.put("medium (0.5-0.8)", predictions.stream()
            .filter(p -> p.getConfidence() != null && p.getConfidence() >= 0.5 && p.getConfidence() < 0.8)
            .count());
        confidenceRanges.put("low (<0.5)", predictions.stream()
            .filter(p -> p.getConfidence() != null && p.getConfidence() < 0.5)
            .count());
        analytics.put("confidenceDistribution", confidenceRanges);
        
        // Predictions over time (daily)
        Map<String, Long> predictionsByDay = predictions.stream()
            .collect(Collectors.groupingBy(
                p -> p.getCreatedAt().toLocalDate().toString(),
                Collectors.counting()
            ));
        analytics.put("predictionsByDay", predictionsByDay);
        
        // Most common predictions
        Map<String, Long> predictionCounts = predictions.stream()
            .collect(Collectors.groupingBy(
                p -> extractPredictionValue(p.getPrediction()),
                Collectors.counting()
            ));
        analytics.put("predictionCounts", predictionCounts);
        
        // Date range
        LocalDateTime earliest = predictions.stream()
            .map(PredictionRecord::getCreatedAt)
            .min(LocalDateTime::compareTo)
            .orElse(null);
        LocalDateTime latest = predictions.stream()
            .map(PredictionRecord::getCreatedAt)
            .max(LocalDateTime::compareTo)
            .orElse(null);
        
        Map<String, String> dateRange = new LinkedHashMap<>();
        if (earliest != null) {
            dateRange.put("earliest", earliest.toString());
        }
        if (latest != null) {
            dateRange.put("latest", latest.toString());
        }
        analytics.put("dateRange", dateRange);
        
        return analytics;
    }
    
    /**
     * Get predictions with filters
     */
    private List<PredictionRecord> getPredictions(Long userId, Long modelId, LocalDateTime startDate, LocalDateTime endDate) {
        List<PredictionRecord> predictions;
        
        if (modelId != null) {
            // Filter by model
            if (startDate != null && endDate != null) {
                predictions = predictionRepository.findByUserIdAndModelIdAndCreatedAtBetweenOrderByCreatedAtDesc(
                    userId, modelId, startDate, endDate);
            } else {
                predictions = predictionRepository.findByUserIdAndModelIdOrderByCreatedAtDesc(userId, modelId);
                // Apply date filters manually if needed
                if (startDate != null || endDate != null) {
                    predictions = predictions.stream()
                        .filter(p -> startDate == null || !p.getCreatedAt().isBefore(startDate))
                        .filter(p -> endDate == null || !p.getCreatedAt().isAfter(endDate))
                        .collect(Collectors.toList());
                }
            }
        } else {
            // All user predictions
            if (startDate != null) {
                predictions = predictionRepository.findByUserIdAndCreatedAtAfter(userId, startDate);
                if (endDate != null) {
                    predictions = predictions.stream()
                        .filter(p -> !p.getCreatedAt().isAfter(endDate))
                        .collect(Collectors.toList());
                }
            } else {
                Pageable pageable = Pageable.unpaged();
                Page<PredictionRecord> page = predictionRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
                predictions = page.getContent();
                if (endDate != null) {
                    predictions = predictions.stream()
                        .filter(p -> !p.getCreatedAt().isAfter(endDate))
                        .collect(Collectors.toList());
                }
            }
        }
        
        return predictions;
    }
    
    /**
     * Extract prediction value from JSON string
     */
    private String extractPredictionValue(String predictionJson) {
        if (predictionJson == null || predictionJson.isEmpty()) {
            return "";
        }
        
        try {
            Object obj = parseJson(predictionJson, Object.class);
            if (obj instanceof Map) {
                Map<?, ?> map = (Map<?, ?>) obj;
                Object value = map.get("value");
                if (value != null) {
                    return value.toString();
                }
                Object prediction = map.get("prediction");
                if (prediction != null) {
                    return prediction.toString();
                }
            }
            return predictionJson;
        } catch (Exception e) {
            return predictionJson;
        }
    }
    
    /**
     * Parse JSON string
     */
    @SuppressWarnings("unchecked")
    private <T> T parseJson(String json, Class<T> clazz) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        
        try {
            if (clazz == Map.class) {
                return (T) objectMapper.readValue(json, Map.class);
            } else if (clazz == List.class) {
                return (T) objectMapper.readValue(json, List.class);
            } else {
                return objectMapper.readValue(json, clazz);
            }
        } catch (Exception e) {
            log.debug("Could not parse JSON: {}", e.getMessage());
            return null;
        }
    }
}
