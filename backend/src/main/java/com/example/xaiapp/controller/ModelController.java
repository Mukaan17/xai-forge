package com.example.xaiapp.controller;

import java.util.List;
import java.util.Map;
import com.example.xaiapp.dto.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import com.example.xaiapp.dto.ApiResponse;
import com.example.xaiapp.dto.ExplanationResponse;
import com.example.xaiapp.dto.PredictionResponse;
import com.example.xaiapp.dto.TrainRequestDto;
import com.example.xaiapp.entity.MLModel;
import com.example.xaiapp.entity.User;
import com.example.xaiapp.service.ModelService;
import com.example.xaiapp.service.XaiService;

@RestController
@RequestMapping("/api/models")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ModelController {
    
    private final ModelService modelService;
    private final XaiService xaiService;
    
    @PostMapping("/train")
    public ResponseEntity<?> trainModel(@Valid @RequestBody TrainRequestDto request,
                                      Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            MLModel model = modelService.trainModel(request, user.getId());
            return ResponseEntity.ok(ApiResponse.success("Model trained successfully", model));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Failed to train model: " + e.getMessage()));
        }
    }
    
    @GetMapping
    public ResponseEntity<List<MLModel>> getUserModels(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        List<MLModel> models = modelService.getUserModels(user.getId());
        return ResponseEntity.ok(models);
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
            PredictionResponse response = xaiService.predict(id, inputData, user.getId());
            return ResponseEntity.ok(response);
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
    public ResponseEntity<ApiResponse> deleteModel(@PathVariable Long id,
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
