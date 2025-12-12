/**
 * @Author: Mukhil Sundararaj
 * @Date:   2025-09-04 16:08:06
 * @Last Modified by:   Mukhil Sundararaj
 * @Last Modified time: 2025-10-24 15:18:28
 */
package com.example.xaiapp.controller;

import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import com.example.xaiapp.dto.ApiResponse;
import com.example.xaiapp.dto.ExplanationResponse;
import com.example.xaiapp.dto.PredictionResponse;
import com.example.xaiapp.dto.TrainRequestDto;
import com.example.xaiapp.entity.MLModel;
import com.example.xaiapp.entity.User;
import com.example.xaiapp.service.ModelService;
import com.example.xaiapp.service.XaiService;
import com.example.xaiapp.service.PredictionHistoryService;
import com.example.xaiapp.service.NotificationService;
import com.example.xaiapp.entity.Notification.NotificationType;
import com.example.xaiapp.exception.DatasetParsingException;
import com.example.xaiapp.exception.ModelTrainingException;
import java.util.HashMap;
import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("/api/models")
@CrossOrigin(origins = "*")
public class ModelController {
    
    private final ModelService modelService;
    private final XaiService xaiService;
    private final PredictionHistoryService predictionHistoryService;
    private final NotificationService notificationService;
    
    public ModelController(ModelService modelService, XaiService xaiService,
                          PredictionHistoryService predictionHistoryService,
                          NotificationService notificationService) {
        this.modelService = modelService;
        this.xaiService = xaiService;
        this.predictionHistoryService = predictionHistoryService;
        this.notificationService = notificationService;
    }
    
    @PostMapping("/train")
    public ResponseEntity<?> trainModel(@Valid @RequestBody TrainRequestDto request,
                                      Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            MLModel model = modelService.trainModel(request, user.getId());
            
            // Create notification for successful training
            notificationService.notifyModelTrained(
                user.getId(),
                model.getId(),
                model.getName() != null ? model.getName() : model.getModelName(),
                model.getAccuracy() != null ? model.getAccuracy() : 0.0
            );
            
            return ResponseEntity.ok(ApiResponse.success("Model trained successfully", model));
        } catch (DatasetParsingException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Invalid dataset: " + e.getMessage()));
        } catch (ModelTrainingException e) {
            // Create notification for training failure if we have model info
            try {
                User user = (User) authentication.getPrincipal();
                // Try to get model name from request
                notificationService.notifyModelFailed(
                    user.getId(),
                    null, // modelId unknown if training failed early
                    request.getModelName(),
                    e.getMessage()
                );
            } catch (Exception notificationError) {
                // Ignore notification errors
            }
            // Model already exists or other training issues should be 400 Bad Request, not 500
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Training failed: " + e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Invalid parameters: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(ApiResponse.error("Unexpected error: " + e.getMessage()));
        }
    }
    
    @GetMapping
    public ResponseEntity<List<MLModel>> getUserModels(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        List<MLModel> models = modelService.getUserModels(user.getId());
        return ResponseEntity.ok(models);
    }
    
    @GetMapping("/ready")
    @Operation(summary = "Get ready models for prediction")
    public ResponseEntity<List<MLModel>> getReadyModels(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        List<MLModel> readyModels = modelService.getReadyModels(user.getId());
        return ResponseEntity.ok(readyModels);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<?> getModel(@PathVariable Long id, Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            MLModel model = modelService.getModel(id, user.getId());
            return ResponseEntity.ok(model);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @PostMapping("/{id}/predict")
    public ResponseEntity<?> predict(@PathVariable Long id,
                                   @RequestBody Map<String, String> inputData,
                                   Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            long startTime = System.currentTimeMillis();
            
            // Make prediction
            PredictionResponse predictionResponse = xaiService.predict(id, inputData, user.getId());
            long predictionTime = System.currentTimeMillis() - startTime;
            
            // Generate explanation
            long explainStartTime = System.currentTimeMillis();
            ExplanationResponse explanationResponse = xaiService.explain(id, inputData, user.getId());
            long explanationTime = System.currentTimeMillis() - explainStartTime;
            
            // Convert explanation to Map for storage
            Map<String, Object> explanationMap = new HashMap<>();
            explanationMap.put("explanationText", explanationResponse.getExplanationText());
            explanationMap.put("featureImportances", explanationResponse.getFeatureContributions().stream()
                .map(fc -> {
                    Map<String, Object> fcMap = new HashMap<>();
                    fcMap.put("featureName", fc.getFeatureName());
                    fcMap.put("importance", fc.getContribution());
                    fcMap.put("direction", fc.getContribution() >= 0 ? "positive" : "negative");
                    return fcMap;
                })
                .collect(java.util.stream.Collectors.toList()));
            
            // Convert inputData to Map<String, Object>
            Map<String, Object> inputDataMap = new HashMap<>(inputData);
            
            // Save prediction to history
            predictionHistoryService.savePrediction(
                user.getId(),
                id,
                inputDataMap,
                predictionResponse.getPrediction(),
                predictionResponse.getConfidence(),
                explanationMap,
                predictionTime,
                explanationTime
            );
            
            // Create notification (optional - predictions are frequent, so we may skip notifications)
            // Uncomment if you want notifications for every prediction:
            // notificationService.createNotification(
            //     user.getId(),
            //     NotificationType.INFO,
            //     "Prediction Completed",
            //     String.format("Prediction: %s (%.1f%%)", predictionResponse.getPrediction(), predictionResponse.getConfidence() * 100),
            //     Map.of("modelId", id, "predictionResult", predictionResponse.getPrediction())
            // );
            
            // Combine prediction and explanation in response
            Map<String, Object> combinedResponse = new HashMap<>();
            combinedResponse.put("prediction", predictionResponse.getPrediction());
            combinedResponse.put("confidence", predictionResponse.getConfidence());
            combinedResponse.put("probabilities", predictionResponse.getProbabilities());
            combinedResponse.put("explanation", explanationMap);
            
            return ResponseEntity.ok(combinedResponse);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Failed to make prediction: " + e.getMessage()));
        }
    }
    
    @PostMapping("/{id}/explain")
    public ResponseEntity<?> explain(@PathVariable Long id,
                                   @RequestBody Map<String, String> inputData,
                                   Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            ExplanationResponse response = xaiService.explain(id, inputData, user.getId());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Failed to generate explanation: " + e.getMessage()));
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteModel(@PathVariable Long id,
                                                 Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            modelService.deleteModel(id, user.getId());
            return ResponseEntity.ok(ApiResponse.success("Model deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Failed to delete model: " + e.getMessage()));
        }
    }
}
