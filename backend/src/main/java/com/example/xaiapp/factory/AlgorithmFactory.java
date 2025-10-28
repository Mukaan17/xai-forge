/**
 * @Author: Mukhil Sundararaj
 * @Date:   2025-10-24 12:14:38
 * @Last Modified by:   Mukhil Sundararaj
 * @Last Modified time: 2025-10-24 18:16:26
 */
package com.example.xaiapp.factory;

import com.example.xaiapp.entity.MLModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.tribuo.MutableDataset;
import org.tribuo.DataSource;
import org.tribuo.classification.LabelFactory;
import org.tribuo.regression.Regressor;
import org.tribuo.data.csv.CSVLoader;

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
                // For regression, create a CSVLoader with Regressor.Factory
                // This supports both single-output and multi-output regression
                yield new CSVLoader<>(new org.tribuo.regression.RegressionFactory());
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
        
        // Load the dataset with headers - use the correct method for CSV with headers
        DataSource<?> dataSource = csvLoader.loadDataSource(csvPath, targetVariable, allColumns.toArray(new String[0]));
        MutableDataset<?> dataset = new MutableDataset<>(dataSource);
        
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
            case REGRESSION -> Regressor.class;
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
