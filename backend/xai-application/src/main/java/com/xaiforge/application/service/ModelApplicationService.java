package com.xaiforge.application.service;

import com.xaiforge.common.dto.ModelDto;
import com.xaiforge.common.dto.TrainRequest;
import com.xaiforge.common.exception.DatasetNotFoundException;
import com.xaiforge.common.exception.ModelNotFoundException;
import com.xaiforge.common.exception.ModelTrainingException;
import com.xaiforge.domain.dataset.entity.Dataset;
import com.xaiforge.domain.model.entity.MLModel;
import com.xaiforge.infrastructure.persistence.dataset.DatasetRepository;
import com.xaiforge.infrastructure.persistence.model.MLModelRepository;
import com.xaiforge.infrastructure.ml.factory.AlgorithmFactory;
import com.xaiforge.infrastructure.ml.factory.ModelFactory;
import com.xaiforge.infrastructure.ml.strategy.ClassificationStrategy;
import com.xaiforge.infrastructure.ml.strategy.RegressionStrategy;
import com.xaiforge.infrastructure.ml.config.MLTrainingConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.tribuo.MutableDataset;
import org.tribuo.Model;
import org.tribuo.classification.Label;
import org.tribuo.classification.evaluation.LabelEvaluator;
import org.tribuo.regression.Regressor;
import org.tribuo.regression.evaluation.RegressionEvaluator;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class ModelApplicationService {
    
    private static final Logger log = LoggerFactory.getLogger(ModelApplicationService.class);
    
    private final MLModelRepository modelRepository;
    private final DatasetRepository datasetRepository;
    private final ModelFactory modelFactory;
    private final AlgorithmFactory algorithmFactory;
    private final MLTrainingConfig mlConfig;
    private final ClassificationStrategy classificationStrategy;
    private final RegressionStrategy regressionStrategy;
    
    @Value("${app.file.upload-dir}")
    private String uploadDir;
    
    public ModelApplicationService(
            MLModelRepository modelRepository,
            DatasetRepository datasetRepository,
            ModelFactory modelFactory,
            AlgorithmFactory algorithmFactory,
            MLTrainingConfig mlConfig,
            ClassificationStrategy classificationStrategy,
            RegressionStrategy regressionStrategy) {
        this.modelRepository = modelRepository;
        this.datasetRepository = datasetRepository;
        this.modelFactory = modelFactory;
        this.algorithmFactory = algorithmFactory;
        this.mlConfig = mlConfig;
        this.classificationStrategy = classificationStrategy;
        this.regressionStrategy = regressionStrategy;
    }
    
    @Transactional(isolation = Isolation.REPEATABLE_READ, timeout = 300)
    public Long trainModel(TrainRequest request, Long userId) {
        log.info("Starting model training - User: {}, Model: {}, Dataset: {}", userId, request.modelName(), request.datasetId());
        
        // Get dataset
        Dataset dataset = datasetRepository.findByIdAndOwnerId(request.datasetId(), userId)
            .orElseThrow(() -> new DatasetNotFoundException(request.datasetId()));
        
        // Check if model already exists
        Optional<MLModel> existingModel = modelRepository.findByDataset(dataset);
        if (existingModel.isPresent()) {
            throw new ModelTrainingException(
                "Model already exists for this dataset",
                request.datasetId(),
                null
            );
        }
        
        try {
            log.info("Loading dataset from CSV...");
            // Load and prepare data
            MutableDataset<?> tribuoDataset = loadDatasetFromCSV(dataset, request);
            log.info("Dataset loaded successfully - {} examples, {} features", 
                tribuoDataset.size(), tribuoDataset.getFeatureMap().size());
            
            // Create parameters map for training configuration
            Map<String, Object> parameters = new HashMap<>();
            // Add any additional parameters from request if needed
            
            // Train model based on type
            log.info("Training {} model...", request.algorithm());
            Model<?> trainedModel;
            MLModel.ModelType modelType = MLModel.ModelType.valueOf(request.algorithm().toUpperCase());
            
            if (modelType == MLModel.ModelType.CLASSIFICATION) {
                trainedModel = classificationStrategy.train(tribuoDataset, parameters);
            } else {
                trainedModel = regressionStrategy.train(tribuoDataset, parameters);
            }
            log.info("Model training completed successfully");
            
            // Serialize and save model
            log.info("Serializing model...");
            String modelPath = serializeModel(trainedModel, request.modelName());
            log.info("Model serialized to: {}", modelPath);
            
            // Calculate accuracy
            Double accuracy = calculateAccuracy(trainedModel, tribuoDataset);
            
            // Create MLModel entity
            MLModel mlModel = new MLModel();
            mlModel.setModelName(request.modelName());
            mlModel.setModelType(modelType);
            mlModel.setSerializedModelPath(modelPath);
            mlModel.setTargetVariable(request.targetColumn());
            mlModel.setFeatureNames(request.featureNames() != null ? request.featureNames() : List.of());
            mlModel.setDataset(dataset);
            mlModel.setAccuracy(accuracy);
            mlModel.setStatus(MLModel.ModelStatus.READY);
            
            MLModel savedModel = modelRepository.save(mlModel);
            log.info("Model saved with ID: {}", savedModel.getId());
            
            return savedModel.getId();
            
        } catch (Exception e) {
            log.error("Model training failed: {}", e.getMessage(), e);
            throw new ModelTrainingException(
                "Model training failed: " + e.getMessage(),
                request.datasetId(),
                e
            );
        }
    }
    
    private MutableDataset<?> loadDatasetFromCSV(Dataset dataset, TrainRequest request) throws Exception {
        Path csvPath = Paths.get(dataset.getFilePath());
        
        log.info("Loading dataset from CSV: {}", csvPath);
        log.info("Target variable: {}, Feature names: {}", request.targetColumn(), request.featureNames());
        
        // Load data based on model type
        MLModel.ModelType modelType = MLModel.ModelType.valueOf(request.algorithm().toUpperCase());
        if (modelType == MLModel.ModelType.CLASSIFICATION) {
            // Classification loading logic
            return algorithmFactory.loadDatasetFromCSV(
                csvPath, 
                request.targetColumn(),
                request.featureNames() != null ? request.featureNames() : List.of(),
                modelType,
                dataset.getHeaders()
            );
        } else {
            // Regression loading logic
            return algorithmFactory.loadDatasetFromCSV(
                csvPath,
                request.targetColumn(),
                request.featureNames() != null ? request.featureNames() : List.of(),
                modelType,
                dataset.getHeaders()
            );
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
    
    private Double calculateAccuracy(Model<?> model, MutableDataset<?> dataset) {
        try {
            String outputInfoClassName = model.getOutputIDInfo().getClass().getName();
            if (outputInfoClassName.contains("LabelInfo") || outputInfoClassName.contains("classification")) {
                // Classification evaluation
                @SuppressWarnings("unchecked")
                Model<Label> labelModel = (Model<Label>) model;
                @SuppressWarnings("unchecked")
                MutableDataset<Label> labelDataset = (MutableDataset<Label>) dataset;
                
                LabelEvaluator evaluator = new LabelEvaluator();
                var evaluation = evaluator.evaluate(labelModel, labelDataset);
                double accuracy = evaluation.accuracy();
                log.info("Classification accuracy: {}", accuracy);
                return accuracy;
            } else {
                // Regression evaluation
                @SuppressWarnings("unchecked")
                Model<Regressor> regressorModel = (Model<Regressor>) model;
                @SuppressWarnings("unchecked")
                MutableDataset<Regressor> regressorDataset = (MutableDataset<Regressor>) dataset;
                
                RegressionEvaluator evaluator = new RegressionEvaluator();
                var evaluation = evaluator.evaluate(regressorModel, regressorDataset);
                
                var r2Map = evaluation.r2();
                double rSquared = r2Map.values().iterator().next();
                
                log.info("Regression R²: {}", rSquared);
                
                if (Double.isNaN(rSquared) || Double.isInfinite(rSquared)) {
                    log.warn("Invalid R² value: {}, using 0.0", rSquared);
                    return 0.0;
                }
                
                return Math.max(0.0, Math.min(1.0, rSquared));
            }
        } catch (Exception e) {
            log.warn("Could not calculate accuracy: {}", e.getMessage());
            return null;
        }
    }
    
    @Transactional(readOnly = true)
    public List<ModelDto> getUserModels(Long userId) {
        return modelRepository.findByDatasetOwnerId(userId)
            .stream()
            .map(this::convertToDto)
            .toList();
    }
    
    @Transactional(readOnly = true)
    public ModelDto getModel(Long modelId, Long userId) {
        MLModel model = modelRepository.findByIdAndDatasetOwnerId(modelId, userId)
            .orElseThrow(() -> new ModelNotFoundException(modelId));
        return convertToDto(model);
    }
    
    @Transactional
    public void deleteModel(Long modelId, Long userId) {
        MLModel model = modelRepository.findByIdAndDatasetOwnerId(modelId, userId)
            .orElseThrow(() -> new ModelNotFoundException(modelId));
        
        try {
            // Delete model file
            Path modelPath = Paths.get(model.getSerializedModelPath());
            if (Files.exists(modelPath)) {
                Files.delete(modelPath);
            }
        } catch (IOException e) {
            log.error("Failed to delete model file: {}", model.getSerializedModelPath(), e);
        }
        
        modelRepository.delete(model);
    }
    
    private ModelDto convertToDto(MLModel model) {
        return new ModelDto(
            model.getId(),
            model.getModelName(),
            model.getModelType().name(),
            model.getTrainingDate(),
            model.getTargetVariable(),
            model.getFeatureNames(),
            model.getAccuracy(),
            model.getPrecision(),
            model.getRecall(),
            model.getF1Score(),
            model.getTrainingTime(),
            model.getStatus() != null ? model.getStatus().name() : "UNKNOWN",
            model.getDataset().getId()
        );
    }
}
