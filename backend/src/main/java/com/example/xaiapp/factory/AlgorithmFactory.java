package com.example.xaiapp.factory;

import com.example.xaiapp.entity.MLModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.tribuo.MutableDataset;
import org.tribuo.classification.Label;
import org.tribuo.classification.LabelFactory;
import org.tribuo.data.csv.CSVLoader;
import org.tribuo.regression.Regressor;
import org.tribuo.regression.RegressorFactory;

import java.nio.file.Path;

/**
 * Factory for creating ML algorithms and data loaders
 * 
 * This factory provides methods for creating appropriate data loaders
 * and algorithms based on the model type and data characteristics.
 * 
 * @author Mukhil Sundararaj
 * @since 1.0.0
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AlgorithmFactory {
    
    /**
     * Create a CSV loader for the specified model type
     * 
     * @param modelType The model type
     * @return The appropriate CSV loader
     */
    public CSVLoader<?> createCSVLoader(MLModel.ModelType modelType) {
        log.info("Creating CSV loader for model type: {}", modelType);
        
        return switch (modelType) {
            case CLASSIFICATION -> {
                LabelFactory labelFactory = new LabelFactory();
                yield new CSVLoader<>(labelFactory);
            }
            case REGRESSION -> {
                RegressorFactory regressorFactory = new RegressorFactory();
                yield new CSVLoader<>(regressorFactory);
            }
            default -> throw new IllegalArgumentException("Unsupported model type: " + modelType);
        };
    }
    
    /**
     * Load dataset from CSV file using the appropriate loader
     * 
     * @param csvPath The path to the CSV file
     * @param targetVariable The target variable name
     * @param featureNames The feature names
     * @param modelType The model type
     * @return The loaded dataset
     * @throws Exception if loading fails
     */
    public MutableDataset<?> loadDatasetFromCSV(Path csvPath, String targetVariable, 
                                               java.util.List<String> featureNames, 
                                               MLModel.ModelType modelType) throws Exception {
        log.info("Loading dataset from CSV: {}", csvPath);
        
        CSVLoader<?> csvLoader = createCSVLoader(modelType);
        
        // Add target variable to feature names for loading
        java.util.List<String> allColumns = new java.util.ArrayList<>(featureNames);
        allColumns.add(targetVariable);
        
        MutableDataset<?> dataset = csvLoader.loadDataSource(csvPath, targetVariable, allColumns);
        
        log.info("Dataset loaded successfully: {} examples, {} features", 
                dataset.size(), dataset.getFeatureMap().size());
        
        return dataset;
    }
    
    /**
     * Get the appropriate factory for the model type
     * 
     * @param modelType The model type
     * @return The factory object
     */
    public Object getFactory(MLModel.ModelType modelType) {
        return switch (modelType) {
            case CLASSIFICATION -> new LabelFactory();
            case REGRESSION -> new RegressorFactory();
            default -> throw new IllegalArgumentException("Unsupported model type: " + modelType);
        };
    }
    
    /**
     * Validate dataset compatibility with model type
     * 
     * @param dataset The dataset to validate
     * @param modelType The model type
     * @throws IllegalArgumentException if dataset is incompatible
     */
    public void validateDatasetCompatibility(MutableDataset<?> dataset, MLModel.ModelType modelType) {
        log.debug("Validating dataset compatibility with model type: {}", modelType);
        
        if (dataset == null) {
            throw new IllegalArgumentException("Dataset cannot be null");
        }
        
        if (dataset.size() == 0) {
            throw new IllegalArgumentException("Dataset cannot be empty");
        }
        
        switch (modelType) {
            case CLASSIFICATION -> {
                if (!(dataset instanceof MutableDataset<?>)) {
                    throw new IllegalArgumentException("Dataset must be compatible with classification");
                }
                // Additional classification-specific validation can be added here
            }
            case REGRESSION -> {
                if (!(dataset instanceof MutableDataset<?>)) {
                    throw new IllegalArgumentException("Dataset must be compatible with regression");
                }
                // Additional regression-specific validation can be added here
            }
            default -> throw new IllegalArgumentException("Unsupported model type: " + modelType);
        }
        
        log.debug("Dataset validation passed for model type: {}", modelType);
    }
}
