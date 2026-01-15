package com.xaiforge.api.v1.controller;

import com.xaiforge.application.service.ModelApplicationService;
import com.xaiforge.application.service.PredictionApplicationService;
import com.xaiforge.common.annotation.LogActivity;
import com.xaiforge.common.dto.ExplanationResponse;
import com.xaiforge.common.dto.BatchPredictionResult;
import com.xaiforge.common.dto.ExtendedMetricsDto;
import com.xaiforge.common.dto.ModelDto;
import com.xaiforge.common.dto.PaginatedResponse;
import com.xaiforge.common.dto.PredictionResponse;
import com.xaiforge.common.dto.TrainRequest;
import com.xaiforge.common.dto.TrainingProgressDto;
import com.xaiforge.domain.model.entity.TrainingJob;
import com.xaiforge.domain.user.entity.User;
import com.xaiforge.application.service.TrainingJobService;
import com.xaiforge.infrastructure.file.CsvParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
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
@Slf4j
public class ModelController {
    
    private final ModelApplicationService modelService;
    private final PredictionApplicationService predictionService;
    private final TrainingJobService trainingJobService;
    private final CsvParser csvParser;
    
    public ModelController(
            ModelApplicationService modelService, 
            PredictionApplicationService predictionService,
            TrainingJobService trainingJobService,
            CsvParser csvParser) {
        this.modelService = modelService;
        this.predictionService = predictionService;
        this.trainingJobService = trainingJobService;
        this.csvParser = csvParser;
    }
    
    @PostMapping("/train")
    @Operation(
        summary = "Train a new ML model (asynchronous)",
        description = """
            Start training a machine learning model using a dataset. Training is performed asynchronously,
            so this endpoint returns immediately with a job ID that can be used to track progress.
            
            **Training Process:**
            1. Dataset is loaded and validated
            2. Features are selected and preprocessed
            3. Data is split into training and testing sets
            4. Model is trained using the selected algorithm
            5. Model is evaluated on test data
            6. Metrics are calculated and stored
            
            **Supported Algorithms:**
            - **Classification:** LOGISTIC_REGRESSION, RANDOM_FOREST_CLASSIFICATION, NEURAL_NETWORK, SVM
            - **Regression:** LINEAR_REGRESSION, RANDOM_FOREST_REGRESSION
            
            **Hyperparameters:**
            Algorithm-specific hyperparameters can be provided:
            - Random Forest: `numTrees`, `maxDepth`
            - Neural Network: `epochs`, `learningRate`, `batchSize`
            - SVM: `epochs`, `learningRate`
            
            **Progress Tracking:**
            Use the returned `jobId` with `/api/v1/models/training/{jobId}/progress` to track training progress.
            """
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201",
            description = "Training job started successfully",
            content = @io.swagger.v3.oas.annotations.media.Content(
                mediaType = "application/json",
                examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                    value = """
                        {
                          "id": 1,
                          "jobId": 42,
                          "message": "Training started"
                        }
                        """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Invalid request (dataset not found, invalid algorithm, etc.)"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Dataset not found"
        )
    })
    @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")
    @LogActivity(
        eventType = "MODEL_TRAINED",
        description = "Model training started: #{#request.modelName}",
        resourceType = "MODEL",
        resourceId = "#{#result.id}",
        resourceName = "#{#request.modelName}"
    )
    public ResponseEntity<Map<String, Object>> trainModel(
            @Valid @RequestBody TrainRequest request,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        Long modelId = modelService.startTraining(request, user.getId());
        
        // Get job ID for progress tracking
        var jobOpt = trainingJobService.getJobByModelId(modelId);
        Long jobId = jobOpt.map(j -> j.getId()).orElse(null);
        
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(Map.of(
                "id", modelId,
                "jobId", jobId != null ? jobId : -1,
                "message", "Model training started"
            ));
    }
    
    @GetMapping("/{id}/progress")
    @Operation(summary = "Get training progress for a model")
    public ResponseEntity<TrainingProgressDto> getTrainingProgress(
            @PathVariable Long id,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        var jobOpt = trainingJobService.getJobByModelId(id);
        
        if (jobOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        TrainingJob job = jobOpt.get();
        if (!job.getUserId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        return ResponseEntity.ok(trainingJobService.toDto(job));
    }
    
    @GetMapping("/training/jobs")
    @Operation(summary = "Get all training jobs for the current user")
    public ResponseEntity<List<TrainingProgressDto>> getTrainingJobs(
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        List<TrainingJob> jobs = trainingJobService.getUserJobs(user.getId());
        List<TrainingProgressDto> dtos = jobs.stream()
            .map(trainingJobService::toDto)
            .toList();
        return ResponseEntity.ok(dtos);
    }
    
    @PostMapping("/training/{jobId}/cancel")
    @Operation(summary = "Cancel a training job")
    public ResponseEntity<Map<String, String>> cancelTraining(
            @PathVariable Long jobId,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        trainingJobService.cancelJob(jobId, user.getId());
        return ResponseEntity.ok(Map.of("message", "Training job cancelled"));
    }
    
    @GetMapping
    @Operation(summary = "List user models")
    public ResponseEntity<?> getUserModels(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        
        // If no pagination params, return list for backward compatibility
        if (page == 0 && size == 20) {
            List<ModelDto> models = modelService.getUserModels(user.getId());
            return ResponseEntity.ok(models);
        }
        
        PaginatedResponse<ModelDto> response = modelService.getUserModels(user.getId(), page, size);
        return ResponseEntity.ok(response);
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
    public ResponseEntity<ExtendedMetricsDto> getModelMetrics(
            @PathVariable Long id,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        ExtendedMetricsDto metrics = modelService.getExtendedMetrics(id, user.getId());
        return ResponseEntity.ok(metrics);
    }
    
    @PostMapping("/{id}/predict")
    @Operation(summary = "Make a prediction")
    @LogActivity(
        eventType = "PREDICTION_MADE",
        description = "Prediction made with model: #{#id}",
        resourceType = "PREDICTION",
        resourceId = "#{#id}"
    )
    public ResponseEntity<PredictionResponse> predict(
            @PathVariable Long id,
            @RequestBody Map<String, String> inputData,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        PredictionResponse response = predictionService.predict(id, inputData, user.getId());
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/{id}/predict/batch")
    @Operation(summary = "Make batch predictions from CSV file")
    @LogActivity(
        eventType = "BATCH_PREDICTION_MADE",
        description = "Batch prediction made with model: #{#id}",
        resourceType = "PREDICTION",
        resourceId = "#{#id}"
    )
    public ResponseEntity<?> batchPredict(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "includeExplanations", defaultValue = "false") boolean includeExplanations,
            @RequestParam(value = "returnCsv", defaultValue = "false") boolean returnCsv,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        
        // Validate file
        if (file.isEmpty()) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "File is empty"));
        }
        
        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase().endsWith(".csv")) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Only CSV files are supported"));
        }
        
        try {
            BatchPredictionResult result = predictionService.batchPredict(
                id, file, user.getId(), includeExplanations);
            
            // Return CSV if requested
            if (returnCsv) {
                List<String> headers = new ArrayList<>();
                headers.add("row_number");
                
                // Add input columns from first successful row
                if (!result.results().isEmpty()) {
                    headers.addAll(result.results().get(0).inputData().keySet());
                }
                
                headers.add("prediction");
                headers.add("confidence");
                
                if (includeExplanations) {
                    headers.add("explanation");
                }
                
                // Build CSV rows
                List<Map<String, String>> csvRows = new ArrayList<>();
                for (BatchPredictionResult.PredictionRow row : result.results()) {
                    Map<String, String> csvRow = new LinkedHashMap<>();
                    csvRow.put("row_number", String.valueOf(row.rowNumber()));
                    csvRow.putAll(row.inputData());
                    csvRow.put("prediction", row.prediction());
                    csvRow.put("confidence", String.valueOf(row.confidence()));
                    if (includeExplanations && row.explanation() != null) {
                        csvRow.put("explanation", row.explanation().summary());
                    }
                    csvRows.add(csvRow);
                }
                
                String csvContent = csvParser.writeCsv(headers, csvRows);
                
                HttpHeaders headers2 = new HttpHeaders();
                headers2.setContentType(MediaType.TEXT_PLAIN);
                headers2.setContentDispositionFormData("attachment", "batch_predictions.csv");
                
                return ResponseEntity.ok()
                    .headers(headers2)
                    .body(csvContent);
            }
            
            // Return JSON
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("Error processing batch prediction: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to process batch prediction: " + e.getMessage()));
        }
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
    @LogActivity(
        eventType = "MODEL_DELETED",
        description = "Model deleted: #{#id}",
        resourceType = "MODEL",
        resourceId = "#{#id}"
    )
    public ResponseEntity<Void> deleteModel(
            @PathVariable Long id,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        modelService.deleteModel(id, user.getId());
        return ResponseEntity.noContent().build();
    }
}

