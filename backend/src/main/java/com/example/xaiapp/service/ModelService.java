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
import com.example.xaiapp.strategy.ClassificationStrategy;
import com.example.xaiapp.strategy.RegressionStrategy;
import com.example.xaiapp.exception.DatasetNotFoundException;
import com.example.xaiapp.exception.ModelTrainingException;
import com.example.xaiapp.exception.ModelNotFoundException;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ModelService {
    
    private final MLModelRepository modelRepository;
    private final DatasetRepository datasetRepository;
    private final AlgorithmFactory algorithmFactory;
    private final ClassificationStrategy classificationStrategy;
    private final RegressionStrategy regressionStrategy;
    
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
        
        return modelRepository.save(mlModel);
    }
    
    private MutableDataset<?> loadDatasetFromCSV(Dataset dataset, TrainRequestDto request) throws Exception {
        Path csvPath = Paths.get(dataset.getFilePath());
        
        // Create feature list
        List<String> featureNames = new ArrayList<>(request.getFeatureNames());
        featureNames.add(request.getTargetVariable());
        
        // Load data based on model type
        if (request.getModelType().equals("CLASSIFICATION")) {
            LabelFactory labelFactory = new LabelFactory();
            CSVLoader<Label> csvLoader = new CSVLoader<>(labelFactory);
            DataSource<Label> dataSource = csvLoader.loadDataSource(csvPath, request.getTargetVariable(), featureNames.toArray(new String[0]));
            return new MutableDataset<>(dataSource);
        } else {
            // For regression, use AlgorithmFactory to load data
            return algorithmFactory.loadDatasetFromCSV(csvPath, request.getTargetVariable(), 
                request.getFeatureNames(), MLModel.ModelType.REGRESSION);
        }
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
