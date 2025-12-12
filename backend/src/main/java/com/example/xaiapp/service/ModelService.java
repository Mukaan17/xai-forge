/**
 * @Author: Mukhil Sundararaj
 * @Date:   2025-09-04 16:07:23
 * @Last Modified by:   Mukhil Sundararaj
 * @Last Modified time: 2025-10-24 15:18:58
 */
package com.example.xaiapp.service;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.tribuo.*;
import org.tribuo.classification.Label;
import org.tribuo.classification.LabelFactory;
import org.tribuo.classification.evaluation.LabelEvaluator;
import org.tribuo.data.csv.CSVLoader;
import org.tribuo.DataSource;
import org.tribuo.regression.Regressor;
import org.tribuo.regression.evaluation.RegressionEvaluator;
import org.tribuo.impl.ArrayExample;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Isolation;
import com.example.xaiapp.dto.TrainRequestDto;
import com.example.xaiapp.entity.Dataset;
import com.example.xaiapp.entity.MLModel;
import com.example.xaiapp.repository.DatasetRepository;
import com.example.xaiapp.repository.MLModelRepository;
import com.example.xaiapp.factory.AlgorithmFactory;
import com.example.xaiapp.factory.ModelFactory;
import com.example.xaiapp.config.MLTrainingConfig;
import com.example.xaiapp.strategy.ClassificationStrategy;
import com.example.xaiapp.strategy.RegressionStrategy;
import com.example.xaiapp.exception.DatasetNotFoundException;
import com.example.xaiapp.exception.ModelTrainingException;
import com.example.xaiapp.exception.ModelNotFoundException;

@Service
@Slf4j
@Transactional
public class ModelService {
    
    // Manual log field (Lombok @Slf4j not generating it)
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ModelService.class);
    
    private final MLModelRepository modelRepository;
    private final DatasetRepository datasetRepository;
    private final ModelFactory modelFactory;
    private final AlgorithmFactory algorithmFactory;
    private final MLTrainingConfig mlConfig;
    private final ClassificationStrategy classificationStrategy;
    private final RegressionStrategy regressionStrategy;
    
    // Manual constructor (Lombok @RequiredArgsConstructor not working with Java 24)
    public ModelService(MLModelRepository modelRepository, DatasetRepository datasetRepository, 
                       ModelFactory modelFactory, AlgorithmFactory algorithmFactory, MLTrainingConfig mlConfig,
                       ClassificationStrategy classificationStrategy, RegressionStrategy regressionStrategy) {
        this.modelRepository = modelRepository;
        this.datasetRepository = datasetRepository;
        this.modelFactory = modelFactory;
        this.algorithmFactory = algorithmFactory;
        this.mlConfig = mlConfig;
        this.classificationStrategy = classificationStrategy;
        this.regressionStrategy = regressionStrategy;
    }
    
    @Value("${app.file.upload-dir}")
    private String uploadDir;
    
    @Transactional(isolation = Isolation.REPEATABLE_READ, timeout = 300)
    public MLModel trainModel(TrainRequestDto request, Long userId) throws Exception {
        // Get dataset with proper transaction isolation
        Dataset dataset = datasetRepository.findByIdAndOwnerId(request.getDatasetId(), userId)
            .orElseThrow(() -> new DatasetNotFoundException(request.getDatasetId()));
        
        // Check if model already exists for this dataset with pessimistic locking
        Optional<MLModel> existingModel = modelRepository.findByDataset(dataset);
        if (existingModel.isPresent()) {
            throw new ModelTrainingException("Model already exists for this dataset", 
                "A model has already been trained for this dataset. Please delete the existing model first.");
        }
        
        // Load and prepare data
        MutableDataset<?> tribuoDataset = loadDatasetFromCSV(dataset, request);
        
        // Train model based on type
        Model<?> trainedModel;
        MLModel.ModelType modelType = MLModel.ModelType.valueOf(request.getModelType());
        
        if (modelType == MLModel.ModelType.CLASSIFICATION) {
            trainedModel = classificationStrategy.train(tribuoDataset, null);
        } else {
            trainedModel = regressionStrategy.train(tribuoDataset, null);
        }
        
        // Serialize and save model
        String modelPath = serializeModel(trainedModel, request.getModelName());
        
        // Create MLModel entity
        MLModel mlModel = new MLModel();
        mlModel.setModelName(request.getModelName());
        mlModel.setModelType(modelType);
        mlModel.setSerializedModelPath(modelPath);
        mlModel.setTargetVariable(request.getTargetVariable());
        mlModel.setFeatureNames(request.getFeatureNames());
        mlModel.setDataset(dataset);
        mlModel.setAccuracy(calculateAccuracy(trainedModel, tribuoDataset));
        
        // Set algorithm based on model type
        if (modelType == MLModel.ModelType.CLASSIFICATION) {
            mlModel.setAlgorithm("LOGISTIC_REGRESSION");
        } else {
            mlModel.setAlgorithm("LINEAR_REGRESSION");
        }
        
        // Set status to READY after successful training
        mlModel.setStatus(MLModel.ModelStatus.READY);
        
        return modelRepository.save(mlModel);
    }
    
    private MutableDataset<?> loadDatasetFromCSV(Dataset dataset, TrainRequestDto request) throws Exception {
        Path csvPath = Paths.get(dataset.getFilePath());
        
        // Tribuo's CSVLoader requires ALL column names from the CSV file
        // Get all headers from the dataset (which were parsed during upload)
        List<String> allColumnNames = new ArrayList<>(dataset.getHeaders());
        
        // Validate that target variable and selected features exist in the dataset
        if (!allColumnNames.contains(request.getTargetVariable())) {
            throw new IllegalArgumentException("Target variable '" + request.getTargetVariable() + "' not found in dataset. Available columns: " + allColumnNames);
        }
        
        for (String featureName : request.getFeatureNames()) {
            if (!allColumnNames.contains(featureName)) {
                throw new IllegalArgumentException("Feature '" + featureName + "' not found in dataset. Available columns: " + allColumnNames);
            }
        }
        
        // Load data with auto-detection of columns from CSV header
        // This avoids column name matching issues (case sensitivity, whitespace, etc.)
        MutableDataset<?> fullDataset;
        try {
            if (request.getModelType().equals("CLASSIFICATION")) {
                LabelFactory labelFactory = new LabelFactory();
                CSVLoader<Label> csvLoader = new CSVLoader<>(labelFactory);
                // Use auto-detect method: reads header automatically, treats all columns except targetVariable as features
                log.info("Loading CSV with auto-detected columns, target variable: {}", request.getTargetVariable());
                DataSource<Label> dataSource = csvLoader.loadDataSource(csvPath, request.getTargetVariable().trim());
                fullDataset = new MutableDataset<>(dataSource);
            } else {
                // For regression, use AlgorithmFactory with auto-detect
                fullDataset = algorithmFactory.loadDatasetFromCSV(csvPath, request.getTargetVariable().trim(), 
                    MLModel.ModelType.REGRESSION);
            }
            log.info("CSV loaded successfully: {} examples", fullDataset.size());
        } catch (NumberFormatException e) {
            log.error("Error parsing CSV data: {}", e.getMessage(), e);
            throw new IllegalArgumentException(
                "Error parsing CSV data. Please ensure all feature columns contain numeric values. " +
                "Non-numeric values found: " + e.getMessage() + 
                ". Make sure your CSV has numeric data in all feature columns (except the target variable for classification).", e);
        } catch (IllegalArgumentException e) {
            log.error("Error loading dataset: {}", e.getMessage(), e);
            if (e.getMessage() != null && e.getMessage().contains("input string")) {
                throw new IllegalArgumentException(
                    "Error parsing CSV data. The CSV contains non-numeric values where numbers are expected. " +
                    "Please check that all feature columns contain only numeric values. " +
                    "Error details: " + e.getMessage(), e);
            }
            throw e;
        } catch (Exception e) {
            log.error("Error loading dataset from CSV: {}", e.getMessage(), e);
            throw new IllegalArgumentException("Error loading dataset: " + e.getMessage(), e);
        }
        
        // Filter dataset to only include selected features (and target variable)
        return filterDatasetToSelectedFeatures(fullDataset, request.getFeatureNames(), request.getTargetVariable(), request.getModelType());
    }
    
    /**
     * Filter dataset to only include selected features and target variable
     * Creates a new dataset with only the features needed for training
     */
    @SuppressWarnings("unchecked")
    private MutableDataset<?> filterDatasetToSelectedFeatures(MutableDataset<?> fullDataset, 
                                                             List<String> selectedFeatures, 
                                                             String targetVariable,
                                                             String modelType) {
        log.info("Filtering dataset: {} features selected from {} total features", 
                selectedFeatures.size(), fullDataset.getFeatureMap().size());
        
        MutableDataset<?> filteredDataset;
        
        if (modelType.equals("CLASSIFICATION")) {
            LabelFactory labelFactory = new LabelFactory();
            filteredDataset = new MutableDataset<>(fullDataset.getProvenance(), labelFactory);
            
            // Iterate through examples and create new examples with only selected features
            for (org.tribuo.Example<Label> example : (MutableDataset<Label>) fullDataset) {
                // Get feature values for selected features only
                double[] featureValues = new double[selectedFeatures.size()];
                String[] featureNames = selectedFeatures.toArray(new String[0]);
                
                // Create a map of feature names to values from the example
                java.util.Map<String, Double> featureMap = new java.util.HashMap<>();
                for (org.tribuo.Feature feature : example) {
                    featureMap.put(feature.getName(), feature.getValue());
                }
                
                // Extract values for selected features
                for (int i = 0; i < selectedFeatures.size(); i++) {
                    String featureName = selectedFeatures.get(i);
                    Double value = featureMap.get(featureName);
                    if (value != null) {
                        featureValues[i] = value;
                    } else {
                        throw new IllegalArgumentException("Feature '" + featureName + "' not found in example");
                    }
                }
                
                // Create new example with only selected features
                org.tribuo.impl.ArrayExample<Label> filteredExample = 
                    new org.tribuo.impl.ArrayExample<>(example.getOutput(), featureNames, featureValues);
                ((MutableDataset<Label>) filteredDataset).add(filteredExample);
            }
        } else {
            // For regression
            org.tribuo.regression.RegressionFactory regressionFactory = new org.tribuo.regression.RegressionFactory();
            filteredDataset = new MutableDataset<>(fullDataset.getProvenance(), regressionFactory);
            
            // Iterate through examples and create new examples with only selected features
            for (org.tribuo.Example<Regressor> example : (MutableDataset<Regressor>) fullDataset) {
                // Get feature values for selected features only
                double[] featureValues = new double[selectedFeatures.size()];
                String[] featureNames = selectedFeatures.toArray(new String[0]);
                
                // Create a map of feature names to values from the example
                java.util.Map<String, Double> featureMap = new java.util.HashMap<>();
                for (org.tribuo.Feature feature : example) {
                    featureMap.put(feature.getName(), feature.getValue());
                }
                
                // Extract values for selected features
                for (int i = 0; i < selectedFeatures.size(); i++) {
                    String featureName = selectedFeatures.get(i);
                    Double value = featureMap.get(featureName);
                    if (value != null) {
                        featureValues[i] = value;
                    } else {
                        throw new IllegalArgumentException("Feature '" + featureName + "' not found in example");
                    }
                }
                
                // Create new example with only selected features
                org.tribuo.impl.ArrayExample<Regressor> filteredExample = 
                    new org.tribuo.impl.ArrayExample<>(example.getOutput(), featureNames, featureValues);
                ((MutableDataset<Regressor>) filteredDataset).add(filteredExample);
            }
        }
        
        log.info("Filtered dataset: {} examples, {} features (selected from {} original features)", 
                filteredDataset.size(), filteredDataset.getFeatureMap().size(), fullDataset.getFeatureMap().size());
        
        return filteredDataset;
    }
    
    
    private String serializeModel(Model<?> model, String modelName) throws IOException {
        Path modelDir = Paths.get(uploadDir, "models");
        if (!Files.exists(modelDir)) {
            Files.createDirectories(modelDir);
        }
        
        String filename = modelName + "_" + UUID.randomUUID().toString() + ".model";
        Path modelPath = modelDir.resolve(filename);
        
        try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(modelPath))) {
            oos.writeObject(model);
        }
        
        return modelPath.toString();
    }
    
    /**
     * Calculate model accuracy/evaluation metrics based on model type
     * 
     * For classification models, returns accuracy (0.0 to 1.0).
     * For regression models, returns R² (coefficient of determination) clamped to [0.0, 1.0].
     * 
     * @param model The trained model to evaluate
     * @param dataset The dataset to evaluate against
     * @return Accuracy score (0.0 to 1.0) or null if evaluation fails
     */
    private Double calculateAccuracy(Model<?> model, MutableDataset<?> dataset) {
        try {
            // For classification, implement real evaluation
            if (model.getOutputIDInfo().getDomain() instanceof org.tribuo.classification.LabelInfo) {
                // Classification evaluation
                @SuppressWarnings("unchecked")
                Model<Label> labelModel = (Model<Label>) model;
                @SuppressWarnings("unchecked")
                MutableDataset<Label> labelDataset = (MutableDataset<Label>) dataset;
                
                // Simple evaluation on the same dataset (in production, use train/test split)
                LabelEvaluator evaluator = new LabelEvaluator();
                var evaluation = evaluator.evaluate(labelModel, labelDataset);
                double accuracy = evaluation.accuracy();
                log.info("Classification accuracy: {}", accuracy);
                return accuracy;
            } else {
                // For regression, calculate actual R² score using Tribuo's RegressionEvaluator
                @SuppressWarnings("unchecked")
                Model<Regressor> regressorModel = (Model<Regressor>) model;
                @SuppressWarnings("unchecked")
                MutableDataset<Regressor> regressorDataset = (MutableDataset<Regressor>) dataset;
                
                // Use Tribuo's RegressionEvaluator for proper regression metrics
                RegressionEvaluator evaluator = new RegressionEvaluator();
                var evaluation = evaluator.evaluate(regressorModel, regressorDataset);
                
                // Calculate R² (coefficient of determination) as primary accuracy metric
                // Get the first (and typically only) output dimension's metrics
                var r2Map = evaluation.r2();
                var rmseMap = evaluation.rmse();
                var maeMap = evaluation.mae();
                
                // Extract the first dimension's values
                double rSquared = r2Map.values().iterator().next();
                double rmse = rmseMap.values().iterator().next();
                double mae = maeMap.values().iterator().next();
                
                log.info("Regression metrics - R²: {}, RMSE: {}, MAE: {}", rSquared, rmse, mae);
                
                // Return R² as the "accuracy" metric (0.0 to 1.0 scale, higher is better)
                // Handle edge cases where R² might be negative or invalid
                if (Double.isNaN(rSquared) || Double.isInfinite(rSquared)) {
                    log.warn("Invalid R² value: {}, using 0.0", rSquared);
                    return 0.0;
                }
                
                // Clamp R² to [0.0, 1.0] range for consistency with accuracy metric
                return Math.max(0.0, Math.min(1.0, rSquared));
            }
        } catch (Exception e) {
            log.warn("Could not calculate accuracy: {}", e.getMessage());
            return null;
        }
    }
    
    @Transactional(readOnly = true)
    public MLModel getModel(Long modelId, Long userId) {
        return modelRepository.findByIdAndDatasetOwnerId(modelId, userId)
            .orElseThrow(() -> new ModelNotFoundException(modelId));
    }
    
    @Transactional(readOnly = true)
    public List<MLModel> getUserModels(Long userId) {
        return modelRepository.findByDatasetOwnerId(userId);
    }
    
    @Transactional(readOnly = true)
    public List<MLModel> getReadyModels(Long userId) {
        return modelRepository.findByUserIdAndStatusIn(
            userId, 
            List.of(MLModel.ModelStatus.READY)
        );
    }
    
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void deleteModel(Long modelId, Long userId) throws IOException {
        MLModel model = getModel(modelId, userId);
        
        try {
            // Delete model file
            Path modelPath = Paths.get(model.getSerializedModelPath());
            if (Files.exists(modelPath)) {
                Files.delete(modelPath);
            }
            
            // Delete from database
            modelRepository.delete(model);
        } catch (IOException e) {
            log.error("Failed to delete model file: {}", model.getSerializedModelPath(), e);
            throw new ModelTrainingException("Failed to delete model file", e);
        }
    }
}
