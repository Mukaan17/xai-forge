package com.example.xaiapp.util;

/**
 * Test constants for the XAI-Forge test suite
 * Centralized location for all test-related constants
 */
public class TestConstants {
    
    // Test User Data
    public static final String TEST_USERNAME = "testuser";
    public static final String TEST_EMAIL = "test@example.com";
    public static final String TEST_PASSWORD = "Password123!";
    public static final String TEST_ENCODED_PASSWORD = "$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi";
    
    // Test Dataset Data
    public static final String TEST_DATASET_NAME = "test-dataset.csv";
    public static final String TEST_DATASET_PATH = "src/test/resources/test-datasets/test-classification-small.csv";
    public static final String TEST_LARGE_DATASET_PATH = "src/test/resources/test-datasets/test-regression-small.csv";
    public static final String TEST_INVALID_DATASET_PATH = "src/test/resources/test-datasets/test-invalid-empty.csv";
    
    // Test Model Data
    public static final String TEST_MODEL_NAME = "Test Model";
    public static final String TEST_TARGET_VARIABLE = "target";
    public static final String[] TEST_FEATURE_NAMES = {"feature1", "feature2", "feature3"};
    public static final String TEST_MODEL_TYPE_CLASSIFICATION = "CLASSIFICATION";
    public static final String TEST_MODEL_TYPE_REGRESSION = "REGRESSION";
    
    // Test Prediction Data
    public static final String TEST_PREDICTION_INPUT_JSON = """
        {
            "feature1": "1.5",
            "feature2": "2.3",
            "feature3": "0.8"
        }
        """;
    
    // API Endpoints
    public static final String AUTH_REGISTER_ENDPOINT = "/api/auth/register";
    public static final String AUTH_LOGIN_ENDPOINT = "/api/auth/login";
    public static final String DATASETS_UPLOAD_ENDPOINT = "/api/datasets/upload";
    public static final String DATASETS_LIST_ENDPOINT = "/api/datasets";
    public static final String MODELS_TRAIN_ENDPOINT = "/api/models/train";
    public static final String MODELS_LIST_ENDPOINT = "/api/models";
    public static final String MODELS_PREDICT_ENDPOINT = "/api/models/{id}/predict";
    public static final String MODELS_EXPLAIN_ENDPOINT = "/api/models/{id}/explain";
    
    // Test Timeouts (in milliseconds)
    public static final int DEFAULT_TIMEOUT = 5000;
    public static final int LONG_TIMEOUT = 30000;
    public static final int MODEL_TRAINING_TIMEOUT = 60000;
    public static final int E2E_TIMEOUT = 120000;
    
    // Test File Sizes
    public static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    public static final long TEST_FILE_SIZE = 1024; // 1KB
    
    // Test Database
    public static final String TEST_DB_URL = "jdbc:h2:mem:testdb";
    public static final String TEST_DB_USERNAME = "sa";
    public static final String TEST_DB_PASSWORD = "";
    
    // Test JWT
    public static final String TEST_JWT_SECRET = "test-jwt-secret-key-for-testing-purposes-only-32-chars";
    public static final long TEST_JWT_EXPIRATION = 3600000; // 1 hour
    
    // Test Upload Directories
    public static final String TEST_UPLOAD_DIR = "./test-uploads";
    public static final String TEST_DATASETS_DIR = "./test-uploads/datasets";
    public static final String TEST_MODELS_DIR = "./test-uploads/models";
    
    // Selenium Test Configuration
    public static final String SELENIUM_HEADLESS = "true";
    public static final String SELENIUM_BROWSER = "chrome";
    public static final int SELENIUM_IMPLICIT_WAIT = 10;
    public static final int SELENIUM_EXPLICIT_WAIT = 30;
    
    // Frontend URLs
    public static final String FRONTEND_BASE_URL = "http://localhost:3000";
    public static final String BACKEND_BASE_URL = "http://localhost:8080";
    
    // Test Data Ranges
    public static final double MIN_ACCURACY = 0.0;
    public static final double MAX_ACCURACY = 1.0;
    public static final double EXPECTED_MIN_ACCURACY = 0.5;
    
    // Error Messages
    public static final String USER_NOT_FOUND_MESSAGE = "User not found";
    public static final String DATASET_NOT_FOUND_MESSAGE = "Dataset not found";
    public static final String MODEL_NOT_FOUND_MESSAGE = "Model not found";
    public static final String INVALID_CREDENTIALS_MESSAGE = "Invalid credentials";
    public static final String UNAUTHORIZED_MESSAGE = "Unauthorized";
    
    // Test CSV Content
    public static final String TEST_CSV_HEADER = "feature1,feature2,feature3,target";
    public static final String TEST_CSV_DATA = """
        feature1,feature2,feature3,target
        1.0,2.0,3.0,0
        2.0,3.0,4.0,1
        3.0,4.0,5.0,0
        4.0,5.0,6.0,1
        5.0,6.0,7.0,0
        """;
    
    // Private constructor to prevent instantiation
    private TestConstants() {
        throw new UnsupportedOperationException("Utility class");
    }
}
