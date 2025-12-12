package com.example.xaiapp.service;

import com.example.xaiapp.entity.MLModel;
import com.example.xaiapp.exception.ResourceNotFoundException;
import com.example.xaiapp.exception.ValidationException;
import com.example.xaiapp.repository.MLModelRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for comparing multiple ML models.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ModelComparisonService {

    private final MLModelRepository modelRepository;
    private static final int MAX_MODELS_TO_COMPARE = 5;

    /**
     * Compare multiple models and return comprehensive comparison data.
     */
    public Map<String, Object> compareModels(Long userId, List<Long> modelIds) {
        if (modelIds == null || modelIds.size() < 2) {
            throw new ValidationException("At least 2 models are required for comparison");
        }
        if (modelIds.size() > MAX_MODELS_TO_COMPARE) {
            throw new ValidationException("Maximum " + MAX_MODELS_TO_COMPARE + " models can be compared");
        }

        List<MLModel> models = modelIds.stream()
            .map(id -> modelRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Model not found: " + id)))
            .collect(Collectors.toList());

        for (MLModel model : models) {
            if (model.getStatus() != MLModel.ModelStatus.READY) {
                throw new ValidationException("Model " + model.getName() + " is not ready for comparison");
            }
        }

        MLModel.ModelType firstType = models.get(0).getModelType();
        boolean sameType = models.stream().allMatch(m -> m.getModelType() == firstType);
        if (!sameType) {
            throw new ValidationException("All models must be of the same type (classification or regression)");
        }

        Map<String, Object> comparison = new HashMap<>();
        comparison.put("models", models.stream().map(this::mapToModelSummary).collect(Collectors.toList()));
        comparison.put("metricsComparison", buildMetricsComparison(models));
        comparison.put("featureImportanceComparison", buildFeatureImportanceComparison(models));
        comparison.put("bestModelId", determineBestModel(models));
        comparison.put("recommendations", generateRecommendations(models));
        comparison.put("modelType", firstType.name());
        return comparison;
    }

    /**
     * Get all versions of a model (by base name).
     */
    public List<Map<String, Object>> getModelVersions(Long userId, String baseName) {
        List<MLModel> models = modelRepository.findByUserIdAndBaseNameOrderByVersionDesc(userId, baseName);
        return models.stream()
            .map(model -> {
                Map<String, Object> dto = new HashMap<>();
                dto.put("id", model.getId());
                dto.put("version", model.getVersion());
                dto.put("accuracy", model.getAccuracy());
                dto.put("featureCount", model.getFeatureColumns().size());
                dto.put("trainedAt", model.getTrainedAt());
                dto.put("status", model.getStatus().name());
                return dto;
            })
            .collect(Collectors.toList());
    }

    /**
     * Get performance trend for a model over its versions.
     */
    public Map<String, Object> getPerformanceTrend(Long userId, Long modelId) {
        MLModel model = modelRepository.findByIdAndUserId(modelId, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Model not found"));

        List<MLModel> versions = modelRepository.findByUserIdAndBaseNameOrderByVersionAsc(userId, model.getBaseName());

        List<Map<String, Object>> trendData = versions.stream()
            .map(m -> {
                Map<String, Object> point = new HashMap<>();
                point.put("version", m.getVersion());
                point.put("accuracy", m.getAccuracy());
                point.put("trainedAt", m.getTrainedAt());
                point.put("featuresCount", m.getFeatureColumns().size());
                return point;
            })
            .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("modelBaseName", model.getBaseName());
        result.put("currentVersion", model.getVersion());
        result.put("trendData", trendData);
        result.put("improvement", calculateImprovement(versions));
        return result;
    }

    // ==================== PRIVATE HELPER METHODS ====================

    private Map<String, Object> mapToModelSummary(MLModel model) {
        Map<String, Object> summary = new HashMap<>();
        summary.put("id", model.getId());
        summary.put("name", model.getName());
        summary.put("version", model.getVersion());
        summary.put("algorithm", model.getAlgorithm());
        summary.put("accuracy", model.getAccuracy());
        summary.put("trainedAt", model.getTrainedAt());
        summary.put("featureCount", model.getFeatureColumns().size());
        return summary;
    }

    private List<Map<String, Object>> buildMetricsComparison(List<MLModel> models) {
        List<Map<String, Object>> comparisons = new ArrayList<>();
        boolean isClassification = models.get(0).getModelType() == MLModel.ModelType.CLASSIFICATION;

        comparisons.add(buildMetricRow("Accuracy", models, MLModel::getAccuracy));
        if (isClassification) {
            comparisons.add(buildMetricRow("Precision", models, MLModel::getPrecisionScore));
            comparisons.add(buildMetricRow("Recall", models, MLModel::getRecallScore));
            comparisons.add(buildMetricRow("F1 Score", models, MLModel::getF1Score));
        } else {
            comparisons.add(buildMetricRow("MSE", models, MLModel::getMse));
            comparisons.add(buildMetricRow("RMSE", models, MLModel::getRmse));
            comparisons.add(buildMetricRow("MAE", models, MLModel::getMae));
            comparisons.add(buildMetricRow("R² Score", models, MLModel::getR2Score));
        }

        comparisons.add(buildMetricRow("Training Time (s)", models, 
            m -> m.getTrainingDurationMs() != null ? m.getTrainingDurationMs() / 1000.0 : null));
        comparisons.add(buildMetricRow("Features Used", models, 
            m -> (double) m.getFeatureColumns().size()));

        return comparisons;
    }

    private Map<String, Object> buildMetricRow(String metricName, List<MLModel> models, 
                                                 java.util.function.Function<MLModel, Double> extractor) {
        Map<Long, Double> values = new LinkedHashMap<>();
        Long bestModelId = null;
        Double bestValue = null;
        boolean higherIsBetter = !metricName.contains("MSE") && !metricName.contains("MAE") && !metricName.contains("Time");

        for (MLModel model : models) {
            Double value = extractor.apply(model);
            values.put(model.getId(), value);
            if (value != null) {
                if (bestValue == null || 
                    (higherIsBetter && value > bestValue) ||
                    (!higherIsBetter && value < bestValue)) {
                    bestValue = value;
                    bestModelId = model.getId();
                }
            }
        }

        Map<String, Object> row = new HashMap<>();
        row.put("metricName", metricName);
        row.put("values", values);
        row.put("bestModelId", bestModelId);
        row.put("higherIsBetter", higherIsBetter);
        return row;
    }

    private Map<String, Map<Long, Double>> buildFeatureImportanceComparison(List<MLModel> models) {
        Set<String> allFeatures = new LinkedHashSet<>();
        for (MLModel model : models) {
            if (model.getFeatureImportance() != null) {
                allFeatures.addAll(model.getFeatureImportance().keySet());
            }
        }

        Map<String, Map<Long, Double>> comparison = new LinkedHashMap<>();
        for (String feature : allFeatures) {
            Map<Long, Double> featureValues = new LinkedHashMap<>();
            for (MLModel model : models) {
                Double importance = model.getFeatureImportance() != null 
                    ? model.getFeatureImportance().get(feature) 
                    : null;
                featureValues.put(model.getId(), importance);
            }
            comparison.put(feature, featureValues);
        }

        return comparison.entrySet().stream()
            .sorted((e1, e2) -> {
                double avg1 = e1.getValue().values().stream()
                    .filter(Objects::nonNull).mapToDouble(d -> d).average().orElse(0);
                double avg2 = e2.getValue().values().stream()
                    .filter(Objects::nonNull).mapToDouble(d -> d).average().orElse(0);
                return Double.compare(avg2, avg1);
            })
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a, LinkedHashMap::new));
    }

    private Long determineBestModel(List<MLModel> models) {
        return models.stream()
            .filter(m -> m.getAccuracy() != null)
            .max(Comparator.comparingDouble(MLModel::getAccuracy))
            .map(MLModel::getId)
            .orElse(null);
    }

    private List<String> generateRecommendations(List<MLModel> models) {
        List<String> recommendations = new ArrayList<>();
        Optional<MLModel> bestModel = models.stream()
            .filter(m -> m.getAccuracy() != null)
            .max(Comparator.comparingDouble(MLModel::getAccuracy));

        if (bestModel.isPresent()) {
            MLModel best = bestModel.get();
            recommendations.add(String.format(
                "%s achieves the highest accuracy (%.1f%%) and is recommended for production use.",
                best.getName(), best.getAccuracy() * 100));
        }

        return recommendations;
    }

    private Double calculateImprovement(List<MLModel> versions) {
        if (versions.size() < 2) return null;
        MLModel first = versions.get(0);
        MLModel last = versions.get(versions.size() - 1);
        if (first.getAccuracy() == null || last.getAccuracy() == null) return null;
        return (last.getAccuracy() - first.getAccuracy()) * 100;
    }
}
