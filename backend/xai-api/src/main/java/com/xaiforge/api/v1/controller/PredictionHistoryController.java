package com.xaiforge.api.v1.controller;

import com.xaiforge.application.service.PredictionExportService;
import com.xaiforge.common.annotation.LogActivity;
import com.xaiforge.common.dto.PaginatedResponse;
import com.xaiforge.domain.prediction.entity.PredictionRecord;
import com.xaiforge.domain.user.entity.User;
import com.xaiforge.infrastructure.persistence.prediction.PredictionRecordRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
@Slf4j
public class PredictionHistoryController {
    
    private final PredictionRecordRepository predictionRepository;
    private final PredictionExportService exportService;
    
    public PredictionHistoryController(
            PredictionRecordRepository predictionRepository,
            PredictionExportService exportService) {
        this.predictionRepository = predictionRepository;
        this.exportService = exportService;
    }
    
    @GetMapping("/history")
    @Operation(summary = "Get prediction history")
    public ResponseEntity<?> getHistory(
            @RequestParam(required = false) Integer days,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        
        Page<PredictionRecord> recordPage;
        if (days != null && days > 0) {
            LocalDateTime after = LocalDateTime.now().minusDays(days);
            // For date-filtered queries, we still need pagination
            Pageable pageable = PageRequest.of(page, size);
            // Note: This requires a new repository method for date-filtered pagination
            // For now, get all and paginate manually (not ideal for large datasets)
            List<PredictionRecord> allRecords = predictionRepository.findByUserIdAndCreatedAtAfter(user.getId(), after);
            int start = page * size;
            int end = Math.min(start + size, allRecords.size());
            List<PredictionRecord> paginatedRecords = allRecords.subList(Math.min(start, allRecords.size()), end);
            
            // Create a manual page
            recordPage = new org.springframework.data.domain.PageImpl<>(
                paginatedRecords,
                pageable,
                allRecords.size()
            );
        } else {
            Pageable pageable = PageRequest.of(page, size);
            recordPage = predictionRepository.findByUserIdOrderByCreatedAtDesc(user.getId(), pageable);
        }
        
        List<Map<String, Object>> content = recordPage.getContent().stream().map(record -> {
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
        
        // Return paginated response if pagination params provided, otherwise return list for backward compatibility
        if (page > 0 || size != 20) {
            PaginatedResponse<Map<String, Object>> response = PaginatedResponse.of(
                content,
                page,
                size,
                recordPage.getTotalElements()
            );
            return ResponseEntity.ok(response);
        }
        
        return ResponseEntity.ok(content);
    }
    
    private Object parseJson(String json) {
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper().readValue(json, Object.class);
        } catch (Exception e) {
            return json;
        }
    }
    
    @GetMapping("/export/csv")
    @Operation(summary = "Export predictions to CSV")
    @LogActivity(
        eventType = "PREDICTION_EXPORTED",
        description = "Predictions exported to CSV",
        resourceType = "PREDICTION"
    )
    public ResponseEntity<String> exportToCsv(
            @RequestParam(required = false) Long modelId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        
        try {
            String csvContent = exportService.exportToCsv(user.getId(), modelId, startDate, endDate);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.TEXT_PLAIN);
            headers.setContentDispositionFormData("attachment", "predictions_export.csv");
            
            return ResponseEntity.ok()
                .headers(headers)
                .body(csvContent);
        } catch (Exception e) {
            log.error("Error exporting predictions to CSV: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error exporting predictions: " + e.getMessage());
        }
    }
    
    @GetMapping("/export/json")
    @Operation(summary = "Export predictions to JSON")
    @LogActivity(
        eventType = "PREDICTION_EXPORTED",
        description = "Predictions exported to JSON",
        resourceType = "PREDICTION"
    )
    public ResponseEntity<List<Map<String, Object>>> exportToJson(
            @RequestParam(required = false) Long modelId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        
        try {
            List<Map<String, Object>> data = exportService.exportToJson(user.getId(), modelId, startDate, endDate);
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            log.error("Error exporting predictions to JSON: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/analytics")
    @Operation(summary = "Get prediction analytics")
    public ResponseEntity<Map<String, Object>> getAnalytics(
            @RequestParam(required = false) Long modelId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        
        try {
            Map<String, Object> analytics = exportService.getAnalytics(user.getId(), modelId, startDate, endDate);
            return ResponseEntity.ok(analytics);
        } catch (Exception e) {
            log.error("Error getting prediction analytics: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
