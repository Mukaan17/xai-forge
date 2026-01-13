package com.xaiforge.api.v1.controller;

import com.xaiforge.application.service.ModelApplicationService;
import com.xaiforge.application.service.PredictionApplicationService;
import com.xaiforge.common.dto.ExplanationResponse;
import com.xaiforge.common.dto.ModelDto;
import com.xaiforge.common.dto.PredictionResponse;
import com.xaiforge.common.dto.TrainRequest;
import com.xaiforge.domain.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/models")
@Tag(name = "Models", description = "ML Model operations")
public class ModelController {
    
    private final ModelApplicationService modelService;
    private final PredictionApplicationService predictionService;
    
    public ModelController(ModelApplicationService modelService, PredictionApplicationService predictionService) {
        this.modelService = modelService;
        this.predictionService = predictionService;
    }
    
    @PostMapping("/train")
    @Operation(summary = "Train a new model")
    public ResponseEntity<Map<String, Object>> trainModel(
            @Valid @RequestBody TrainRequest request,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        Long modelId = modelService.trainModel(request, user.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(Map.of("id", modelId, "message", "Model training started"));
    }
    
    @GetMapping
    @Operation(summary = "List user models")
    public ResponseEntity<List<ModelDto>> getUserModels(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        List<ModelDto> models = modelService.getUserModels(user.getId());
        return ResponseEntity.ok(models);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get model by ID")
    public ResponseEntity<ModelDto> getModel(
            @PathVariable Long id,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        ModelDto model = modelService.getModel(id, user.getId());
        return ResponseEntity.ok(model);
    }
    
    @GetMapping("/{id}/metrics")
    @Operation(summary = "Get extended model metrics")
    public ResponseEntity<?> getModelMetrics(
            @PathVariable Long id,
            Authentication authentication) {
        // TODO: Implement extended metrics
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/{id}/predict")
    @Operation(summary = "Make a prediction")
    public ResponseEntity<PredictionResponse> predict(
            @PathVariable Long id,
            @RequestBody Map<String, String> inputData,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        PredictionResponse response = predictionService.predict(id, inputData, user.getId());
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/{id}/explain")
    @Operation(summary = "Get XAI explanation")
    public ResponseEntity<ExplanationResponse> explain(
            @PathVariable Long id,
            @RequestBody Map<String, String> inputData,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        ExplanationResponse response = predictionService.explain(id, inputData, user.getId());
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete model")
    public ResponseEntity<Void> deleteModel(
            @PathVariable Long id,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        modelService.deleteModel(id, user.getId());
        return ResponseEntity.noContent().build();
    }
}

