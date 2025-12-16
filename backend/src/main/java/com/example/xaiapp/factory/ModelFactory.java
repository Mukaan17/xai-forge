package com.example.xaiapp.factory;

import com.example.xaiapp.entity.MLModel;
import com.example.xaiapp.strategy.ClassificationStrategy;
import com.example.xaiapp.strategy.RegressionStrategy;
import com.example.xaiapp.strategy.TrainingStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.tribuo.MutableDataset;
import org.tribuo.Model;

import java.util.HashMap;
import java.util.Map;

/**
 * Factory for creating ML models and training strategies
 * 
 * This factory provides methods for creating ML models and selecting
 * appropriate training strategies based on model type.
 * 
 * @since 1.0.0
 */
@Component
@Slf4j
public class ModelFactory {
    
    // Manual log field (Lombok @Slf4j not generating it)
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ModelFactory.class);
    
    private final ClassificationStrategy classificationStrategy;
    private final RegressionStrategy regressionStrategy;
    
    // Manual constructor (Lombok @RequiredArgsConstructor not working with Java 24)
    public ModelFactory(ClassificationStrategy classificationStrategy, RegressionStrategy regressionStrategy) {
        this.classificationStrategy = classificationStrategy;
        this.regressionStrategy = regressionStrategy;
    }
    
    /**
     * Create a trained model using the appropriate strategy
     * 
     * @param dataset The training dataset
     * @param modelType The type of model to create
     * @param parameters Additional training parameters
     * @return The trained model
     * @throws Exception if model creation fails
     */
    public Model<?> createModel(MutableDataset<?> dataset, MLModel.ModelType modelType, Map<String, Object> parameters) throws Exception {
        log.info("Creating model of type: {}", modelType);
        
        TrainingStrategy strategy = getStrategy(modelType);
        strategy.validateDataset(dataset);
        
        return strategy.train(dataset, parameters);
    }
    
    /**
     * Get the appropriate training strategy for the model type
     * 
     * @param modelType The model type
     * @return The training strategy
     * @throws IllegalArgumentException if model type is not supported
     */
    public TrainingStrategy getStrategy(MLModel.ModelType modelType) {
        return switch (modelType) {
            case CLASSIFICATION -> classificationStrategy;
            case REGRESSION -> regressionStrategy;
            default -> throw new IllegalArgumentException("Unsupported model type: " + modelType);
        };
    }
    
    /**
     * Get all available strategies
     * 
     * @return Map of model types to strategies
     */
    public Map<MLModel.ModelType, TrainingStrategy> getAllStrategies() {
        Map<MLModel.ModelType, TrainingStrategy> strategies = new HashMap<>();
        strategies.put(MLModel.ModelType.CLASSIFICATION, classificationStrategy);
        strategies.put(MLModel.ModelType.REGRESSION, regressionStrategy);
        return strategies;
    }
    
    /**
     * Get strategy information for a model type
     * 
     * @param modelType The model type
     * @return Map containing strategy information
     */
    public Map<String, String> getStrategyInfo(MLModel.ModelType modelType) {
        TrainingStrategy strategy = getStrategy(modelType);
        Map<String, String> info = new HashMap<>();
        info.put("algorithmName", strategy.getAlgorithmName());
        info.put("modelType", strategy.getModelType());
        return info;
    }
}
