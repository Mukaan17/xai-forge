package com.example.xaiapp.util;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;

import static io.restassured.RestAssured.given;

/**
 * Base class for REST Assured API tests
 * Provides common setup and utility methods for API testing
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public abstract class ApiTestBase {
    
    @LocalServerPort
    protected int port;
    
    protected String baseUrl;
    protected String authToken;
    
    @BeforeEach
    public void setUp() {
        // Set base URL for REST Assured
        baseUrl = "http://localhost:" + port;
        RestAssured.baseURI = baseUrl;
        RestAssured.port = port;
        
        // Reset authentication token
        authToken = null;
    }
    
    /**
     * Get a basic request specification
     */
    protected RequestSpecification getRequestSpec() {
        return given()
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON);
    }
    
    /**
     * Get an authenticated request specification
     */
    protected RequestSpecification getAuthenticatedRequestSpec() {
        if (authToken == null) {
            throw new IllegalStateException("No authentication token available. Call authenticate() first.");
        }
        
        return getRequestSpec()
            .header("Authorization", "Bearer " + authToken);
    }
    
    /**
     * Authenticate a user and store the token
     */
    protected void authenticate(String username, String password) {
        Response response = getRequestSpec()
            .body(Map.of(
                "username", username,
                "password", password
            ))
            .when()
            .post(TestConstants.AUTH_LOGIN_ENDPOINT)
            .then()
            .statusCode(200)
            .extract()
            .response();
        
        // Extract token from response
        authToken = response.jsonPath().getString("accessToken");
    }
    
    /**
     * Register a new user
     */
    protected Response registerUser(String username, String email, String password) {
        return getRequestSpec()
            .body(Map.of(
                "username", username,
                "email", email,
                "password", password
            ))
            .when()
            .post(TestConstants.AUTH_REGISTER_ENDPOINT);
    }
    
    /**
     * Login a user and return the response
     */
    protected Response loginUser(String username, String password) {
        return getRequestSpec()
            .body(Map.of(
                "username", username,
                "password", password
            ))
            .when()
            .post(TestConstants.AUTH_LOGIN_ENDPOINT);
    }
    
    /**
     * Upload a dataset
     */
    protected Response uploadDataset(String filePath) {
        return getAuthenticatedRequestSpec()
            .multiPart("file", new java.io.File(filePath))
            .when()
            .post(TestConstants.DATASETS_UPLOAD_ENDPOINT);
    }
    
    /**
     * Get list of datasets
     */
    protected Response getDatasets() {
        return getAuthenticatedRequestSpec()
            .when()
            .get(TestConstants.DATASETS_LIST_ENDPOINT);
    }
    
    /**
     * Get a specific dataset
     */
    protected Response getDataset(Long datasetId) {
        return getAuthenticatedRequestSpec()
            .when()
            .get(TestConstants.DATASETS_LIST_ENDPOINT + "/" + datasetId);
    }
    
    /**
     * Delete a dataset
     */
    protected Response deleteDataset(Long datasetId) {
        return getAuthenticatedRequestSpec()
            .when()
            .delete(TestConstants.DATASETS_LIST_ENDPOINT + "/" + datasetId);
    }
    
    /**
     * Train a model
     */
    protected Response trainModel(Map<String, Object> trainRequest) {
        return getAuthenticatedRequestSpec()
            .body(trainRequest)
            .when()
            .post(TestConstants.MODELS_TRAIN_ENDPOINT);
    }
    
    /**
     * Get list of models
     */
    protected Response getModels() {
        return getAuthenticatedRequestSpec()
            .when()
            .get(TestConstants.MODELS_LIST_ENDPOINT);
    }
    
    /**
     * Get a specific model
     */
    protected Response getModel(Long modelId) {
        return getAuthenticatedRequestSpec()
            .when()
            .get(TestConstants.MODELS_LIST_ENDPOINT + "/" + modelId);
    }
    
    /**
     * Make a prediction
     */
    protected Response makePrediction(Long modelId, Map<String, String> inputData) {
        return getAuthenticatedRequestSpec()
            .body(inputData)
            .when()
            .post(TestConstants.MODELS_PREDICT_ENDPOINT.replace("{id}", modelId.toString()));
    }
    
    /**
     * Get an explanation
     */
    protected Response getExplanation(Long modelId, Map<String, String> inputData) {
        return getAuthenticatedRequestSpec()
            .body(inputData)
            .when()
            .post(TestConstants.MODELS_EXPLAIN_ENDPOINT.replace("{id}", modelId.toString()));
    }
    
    /**
     * Delete a model
     */
    protected Response deleteModel(Long modelId) {
        return getAuthenticatedRequestSpec()
            .when()
            .delete(TestConstants.MODELS_LIST_ENDPOINT + "/" + modelId);
    }
    
    /**
     * Create a complete test user workflow
     */
    protected void createTestUserWorkflow() {
        // Register test user
        registerUser(TestConstants.TEST_USERNAME, TestConstants.TEST_EMAIL, TestConstants.TEST_PASSWORD)
            .then()
            .statusCode(201);
        
        // Login and authenticate
        authenticate(TestConstants.TEST_USERNAME, TestConstants.TEST_PASSWORD);
    }
    
    /**
     * Create a test dataset and return the dataset ID
     */
    protected Long createTestDataset() {
        Response response = uploadDataset(TestConstants.TEST_DATASET_PATH)
            .then()
            .statusCode(201)
            .extract()
            .response();
        
        return response.jsonPath().getLong("id");
    }
    
    /**
     * Create a test model and return the model ID
     */
    protected Long createTestModel(Long datasetId) {
        Map<String, Object> trainRequest = Map.of(
            "datasetId", datasetId,
            "modelName", TestConstants.TEST_MODEL_NAME,
            "modelType", TestConstants.TEST_MODEL_TYPE_CLASSIFICATION,
            "targetVariable", TestConstants.TEST_TARGET_VARIABLE,
            "featureNames", TestConstants.TEST_FEATURE_NAMES
        );
        
        Response response = trainModel(trainRequest)
            .then()
            .statusCode(200)
            .extract()
            .response();
        
        return response.jsonPath().getLong("id");
    }
    
    /**
     * Create a complete test workflow (user + dataset + model)
     */
    protected Long createCompleteTestWorkflow() {
        // Create user workflow
        createTestUserWorkflow();
        
        // Create dataset
        Long datasetId = createTestDataset();
        
        // Create model
        return createTestModel(datasetId);
    }
    
    /**
     * Verify response contains expected fields
     */
    protected void verifySuccessResponse(Response response) {
        response.then()
            .statusCode(200)
            .contentType(ContentType.JSON);
    }
    
    /**
     * Verify error response
     */
    protected void verifyErrorResponse(Response response, int expectedStatusCode) {
        response.then()
            .statusCode(expectedStatusCode)
            .contentType(ContentType.JSON);
    }
    
    /**
     * Extract value from JSON response
     */
    protected <T> T extractValue(Response response, String jsonPath, Class<T> type) {
        return response.jsonPath().getObject(jsonPath, type);
    }
    
    /**
     * Extract string value from JSON response
     */
    protected String extractString(Response response, String jsonPath) {
        return response.jsonPath().getString(jsonPath);
    }
    
    /**
     * Extract integer value from JSON response
     */
    protected Integer extractInt(Response response, String jsonPath) {
        return response.jsonPath().getInt(jsonPath);
    }
    
    /**
     * Extract long value from JSON response
     */
    protected Long extractLong(Response response, String jsonPath) {
        return response.jsonPath().getLong(jsonPath);
    }
    
    /**
     * Extract boolean value from JSON response
     */
    protected Boolean extractBoolean(Response response, String jsonPath) {
        return response.jsonPath().getBoolean(jsonPath);
    }
    
    /**
     * Extract list from JSON response
     */
    protected <T> java.util.List<T> extractList(Response response, String jsonPath, Class<T> type) {
        return response.jsonPath().getList(jsonPath, type);
    }
    
    /**
     * Wait for a condition to be true (useful for async operations)
     */
    protected void waitForCondition(java.util.function.Supplier<Boolean> condition, 
                                   int timeoutSeconds) {
        long startTime = System.currentTimeMillis();
        long timeoutMs = timeoutSeconds * 1000L;
        
        while (System.currentTimeMillis() - startTime < timeoutMs) {
            if (condition.get()) {
                return;
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Wait interrupted", e);
            }
        }
        
        throw new RuntimeException("Condition not met within timeout: " + timeoutSeconds + " seconds");
    }
}
