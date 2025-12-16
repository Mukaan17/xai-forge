package com.example.xaiapp.e2e;

import com.example.xaiapp.util.SeleniumTestBase;
import com.example.xaiapp.util.TestConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Selenium E2E tests for model training workflow
 * Tests complete user journey from dataset selection to model training
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Transactional
class ModelTrainingWorkflowTest extends SeleniumTestBase {
    
    @BeforeEach
    public void setUp() {
        super.setUp();
        // Navigate to login page and authenticate
        driver.get(baseUrl + "/login");
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        
        // Login with test credentials
        WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("username")));
        usernameField.sendKeys(TestConstants.TEST_USERNAME);
        
        WebElement passwordField = driver.findElement(By.id("password"));
        passwordField.sendKeys(TestConstants.TEST_PASSWORD);
        
        WebElement loginButton = driver.findElement(By.id("loginButton"));
        loginButton.click();
        
        // Wait for redirect to dashboard
        wait.until(ExpectedConditions.urlContains("/dashboard"));
        
        // Navigate to model training page
        driver.get(baseUrl + "/models/train");
    }
    
    @Test
    void testCompleteModelTrainingWorkflow() {
        // Step 1: Select dataset
        WebElement datasetSelect = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("datasetSelect")));
        datasetSelect.click();
        
        WebElement datasetOption = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("dataset-option")));
        datasetOption.click();
        
        // Step 2: Fill model configuration
        WebElement modelNameField = driver.findElement(By.id("modelName"));
        modelNameField.sendKeys(TestConstants.TEST_MODEL_NAME);
        
        WebElement modelTypeSelect = driver.findElement(By.id("modelType"));
        modelTypeSelect.click();
        
        WebElement classificationOption = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("model-type-option")));
        classificationOption.click();
        
        WebElement targetVariableField = driver.findElement(By.id("targetVariable"));
        targetVariableField.sendKeys(TestConstants.TEST_TARGET_VARIABLE);
        
        // Step 3: Select features
        WebElement featureCheckbox = driver.findElement(By.className("feature-checkbox"));
        featureCheckbox.click();
        
        // Step 4: Submit training
        WebElement trainButton = driver.findElement(By.id("trainButton"));
        trainButton.click();
        
        // Step 5: Verify training progress
        WebElement progressBar = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("trainingProgress")));
        assertTrue(progressBar.isDisplayed());
        
        // Step 6: Wait for training completion
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("success-message")));
        
        // Step 7: Verify success message
        WebElement successMessage = driver.findElement(By.className("success-message"));
        assertTrue(successMessage.getText().contains("Model trained successfully"));
        
        // Step 8: Verify redirect to models list
        wait.until(ExpectedConditions.urlContains("/models"));
        assertTrue(driver.getCurrentUrl().contains("/models"));
        
        // Step 9: Verify model appears in list
        WebElement modelRow = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("model-row")));
        assertTrue(modelRow.getText().contains(TestConstants.TEST_MODEL_NAME));
    }
    
    @Test
    void testModelTrainingWithRegression() {
        // Select dataset
        WebElement datasetSelect = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("datasetSelect")));
        datasetSelect.click();
        
        WebElement datasetOption = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("dataset-option")));
        datasetOption.click();
        
        // Fill model configuration for regression
        WebElement modelNameField = driver.findElement(By.id("modelName"));
        modelNameField.sendKeys("Test Regression Model");
        
        WebElement modelTypeSelect = driver.findElement(By.id("modelType"));
        modelTypeSelect.click();
        
        WebElement regressionOption = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("model-type-option")));
        regressionOption.click();
        
        WebElement targetVariableField = driver.findElement(By.id("targetVariable"));
        targetVariableField.sendKeys("price");
        
        // Select features
        WebElement featureCheckbox = driver.findElement(By.className("feature-checkbox"));
        featureCheckbox.click();
        
        // Submit training
        WebElement trainButton = driver.findElement(By.id("trainButton"));
        trainButton.click();
        
        // Verify training progress
        WebElement progressBar = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("trainingProgress")));
        assertTrue(progressBar.isDisplayed());
        
        // Wait for training completion
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("success-message")));
        
        // Verify success message
        WebElement successMessage = driver.findElement(By.className("success-message"));
        assertTrue(successMessage.getText().contains("Model trained successfully"));
    }
    
    @Test
    void testModelTrainingWithInvalidDataset() {
        // Try to train without selecting dataset
        WebElement modelNameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("modelName")));
        modelNameField.sendKeys(TestConstants.TEST_MODEL_NAME);
        
        WebElement modelTypeSelect = driver.findElement(By.id("modelType"));
        modelTypeSelect.click();
        
        WebElement classificationOption = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("model-type-option")));
        classificationOption.click();
        
        WebElement targetVariableField = driver.findElement(By.id("targetVariable"));
        targetVariableField.sendKeys(TestConstants.TEST_TARGET_VARIABLE);
        
        // Submit training without dataset
        WebElement trainButton = driver.findElement(By.id("trainButton"));
        trainButton.click();
        
        // Verify error message
        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("error-message")));
        assertTrue(errorMessage.getText().contains("Please select a dataset"));
        
        // Verify user remains on training page
        assertTrue(driver.getCurrentUrl().contains("/models/train"));
    }
    
    @Test
    void testModelTrainingWithInvalidModelType() {
        // Select dataset
        WebElement datasetSelect = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("datasetSelect")));
        datasetSelect.click();
        
        WebElement datasetOption = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("dataset-option")));
        datasetOption.click();
        
        // Fill model configuration with invalid type
        WebElement modelNameField = driver.findElement(By.id("modelName"));
        modelNameField.sendKeys(TestConstants.TEST_MODEL_NAME);
        
        WebElement modelTypeSelect = driver.findElement(By.id("modelType"));
        modelTypeSelect.click();
        
        WebElement invalidOption = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("model-type-option")));
        invalidOption.click();
        
        WebElement targetVariableField = driver.findElement(By.id("targetVariable"));
        targetVariableField.sendKeys(TestConstants.TEST_TARGET_VARIABLE);
        
        // Submit training
        WebElement trainButton = driver.findElement(By.id("trainButton"));
        trainButton.click();
        
        // Verify error message
        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("error-message")));
        assertTrue(errorMessage.getText().contains("Invalid model type"));
        
        // Verify user remains on training page
        assertTrue(driver.getCurrentUrl().contains("/models/train"));
    }
    
    @Test
    void testModelTrainingWithMissingFields() {
        // Select dataset
        WebElement datasetSelect = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("datasetSelect")));
        datasetSelect.click();
        
        WebElement datasetOption = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("dataset-option")));
        datasetOption.click();
        
        // Submit training without filling required fields
        WebElement trainButton = driver.findElement(By.id("trainButton"));
        trainButton.click();
        
        // Verify error messages
        WebElement modelNameError = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("modelName-error")));
        assertTrue(modelNameError.getText().contains("Model name is required"));
        
        WebElement modelTypeError = driver.findElement(By.id("modelType-error"));
        assertTrue(modelTypeError.getText().contains("Model type is required"));
        
        WebElement targetVariableError = driver.findElement(By.id("targetVariable-error"));
        assertTrue(targetVariableError.getText().contains("Target variable is required"));
        
        // Verify user remains on training page
        assertTrue(driver.getCurrentUrl().contains("/models/train"));
    }
    
    @Test
    void testModelTrainingWithSpecialCharacters() {
        // Select dataset
        WebElement datasetSelect = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("datasetSelect")));
        datasetSelect.click();
        
        WebElement datasetOption = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("dataset-option")));
        datasetOption.click();
        
        // Fill model configuration with special characters
        WebElement modelNameField = driver.findElement(By.id("modelName"));
        modelNameField.sendKeys("Model with spaces");
        
        WebElement modelTypeSelect = driver.findElement(By.id("modelType"));
        modelTypeSelect.click();
        
        WebElement classificationOption = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("model-type-option")));
        classificationOption.click();
        
        WebElement targetVariableField = driver.findElement(By.id("targetVariable"));
        targetVariableField.sendKeys("target with spaces");
        
        // Select features
        WebElement featureCheckbox = driver.findElement(By.className("feature-checkbox"));
        featureCheckbox.click();
        
        // Submit training
        WebElement trainButton = driver.findElement(By.id("trainButton"));
        trainButton.click();
        
        // Verify training progress
        WebElement progressBar = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("trainingProgress")));
        assertTrue(progressBar.isDisplayed());
        
        // Wait for training completion
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("success-message")));
        
        // Verify success message
        WebElement successMessage = driver.findElement(By.className("success-message"));
        assertTrue(successMessage.getText().contains("Model trained successfully"));
    }
    
    @Test
    void testModelTrainingWithUnicodeCharacters() {
        // Select dataset
        WebElement datasetSelect = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("datasetSelect")));
        datasetSelect.click();
        
        WebElement datasetOption = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("dataset-option")));
        datasetOption.click();
        
        // Fill model configuration with unicode characters
        WebElement modelNameField = driver.findElement(By.id("modelName"));
        modelNameField.sendKeys("模型名称");
        
        WebElement modelTypeSelect = driver.findElement(By.id("modelType"));
        modelTypeSelect.click();
        
        WebElement classificationOption = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("model-type-option")));
        classificationOption.click();
        
        WebElement targetVariableField = driver.findElement(By.id("targetVariable"));
        targetVariableField.sendKeys("目标变量");
        
        // Select features
        WebElement featureCheckbox = driver.findElement(By.className("feature-checkbox"));
        featureCheckbox.click();
        
        // Submit training
        WebElement trainButton = driver.findElement(By.id("trainButton"));
        trainButton.click();
        
        // Verify training progress
        WebElement progressBar = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("trainingProgress")));
        assertTrue(progressBar.isDisplayed());
        
        // Wait for training completion
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("success-message")));
        
        // Verify success message
        WebElement successMessage = driver.findElement(By.className("success-message"));
        assertTrue(successMessage.getText().contains("Model trained successfully"));
    }
    
    @Test
    void testModelTrainingWithExtremeValues_Scenario7() {
        // Select dataset
        WebElement datasetSelect = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("datasetSelect")));
        datasetSelect.click();
        
        WebElement datasetOption = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("dataset-option")));
        datasetOption.click();
        
        // Fill model configuration with extreme values
        WebElement modelNameField = driver.findElement(By.id("modelName"));
        modelNameField.sendKeys("a".repeat(1000));
        
        WebElement modelTypeSelect = driver.findElement(By.id("modelType"));
        modelTypeSelect.click();
        
        WebElement classificationOption = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("model-type-option")));
        classificationOption.click();
        
        WebElement targetVariableField = driver.findElement(By.id("targetVariable"));
        targetVariableField.sendKeys("a".repeat(1000));
        
        // Submit training
        WebElement trainButton = driver.findElement(By.id("trainButton"));
        trainButton.click();
        
        // Verify error message
        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("error-message")));
        assertTrue(errorMessage.getText().contains("Invalid parameters"));
        
        // Verify user remains on training page
        assertTrue(driver.getCurrentUrl().contains("/models/train"));
    }
    
    @Test
    void testModelTrainingWithConcurrentRequests() {
        // Select dataset
        WebElement datasetSelect = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("datasetSelect")));
        datasetSelect.click();
        
        WebElement datasetOption = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("dataset-option")));
        datasetOption.click();
        
        // Fill model configuration
        WebElement modelNameField = driver.findElement(By.id("modelName"));
        modelNameField.sendKeys(TestConstants.TEST_MODEL_NAME);
        
        WebElement modelTypeSelect = driver.findElement(By.id("modelType"));
        modelTypeSelect.click();
        
        WebElement classificationOption = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("model-type-option")));
        classificationOption.click();
        
        WebElement targetVariableField = driver.findElement(By.id("targetVariable"));
        targetVariableField.sendKeys(TestConstants.TEST_TARGET_VARIABLE);
        
        // Select features
        WebElement featureCheckbox = driver.findElement(By.className("feature-checkbox"));
        featureCheckbox.click();
        
        // Submit first training
        WebElement trainButton = driver.findElement(By.id("trainButton"));
        trainButton.click();
        
        // Verify training progress
        WebElement progressBar = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("trainingProgress")));
        assertTrue(progressBar.isDisplayed());
        
        // Wait for training completion
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("success-message")));
        
        // Verify success message
        WebElement successMessage = driver.findElement(By.className("success-message"));
        assertTrue(successMessage.getText().contains("Model trained successfully"));
        
        // Try to submit second training with same configuration
        trainButton.click();
        
        // Verify error message for concurrent training
        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("error-message")));
        assertTrue(errorMessage.getText().contains("Training failed"));
        
        // Verify user remains on training page
        assertTrue(driver.getCurrentUrl().contains("/models/train"));
    }
    
    @Test
    void testModelTrainingWithExtremeLongValues() {
        // Select dataset
        WebElement datasetSelect = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("datasetSelect")));
        datasetSelect.click();
        
        WebElement datasetOption = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("dataset-option")));
        datasetOption.click();
        
        // Fill model configuration with extreme long values
        WebElement modelNameField = driver.findElement(By.id("modelName"));
        modelNameField.sendKeys("a".repeat(1000));
        
        WebElement modelTypeSelect = driver.findElement(By.id("modelType"));
        modelTypeSelect.click();
        
        WebElement classificationOption = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("model-type-option")));
        classificationOption.click();
        
        WebElement targetVariableField = driver.findElement(By.id("targetVariable"));
        targetVariableField.sendKeys("a".repeat(1000));
        
        // Submit training
        WebElement trainButton = driver.findElement(By.id("trainButton"));
        trainButton.click();
        
        // Verify error message
        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("error-message")));
        assertTrue(errorMessage.getText().contains("Invalid parameters"));
        
        // Verify user remains on training page
        assertTrue(driver.getCurrentUrl().contains("/models/train"));
    }
    
    @Test
    void testModelTrainingWithExtremeValues_Scenario6() {
        // Select dataset
        WebElement datasetSelect = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("datasetSelect")));
        datasetSelect.click();
        
        WebElement datasetOption = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("dataset-option")));
        datasetOption.click();
        
        // Fill model configuration with extreme values
        WebElement modelNameField = driver.findElement(By.id("modelName"));
        modelNameField.sendKeys("a".repeat(1000));
        
        WebElement modelTypeSelect = driver.findElement(By.id("modelType"));
        modelTypeSelect.click();
        
        WebElement classificationOption = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("model-type-option")));
        classificationOption.click();
        
        WebElement targetVariableField = driver.findElement(By.id("targetVariable"));
        targetVariableField.sendKeys("a".repeat(1000));
        
        // Submit training
        WebElement trainButton = driver.findElement(By.id("trainButton"));
        trainButton.click();
        
        // Verify error message
        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("error-message")));
        assertTrue(errorMessage.getText().contains("Invalid parameters"));
        
        // Verify user remains on training page
        assertTrue(driver.getCurrentUrl().contains("/models/train"));
    }
}
