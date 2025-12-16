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
import org.tribuo.data.csv.CSVDataSource;
import org.tribuo.data.columnar.RowProcessor;
import org.tribuo.data.columnar.FieldProcessor;
import org.tribuo.data.columnar.processors.response.FieldResponseProcessor;
import org.tribuo.data.columnar.processors.field.DoubleFieldProcessor;
import java.util.HashMap;
import java.util.Map;
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
        log.info("Starting model training - User: {}, Model: {}, Dataset: {}", userId, request.getModelName(),
                request.getDatasetId());

        // Get dataset with proper transaction isolation
        Dataset dataset = datasetRepository.findByIdAndOwnerId(request.getDatasetId(), userId)
                .orElseThrow(() -> new DatasetNotFoundException(request.getDatasetId()));

        // Check if model already exists for this dataset with pessimistic locking
        Optional<MLModel> existingModel = modelRepository.findByDataset(dataset);
        if (existingModel.isPresent()) {
            log.warn("Model already exists for dataset {} - User: {}", request.getDatasetId(), userId);
            throw new ModelTrainingException("Model already exists for this dataset",
                    "A model has already been trained for this dataset. Please delete the existing model first.");
        }

        log.info("Loading dataset from CSV...");
        // Load and prepare data
        MutableDataset<?> tribuoDataset = loadDatasetFromCSV(dataset, request);
        log.info("Dataset loaded successfully - {} examples, {} features", tribuoDataset.size(),
                tribuoDataset.getFeatureMap().size());

        // Create parameters map for training configuration
        Map<String, Object> parameters = new HashMap<>();
        if (request.getEpochs() != null)
            parameters.put("epochs", request.getEpochs());
        if (request.getLearningRate() != null)
            parameters.put("learningRate", request.getLearningRate());
        if (request.getBatchSize() != null)
            parameters.put("batchSize", request.getBatchSize());

        // Train model based on type
        log.info("Training {} model...", request.getModelType());
        Model<?> trainedModel;
        MLModel.ModelType modelType = MLModel.ModelType.valueOf(request.getModelType());

        if (modelType == MLModel.ModelType.CLASSIFICATION) {
            trainedModel = classificationStrategy.train(tribuoDataset, parameters);
        } else {
            trainedModel = regressionStrategy.train(tribuoDataset, parameters);
        }
        log.info("Model training completed successfully");

        // Serialize and save model
        log.info("Serializing model...");
        String modelPath = serializeModel(trainedModel, request.getModelName());
        log.info("Model serialized to: {}", modelPath);

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

        log.info("Loading dataset from CSV: {}", csvPath);
        log.info("Target variable: {}, Feature names: {}", request.getTargetVariable(), request.getFeatureNames());

        // Load data based on model type
        if (request.getModelType().equals("CLASSIFICATION")) {
            try {
                LabelFactory labelFactory = new LabelFactory();

                // Get all columns and selected features
                List<String> allColumns = new ArrayList<>(dataset.getHeaders());
                List<String> selectedFeatures = new ArrayList<>(request.getFeatureNames());
                selectedFeatures.remove(request.getTargetVariable());

                // Create response processor for target variable
                // Constructor: FieldResponseProcessor(String fieldName, String outputName,
                // OutputFactory<T>)
                FieldResponseProcessor<Label> responseProcessor = new FieldResponseProcessor<Label>(
                        request.getTargetVariable(), request.getTargetVariable(), labelFactory);

                // Create field processors map - only add selected features
                Map<String, FieldProcessor> fieldProcessors = new HashMap<>();
                for (String column : allColumns) {
                    if (column.equals(request.getTargetVariable())) {
                        // Skip target - it's handled by response processor
                        continue;
                    } else if (selectedFeatures.contains(column)) {
                        // Add as feature (using DoubleFieldProcessor for numeric columns)
                        fieldProcessors.put(column, new DoubleFieldProcessor(column));
                    }
                    // Unselected columns are simply not added to the map (they'll be skipped)
                }

                // Create RowProcessor with response processor first, then field processors map
                // Constructor: RowProcessor(ResponseProcessor<T>, Map<String, FieldProcessor>)
                RowProcessor<Label> rowProcessor = new RowProcessor<Label>(responseProcessor, fieldProcessors);

                log.info("Target variable: {}, All columns: {}, Selected features: {}",
                        request.getTargetVariable(), allColumns, selectedFeatures);

                // Use CSVDataSource with RowProcessor (true = has header row)
                CSVDataSource<Label> dataSource = new CSVDataSource<>(csvPath, rowProcessor, true);
                MutableDataset<Label> loadedDataset = new MutableDataset<>(dataSource);

                log.info("Dataset loaded: {} examples, {} features",
                        loadedDataset.size(), loadedDataset.getFeatureMap().size());

                return loadedDataset;
            } catch (Exception e) {
                log.error("Error loading classification dataset: {}", e.getMessage(), e);
                log.error("CSV Path: {}, Target: {}, Features: {}", csvPath, request.getTargetVariable(),
                        request.getFeatureNames());
                e.printStackTrace();
                throw new IllegalArgumentException("Failed to load dataset: " + e.getMessage(), e);
            }
        } else {
            // For regression, use AlgorithmFactory to load data
            try {
                // Pass all columns (excluding target) so Tribuo can parse CSV correctly
                // Then filter to use only selected features
                List<String> allColumns = new ArrayList<>(dataset.getHeaders());
                List<String> selectedFeatures = new ArrayList<>(request.getFeatureNames());
                selectedFeatures.remove(request.getTargetVariable());

                return algorithmFactory.loadDatasetFromCSV(csvPath, request.getTargetVariable(),
                        selectedFeatures, MLModel.ModelType.REGRESSION, allColumns);
            } catch (Exception e) {
                log.error("Error loading regression dataset: {}", e.getMessage(), e);
                throw new IllegalArgumentException("Failed to load dataset: " + e.getMessage(), e);
            }
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
     * For regression models, returns R² (coefficient of determination) clamped to
     * [0.0, 1.0].
     * 
     * @param model   The trained model to evaluate
     * @param dataset The dataset to evaluate against
     * @return Accuracy score (0.0 to 1.0) or null if evaluation fails
     */
    private Double calculateAccuracy(Model<?> model, MutableDataset<?> dataset) {
        try {
            // For classification, implement real evaluation
            // Check if the model's output info is for classification by checking the class
            // name
            String outputInfoClassName = model.getOutputIDInfo().getClass().getName();
            if (outputInfoClassName.contains("LabelInfo") || outputInfoClassName.contains("classification")) {
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
        MLModel model = modelRepository.findByIdAndDatasetOwnerId(modelId, userId)
                .orElseThrow(() -> new ModelNotFoundException(modelId));

        // Explicitly initialize featureNames collection to force Hibernate to load it
        if (model.getFeatureNames() != null) {
            model.getFeatureNames().size(); // Force initialization
        }

        return model;
    }

    @Transactional(readOnly = true)
    public List<MLModel> getUserModels(Long userId) {
        List<MLModel> models = modelRepository.findByDatasetOwnerId(userId);

        // Explicitly initialize featureNames collection for each model
        for (MLModel model : models) {
            if (model.getFeatureNames() != null) {
                model.getFeatureNames().size(); // Force initialization
            }
        }

        return models;
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
