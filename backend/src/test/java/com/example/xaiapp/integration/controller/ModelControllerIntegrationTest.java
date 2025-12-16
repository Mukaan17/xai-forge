package com.example.xaiapp.integration.controller;

import com.example.xaiapp.entity.Dataset;
import com.example.xaiapp.entity.MLModel;
import com.example.xaiapp.entity.User;
import com.example.xaiapp.repository.DatasetRepository;
import com.example.xaiapp.repository.MLModelRepository;
import com.example.xaiapp.repository.UserRepository;
import com.example.xaiapp.util.TestConstants;
import com.example.xaiapp.util.TestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for ModelController
 * Tests complete model lifecycle with real database and file operations
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Transactional
class ModelControllerIntegrationTest {
    
    @LocalServerPort
    private int port;
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private DatasetRepository datasetRepository;
    
    @Autowired
    private MLModelRepository modelRepository;
    
    private String baseUrl;
    private String authToken;
    private User testUser;
    private Dataset testDataset;
    
    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port;
        
        // Clean up before each test
        modelRepository.deleteAll();
        datasetRepository.deleteAll();
        userRepository.deleteAll();
        
        // Create test user and get auth token
        testUser = TestDataBuilder.createTestUser();
        userRepository.save(testUser);
        authToken = getAuthToken();
        
        // Create test dataset
        testDataset = TestDataBuilder.createTestDataset(testUser);
        datasetRepository.save(testDataset);
    }
    
    private String getAuthToken() {
        // Login to get JWT token
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        
        String loginRequest = String.format(
            "{\"username\":\"%s\",\"password\":\"%s\"}",
            TestConstants.TEST_USERNAME,
            TestConstants.TEST_PASSWORD
        );
        
        HttpEntity<String> request = new HttpEntity<>(loginRequest, headers);
        ResponseEntity<String> response = restTemplate.exchange(
            baseUrl + TestConstants.AUTH_LOGIN_ENDPOINT,
            HttpMethod.POST,
            request,
            String.class
        );
        
        // Extract token from response
        String responseBody = response.getBody();
        if (responseBody != null && responseBody.contains("accessToken")) {
            return responseBody.substring(
                responseBody.indexOf("\"accessToken\":\"") + 16,
                responseBody.indexOf("\"", responseBody.indexOf("\"accessToken\":\"") + 16)
            );
        }
        return null;
    }
    
    @Test
    void testTrainModel_Success_Classification() {
        // Arrange
        Map<String, Object> trainRequest = Map.of(
            "datasetId", testDataset.getId(),
            "modelName", TestConstants.TEST_MODEL_NAME,
            "modelType", TestConstants.TEST_MODEL_TYPE_CLASSIFICATION,
            "targetVariable", TestConstants.TEST_TARGET_VARIABLE,
            "featureNames", TestConstants.TEST_FEATURE_NAMES
        );
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + authToken);
        headers.set("Content-Type", "application/json");
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(trainRequest, headers);
        
        // Act
        ResponseEntity<MLModel> response = restTemplate.exchange(
            baseUrl + TestConstants.MODELS_TRAIN_ENDPOINT,
            HttpMethod.POST,
            request,
            MLModel.class
        );
        
        // Assert
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(TestConstants.TEST_MODEL_NAME, response.getBody().getModelName());
        assertEquals(MLModel.ModelType.CLASSIFICATION, response.getBody().getModelType());
        assertEquals(TestConstants.TEST_TARGET_VARIABLE, response.getBody().getTargetVariable());
        assertEquals(List.of(TestConstants.TEST_FEATURE_NAMES), response.getBody().getFeatureNames());
        assertNotNull(response.getBody().getAccuracy());
        assertTrue(response.getBody().getAccuracy() >= 0.0);
        assertTrue(response.getBody().getAccuracy() <= 1.0);
        
        // Verify model was saved in database
        List<MLModel> models = modelRepository.findByDatasetOwnerId(testUser.getId());
        assertEquals(1, models.size());
        assertEquals(TestConstants.TEST_MODEL_NAME, models.get(0).getModelName());
    }
    
    @Test
    void testTrainModel_Success_Regression() {
        // Arrange
        Map<String, Object> trainRequest = Map.of(
            "datasetId", testDataset.getId(),
            "modelName", "Test Regression Model",
            "modelType", TestConstants.TEST_MODEL_TYPE_REGRESSION,
            "targetVariable", "price",
            "featureNames", List.of("bedrooms", "bathrooms", "sqft")
        );
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + authToken);
        headers.set("Content-Type", "application/json");
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(trainRequest, headers);
        
        // Act
        ResponseEntity<MLModel> response = restTemplate.exchange(
            baseUrl + TestConstants.MODELS_TRAIN_ENDPOINT,
            HttpMethod.POST,
            request,
            MLModel.class
        );
        
        // Assert
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("Test Regression Model", response.getBody().getModelName());
        assertEquals(MLModel.ModelType.REGRESSION, response.getBody().getModelType());
        assertEquals("price", response.getBody().getTargetVariable());
        assertEquals(List.of("bedrooms", "bathrooms", "sqft"), response.getBody().getFeatureNames());
    }
    
    @Test
    void testTrainModel_Unauthorized() {
        // Arrange
        Map<String, Object> trainRequest = Map.of(
            "datasetId", testDataset.getId(),
            "modelName", TestConstants.TEST_MODEL_NAME,
            "modelType", TestConstants.TEST_MODEL_TYPE_CLASSIFICATION,
            "targetVariable", TestConstants.TEST_TARGET_VARIABLE,
            "featureNames", TestConstants.TEST_FEATURE_NAMES
        );
        
        HttpHeaders headers = new HttpHeaders();
        // No Authorization header
        headers.set("Content-Type", "application/json");
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(trainRequest, headers);
        
        // Act
        ResponseEntity<String> response = restTemplate.exchange(
            baseUrl + TestConstants.MODELS_TRAIN_ENDPOINT,
            HttpMethod.POST,
            request,
            String.class
        );
        
        // Assert
        assertEquals(401, response.getStatusCode().value());
        assertTrue(response.getBody().contains("Unauthorized"));
    }
    
    @Test
    void testTrainModel_InvalidDataset() {
        // Arrange
        Map<String, Object> trainRequest = Map.of(
            "datasetId", 999L, // Non-existent dataset
            "modelName", TestConstants.TEST_MODEL_NAME,
            "modelType", TestConstants.TEST_MODEL_TYPE_CLASSIFICATION,
            "targetVariable", TestConstants.TEST_TARGET_VARIABLE,
            "featureNames", TestConstants.TEST_FEATURE_NAMES
        );
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + authToken);
        headers.set("Content-Type", "application/json");
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(trainRequest, headers);
        
        // Act
        ResponseEntity<String> response = restTemplate.exchange(
            baseUrl + TestConstants.MODELS_TRAIN_ENDPOINT,
            HttpMethod.POST,
            request,
            String.class
        );
        
        // Assert
        assertEquals(400, response.getStatusCode().value());
        assertTrue(response.getBody().contains("Invalid dataset"));
    }
    
    @Test
    void testTrainModel_InvalidModelType() {
        // Arrange
        Map<String, Object> trainRequest = Map.of(
            "datasetId", testDataset.getId(),
            "modelName", TestConstants.TEST_MODEL_NAME,
            "modelType", "INVALID_TYPE",
            "targetVariable", TestConstants.TEST_TARGET_VARIABLE,
            "featureNames", TestConstants.TEST_FEATURE_NAMES
        );
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + authToken);
        headers.set("Content-Type", "application/json");
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(trainRequest, headers);
        
        // Act
        ResponseEntity<String> response = restTemplate.exchange(
            baseUrl + TestConstants.MODELS_TRAIN_ENDPOINT,
            HttpMethod.POST,
            request,
            String.class
        );
        
        // Assert
        assertEquals(400, response.getStatusCode().value());
        assertTrue(response.getBody().contains("Invalid parameters"));
    }
    
    @Test
    void testTrainModel_MissingFields() {
        // Arrange
        Map<String, Object> trainRequest = Map.of(
            "datasetId", testDataset.getId(),
            "modelName", TestConstants.TEST_MODEL_NAME
            // Missing required fields
        );
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + authToken);
        headers.set("Content-Type", "application/json");
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(trainRequest, headers);
        
        // Act
        ResponseEntity<String> response = restTemplate.exchange(
            baseUrl + TestConstants.MODELS_TRAIN_ENDPOINT,
            HttpMethod.POST,
            request,
            String.class
        );
        
        // Assert
        assertEquals(400, response.getStatusCode().value());
        assertTrue(response.getBody().contains("Required fields missing"));
    }
    
    @Test
    void testGetUserModels_Success() {
        // Arrange - Create test models
        MLModel model1 = TestDataBuilder.createTestModel(testDataset, "Model 1");
        MLModel model2 = TestDataBuilder.createTestModel(testDataset, "Model 2");
        modelRepository.save(model1);
        modelRepository.save(model2);
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + authToken);
        HttpEntity<String> request = new HttpEntity<>(headers);
        
        // Act
        ResponseEntity<MLModel[]> response = restTemplate.exchange(
            baseUrl + TestConstants.MODELS_LIST_ENDPOINT,
            HttpMethod.GET,
            request,
            MLModel[].class
        );
        
        // Assert
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().length);
    }
    
    @Test
    void testGetUserModels_EmptyList() {
        // Arrange
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + authToken);
        HttpEntity<String> request = new HttpEntity<>(headers);
        
        // Act
        ResponseEntity<MLModel[]> response = restTemplate.exchange(
            baseUrl + TestConstants.MODELS_LIST_ENDPOINT,
            HttpMethod.GET,
            request,
            MLModel[].class
        );
        
        // Assert
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(0, response.getBody().length);
    }
    
    @Test
    void testGetUserModels_Unauthorized() {
        // Arrange
        HttpHeaders headers = new HttpHeaders();
        // No Authorization header
        HttpEntity<String> request = new HttpEntity<>(headers);
        
        // Act
        ResponseEntity<String> response = restTemplate.exchange(
            baseUrl + TestConstants.MODELS_LIST_ENDPOINT,
            HttpMethod.GET,
            request,
            String.class
        );
        
        // Assert
        assertEquals(401, response.getStatusCode().value());
        assertTrue(response.getBody().contains("Unauthorized"));
    }
    
    @Test
    void testGetModel_Success() {
        // Arrange - Create test model
        MLModel testModel = TestDataBuilder.createTestModel(testDataset, "Test Model");
        modelRepository.save(testModel);
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + authToken);
        HttpEntity<String> request = new HttpEntity<>(headers);
        
        // Act
        ResponseEntity<MLModel> response = restTemplate.exchange(
            baseUrl + TestConstants.MODELS_LIST_ENDPOINT + "/" + testModel.getId(),
            HttpMethod.GET,
            request,
            MLModel.class
        );
        
        // Assert
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(testModel.getId(), response.getBody().getId());
        assertEquals("Test Model", response.getBody().getModelName());
    }
    
    @Test
    void testGetModel_NotFound() {
        // Arrange
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + authToken);
        HttpEntity<String> request = new HttpEntity<>(headers);
        
        // Act
        ResponseEntity<String> response = restTemplate.exchange(
            baseUrl + TestConstants.MODELS_LIST_ENDPOINT + "/999",
            HttpMethod.GET,
            request,
            String.class
        );
        
        // Assert
        assertEquals(400, response.getStatusCode().value());
        assertTrue(response.getBody().contains("Model not found"));
    }
    
    @Test
    void testGetModel_Unauthorized() {
        // Arrange
        HttpHeaders headers = new HttpHeaders();
        // No Authorization header
        HttpEntity<String> request = new HttpEntity<>(headers);
        
        // Act
        ResponseEntity<String> response = restTemplate.exchange(
            baseUrl + TestConstants.MODELS_LIST_ENDPOINT + "/1",
            HttpMethod.GET,
            request,
            String.class
        );
        
        // Assert
        assertEquals(401, response.getStatusCode().value());
        assertTrue(response.getBody().contains("Unauthorized"));
    }
    
    @Test
    void testMakePrediction_Success() {
        // Arrange - Create test model
        MLModel testModel = TestDataBuilder.createTestModel(testDataset, "Test Model");
        modelRepository.save(testModel);
        
        Map<String, String> inputData = Map.of(
            "feature1", "1.5",
            "feature2", "2.3",
            "feature3", "0.8"
        );
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + authToken);
        headers.set("Content-Type", "application/json");
        HttpEntity<Map<String, String>> request = new HttpEntity<>(inputData, headers);
        
        // Act
        ResponseEntity<String> response = restTemplate.exchange(
            baseUrl + TestConstants.MODELS_PREDICT_ENDPOINT.replace("{id}", testModel.getId().toString()),
            HttpMethod.POST,
            request,
            String.class
        );
        
        // Assert
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("prediction"));
    }
    
    @Test
    void testMakePrediction_ModelNotFound() {
        // Arrange
        Map<String, String> inputData = Map.of(
            "feature1", "1.5",
            "feature2", "2.3",
            "feature3", "0.8"
        );
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + authToken);
        headers.set("Content-Type", "application/json");
        HttpEntity<Map<String, String>> request = new HttpEntity<>(inputData, headers);
        
        // Act
        ResponseEntity<String> response = restTemplate.exchange(
            baseUrl + TestConstants.MODELS_PREDICT_ENDPOINT.replace("{id}", "999"),
            HttpMethod.POST,
            request,
            String.class
        );
        
        // Assert
        assertEquals(400, response.getStatusCode().value());
        assertTrue(response.getBody().contains("Failed to make prediction"));
    }
    
    @Test
    void testMakePrediction_InvalidInput() {
        // Arrange - Create test model
        MLModel testModel = TestDataBuilder.createTestModel(testDataset, "Test Model");
        modelRepository.save(testModel);
        
        Map<String, String> invalidInput = Map.of(
            "invalid_feature", "invalid_value"
        );
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + authToken);
        headers.set("Content-Type", "application/json");
        HttpEntity<Map<String, String>> request = new HttpEntity<>(invalidInput, headers);
        
        // Act
        ResponseEntity<String> response = restTemplate.exchange(
            baseUrl + TestConstants.MODELS_PREDICT_ENDPOINT.replace("{id}", testModel.getId().toString()),
            HttpMethod.POST,
            request,
            String.class
        );
        
        // Assert
        assertEquals(400, response.getStatusCode().value());
        assertTrue(response.getBody().contains("Failed to make prediction"));
    }
    
    @Test
    void testGetExplanation_Success() {
        // Arrange - Create test model
        MLModel testModel = TestDataBuilder.createTestModel(testDataset, "Test Model");
        modelRepository.save(testModel);
        
        Map<String, String> inputData = Map.of(
            "feature1", "1.5",
            "feature2", "2.3",
            "feature3", "0.8"
        );
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + authToken);
        headers.set("Content-Type", "application/json");
        HttpEntity<Map<String, String>> request = new HttpEntity<>(inputData, headers);
        
        // Act
        ResponseEntity<String> response = restTemplate.exchange(
            baseUrl + TestConstants.MODELS_EXPLAIN_ENDPOINT.replace("{id}", testModel.getId().toString()),
            HttpMethod.POST,
            request,
            String.class
        );
        
        // Assert
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("prediction"));
        assertTrue(response.getBody().contains("explanation"));
    }
    
    @Test
    void testGetExplanation_ModelNotFound() {
        // Arrange
        Map<String, String> inputData = Map.of(
            "feature1", "1.5",
            "feature2", "2.3",
            "feature3", "0.8"
        );
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + authToken);
        headers.set("Content-Type", "application/json");
        HttpEntity<Map<String, String>> request = new HttpEntity<>(inputData, headers);
        
        // Act
        ResponseEntity<String> response = restTemplate.exchange(
            baseUrl + TestConstants.MODELS_EXPLAIN_ENDPOINT.replace("{id}", "999"),
            HttpMethod.POST,
            request,
            String.class
        );
        
        // Assert
        assertEquals(400, response.getStatusCode().value());
        assertTrue(response.getBody().contains("Failed to generate explanation"));
    }
    
    @Test
    void testDeleteModel_Success() {
        // Arrange - Create test model
        MLModel testModel = TestDataBuilder.createTestModel(testDataset, "Test Model");
        modelRepository.save(testModel);
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + authToken);
        HttpEntity<String> request = new HttpEntity<>(headers);
        
        // Act
        ResponseEntity<String> response = restTemplate.exchange(
            baseUrl + TestConstants.MODELS_LIST_ENDPOINT + "/" + testModel.getId(),
            HttpMethod.DELETE,
            request,
            String.class
        );
        
        // Assert
        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().contains("Model deleted successfully"));
        
        // Verify model was deleted from database
        assertFalse(modelRepository.findById(testModel.getId()).isPresent());
    }
    
    @Test
    void testDeleteModel_NotFound() {
        // Arrange
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + authToken);
        HttpEntity<String> request = new HttpEntity<>(headers);
        
        // Act
        ResponseEntity<String> response = restTemplate.exchange(
            baseUrl + TestConstants.MODELS_LIST_ENDPOINT + "/999",
            HttpMethod.DELETE,
            request,
            String.class
        );
        
        // Assert
        assertEquals(400, response.getStatusCode().value());
        assertTrue(response.getBody().contains("Failed to delete model"));
    }
    
    @Test
    void testDeleteModel_Unauthorized() {
        // Arrange
        HttpHeaders headers = new HttpHeaders();
        // No Authorization header
        HttpEntity<String> request = new HttpEntity<>(headers);
        
        // Act
        ResponseEntity<String> response = restTemplate.exchange(
            baseUrl + TestConstants.MODELS_LIST_ENDPOINT + "/1",
            HttpMethod.DELETE,
            request,
            String.class
        );
        
        // Assert
        assertEquals(401, response.getStatusCode().value());
        assertTrue(response.getBody().contains("Unauthorized"));
    }
    
    @Test
    void testDeleteModel_OtherUserModel() {
        // Arrange - Create another user and model
        User otherUser = TestDataBuilder.createTestUser("otheruser");
        userRepository.save(otherUser);
        Dataset otherDataset = TestDataBuilder.createTestDataset(otherUser, "other-dataset.csv");
        datasetRepository.save(otherDataset);
        MLModel otherModel = TestDataBuilder.createTestModel(otherDataset, "Other Model");
        modelRepository.save(otherModel);
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + authToken);
        HttpEntity<String> request = new HttpEntity<>(headers);
        
        // Act
        ResponseEntity<String> response = restTemplate.exchange(
            baseUrl + TestConstants.MODELS_LIST_ENDPOINT + "/" + otherModel.getId(),
            HttpMethod.DELETE,
            request,
            String.class
        );
        
        // Assert
        assertEquals(400, response.getStatusCode().value());
        assertTrue(response.getBody().contains("Failed to delete model"));
        
        // Verify other user's model still exists
        assertTrue(modelRepository.findById(otherModel.getId()).isPresent());
    }
    
    @Test
    void testTrainModel_ConcurrentTraining() {
        // Arrange
        Map<String, Object> trainRequest = Map.of(
            "datasetId", testDataset.getId(),
            "modelName", TestConstants.TEST_MODEL_NAME,
            "modelType", TestConstants.TEST_MODEL_TYPE_CLASSIFICATION,
            "targetVariable", TestConstants.TEST_TARGET_VARIABLE,
            "featureNames", TestConstants.TEST_FEATURE_NAMES
        );
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + authToken);
        headers.set("Content-Type", "application/json");
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(trainRequest, headers);
        
        // Act - First training should succeed
        ResponseEntity<MLModel> response1 = restTemplate.exchange(
            baseUrl + TestConstants.MODELS_TRAIN_ENDPOINT,
            HttpMethod.POST,
            request,
            MLModel.class
        );
        
        // Second training should fail (model already exists)
        ResponseEntity<String> response2 = restTemplate.exchange(
            baseUrl + TestConstants.MODELS_TRAIN_ENDPOINT,
            HttpMethod.POST,
            request,
            String.class
        );
        
        // Assert
        assertEquals(200, response1.getStatusCode().value());
        assertEquals(500, response2.getStatusCode().value());
        assertTrue(response2.getBody().contains("Training failed"));
    }
}
