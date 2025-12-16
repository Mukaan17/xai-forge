package com.example.xaiapp.util;

import com.example.xaiapp.dto.TrainRequestDto;
import com.example.xaiapp.entity.Dataset;
import com.example.xaiapp.entity.MLModel;
import com.example.xaiapp.entity.User;
import com.example.xaiapp.entity.MLModel.ModelType;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * Test data builder utility for creating test entities
 * Provides fluent API for building test objects with sensible defaults
 */
public class TestDataBuilder {
    
    /**
     * Create a test user with default values
     */
    public static User createTestUser() {
        return createTestUser(TestConstants.TEST_USERNAME);
    }
    
    /**
     * Create a test user with custom username
     */
    public static User createTestUser(String username) {
        User user = new User();
        user.setId(1L);
        user.setUsername(username);
        user.setEmail(username + "@test.com");
        user.setPassword(TestConstants.TEST_ENCODED_PASSWORD);
        user.setCreatedAt(LocalDateTime.now());
        return user;
    }
    
    /**
     * Create a test user with all custom fields
     */
    public static User createTestUser(Long id, String username, String email, String password) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(password);
        user.setCreatedAt(LocalDateTime.now());
        return user;
    }
    
    /**
     * Create a test dataset with default values
     */
    public static Dataset createTestDataset() {
        return createTestDataset(TestDataBuilder.createTestUser());
    }
    
    /**
     * Create a test dataset with custom owner
     */
    public static Dataset createTestDataset(User owner) {
        return createTestDataset(owner, TestConstants.TEST_DATASET_NAME);
    }
    
    /**
     * Create a test dataset with custom owner and filename
     */
    public static Dataset createTestDataset(User owner, String filename) {
        Dataset dataset = new Dataset();
        dataset.setId(1L);
        dataset.setFileName(filename);
        dataset.setFilePath(TestConstants.TEST_DATASET_PATH);
        dataset.setRowCount(100L);
        dataset.setHeaders(Arrays.asList("feature1", "feature2", "feature3", "target"));
        dataset.setOwner(owner);
        dataset.setUploadDate(LocalDateTime.now());
        return dataset;
    }
    
    /**
     * Create a test dataset with all custom fields
     */
    public static Dataset createTestDataset(Long id, User owner, String filename, String filePath, 
                                           Long rowCount, List<String> headers) {
        Dataset dataset = new Dataset();
        dataset.setId(id);
        dataset.setFileName(filename);
        dataset.setFilePath(filePath);
        dataset.setRowCount(rowCount);
        dataset.setHeaders(headers);
        dataset.setOwner(owner);
        dataset.setUploadDate(LocalDateTime.now());
        return dataset;
    }
    
    /**
     * Create a test ML model with default values
     */
    public static MLModel createTestModel() {
        return createTestModel(TestDataBuilder.createTestDataset());
    }
    
    /**
     * Create a test ML model with custom dataset
     */
    public static MLModel createTestModel(Dataset dataset) {
        return createTestModel(dataset, TestConstants.TEST_MODEL_NAME);
    }
    
    /**
     * Create a test ML model with custom dataset and name
     */
    public static MLModel createTestModel(Dataset dataset, String modelName) {
        MLModel model = new MLModel();
        model.setId(1L);
        model.setModelName(modelName);
        model.setModelType(ModelType.CLASSIFICATION);
        model.setSerializedModelPath(TestConstants.TEST_MODELS_DIR + "/" + modelName + ".model");
        model.setTargetVariable(TestConstants.TEST_TARGET_VARIABLE);
        model.setFeatureNames(Arrays.asList(TestConstants.TEST_FEATURE_NAMES));
        model.setDataset(dataset);
        model.setAccuracy(0.85);
        // trainingDate is auto-set via @PrePersist
        return model;
    }
    
    /**
     * Create a test ML model with all custom fields
     */
    public static MLModel createTestModel(Long id, Dataset dataset, String modelName, 
                                        ModelType modelType, String targetVariable, 
                                        List<String> featureNames, Double accuracy) {
        MLModel model = new MLModel();
        model.setId(id);
        model.setModelName(modelName);
        model.setModelType(modelType);
        model.setSerializedModelPath(TestConstants.TEST_MODELS_DIR + "/" + modelName + ".model");
        model.setTargetVariable(targetVariable);
        model.setFeatureNames(featureNames);
        model.setDataset(dataset);
        model.setAccuracy(accuracy);
        // trainingDate is auto-set via @PrePersist
        return model;
    }
    
    /**
     * Create a test training request with default values
     */
    public static TrainRequestDto createTrainRequest() {
        return createTrainRequest(1L);
    }
    
    /**
     * Create a test training request with custom dataset ID
     */
    public static TrainRequestDto createTrainRequest(Long datasetId) {
        TrainRequestDto request = new TrainRequestDto();
        request.setDatasetId(datasetId);
        request.setModelName(TestConstants.TEST_MODEL_NAME);
        request.setModelType(TestConstants.TEST_MODEL_TYPE_CLASSIFICATION);
        request.setTargetVariable(TestConstants.TEST_TARGET_VARIABLE);
        request.setFeatureNames(Arrays.asList(TestConstants.TEST_FEATURE_NAMES));
        return request;
    }
    
    /**
     * Create a test training request with all custom fields
     */
    public static TrainRequestDto createTrainRequest(Long datasetId, String modelName, 
                                                   String modelType, String targetVariable, 
                                                   List<String> featureNames) {
        TrainRequestDto request = new TrainRequestDto();
        request.setDatasetId(datasetId);
        request.setModelName(modelName);
        request.setModelType(modelType);
        request.setTargetVariable(targetVariable);
        request.setFeatureNames(featureNames);
        return request;
    }
    
    /**
     * Create a regression training request
     */
    public static TrainRequestDto createRegressionTrainRequest(Long datasetId) {
        TrainRequestDto request = new TrainRequestDto();
        request.setDatasetId(datasetId);
        request.setModelName("Test Regression Model");
        request.setModelType(TestConstants.TEST_MODEL_TYPE_REGRESSION);
        request.setTargetVariable("price");
        request.setFeatureNames(Arrays.asList("bedrooms", "bathrooms", "sqft"));
        return request;
    }
    
    /**
     * Create a classification training request
     */
    public static TrainRequestDto createClassificationTrainRequest(Long datasetId) {
        TrainRequestDto request = new TrainRequestDto();
        request.setDatasetId(datasetId);
        request.setModelName("Test Classification Model");
        request.setModelType(TestConstants.TEST_MODEL_TYPE_CLASSIFICATION);
        request.setTargetVariable("churn");
        request.setFeatureNames(Arrays.asList("age", "tenure", "monthly_bill"));
        return request;
    }
    
    /**
     * Create test CSV content
     */
    public static String createTestCSVContent() {
        return TestConstants.TEST_CSV_DATA;
    }
    
    /**
     * Create test CSV content with custom data
     */
    public static String createTestCSVContent(String header, String... rows) {
        StringBuilder csv = new StringBuilder(header).append("\n");
        for (String row : rows) {
            csv.append(row).append("\n");
        }
        return csv.toString();
    }
    
    /**
     * Create test prediction input data
     */
    public static String createTestPredictionInput() {
        return TestConstants.TEST_PREDICTION_INPUT_JSON;
    }
    
    /**
     * Create test prediction input with custom values
     */
    public static String createTestPredictionInput(String feature1, String feature2, String feature3) {
        return String.format("""
            {
                "feature1": "%s",
                "feature2": "%s", 
                "feature3": "%s"
            }
            """, feature1, feature2, feature3);
    }
    
    // Private constructor to prevent instantiation
    private TestDataBuilder() {
        throw new UnsupportedOperationException("Utility class");
    }
}
