package com.xaiforge.api.v1.controller;

import com.xaiforge.domain.prediction.entity.PredictionRecord;
import com.xaiforge.domain.user.entity.User;
import com.xaiforge.infrastructure.persistence.prediction.PredictionRecordRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/predictions")
@Tag(name = "Prediction History", description = "Prediction history operations")
public class PredictionHistoryController {
    
    private final PredictionRecordRepository predictionRepository;
    
    public PredictionHistoryController(PredictionRecordRepository predictionRepository) {
        this.predictionRepository = predictionRepository;
    }
    
    @GetMapping("/history")
    @Operation(summary = "Get prediction history")
    public ResponseEntity<List<Map<String, Object>>> getHistory(
            @RequestParam(required = false) Integer days,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        
        List<PredictionRecord> records;
        if (days != null && days > 0) {
            LocalDateTime after = LocalDateTime.now().minusDays(days);
            records = predictionRepository.findByUserIdAndCreatedAtAfter(user.getId(), after);
        } else {
            Pageable pageable = PageRequest.of(0, 1000); // Get up to 1000 records
            Page<PredictionRecord> page = predictionRepository.findByUserIdOrderByCreatedAtDesc(user.getId(), pageable);
            records = page.getContent();
        }
        
        List<Map<String, Object>> result = records.stream().map(record -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", record.getId());
            map.put("modelId", record.getModel().getId());
            map.put("modelName", record.getModel().getModelName());
            
            // Parse input data JSON
            try {
                if (record.getInputData() != null) {
                    map.put("inputData", parseJson(record.getInputData()));
                } else {
                    map.put("inputData", new HashMap<>());
                }
            } catch (Exception e) {
                map.put("inputData", new HashMap<>());
            }
            
            // Parse prediction JSON
            try {
                if (record.getPrediction() != null) {
                    Object predObj = parseJson(record.getPrediction());
                    if (predObj instanceof Map) {
                        Map<?, ?> predMap = (Map<?, ?>) predObj;
                        map.put("prediction", predMap.get("value") != null ? predMap.get("value").toString() : record.getPrediction());
                    } else {
                        map.put("prediction", record.getPrediction());
                    }
                } else {
                    map.put("prediction", "");
                }
            } catch (Exception e) {
                map.put("prediction", record.getPrediction() != null ? record.getPrediction() : "");
            }
            
            map.put("confidence", record.getConfidence() != null ? record.getConfidence() : 0.0);
            
            // Parse explanation JSON
            try {
                if (record.getExplanation() != null) {
                    map.put("explanation", parseJson(record.getExplanation()).toString());
                } else {
                    map.put("explanation", null);
                }
            } catch (Exception e) {
                map.put("explanation", record.getExplanation());
            }
            
            map.put("createdAt", record.getCreatedAt().toString());
            return map;
        }).collect(Collectors.toList());
        
        return ResponseEntity.ok(result);
    }
    
    private Object parseJson(String json) {
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper().readValue(json, Object.class);
        } catch (Exception e) {
            return json;
        }
    }
}
