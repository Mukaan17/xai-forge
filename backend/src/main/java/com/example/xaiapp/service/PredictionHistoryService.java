package com.example.xaiapp.service;

import com.example.xaiapp.entity.ActivityLog;
import com.example.xaiapp.entity.MLModel;
import com.example.xaiapp.entity.Prediction;
import com.example.xaiapp.entity.User;
import com.example.xaiapp.exception.ResourceNotFoundException;
import com.example.xaiapp.repository.MLModelRepository;
import com.example.xaiapp.repository.PredictionRepository;
import com.example.xaiapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service for managing prediction history.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PredictionHistoryService {

    private final PredictionRepository predictionRepository;
    private final MLModelRepository modelRepository;
    private final UserRepository userRepository;
    private final XaiService xaiService;
    private final ActivityLogService activityLogService;

    /**
     * Save a new prediction to history.
     */
    public Prediction savePrediction(Long userId, Long modelId, Map<String, Object> inputData,
                                     String predictionResult, Double confidence, Map<String, Object> explanation,
                                     Long predictionTimeMs, Long explanationTimeMs) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        MLModel model = modelRepository.findByIdAndUserId(modelId, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Model not found"));

        String explanationSummary = generateExplanationSummary(explanation, predictionResult);

        Prediction prediction = Prediction.builder()
            .user(user)
            .model(model)
            .inputData(inputData)
            .predictionResult(predictionResult)
            .confidence(confidence)
            .explanation(explanation)
            .explanationSummary(explanationSummary)
            .predictionTimeMs(predictionTimeMs)
            .explanationTimeMs(explanationTimeMs)
            .build();

        prediction = predictionRepository.save(prediction);
        model.recordPrediction();
        modelRepository.save(model);

        log.debug("Prediction saved: id={}, modelId={}, result={}", prediction.getId(), modelId, predictionResult);
        return prediction;
    }

    /**
     * Get predictions for a user with optional filters.
     */
    @Transactional(readOnly = true)
    public Page<Prediction> getPredictions(Long userId, Long modelId, LocalDateTime startDate,
                                           LocalDateTime endDate, Pageable pageable) {
        if (modelId != null) {
            return predictionRepository.findByUserIdAndModelIdOrderByCreatedAtDesc(userId, modelId, pageable);
        } else if (startDate != null && endDate != null) {
            return predictionRepository.findByUserIdAndDateRange(userId, startDate, endDate, pageable);
        } else {
            return predictionRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        }
    }

    /**
     * Get detailed prediction by ID.
     */
    @Transactional(readOnly = true)
    public Prediction getPrediction(Long userId, Long predictionId) {
        return predictionRepository.findByIdAndUserId(predictionId, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Prediction not found"));
    }

    /**
     * Delete a prediction.
     */
    public void deletePrediction(Long userId, Long predictionId) {
        Prediction prediction = predictionRepository.findByIdAndUserId(predictionId, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Prediction not found"));
        predictionRepository.delete(prediction);
        activityLogService.logActivity(userId, ActivityLog.ActionType.PREDICTION_DELETED, "PREDICTION",
            predictionId, null, "Prediction deleted", Map.of("modelId", prediction.getModel().getId()));
        log.info("Prediction deleted: id={}, userId={}", predictionId, userId);
    }

    /**
     * Bulk delete predictions.
     */
    public int bulkDeletePredictions(Long userId, List<Long> predictionIds) {
        int deleted = predictionRepository.deleteByIdInAndUserId(predictionIds, userId);
        activityLogService.logActivity(userId, ActivityLog.ActionType.PREDICTION_DELETED, "PREDICTION",
            null, null, "Bulk delete: " + deleted + " predictions", Map.of("count", deleted));
        log.info("Bulk delete predictions: userId={}, count={}", userId, deleted);
        return deleted;
    }

    /**
     * Export predictions to CSV.
     */
    @Transactional(readOnly = true)
    public byte[] exportPredictionsToCsv(Long userId, Long modelId, LocalDateTime startDate, LocalDateTime endDate) {
        List<Prediction> predictions;
        if (modelId != null) {
            predictions = predictionRepository.findByUserIdAndModelIdOrderByCreatedAtDesc(userId, modelId, Pageable.unpaged()).getContent();
        } else if (startDate != null && endDate != null) {
            predictions = predictionRepository.findByUserIdAndDateRange(userId, startDate, endDate, Pageable.unpaged()).getContent();
        } else {
            predictions = predictionRepository.findByUserIdOrderByCreatedAtDesc(userId, Pageable.unpaged()).getContent();
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(out);
        writer.println("ID,Model,Prediction,Confidence,Created At,Input Data");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        for (Prediction p : predictions) {
            writer.printf("%d,\"%s\",\"%s\",%.4f,%s,\"%s\"%n",
                p.getId(), escapeCSV(p.getModel().getName()), escapeCSV(p.getPredictionResult()),
                p.getConfidence(), p.getCreatedAt().format(formatter), escapeCSV(p.getInputData().toString()));
        }
        writer.flush();
        return out.toByteArray();
    }

    /**
     * Regenerate explanation for an existing prediction.
     */
    public Map<String, Object> regenerateExplanation(Long userId, Long predictionId) {
        Prediction prediction = predictionRepository.findByIdAndUserId(predictionId, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Prediction not found"));
        MLModel model = prediction.getModel();

        long startTime = System.currentTimeMillis();
        Map<String, Object> newExplanation = xaiService.generateExplanation(model, prediction.getInputData(), prediction.getPredictionResult());
        long explanationTime = System.currentTimeMillis() - startTime;

        prediction.setExplanation(newExplanation);
        prediction.setExplanationSummary(generateExplanationSummary(newExplanation, prediction.getPredictionResult()));
        prediction.setExplanationTimeMs(explanationTime);
        predictionRepository.save(prediction);

        activityLogService.logActivity(userId, ActivityLog.ActionType.EXPLANATION_GENERATED, "PREDICTION",
            predictionId, null, "Explanation regenerated", Map.of("modelId", model.getId()));

        Map<String, Object> result = new HashMap<>();
        result.put("predictionId", prediction.getId());
        result.put("explanation", newExplanation);
        result.put("explanationSummary", prediction.getExplanationSummary());
        result.put("explanationTimeMs", explanationTime);
        return result;
    }

    /**
     * Get daily prediction statistics for charts.
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getDailyPredictionStats(Long userId, int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        List<Object[]> rawData = predictionRepository.getDailyPredictionCounts(userId, since);
        return rawData.stream()
            .map(row -> Map.<String, Object>of("date", row[0].toString(), "count", ((Number) row[1]).longValue()))
            .collect(Collectors.toList());
    }

    // ==================== PRIVATE HELPER METHODS ====================

    private String generateExplanationSummary(Map<String, Object> explanation, String predictionResult) {
        if (explanation == null || !explanation.containsKey("featureImportances")) {
            return "No explanation available.";
        }
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> importances = (List<Map<String, Object>>) explanation.get("featureImportances");
        if (importances == null || importances.isEmpty()) {
            return "No significant features identified.";
        }
        StringBuilder summary = new StringBuilder();
        summary.append("This prediction of \"").append(predictionResult).append("\" is primarily influenced by:\n\n");
        // Simplified summary - can be enhanced
        return summary.toString();
    }

    private String escapeCSV(String value) {
        if (value == null) return "";
        return value.replace("\"", "\"\"");
    }
}
