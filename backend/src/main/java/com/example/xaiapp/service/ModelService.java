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
import org.tribuo.classification.sgd.linear.LogisticRegressionTrainer;
import org.tribuo.data.csv.CSVLoader;
import org.tribuo.regression.Regressor;
import org.tribuo.regression.RegressorFactory;
import org.tribuo.regression.sgd.linear.LinearSGDTrainer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import main.java.com.example.xaiapp.dto.TrainRequestDto;
import main.java.com.example.xaiapp.entity.Dataset;
import main.java.com.example.xaiapp.entity.MLModel;
import main.java.com.example.xaiapp.repository.DatasetRepository;
import main.java.com.example.xaiapp.repository.MLModelRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class ModelService {
    
    private final MLModelRepository modelRepository;
    private final DatasetRepository datasetRepository;
    
    @Value("${app.file.upload-dir}")
    private String uploadDir;
    
    public MLModel trainModel(TrainRequestDto request, Long userId) throws Exception {
        // Get dataset
        Dataset dataset = datasetRepository.findByIdAndOwnerId(request.getDatasetId(), userId)
            .orElseThrow(() -> new RuntimeException("Dataset not found or access denied"));
        
        // Check if model already exists for this dataset
        Optional<MLModel> existingModel = modelRepository.findByDataset(dataset);
        if (existingModel.isPresent()) {
            throw new RuntimeException("Model already exists for this dataset");
        }
        
        // Load and prepare data
        MutableDataset<?> tribuoDataset = loadDatasetFromCSV(dataset, request);
        
        // Train model based on type
        Model<?> trainedModel;
        MLModel.ModelType modelType = MLModel.ModelType.valueOf(request.getModelType());
        
        if (modelType == MLModel.ModelType.CLASSIFICATION) {
            trainedModel = trainClassificationModel(tribuoDataset);
        } else {
            trainedModel = trainRegressionModel(tribuoDataset);
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
            return csvLoader.loadDataSource(csvPath, request.getTargetVariable(), featureNames);
        } else {
            RegressorFactory regressorFactory = new RegressorFactory();
            CSVLoader<Regressor> csvLoader = new CSVLoader<>(regressorFactory);
            return csvLoader.loadDataSource(csvPath, request.getTargetVariable(), featureNames);
        }
    }
    
    private Model<Label> trainClassificationModel(MutableDataset<?> dataset) {
        @SuppressWarnings("unchecked")
        MutableDataset<Label> labelDataset = (MutableDataset<Label>) dataset;
        
        LogisticRegressionTrainer trainer = new LogisticRegressionTrainer();
        return trainer.train(labelDataset);
    }
    
    private Model<Regressor> trainRegressionModel(MutableDataset<?> dataset) {
        @SuppressWarnings("unchecked")
        MutableDataset<Regressor> regressorDataset = (MutableDataset<Regressor>) dataset;
        
        LinearSGDTrainer trainer = new LinearSGDTrainer();
        return trainer.train(regressorDataset);
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
            Evaluation<?> evaluation = model.evaluate(dataset);
            return evaluation.accuracy();
        } catch (Exception e) {
            log.warn("Could not calculate accuracy: {}", e.getMessage());
            return null;
        }
    }
    
    public MLModel getModel(Long modelId, Long userId) {
        return modelRepository.findByIdAndDatasetOwnerId(modelId, userId)
            .orElseThrow(() -> new RuntimeException("Model not found or access denied"));
    }
    
    public List<MLModel> getUserModels(Long userId) {
        return modelRepository.findByDatasetOwnerId(userId);
    }
    
    public void deleteModel(Long modelId, Long userId) throws IOException {
        MLModel model = getModel(modelId, userId);
        
        // Delete model file
        Path modelPath = Paths.get(model.getSerializedModelPath());
        if (Files.exists(modelPath)) {
            Files.delete(modelPath);
        }
        
        // Delete from database
        modelRepository.delete(model);
    }
}
