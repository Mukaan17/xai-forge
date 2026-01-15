package com.xaiforge.infrastructure.ml.factory;

import com.xaiforge.domain.model.entity.MLModel;
import com.xaiforge.infrastructure.ml.strategy.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Registry for mapping algorithm names to training strategies
 * 
 * This registry provides a centralized way to select the appropriate
 * training strategy based on algorithm name and model type.
 * 
 * @since 1.0.0
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AlgorithmStrategyRegistry {

    private final ClassificationStrategy classificationStrategy;
    private final RegressionStrategy regressionStrategy;
    private final RandomForestClassificationStrategy randomForestClassificationStrategy;
    private final RandomForestRegressionStrategy randomForestRegressionStrategy;
    private final NeuralNetworkClassificationStrategy neuralNetworkClassificationStrategy;
    private final SVMClassificationStrategy svmClassificationStrategy;

    private Map<String, TrainingStrategy> strategyMap;

    /**
     * Get the appropriate training strategy for the given algorithm and model type
     * 
     * @param algorithm The algorithm name (e.g., "logistic", "random_forest", "neural_network", "svm")
     * @param modelType The model type (CLASSIFICATION or REGRESSION)
     * @return The training strategy
     * @throws IllegalArgumentException if algorithm/model type combination is not supported
     */
    public TrainingStrategy getStrategy(String algorithm, MLModel.ModelType modelType) {
        if (strategyMap == null) {
            initializeStrategyMap();
        }

        String key = algorithm.toLowerCase() + "_" + modelType.name();
        TrainingStrategy strategy = strategyMap.get(key);

        if (strategy == null) {
            // Fallback to default strategies
            log.warn("Algorithm '{}' not found for model type '{}', using default strategy", algorithm, modelType);
            if (modelType == MLModel.ModelType.CLASSIFICATION) {
                strategy = classificationStrategy;
            } else {
                strategy = regressionStrategy;
            }
        }

        return strategy;
    }

    /**
     * Initialize the strategy map with all available algorithm/model type combinations
     */
    private void initializeStrategyMap() {
        strategyMap = new HashMap<>();

        // Classification algorithms
        strategyMap.put("logistic_classification", classificationStrategy);
        strategyMap.put("logistic_regression_classification", classificationStrategy);
        strategyMap.put("random_forest_classification", randomForestClassificationStrategy);
        strategyMap.put("randomforest_classification", randomForestClassificationStrategy);
        strategyMap.put("neural_network_classification", neuralNetworkClassificationStrategy);
        strategyMap.put("neuralnetwork_classification", neuralNetworkClassificationStrategy);
        strategyMap.put("mlp_classification", neuralNetworkClassificationStrategy);
        strategyMap.put("svm_classification", svmClassificationStrategy);
        strategyMap.put("support_vector_machine_classification", svmClassificationStrategy);

        // Regression algorithms
        strategyMap.put("linear_regression", regressionStrategy);
        strategyMap.put("linear_sgd_regression", regressionStrategy);
        strategyMap.put("sgd_regression", regressionStrategy);
        strategyMap.put("random_forest_regression", randomForestRegressionStrategy);
        strategyMap.put("randomforest_regression", randomForestRegressionStrategy);

        log.info("Initialized algorithm strategy registry with {} strategies", strategyMap.size());
    }

    /**
     * Get all available algorithms for a given model type
     * 
     * @param modelType The model type
     * @return Map of algorithm names to their display names
     */
    public Map<String, String> getAvailableAlgorithms(MLModel.ModelType modelType) {
        Map<String, String> algorithms = new HashMap<>();

        if (modelType == MLModel.ModelType.CLASSIFICATION) {
            algorithms.put("logistic", "Logistic Regression");
            algorithms.put("random_forest", "Random Forest");
            algorithms.put("neural_network", "Neural Network (MLP)");
            algorithms.put("svm", "Support Vector Machine (SVM)");
        } else {
            algorithms.put("linear", "Linear Regression (SGD)");
            algorithms.put("random_forest", "Random Forest");
        }

        return algorithms;
    }
}
