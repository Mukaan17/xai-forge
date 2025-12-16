package com.example.xaiapp.api;

import com.example.xaiapp.entity.Dataset;
import com.example.xaiapp.entity.MLModel;
import com.example.xaiapp.entity.User;
import com.example.xaiapp.repository.DatasetRepository;
import com.example.xaiapp.repository.MLModelRepository;
import com.example.xaiapp.repository.UserRepository;
import com.example.xaiapp.util.ApiTestBase;
import com.example.xaiapp.util.TestConstants;
import com.example.xaiapp.util.TestDataBuilder;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * REST Assured API contract tests for model endpoints
 * Tests training request/response, prediction API, explanation API
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Transactional
class ModelApiTest extends ApiTestBase {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private DatasetRepository datasetRepository;
    
    @Autowired
    private MLModelRepository modelRepository;
    
    private Dataset testDataset;
    
    @BeforeEach
    public void setUp() {
        super.setUp();
        modelRepository.deleteAll();
        datasetRepository.deleteAll();
        userRepository.deleteAll();
        
        // Create test user, authenticate, and create dataset
        createTestUserWorkflow();
        testDataset = TestDataBuilder.createTestDataset(TestDataBuilder.createTestUser());
        datasetRepository.save(testDataset);
    }
    
    @Test
    void testTrainModelApiContract() {
        // Arrange
        Map<String, Object> trainRequest = Map.of(
            "datasetId", testDataset.getId(),
            "modelName", TestConstants.TEST_MODEL_NAME,
            "modelType", TestConstants.TEST_MODEL_TYPE_CLASSIFICATION,
            "targetVariable", TestConstants.TEST_TARGET_VARIABLE,
            "featureNames", TestConstants.TEST_FEATURE_NAMES
        );
        
        // Act & Assert
        given()
            .header("Authorization", "Bearer " + authToken)
            .contentType(ContentType.JSON)
            .body(trainRequest)
        .when()
            .post(TestConstants.MODELS_TRAIN_ENDPOINT)
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("success", equalTo(true))
            .body("message", containsString("Model trained successfully"))
            .body("data", notNullValue())
            .body("data.id", notNullValue())
            .body("data.modelName", equalTo(TestConstants.TEST_MODEL_NAME))
            .body("data.modelType", equalTo(TestConstants.TEST_MODEL_TYPE_CLASSIFICATION))
            .body("data.targetVariable", equalTo(TestConstants.TEST_TARGET_VARIABLE))
            .body("data.featureNames", notNullValue())
            .body("data.accuracy", notNullValue())
            .body("data.createdAt", notNullValue())
            .body("data.dataset", notNullValue())
            .body("data.dataset.id", equalTo(testDataset.getId().intValue()))
            .body("data.dataset.fileName", notNullValue())
            .body("data.dataset.rowCount", notNullValue())
            .body("data.dataset.headers", notNullValue())
            .body("data.dataset.uploadDate", notNullValue())
            .body("data.dataset.owner", notNullValue())
            .body("data.dataset.owner.id", notNullValue())
            .body("data.dataset.owner.username", notNullValue())
            .body("data.dataset.owner.email", notNullValue())
            .body("data.dataset.owner.password", nullValue()); // Password should not be returned
    }
    
    @Test
    void testTrainModelApiContract_Regression() {
        // Arrange
        Map<String, Object> trainRequest = Map.of(
            "datasetId", testDataset.getId(),
            "modelName", "Test Regression Model",
            "modelType", TestConstants.TEST_MODEL_TYPE_REGRESSION,
            "targetVariable", "price",
            "featureNames", List.of("bedrooms", "bathrooms", "sqft")
        );
        
        // Act & Assert
        given()
            .header("Authorization", "Bearer " + authToken)
            .contentType(ContentType.JSON)
            .body(trainRequest)
        .when()
            .post(TestConstants.MODELS_TRAIN_ENDPOINT)
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("success", equalTo(true))
            .body("message", containsString("Model trained successfully"))
            .body("data", notNullValue())
            .body("data.id", notNullValue())
            .body("data.modelName", equalTo("Test Regression Model"))
            .body("data.modelType", equalTo(TestConstants.TEST_MODEL_TYPE_REGRESSION))
            .body("data.targetVariable", equalTo("price"))
            .body("data.featureNames", notNullValue())
            .body("data.accuracy", notNullValue())
            .body("data.createdAt", notNullValue());
    }
    
    @Test
    void testTrainModelApiContract_Unauthorized() {
        // Arrange
        Map<String, Object> trainRequest = Map.of(
            "datasetId", testDataset.getId(),
            "modelName", TestConstants.TEST_MODEL_NAME,
            "modelType", TestConstants.TEST_MODEL_TYPE_CLASSIFICATION,
            "targetVariable", TestConstants.TEST_TARGET_VARIABLE,
            "featureNames", TestConstants.TEST_FEATURE_NAMES
        );
        
        // Act & Assert
        given()
            .contentType(ContentType.JSON)
            .body(trainRequest)
        .when()
            .post(TestConstants.MODELS_TRAIN_ENDPOINT)
        .then()
            .statusCode(401)
            .contentType(ContentType.JSON)
            .body("success", equalTo(false))
            .body("message", containsString("Unauthorized"))
            .body("error", notNullValue())
            .body("timestamp", notNullValue());
    }
    
    @Test
    void testTrainModelApiContract_InvalidDataset() {
        // Arrange
        Map<String, Object> trainRequest = Map.of(
            "datasetId", 999L, // Non-existent dataset
            "modelName", TestConstants.TEST_MODEL_NAME,
            "modelType", TestConstants.TEST_MODEL_TYPE_CLASSIFICATION,
            "targetVariable", TestConstants.TEST_TARGET_VARIABLE,
            "featureNames", TestConstants.TEST_FEATURE_NAMES
        );
        
        // Act & Assert
        given()
            .header("Authorization", "Bearer " + authToken)
            .contentType(ContentType.JSON)
            .body(trainRequest)
        .when()
            .post(TestConstants.MODELS_TRAIN_ENDPOINT)
        .then()
            .statusCode(400)
            .contentType(ContentType.JSON)
            .body("success", equalTo(false))
            .body("message", containsString("Invalid dataset"))
            .body("error", notNullValue())
            .body("timestamp", notNullValue());
    }
    
    @Test
    void testTrainModelApiContract_InvalidModelType() {
        // Arrange
        Map<String, Object> trainRequest = Map.of(
            "datasetId", testDataset.getId(),
            "modelName", TestConstants.TEST_MODEL_NAME,
            "modelType", "INVALID_TYPE",
            "targetVariable", TestConstants.TEST_TARGET_VARIABLE,
            "featureNames", TestConstants.TEST_FEATURE_NAMES
        );
        
        // Act & Assert
        given()
            .header("Authorization", "Bearer " + authToken)
            .contentType(ContentType.JSON)
            .body(trainRequest)
        .when()
            .post(TestConstants.MODELS_TRAIN_ENDPOINT)
        .then()
            .statusCode(400)
            .contentType(ContentType.JSON)
            .body("success", equalTo(false))
            .body("message", containsString("Invalid parameters"))
            .body("error", notNullValue())
            .body("timestamp", notNullValue());
    }
    
    @Test
    void testTrainModelApiContract_MissingFields() {
        // Arrange
        Map<String, Object> trainRequest = Map.of(
            "datasetId", testDataset.getId(),
            "modelName", TestConstants.TEST_MODEL_NAME
            // Missing required fields
        );
        
        // Act & Assert
        given()
            .header("Authorization", "Bearer " + authToken)
            .contentType(ContentType.JSON)
            .body(trainRequest)
        .when()
            .post(TestConstants.MODELS_TRAIN_ENDPOINT)
        .then()
            .statusCode(400)
            .contentType(ContentType.JSON)
            .body("success", equalTo(false))
            .body("message", containsString("Required fields missing"))
            .body("error", notNullValue())
            .body("timestamp", notNullValue());
    }
    
    @Test
    void testGetModelsApiContract() {
        // Arrange - Create test models
        MLModel model1 = TestDataBuilder.createTestModel(testDataset, "Model 1");
        MLModel model2 = TestDataBuilder.createTestModel(testDataset, "Model 2");
        modelRepository.save(model1);
        modelRepository.save(model2);
        
        // Act & Assert
        given()
            .header("Authorization", "Bearer " + authToken)
        .when()
            .get(TestConstants.MODELS_LIST_ENDPOINT)
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("success", equalTo(true))
            .body("message", containsString("Models retrieved successfully"))
            .body("data", notNullValue())
            .body("data", hasSize(2))
            .body("data[0].id", notNullValue())
            .body("data[0].modelName", notNullValue())
            .body("data[0].modelType", notNullValue())
            .body("data[0].targetVariable", notNullValue())
            .body("data[0].featureNames", notNullValue())
            .body("data[0].accuracy", notNullValue())
            .body("data[0].createdAt", notNullValue())
            .body("data[0].dataset", notNullValue())
            .body("data[0].dataset.id", notNullValue())
            .body("data[0].dataset.fileName", notNullValue())
            .body("data[0].dataset.rowCount", notNullValue())
            .body("data[0].dataset.headers", notNullValue())
            .body("data[0].dataset.uploadDate", notNullValue())
            .body("data[0].dataset.owner", notNullValue())
            .body("data[0].dataset.owner.id", notNullValue())
            .body("data[0].dataset.owner.username", notNullValue())
            .body("data[0].dataset.owner.email", notNullValue())
            .body("data[0].dataset.owner.password", nullValue()); // Password should not be returned
    }
    
    @Test
    void testGetModelsApiContract_EmptyList() {
        // Act & Assert
        given()
            .header("Authorization", "Bearer " + authToken)
        .when()
            .get(TestConstants.MODELS_LIST_ENDPOINT)
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("success", equalTo(true))
            .body("message", containsString("Models retrieved successfully"))
            .body("data", notNullValue())
            .body("data", hasSize(0));
    }
    
    @Test
    void testGetModelsApiContract_Unauthorized() {
        // Act & Assert
        given()
        .when()
            .get(TestConstants.MODELS_LIST_ENDPOINT)
        .then()
            .statusCode(401)
            .contentType(ContentType.JSON)
            .body("success", equalTo(false))
            .body("message", containsString("Unauthorized"))
            .body("error", notNullValue())
            .body("timestamp", notNullValue());
    }
    
    @Test
    void testGetModelApiContract() {
        // Arrange - Create test model
        MLModel testModel = TestDataBuilder.createTestModel(testDataset, "Test Model");
        modelRepository.save(testModel);
        
        // Act & Assert
        given()
            .header("Authorization", "Bearer " + authToken)
        .when()
            .get(TestConstants.MODELS_LIST_ENDPOINT + "/" + testModel.getId())
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("success", equalTo(true))
            .body("message", containsString("Model retrieved successfully"))
            .body("data", notNullValue())
            .body("data.id", equalTo(testModel.getId().intValue()))
            .body("data.modelName", equalTo("Test Model"))
            .body("data.modelType", notNullValue())
            .body("data.targetVariable", notNullValue())
            .body("data.featureNames", notNullValue())
            .body("data.accuracy", notNullValue())
            .body("data.createdAt", notNullValue())
            .body("data.dataset", notNullValue())
            .body("data.dataset.id", notNullValue())
            .body("data.dataset.fileName", notNullValue())
            .body("data.dataset.rowCount", notNullValue())
            .body("data.dataset.headers", notNullValue())
            .body("data.dataset.uploadDate", notNullValue());
    }
    
    @Test
    void testGetModelApiContract_NotFound() {
        // Act & Assert
        given()
            .header("Authorization", "Bearer " + authToken)
        .when()
            .get(TestConstants.MODELS_LIST_ENDPOINT + "/999")
        .then()
            .statusCode(400)
            .contentType(ContentType.JSON)
            .body("success", equalTo(false))
            .body("message", containsString("Model not found"))
            .body("error", notNullValue())
            .body("timestamp", notNullValue());
    }
    
    @Test
    void testGetModelApiContract_Unauthorized() {
        // Act & Assert
        given()
        .when()
            .get(TestConstants.MODELS_LIST_ENDPOINT + "/1")
        .then()
            .statusCode(401)
            .contentType(ContentType.JSON)
            .body("success", equalTo(false))
            .body("message", containsString("Unauthorized"))
            .body("error", notNullValue())
            .body("timestamp", notNullValue());
    }
    
    @Test
    void testMakePredictionApiContract() {
        // Arrange - Create test model
        MLModel testModel = TestDataBuilder.createTestModel(testDataset, "Test Model");
        modelRepository.save(testModel);
        
        Map<String, String> inputData = Map.of(
            "feature1", "1.5",
            "feature2", "2.3",
            "feature3", "0.8"
        );
        
        // Act & Assert
        given()
            .header("Authorization", "Bearer " + authToken)
            .contentType(ContentType.JSON)
            .body(inputData)
        .when()
            .post(TestConstants.MODELS_PREDICT_ENDPOINT.replace("{id}", testModel.getId().toString()))
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("success", equalTo(true))
            .body("message", containsString("Prediction generated successfully"))
            .body("data", notNullValue())
            .body("data.prediction", notNullValue())
            .body("data.confidenceScores", notNullValue())
            .body("data.inputData", notNullValue())
            .body("data.inputData.feature1", equalTo("1.5"))
            .body("data.inputData.feature2", equalTo("2.3"))
            .body("data.inputData.feature3", equalTo("0.8"));
    }
    
    @Test
    void testMakePredictionApiContract_ModelNotFound() {
        // Arrange
        Map<String, String> inputData = Map.of(
            "feature1", "1.5",
            "feature2", "2.3",
            "feature3", "0.8"
        );
        
        // Act & Assert
        given()
            .header("Authorization", "Bearer " + authToken)
            .contentType(ContentType.JSON)
            .body(inputData)
        .when()
            .post(TestConstants.MODELS_PREDICT_ENDPOINT.replace("{id}", "999"))
        .then()
            .statusCode(400)
            .contentType(ContentType.JSON)
            .body("success", equalTo(false))
            .body("message", containsString("Failed to make prediction"))
            .body("error", notNullValue())
            .body("timestamp", notNullValue());
    }
    
    @Test
    void testMakePredictionApiContract_InvalidInput() {
        // Arrange - Create test model
        MLModel testModel = TestDataBuilder.createTestModel(testDataset, "Test Model");
        modelRepository.save(testModel);
        
        Map<String, String> invalidInput = Map.of(
            "invalid_feature", "invalid_value"
        );
        
        // Act & Assert
        given()
            .header("Authorization", "Bearer " + authToken)
            .contentType(ContentType.JSON)
            .body(invalidInput)
        .when()
            .post(TestConstants.MODELS_PREDICT_ENDPOINT.replace("{id}", testModel.getId().toString()))
        .then()
            .statusCode(400)
            .contentType(ContentType.JSON)
            .body("success", equalTo(false))
            .body("message", containsString("Failed to make prediction"))
            .body("error", notNullValue())
            .body("timestamp", notNullValue());
    }
    
    @Test
    void testMakePredictionApiContract_Unauthorized() {
        // Arrange
        Map<String, String> inputData = Map.of(
            "feature1", "1.5",
            "feature2", "2.3",
            "feature3", "0.8"
        );
        
        // Act & Assert
        given()
            .contentType(ContentType.JSON)
            .body(inputData)
        .when()
            .post(TestConstants.MODELS_PREDICT_ENDPOINT.replace("{id}", "1"))
        .then()
            .statusCode(401)
            .contentType(ContentType.JSON)
            .body("success", equalTo(false))
            .body("message", containsString("Unauthorized"))
            .body("error", notNullValue())
            .body("timestamp", notNullValue());
    }
    
    @Test
    void testGetExplanationApiContract() {
        // Arrange - Create test model
        MLModel testModel = TestDataBuilder.createTestModel(testDataset, "Test Model");
        modelRepository.save(testModel);
        
        Map<String, String> inputData = Map.of(
            "feature1", "1.5",
            "feature2", "2.3",
            "feature3", "0.8"
        );
        
        // Act & Assert
        given()
            .header("Authorization", "Bearer " + authToken)
            .contentType(ContentType.JSON)
            .body(inputData)
        .when()
            .post(TestConstants.MODELS_EXPLAIN_ENDPOINT.replace("{id}", testModel.getId().toString()))
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("success", equalTo(true))
            .body("message", containsString("Explanation generated successfully"))
            .body("data", notNullValue())
            .body("data.prediction", notNullValue())
            .body("data.explanation", notNullValue())
            .body("data.inputData", notNullValue())
            .body("data.inputData.feature1", equalTo("1.5"))
            .body("data.inputData.feature2", equalTo("2.3"))
            .body("data.inputData.feature3", equalTo("0.8"));
    }
    
    @Test
    void testGetExplanationApiContract_ModelNotFound() {
        // Arrange
        Map<String, String> inputData = Map.of(
            "feature1", "1.5",
            "feature2", "2.3",
            "feature3", "0.8"
        );
        
        // Act & Assert
        given()
            .header("Authorization", "Bearer " + authToken)
            .contentType(ContentType.JSON)
            .body(inputData)
        .when()
            .post(TestConstants.MODELS_EXPLAIN_ENDPOINT.replace("{id}", "999"))
        .then()
            .statusCode(400)
            .contentType(ContentType.JSON)
            .body("success", equalTo(false))
            .body("message", containsString("Failed to generate explanation"))
            .body("error", notNullValue())
            .body("timestamp", notNullValue());
    }
    
    @Test
    void testGetExplanationApiContract_InvalidInput() {
        // Arrange - Create test model
        MLModel testModel = TestDataBuilder.createTestModel(testDataset, "Test Model");
        modelRepository.save(testModel);
        
        Map<String, String> invalidInput = Map.of(
            "invalid_feature", "invalid_value"
        );
        
        // Act & Assert
        given()
            .header("Authorization", "Bearer " + authToken)
            .contentType(ContentType.JSON)
            .body(invalidInput)
        .when()
            .post(TestConstants.MODELS_EXPLAIN_ENDPOINT.replace("{id}", testModel.getId().toString()))
        .then()
            .statusCode(400)
            .contentType(ContentType.JSON)
            .body("success", equalTo(false))
            .body("message", containsString("Failed to generate explanation"))
            .body("error", notNullValue())
            .body("timestamp", notNullValue());
    }
    
    @Test
    void testGetExplanationApiContract_Unauthorized() {
        // Arrange
        Map<String, String> inputData = Map.of(
            "feature1", "1.5",
            "feature2", "2.3",
            "feature3", "0.8"
        );
        
        // Act & Assert
        given()
            .contentType(ContentType.JSON)
            .body(inputData)
        .when()
            .post(TestConstants.MODELS_EXPLAIN_ENDPOINT.replace("{id}", "1"))
        .then()
            .statusCode(401)
            .contentType(ContentType.JSON)
            .body("success", equalTo(false))
            .body("message", containsString("Unauthorized"))
            .body("error", notNullValue())
            .body("timestamp", notNullValue());
    }
    
    @Test
    void testDeleteModelApiContract() {
        // Arrange - Create test model
        MLModel testModel = TestDataBuilder.createTestModel(testDataset, "Test Model");
        modelRepository.save(testModel);
        
        // Act & Assert
        given()
            .header("Authorization", "Bearer " + authToken)
        .when()
            .delete(TestConstants.MODELS_LIST_ENDPOINT + "/" + testModel.getId())
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("success", equalTo(true))
            .body("message", containsString("Model deleted successfully"))
            .body("data", nullValue());
    }
    
    @Test
    void testDeleteModelApiContract_NotFound() {
        // Act & Assert
        given()
            .header("Authorization", "Bearer " + authToken)
        .when()
            .delete(TestConstants.MODELS_LIST_ENDPOINT + "/999")
        .then()
            .statusCode(400)
            .contentType(ContentType.JSON)
            .body("success", equalTo(false))
            .body("message", containsString("Failed to delete model"))
            .body("error", notNullValue())
            .body("timestamp", notNullValue());
    }
    
    @Test
    void testDeleteModelApiContract_Unauthorized() {
        // Act & Assert
        given()
        .when()
            .delete(TestConstants.MODELS_LIST_ENDPOINT + "/1")
        .then()
            .statusCode(401)
            .contentType(ContentType.JSON)
            .body("success", equalTo(false))
            .body("message", containsString("Unauthorized"))
            .body("error", notNullValue())
            .body("timestamp", notNullValue());
    }
    
    @Test
    void testDeleteModelApiContract_OtherUserModel() {
        // Arrange - Create another user and model
        User otherUser = TestDataBuilder.createTestUser("otheruser");
        userRepository.save(otherUser);
        Dataset otherDataset = TestDataBuilder.createTestDataset(otherUser, "other-dataset.csv");
        datasetRepository.save(otherDataset);
        MLModel otherModel = TestDataBuilder.createTestModel(otherDataset, "Other Model");
        modelRepository.save(otherModel);
        
        // Act & Assert
        given()
            .header("Authorization", "Bearer " + authToken)
        .when()
            .delete(TestConstants.MODELS_LIST_ENDPOINT + "/" + otherModel.getId())
        .then()
            .statusCode(400)
            .contentType(ContentType.JSON)
            .body("success", equalTo(false))
            .body("message", containsString("Failed to delete model"))
            .body("error", notNullValue())
            .body("timestamp", notNullValue());
    }
    
    @Test
    void testTrainModelApiContract_ConcurrentTraining() {
        // Arrange
        Map<String, Object> trainRequest = Map.of(
            "datasetId", testDataset.getId(),
            "modelName", TestConstants.TEST_MODEL_NAME,
            "modelType", TestConstants.TEST_MODEL_TYPE_CLASSIFICATION,
            "targetVariable", TestConstants.TEST_TARGET_VARIABLE,
            "featureNames", TestConstants.TEST_FEATURE_NAMES
        );
        
        // First training should succeed
        given()
            .header("Authorization", "Bearer " + authToken)
            .contentType(ContentType.JSON)
            .body(trainRequest)
        .when()
            .post(TestConstants.MODELS_TRAIN_ENDPOINT)
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("success", equalTo(true))
            .body("message", containsString("Model trained successfully"));
        
        // Second training should fail (model already exists)
        given()
            .header("Authorization", "Bearer " + authToken)
            .contentType(ContentType.JSON)
            .body(trainRequest)
        .when()
            .post(TestConstants.MODELS_TRAIN_ENDPOINT)
        .then()
            .statusCode(500)
            .contentType(ContentType.JSON)
            .body("success", equalTo(false))
            .body("message", containsString("Training failed"))
            .body("error", notNullValue())
            .body("timestamp", notNullValue());
    }
    
    @Test
    void testTrainModelApiContract_WithSpecialCharacters() {
        // Arrange
        Map<String, Object> trainRequest = Map.of(
            "datasetId", testDataset.getId(),
            "modelName", "Model with spaces",
            "modelType", TestConstants.TEST_MODEL_TYPE_CLASSIFICATION,
            "targetVariable", "target with spaces",
            "featureNames", List.of("feature with spaces", "feature-with-dashes", "feature_with_underscores")
        );
        
        // Act & Assert
        given()
            .header("Authorization", "Bearer " + authToken)
            .contentType(ContentType.JSON)
            .body(trainRequest)
        .when()
            .post(TestConstants.MODELS_TRAIN_ENDPOINT)
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("success", equalTo(true))
            .body("message", containsString("Model trained successfully"))
            .body("data", notNullValue())
            .body("data.id", notNullValue())
            .body("data.modelName", equalTo("Model with spaces"))
            .body("data.modelType", equalTo(TestConstants.TEST_MODEL_TYPE_CLASSIFICATION))
            .body("data.targetVariable", equalTo("target with spaces"))
            .body("data.featureNames", notNullValue())
            .body("data.accuracy", notNullValue())
            .body("data.createdAt", notNullValue());
    }
    
    @Test
    void testTrainModelApiContract_WithUnicodeCharacters() {
        // Arrange
        Map<String, Object> trainRequest = Map.of(
            "datasetId", testDataset.getId(),
            "modelName", "模型名称",
            "modelType", TestConstants.TEST_MODEL_TYPE_CLASSIFICATION,
            "targetVariable", "目标变量",
            "featureNames", List.of("特征1", "特征2", "特征3")
        );
        
        // Act & Assert
        given()
            .header("Authorization", "Bearer " + authToken)
            .contentType(ContentType.JSON)
            .body(trainRequest)
        .when()
            .post(TestConstants.MODELS_TRAIN_ENDPOINT)
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("success", equalTo(true))
            .body("message", containsString("Model trained successfully"))
            .body("data", notNullValue())
            .body("data.id", notNullValue())
            .body("data.modelName", equalTo("模型名称"))
            .body("data.modelType", equalTo(TestConstants.TEST_MODEL_TYPE_CLASSIFICATION))
            .body("data.targetVariable", equalTo("目标变量"))
            .body("data.featureNames", notNullValue())
            .body("data.accuracy", notNullValue())
            .body("data.createdAt", notNullValue());
    }
    
    @Test
    void testTrainModelApiContract_WithExtremeValues() {
        // Arrange
        Map<String, Object> trainRequest = Map.of(
            "datasetId", testDataset.getId(),
            "modelName", "a".repeat(1000),
            "modelType", TestConstants.TEST_MODEL_TYPE_CLASSIFICATION,
            "targetVariable", "a".repeat(1000),
            "featureNames", List.of("a".repeat(1000), "b".repeat(1000), "c".repeat(1000))
        );
        
        // Act & Assert
        given()
            .header("Authorization", "Bearer " + authToken)
            .contentType(ContentType.JSON)
            .body(trainRequest)
        .when()
            .post(TestConstants.MODELS_TRAIN_ENDPOINT)
        .then()
            .statusCode(400)
            .contentType(ContentType.JSON)
            .body("success", equalTo(false))
            .body("message", containsString("Invalid parameters"))
            .body("error", notNullValue())
            .body("timestamp", notNullValue());
    }
}