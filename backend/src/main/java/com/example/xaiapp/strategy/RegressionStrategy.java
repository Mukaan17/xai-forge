package com.example.xaiapp.strategy;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.tribuo.MutableDataset;
import org.tribuo.Model;
import org.tribuo.regression.Regressor;
import org.tribuo.regression.sgd.linear.LinearSGDTrainer;

import java.util.Map;

/**
 * Regression training strategy using Linear SGD
 * 
 * This strategy implements the TrainingStrategy interface for
 * regression tasks using Tribuo's LinearSGDTrainer.
 * 
 * @author Mukhil Sundararaj
 * @since 1.0.0
 */
@Component
@Slf4j
public class RegressionStrategy implements TrainingStrategy {
    
    @Override
    public Model<?> train(MutableDataset<?> dataset, Map<String, Object> parameters) throws Exception {
        log.info("Starting regression training with Linear SGD");
        
        validateDataset(dataset);
        
        @SuppressWarnings("unchecked")
        MutableDataset<Regressor> regressorDataset = (MutableDataset<Regressor>) dataset;
        
        LinearSGDTrainer trainer = new LinearSGDTrainer();
        
        log.info("Training dataset size: {} examples", regressorDataset.size());
        log.info("Number of features: {}", regressorDataset.getFeatureMap().size());
        log.info("Number of outputs: {}", regressorDataset.getOutputInfo().getOutputCount());
        
        Model<Regressor> model = trainer.train(regressorDataset);
        
        log.info("Regression training completed successfully");
        return model;
    }
    
    @Override
    public String getAlgorithmName() {
        return "Linear SGD";
    }
    
    @Override
    public String getModelType() {
        return "REGRESSION";
    }
    
    @Override
    public void validateDataset(MutableDataset<?> dataset) throws IllegalArgumentException {
        if (dataset == null) {
            throw new IllegalArgumentException("Dataset cannot be null");
        }
        
        if (dataset.size() == 0) {
            throw new IllegalArgumentException("Dataset cannot be empty");
        }
        
        if (dataset.getFeatureMap().size() == 0) {
            throw new IllegalArgumentException("Dataset must have at least one feature");
        }
        
        // Check if it's a regression dataset
        if (!(dataset instanceof MutableDataset<?>)) {
            throw new IllegalArgumentException("Dataset must be a MutableDataset for regression");
        }
        
        // Additional validation for regression
        @SuppressWarnings("unchecked")
        MutableDataset<Regressor> regressorDataset = (MutableDataset<Regressor>) dataset;
        
        if (regressorDataset.getOutputInfo().getOutputCount() == 0) {
            throw new IllegalArgumentException("Regression requires at least one output variable");
        }
        
        log.debug("Dataset validation passed for regression");
    }
}
