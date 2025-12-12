package com.example.xaiapp.controller;

import com.example.xaiapp.dto.request.BulkDeleteRequest;
import com.example.xaiapp.dto.request.PredictionFilterRequest;
import com.example.xaiapp.dto.response.*;
import com.example.xaiapp.entity.Prediction;
import com.example.xaiapp.security.CurrentUser;
import com.example.xaiapp.security.UserPrincipal;
import com.example.xaiapp.service.PredictionHistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST controller for prediction history management.
 */
@RestController
@RequestMapping("/api/predictions")
@RequiredArgsConstructor
@Tag(name = "Predictions", description = "Prediction history endpoints")
public class PredictionController {

    private final PredictionHistoryService predictionHistoryService;

    @GetMapping
    @Operation(summary = "List predictions")
    public ResponseEntity<Page<PredictionDTO>> getPredictions(
            @CurrentUser UserPrincipal currentUser,
            @ModelAttribute PredictionFilterRequest filter,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<Prediction> predictions = predictionHistoryService.getPredictions(
            currentUser.getId(), filter.getModelId(), filter.getStartDate(), filter.getEndDate(), pageable);
        Page<PredictionDTO> dtoPage = predictions.map(this::mapToPredictionDTO);
        return ResponseEntity.ok(dtoPage);
    }

    @GetMapping("/{predictionId}")
    @Operation(summary = "Get prediction details")
    public ResponseEntity<PredictionDetailDTO> getPrediction(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable Long predictionId) {
        Prediction prediction = predictionHistoryService.getPrediction(currentUser.getId(), predictionId);
        PredictionDetailDTO dto = mapToPredictionDetailDTO(prediction);
        return ResponseEntity.ok(dto);
    }

    @DeleteMapping("/{predictionId}")
    @Operation(summary = "Delete prediction")
    public ResponseEntity<Void> deletePrediction(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable Long predictionId) {
        predictionHistoryService.deletePrediction(currentUser.getId(), predictionId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/bulk-delete")
    @Operation(summary = "Bulk delete predictions")
    public ResponseEntity<BulkDeleteResponse> bulkDelete(
            @CurrentUser UserPrincipal currentUser,
            @Valid @RequestBody BulkDeleteRequest request) {
        int deleted = predictionHistoryService.bulkDeletePredictions(currentUser.getId(), request.getIds());
        return ResponseEntity.ok(new BulkDeleteResponse(deleted, "Deleted " + deleted + " predictions"));
    }

    @GetMapping("/export")
    @Operation(summary = "Export predictions to CSV")
    public ResponseEntity<byte[]> exportPredictions(
            @CurrentUser UserPrincipal currentUser,
            @ModelAttribute PredictionFilterRequest filter) {
        byte[] data = predictionHistoryService.exportPredictionsToCsv(
            currentUser.getId(), filter.getModelId(), filter.getStartDate(), filter.getEndDate());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "predictions.csv");
        return ResponseEntity.ok().headers(headers).body(data);
    }

    @PostMapping("/{predictionId}/re-explain")
    @Operation(summary = "Regenerate explanation for prediction")
    public ResponseEntity<Map<String, Object>> regenerateExplanation(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable Long predictionId) {
        Map<String, Object> result = predictionHistoryService.regenerateExplanation(currentUser.getId(), predictionId);
        return ResponseEntity.ok(result);
    }

    private PredictionDTO mapToPredictionDTO(Prediction prediction) {
        return PredictionDTO.builder()
            .id(prediction.getId())
            .modelId(prediction.getModel().getId())
            .modelName(prediction.getModel().getName())
            .predictionResult(prediction.getPredictionResult())
            .confidence(prediction.getConfidence())
            .inputSummary(prediction.getInputData() != null ? prediction.getInputData().toString() : null)
            .createdAt(prediction.getCreatedAt())
            .build();
    }

    private PredictionDetailDTO mapToPredictionDetailDTO(Prediction prediction) {
        return PredictionDetailDTO.builder()
            .id(prediction.getId())
            .modelId(prediction.getModel().getId())
            .modelName(prediction.getModel().getName())
            .modelType(prediction.getModel().getModelType().name())
            .inputData(prediction.getInputData())
            .predictionResult(prediction.getPredictionResult())
            .confidence(prediction.getConfidence())
            .explanation(prediction.getExplanation())
            .explanationSummary(prediction.getExplanationSummary())
            .predictionTimeMs(prediction.getPredictionTimeMs())
            .explanationTimeMs(prediction.getExplanationTimeMs())
            .createdAt(prediction.getCreatedAt())
            .build();
    }
}
