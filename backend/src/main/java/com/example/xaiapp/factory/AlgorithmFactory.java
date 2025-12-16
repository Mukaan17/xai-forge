package com.example.xaiapp.factory;

import com.example.xaiapp.entity.MLModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.tribuo.MutableDataset;
import org.tribuo.data.csv.CSVDataSource;
import org.tribuo.data.columnar.RowProcessor;
import org.tribuo.data.columnar.FieldProcessor;
import org.tribuo.data.columnar.processors.response.FieldResponseProcessor;
import org.tribuo.data.columnar.processors.field.DoubleFieldProcessor;
import org.tribuo.classification.LabelFactory;
import org.tribuo.regression.Regressor;
import org.tribuo.regression.RegressionFactory;
import java.util.HashMap;
import java.util.Map;

import java.nio.file.Path;

/**
 * Factory for creating ML algorithms and data loaders
 * 
 * This factory provides methods for creating appropriate data loaders
 * and algorithms based on the model type and data characteristics.
 * 
 * @since 1.0.0
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AlgorithmFactory {
    
    // Manual log field (Lombok @Slf4j not generating it)
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AlgorithmFactory.class);
    
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
                                               MLModel.ModelType modelType,
                                               java.util.List<String> allColumnNames) throws Exception {
        log.info("Loading dataset from CSV: {}", csvPath);
        
        // Get selected features (excluding target)
        java.util.List<String> selectedFeatures = new java.util.ArrayList<>(featureNames);
        selectedFeatures.remove(targetVariable);
        
        // Create appropriate factory for model type
        Object factory = getFactory(modelType);
        
        // Create field processors map - only add selected features
        Map<String, FieldProcessor> fieldProcessors = new HashMap<>();
        for (String column : allColumnNames) {
            if (column.equals(targetVariable)) {
                continue; // Skip target - handled by response processor
            } else if (selectedFeatures.contains(column)) {
                // Add as feature (using DoubleFieldProcessor for numeric columns)
                fieldProcessors.put(column, new DoubleFieldProcessor(column));
            }
            // Unselected columns are simply not added to the map (they'll be skipped)
        }
        
        // Create RowProcessor with response processor first, then field processors map
        RowProcessor<?> rowProcessor;
        if (modelType == MLModel.ModelType.CLASSIFICATION) {
            LabelFactory labelFactory = (LabelFactory) factory;
            // Constructor: FieldResponseProcessor(String fieldName, String outputName, OutputFactory<T>)
            FieldResponseProcessor<org.tribuo.classification.Label> responseProcessor = 
                new FieldResponseProcessor<org.tribuo.classification.Label>(targetVariable, targetVariable, labelFactory);
            // Constructor: RowProcessor(ResponseProcessor<T>, Map<String, FieldProcessor>)
            rowProcessor = new RowProcessor<org.tribuo.classification.Label>(responseProcessor, fieldProcessors);
        } else {
            RegressionFactory regressionFactory = (RegressionFactory) factory;
            // Constructor: FieldResponseProcessor(String fieldName, String outputName, OutputFactory<T>)
            FieldResponseProcessor<org.tribuo.regression.Regressor> responseProcessor = 
                new FieldResponseProcessor<org.tribuo.regression.Regressor>(targetVariable, targetVariable, regressionFactory);
            // Constructor: RowProcessor(ResponseProcessor<T>, Map<String, FieldProcessor>)
            rowProcessor = new RowProcessor<org.tribuo.regression.Regressor>(responseProcessor, fieldProcessors);
        }
        
        log.info("Target variable: {}, All columns: {}, Selected features: {}", 
            targetVariable, allColumnNames, selectedFeatures);
        
        // Use CSVDataSource with RowProcessor (true = has header row)
        CSVDataSource<?> dataSource = new CSVDataSource<>(csvPath, rowProcessor, true);
        MutableDataset<?> dataset = new MutableDataset<>(dataSource);
        
        log.info("Dataset loaded: {} examples, {} features", 
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
            case REGRESSION -> new RegressionFactory();
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
