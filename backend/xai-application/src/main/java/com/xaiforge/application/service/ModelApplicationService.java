package com.xaiforge.application.service;

import com.xaiforge.common.dto.ExtendedMetricsDto;
import com.xaiforge.common.dto.ModelDto;
import com.xaiforge.common.dto.PaginatedResponse;
import com.xaiforge.common.dto.TrainRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import com.xaiforge.common.exception.DatasetNotFoundException;
import com.xaiforge.common.exception.ModelNotFoundException;
import com.xaiforge.common.exception.ModelTrainingException;
import com.xaiforge.domain.dataset.entity.Dataset;
import com.xaiforge.domain.model.entity.MLModel;
import com.xaiforge.infrastructure.persistence.dataset.DatasetRepository;
import com.xaiforge.infrastructure.persistence.model.MLModelRepository;
import com.xaiforge.infrastructure.ml.factory.AlgorithmFactory;
import com.xaiforge.infrastructure.ml.factory.AlgorithmStrategyRegistry;
import com.xaiforge.infrastructure.ml.factory.ModelFactory;
import com.xaiforge.infrastructure.ml.strategy.TrainingStrategy;
import com.xaiforge.infrastructure.ml.config.MLTrainingConfig;
import com.xaiforge.infrastructure.ml.util.DatasetSplitter;
import com.xaiforge.infrastructure.ml.util.CrossValidationEvaluator;
import com.xaiforge.infrastructure.ml.util.ModelMetricsCalculator;
import com.xaiforge.domain.model.entity.TrainingJob;
import com.xaiforge.application.service.TrainingJobService;
import com.xaiforge.application.service.NotificationApplicationService;
import com.xaiforge.domain.notification.entity.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import java.util.concurrent.CompletableFuture;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
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
    private final AlgorithmStrategyRegistry algorithmStrategyRegistry;
    private final MLTrainingConfig mlConfig;
    private final DatasetSplitter datasetSplitter;
    private final CrossValidationEvaluator crossValidationEvaluator;
    private final TrainingJobService trainingJobService;
    private final ModelMetricsCalculator metricsCalculator;
    private final NotificationApplicationService notificationService;
    
    @Value("${app.file.upload-dir}")
    private String uploadDir;
    
    public ModelApplicationService(
            MLModelRepository modelRepository,
            DatasetRepository datasetRepository,
            ModelFactory modelFactory,
            AlgorithmFactory algorithmFactory,
            AlgorithmStrategyRegistry algorithmStrategyRegistry,
            MLTrainingConfig mlConfig,
            DatasetSplitter datasetSplitter,
            CrossValidationEvaluator crossValidationEvaluator,
            TrainingJobService trainingJobService,
            ModelMetricsCalculator metricsCalculator,
            NotificationApplicationService notificationService) {
        this.modelRepository = modelRepository;
        this.datasetRepository = datasetRepository;
        this.modelFactory = modelFactory;
        this.algorithmFactory = algorithmFactory;
        this.algorithmStrategyRegistry = algorithmStrategyRegistry;
        this.mlConfig = mlConfig;
        this.datasetSplitter = datasetSplitter;
        this.crossValidationEvaluator = crossValidationEvaluator;
        this.trainingJobService = trainingJobService;
        this.metricsCalculator = metricsCalculator;
        this.notificationService = notificationService;
    }
    
    /**
     * Start async model training
     * Creates a training job and starts async training process
     */
    @Transactional
    public Long startTraining(TrainRequest request, Long userId) {
        log.info("Starting async model training - User: {}, Model: {}, Dataset: {}", userId, request.modelName(), request.datasetId());
        
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
        
        // Create model entity with TRAINING status
        MLModel mlModel = new MLModel();
        mlModel.setModelName(request.modelName());
        mlModel.setTargetVariable(request.targetColumn());
        mlModel.setFeatureNames(request.featureNames() != null ? request.featureNames() : List.of());
        mlModel.setDataset(dataset);
        mlModel.setStatus(MLModel.ModelStatus.TRAINING);
        
        // Determine model type
        MLModel.ModelType modelType;
        String algorithmName = request.algorithm().toLowerCase();
        if (algorithmName.equals("classification")) {
            modelType = MLModel.ModelType.CLASSIFICATION;
            algorithmName = "logistic";
        } else if (algorithmName.equals("regression")) {
            modelType = MLModel.ModelType.REGRESSION;
            algorithmName = "linear";
        } else {
            if (algorithmName.contains("random_forest") || algorithmName.contains("randomforest") ||
                algorithmName.contains("neural_network") || algorithmName.contains("neuralnetwork") ||
                algorithmName.contains("mlp") || algorithmName.contains("svm") ||
                algorithmName.contains("logistic")) {
                modelType = MLModel.ModelType.CLASSIFICATION;
            } else {
                modelType = MLModel.ModelType.REGRESSION;
            }
        }
        mlModel.setModelType(modelType);
        
        MLModel savedModel = modelRepository.save(mlModel);
        
        // Create training job
        TrainingJob job = trainingJobService.createJob(savedModel.getId(), userId);
        
        // Start async training
        trainModelAsync(request, userId, savedModel.getId(), job.getId());
        
        return savedModel.getId();
    }
    
    /**
     * Async model training with progress tracking
     */
    @Async("mlTrainingExecutor")
    @Transactional(isolation = Isolation.REPEATABLE_READ, timeout = 300)
    public CompletableFuture<Long> trainModelAsync(TrainRequest request, Long userId, Long modelId, Long jobId) {
        try {
            trainingJobService.updateProgress(jobId, 5, "Loading dataset...");
            
            // Get dataset
            Dataset dataset = datasetRepository.findByIdAndOwnerId(request.datasetId(), userId)
                .orElseThrow(() -> new DatasetNotFoundException(request.datasetId()));
            
            trainingJobService.updateProgress(jobId, 10, "Parsing dataset...");
            MutableDataset<?> tribuoDataset = loadDatasetFromCSV(dataset, request);
            log.info("Dataset loaded successfully - {} examples, {} features", 
                tribuoDataset.size(), tribuoDataset.getFeatureMap().size());
            
            trainingJobService.updateProgress(jobId, 20, "Preparing training configuration...");
            
            // Merge hyperparameters from request
            Map<String, Object> parameters = new HashMap<>();
            if (request.hyperparameters() != null) {
                parameters.putAll(request.hyperparameters());
            }
            
            // Determine model type from algorithm
            MLModel.ModelType modelType;
            String algorithmName = request.algorithm().toLowerCase();
            
            if (algorithmName.equals("classification")) {
                modelType = MLModel.ModelType.CLASSIFICATION;
                algorithmName = "logistic";
            } else if (algorithmName.equals("regression")) {
                modelType = MLModel.ModelType.REGRESSION;
                algorithmName = "linear";
            } else {
                if (algorithmName.contains("random_forest") || algorithmName.contains("randomforest") ||
                    algorithmName.contains("neural_network") || algorithmName.contains("neuralnetwork") ||
                    algorithmName.contains("mlp") || algorithmName.contains("svm") ||
                    algorithmName.contains("logistic")) {
                    modelType = MLModel.ModelType.CLASSIFICATION;
                } else {
                    modelType = MLModel.ModelType.REGRESSION;
                }
            }
            
            trainingJobService.updateProgress(jobId, 30, "Splitting dataset...");
            
            // Get the appropriate training strategy
            TrainingStrategy strategy = algorithmStrategyRegistry.getStrategy(algorithmName, modelType);
            log.info("Training {} model using {} algorithm...", modelType, strategy.getAlgorithmName());
            
            Model<?> trainedModel;
            MutableDataset<?> trainingDataset = tribuoDataset;
            MutableDataset<?> testDataset = null;
            Double accuracy;
            Double crossValidationScore = null;
            
            // Handle train/test split
            if (request.trainTestSplit() > 0 && request.trainTestSplit() < 100) {
                log.info("Splitting dataset: {}% training, {}% testing", 
                    request.trainTestSplit(), 100 - request.trainTestSplit());
                DatasetSplitter.SplitResult splitResult = datasetSplitter.splitDataset(
                    tribuoDataset, 
                    request.trainTestSplit()
                );
                trainingDataset = splitResult.getTrain();
                testDataset = splitResult.getTest();
                log.info("Training on {} examples, will evaluate on {} examples", 
                    trainingDataset.size(), testDataset.size());
            }
            
            trainingJobService.updateProgress(jobId, 40, "Training model...");
            
            // Handle cross-validation if requested
            if (request.crossValidation()) {
                log.info("Performing cross-validation...");
                CrossValidationEvaluator.CrossValidationResult cvResult = 
                    crossValidationEvaluator.performCrossValidation(trainingDataset);
                crossValidationScore = cvResult.getAverageAccuracy();
                log.info("Cross-validation completed - Average accuracy: {}", crossValidationScore);
            }
            
            // Train the model on training dataset
            trainedModel = strategy.train(trainingDataset, parameters);
            log.info("Model training completed successfully");
            
            trainingJobService.updateProgress(jobId, 70, "Evaluating model...");
            
            // Evaluate on test set if available, otherwise use training set
            MutableDataset<?> evaluationDataset = (testDataset != null) ? testDataset : trainingDataset;
            
            // Calculate comprehensive metrics
            ModelMetricsCalculator.MetricsResult metrics = metricsCalculator.calculateMetrics(
                trainedModel, evaluationDataset);
            
            accuracy = metrics.getAccuracy();
            
            if (testDataset != null) {
                log.info("Model metrics on test set - Accuracy: {}, Precision: {}, Recall: {}, F1: {}", 
                    accuracy, metrics.getPrecision(), metrics.getRecall(), metrics.getF1Score());
            } else {
                log.info("Model metrics on training set - Accuracy: {}, Precision: {}, Recall: {}, F1: {}", 
                    accuracy, metrics.getPrecision(), metrics.getRecall(), metrics.getF1Score());
            }
            
            trainingJobService.updateProgress(jobId, 80, "Saving model...");
            
            // Serialize and save model
            log.info("Serializing model...");
            String modelPath = serializeModel(trainedModel, request.modelName());
            log.info("Model serialized to: {}", modelPath);
            
            // Update model entity
            MLModel mlModel = modelRepository.findById(modelId)
                .orElseThrow(() -> new ModelNotFoundException(modelId));
            
            mlModel.setSerializedModelPath(modelPath);
            mlModel.setAccuracy(accuracy);
            
            // Store classification metrics
            if (modelType == MLModel.ModelType.CLASSIFICATION) {
                mlModel.setPrecision(metrics.getPrecision());
                mlModel.setRecall(metrics.getRecall());
                mlModel.setF1Score(metrics.getF1Score());
                
                // Store confusion matrix as JSON
                if (metrics.getConfusionMatrix() != null && !metrics.getConfusionMatrix().isEmpty()) {
                    try {
                        com.fasterxml.jackson.databind.ObjectMapper mapper = 
                            new com.fasterxml.jackson.databind.ObjectMapper();
                        mlModel.setConfusionMatrix(mapper.writeValueAsString(metrics.getConfusionMatrix()));
                    } catch (Exception e) {
                        log.warn("Failed to serialize confusion matrix: {}", e.getMessage());
                    }
                }
            } else {
                // Store regression metrics
                mlModel.setMse(metrics.getMse());
                mlModel.setRmse(metrics.getRmse());
                mlModel.setMae(metrics.getMae());
                mlModel.setR2Score(metrics.getR2Score());
                // For regression, accuracy field stores R²
                mlModel.setAccuracy(metrics.getR2Score());
            }
            
            mlModel.setStatus(MLModel.ModelStatus.READY);
            
            // Store training configuration in metadata
            Map<String, Object> trainingConfig = new HashMap<>();
            trainingConfig.put("algorithm", algorithmName);
            trainingConfig.put("trainTestSplit", request.trainTestSplit());
            trainingConfig.put("crossValidation", request.crossValidation());
            trainingConfig.put("hyperparameters", request.hyperparameters());
            trainingConfig.put("trainingSetSize", trainingDataset.size());
            if (testDataset != null) {
                trainingConfig.put("testSetSize", testDataset.size());
            }
            if (crossValidationScore != null) {
                trainingConfig.put("crossValidationScore", crossValidationScore);
            }
            
            // Convert to JSON string for storage
            try {
                com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
                mlModel.setModelMetadata(objectMapper.writeValueAsString(trainingConfig));
            } catch (Exception e) {
                log.warn("Failed to serialize training configuration: {}", e.getMessage());
            }
            
            trainingJobService.updateProgress(jobId, 95, "Finalizing...");
            
            MLModel savedModel = modelRepository.save(mlModel);
            log.info("Model saved with ID: {}", savedModel.getId());
            
            trainingJobService.completeJob(jobId);
            
            // Create success notification
            String accuracyText = accuracy != null 
                ? String.format("%.1f%%", accuracy * 100) 
                : "completed";
            notificationService.createNotification(
                userId,
                Notification.NotificationType.TRAINING_COMPLETE,
                "Model Training Complete",
                String.format("\"%s\" finished training successfully", request.modelName()),
                String.format("Accuracy: %s", accuracyText)
            );
            
            return CompletableFuture.completedFuture(savedModel.getId());
            
        } catch (Exception e) {
            log.error("Model training failed: {}", e.getMessage(), e);
            trainingJobService.failJob(jobId, e.getMessage());
            
            // Update model status to FAILED
            try {
                MLModel mlModel = modelRepository.findById(modelId).orElse(null);
                if (mlModel != null) {
                    mlModel.setStatus(MLModel.ModelStatus.FAILED);
                    modelRepository.save(mlModel);
                }
            } catch (Exception ex) {
                log.error("Failed to update model status: {}", ex.getMessage());
            }
            
            return CompletableFuture.failedFuture(e);
        }
    }
    
    /**
     * Synchronous training (kept for backward compatibility)
     * @deprecated Use startTraining() for async training with progress tracking
     */
    @Deprecated
    @Transactional(isolation = Isolation.REPEATABLE_READ, timeout = 300)
    public Long trainModel(TrainRequest request, Long userId) {
        log.info("Starting synchronous model training - User: {}, Model: {}, Dataset: {}", userId, request.modelName(), request.datasetId());
        
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
            
            // Merge hyperparameters from request
            Map<String, Object> parameters = new HashMap<>();
            if (request.hyperparameters() != null) {
                parameters.putAll(request.hyperparameters());
            }
            
            // Determine model type from algorithm (backward compatibility)
            // If algorithm is "CLASSIFICATION" or "REGRESSION", use it as model type
            // Otherwise, infer from algorithm name
            MLModel.ModelType modelType;
            String algorithmName = request.algorithm().toLowerCase();
            
            if (algorithmName.equals("classification")) {
                modelType = MLModel.ModelType.CLASSIFICATION;
                algorithmName = "logistic"; // Default to logistic regression
            } else if (algorithmName.equals("regression")) {
                modelType = MLModel.ModelType.REGRESSION;
                algorithmName = "linear"; // Default to linear regression
            } else {
                // Infer model type from algorithm name
                if (algorithmName.contains("random_forest") || algorithmName.contains("randomforest") ||
                    algorithmName.contains("neural_network") || algorithmName.contains("neuralnetwork") ||
                    algorithmName.contains("mlp") || algorithmName.contains("svm") ||
                    algorithmName.contains("logistic")) {
                    modelType = MLModel.ModelType.CLASSIFICATION;
                } else {
                    modelType = MLModel.ModelType.REGRESSION;
                }
            }
            
            // Get the appropriate training strategy
            TrainingStrategy strategy = algorithmStrategyRegistry.getStrategy(algorithmName, modelType);
            log.info("Training {} model using {} algorithm...", modelType, strategy.getAlgorithmName());
            
            Model<?> trainedModel;
            MutableDataset<?> trainingDataset = tribuoDataset;
            MutableDataset<?> testDataset = null;
            Double accuracy;
            Double crossValidationScore = null;
            
            // Handle train/test split
            if (request.trainTestSplit() > 0 && request.trainTestSplit() < 100) {
                log.info("Splitting dataset: {}% training, {}% testing", 
                    request.trainTestSplit(), 100 - request.trainTestSplit());
                DatasetSplitter.SplitResult splitResult = datasetSplitter.splitDataset(
                    tribuoDataset, 
                    request.trainTestSplit()
                );
                trainingDataset = splitResult.getTrain();
                testDataset = splitResult.getTest();
                log.info("Training on {} examples, will evaluate on {} examples", 
                    trainingDataset.size(), testDataset.size());
            }
            
            // Handle cross-validation if requested
            if (request.crossValidation()) {
                log.info("Performing cross-validation...");
                // Note: Cross-validation is performed for evaluation, but we still train on full training set
                // In a full implementation, we would use the CV results for hyperparameter tuning
                // For now, we'll train normally and note that CV was requested
                CrossValidationEvaluator.CrossValidationResult cvResult = 
                    crossValidationEvaluator.performCrossValidation(trainingDataset);
                crossValidationScore = cvResult.getAverageAccuracy();
                log.info("Cross-validation completed - Average accuracy: {}", crossValidationScore);
            }
            
            // Train the model on training dataset
            trainedModel = strategy.train(trainingDataset, parameters);
            log.info("Model training completed successfully");
            
            // Evaluate on test set if available, otherwise use training set
            MutableDataset<?> evaluationDataset = (testDataset != null) ? testDataset : trainingDataset;
            accuracy = calculateAccuracy(trainedModel, evaluationDataset);
            
            if (testDataset != null) {
                log.info("Model accuracy on test set: {}", accuracy);
            } else {
                log.info("Model accuracy on training set: {}", accuracy);
            }
            
            // Serialize and save model
            log.info("Serializing model...");
            String modelPath = serializeModel(trainedModel, request.modelName());
            log.info("Model serialized to: {}", modelPath);
            
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
            
            // Store training configuration in metadata
            Map<String, Object> trainingConfig = new HashMap<>();
            trainingConfig.put("algorithm", algorithmName);
            trainingConfig.put("trainTestSplit", request.trainTestSplit());
            trainingConfig.put("crossValidation", request.crossValidation());
            trainingConfig.put("hyperparameters", request.hyperparameters());
            trainingConfig.put("trainingSetSize", trainingDataset.size());
            if (testDataset != null) {
                trainingConfig.put("testSetSize", testDataset.size());
            }
            if (crossValidationScore != null) {
                trainingConfig.put("crossValidationScore", crossValidationScore);
            }
            
            // Convert to JSON string for storage
            try {
                com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
                mlModel.setModelMetadata(objectMapper.writeValueAsString(trainingConfig));
            } catch (Exception e) {
                log.warn("Failed to serialize training configuration: {}", e.getMessage());
            }
            
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
    public PaginatedResponse<ModelDto> getUserModels(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<MLModel> modelPage = modelRepository.findByDatasetOwnerIdOrderByTrainingDateDesc(userId, pageable);
        
        List<ModelDto> content = modelPage.getContent()
            .stream()
            .map(this::convertToDto)
            .toList();
        
        return PaginatedResponse.of(
            content,
            page,
            size,
            modelPage.getTotalElements()
        );
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
    
    @Transactional(readOnly = true)
    public ExtendedMetricsDto getExtendedMetrics(Long modelId, Long userId) {
        MLModel model = modelRepository.findByIdAndDatasetOwnerId(modelId, userId)
            .orElseThrow(() -> new ModelNotFoundException(modelId));
        
        // Return stored metrics from database
        // Extended calculation can be added later when needed
        List<List<Integer>> confusionMatrixList = new ArrayList<>();
        List<String> classLabels = new ArrayList<>();
        List<ExtendedMetricsDto.RocPoint> rocCurve = new ArrayList<>();
        
        // Parse stored confusion matrix if available
        if (model.getConfusionMatrix() != null && !model.getConfusionMatrix().isEmpty()) {
            try {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                String confusionMatrixJson = model.getConfusionMatrix();
                confusionMatrixList = mapper.readValue(confusionMatrixJson, 
                    new com.fasterxml.jackson.core.type.TypeReference<List<List<Integer>>>() {});
            } catch (Exception e) {
                log.debug("Could not parse stored confusion matrix: {}", e.getMessage());
            }
        }
        
        // Parse stored ROC curve if available
        if (model.getRocCurveData() != null && !model.getRocCurveData().isEmpty()) {
            try {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                String rocCurveJson = model.getRocCurveData();
                // Parse as list of maps and convert to RocPoint
                List<Map<String, Object>> rocData = mapper.readValue(rocCurveJson,
                    new com.fasterxml.jackson.core.type.TypeReference<List<Map<String, Object>>>() {});
                for (Map<String, Object> point : rocData) {
                    rocCurve.add(new ExtendedMetricsDto.RocPoint(
                        ((Number) point.getOrDefault("falsePositiveRate", 0.0)).doubleValue(),
                        ((Number) point.getOrDefault("truePositiveRate", 0.0)).doubleValue(),
                        ((Number) point.getOrDefault("threshold", 0.0)).doubleValue()
                    ));
                }
            } catch (Exception e) {
                log.debug("Could not parse stored ROC curve: {}", e.getMessage());
            }
        }
        
        // Extract feature importance (placeholder - equal importance for now)
        Map<String, Double> featureImportance = new LinkedHashMap<>();
        if (model.getFeatureNames() != null && !model.getFeatureNames().isEmpty()) {
            double equalImportance = 1.0 / model.getFeatureNames().size();
            for (String feature : model.getFeatureNames()) {
                featureImportance.put(feature, equalImportance);
            }
        }
        
        return new ExtendedMetricsDto(
            model.getAccuracy(),
            model.getPrecision(),
            model.getRecall(),
            model.getF1Score(),
            model.getMse(), // MSE for regression
            model.getRmse(), // RMSE for regression
            model.getMae(), // MAE for regression
            model.getR2Score() != null ? model.getR2Score() : model.getAccuracy(), // R² for regression
            confusionMatrixList,
            classLabels,
            rocCurve,
            featureImportance,
            new ArrayList<>() // Training history
        );
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
