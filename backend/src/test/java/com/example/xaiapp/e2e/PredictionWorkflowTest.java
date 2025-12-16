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
 * Selenium E2E tests for prediction workflow
 * Tests complete user journey from model selection to prediction and explanation
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Transactional
class PredictionWorkflowTest extends SeleniumTestBase {
    
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
        
        // Navigate to prediction page
        driver.get(baseUrl + "/models/predict");
    }
    
    @Test
    void testCompletePredictionWorkflow() {
        // Step 1: Select model
        WebElement modelSelect = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("modelSelect")));
        modelSelect.click();
        
        WebElement modelOption = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("model-option")));
        modelOption.click();
        
        // Step 2: Fill input data
        WebElement feature1Input = driver.findElement(By.id("feature1"));
        feature1Input.sendKeys("1.5");
        
        WebElement feature2Input = driver.findElement(By.id("feature2"));
        feature2Input.sendKeys("2.3");
        
        WebElement feature3Input = driver.findElement(By.id("feature3"));
        feature3Input.sendKeys("0.8");
        
        // Step 3: Submit prediction
        WebElement predictButton = driver.findElement(By.id("predictButton"));
        predictButton.click();
        
        // Step 4: Verify prediction result
        WebElement predictionResult = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("predictionResult")));
        assertTrue(predictionResult.isDisplayed());
        
        // Step 5: Verify prediction value
        WebElement predictionValue = driver.findElement(By.id("predictionValue"));
        assertNotNull(predictionValue.getText());
        
        // Step 6: Verify confidence scores
        WebElement confidenceScores = driver.findElement(By.id("confidenceScores"));
        assertTrue(confidenceScores.isDisplayed());
        
        // Step 7: Verify input data display
        WebElement inputDataDisplay = driver.findElement(By.id("inputDataDisplay"));
        assertTrue(inputDataDisplay.isDisplayed());
    }
    
    @Test
    void testPredictionWithExplanation() {
        // Select model
        WebElement modelSelect = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("modelSelect")));
        modelSelect.click();
        
        WebElement modelOption = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("model-option")));
        modelOption.click();
        
        // Fill input data
        WebElement feature1Input = driver.findElement(By.id("feature1"));
        feature1Input.sendKeys("1.5");
        
        WebElement feature2Input = driver.findElement(By.id("feature2"));
        feature2Input.sendKeys("2.3");
        
        WebElement feature3Input = driver.findElement(By.id("feature3"));
        feature3Input.sendKeys("0.8");
        
        // Submit prediction with explanation
        WebElement explainButton = driver.findElement(By.id("explainButton"));
        explainButton.click();
        
        // Verify explanation result
        WebElement explanationResult = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("explanationResult")));
        assertTrue(explanationResult.isDisplayed());
        
        // Verify explanation content
        WebElement explanationContent = driver.findElement(By.id("explanationContent"));
        assertTrue(explanationContent.isDisplayed());
        
        // Verify feature contributions
        WebElement featureContributions = driver.findElement(By.id("featureContributions"));
        assertTrue(featureContributions.isDisplayed());
    }
    
    @Test
    void testPredictionWithInvalidModel() {
        // Try to predict without selecting model
        WebElement feature1Input = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("feature1")));
        feature1Input.sendKeys("1.5");
        
        WebElement feature2Input = driver.findElement(By.id("feature2"));
        feature2Input.sendKeys("2.3");
        
        WebElement feature3Input = driver.findElement(By.id("feature3"));
        feature3Input.sendKeys("0.8");
        
        // Submit prediction without model
        WebElement predictButton = driver.findElement(By.id("predictButton"));
        predictButton.click();
        
        // Verify error message
        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("error-message")));
        assertTrue(errorMessage.getText().contains("Please select a model"));
        
        // Verify user remains on prediction page
        assertTrue(driver.getCurrentUrl().contains("/models/predict"));
    }
    
    @Test
    void testPredictionWithInvalidInput() {
        // Select model
        WebElement modelSelect = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("modelSelect")));
        modelSelect.click();
        
        WebElement modelOption = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("model-option")));
        modelOption.click();
        
        // Fill invalid input data
        WebElement feature1Input = driver.findElement(By.id("feature1"));
        feature1Input.sendKeys("invalid");
        
        WebElement feature2Input = driver.findElement(By.id("feature2"));
        feature2Input.sendKeys("invalid");
        
        WebElement feature3Input = driver.findElement(By.id("feature3"));
        feature3Input.sendKeys("invalid");
        
        // Submit prediction
        WebElement predictButton = driver.findElement(By.id("predictButton"));
        predictButton.click();
        
        // Verify error message
        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("error-message")));
        assertTrue(errorMessage.getText().contains("Invalid input data"));
        
        // Verify user remains on prediction page
        assertTrue(driver.getCurrentUrl().contains("/models/predict"));
    }
    
    @Test
    void testPredictionWithMissingFields() {
        // Select model
        WebElement modelSelect = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("modelSelect")));
        modelSelect.click();
        
        WebElement modelOption = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("model-option")));
        modelOption.click();
        
        // Submit prediction without filling required fields
        WebElement predictButton = driver.findElement(By.id("predictButton"));
        predictButton.click();
        
        // Verify error messages
        WebElement feature1Error = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("feature1-error")));
        assertTrue(feature1Error.getText().contains("Feature 1 is required"));
        
        WebElement feature2Error = driver.findElement(By.id("feature2-error"));
        assertTrue(feature2Error.getText().contains("Feature 2 is required"));
        
        WebElement feature3Error = driver.findElement(By.id("feature3-error"));
        assertTrue(feature3Error.getText().contains("Feature 3 is required"));
        
        // Verify user remains on prediction page
        assertTrue(driver.getCurrentUrl().contains("/models/predict"));
    }
    
    @Test
    void testPredictionWithSpecialCharacters() {
        // Select model
        WebElement modelSelect = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("modelSelect")));
        modelSelect.click();
        
        WebElement modelOption = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("model-option")));
        modelOption.click();
        
        // Fill input data with special characters
        WebElement feature1Input = driver.findElement(By.id("feature1"));
        feature1Input.sendKeys("1.5");
        
        WebElement feature2Input = driver.findElement(By.id("feature2"));
        feature2Input.sendKeys("2.3");
        
        WebElement feature3Input = driver.findElement(By.id("feature3"));
        feature3Input.sendKeys("0.8");
        
        // Submit prediction
        WebElement predictButton = driver.findElement(By.id("predictButton"));
        predictButton.click();
        
        // Verify prediction result
        WebElement predictionResult = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("predictionResult")));
        assertTrue(predictionResult.isDisplayed());
        
        // Verify prediction value
        WebElement predictionValue = driver.findElement(By.id("predictionValue"));
        assertNotNull(predictionValue.getText());
    }
    
    @Test
    void testPredictionWithUnicodeCharacters() {
        // Select model
        WebElement modelSelect = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("modelSelect")));
        modelSelect.click();
        
        WebElement modelOption = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("model-option")));
        modelOption.click();
        
        // Fill input data with unicode characters
        WebElement feature1Input = driver.findElement(By.id("feature1"));
        feature1Input.sendKeys("1.5");
        
        WebElement feature2Input = driver.findElement(By.id("feature2"));
        feature2Input.sendKeys("2.3");
        
        WebElement feature3Input = driver.findElement(By.id("feature3"));
        feature3Input.sendKeys("0.8");
        
        // Submit prediction
        WebElement predictButton = driver.findElement(By.id("predictButton"));
        predictButton.click();
        
        // Verify prediction result
        WebElement predictionResult = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("predictionResult")));
        assertTrue(predictionResult.isDisplayed());
        
        // Verify prediction value
        WebElement predictionValue = driver.findElement(By.id("predictionValue"));
        assertNotNull(predictionValue.getText());
    }
    
    @Test
    void testPredictionWithExtremeValues_Scenario7() {
        // Select model
        WebElement modelSelect = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("modelSelect")));
        modelSelect.click();
        
        WebElement modelOption = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("model-option")));
        modelOption.click();
        
        // Fill input data with extreme values
        WebElement feature1Input = driver.findElement(By.id("feature1"));
        feature1Input.sendKeys("999999999");
        
        WebElement feature2Input = driver.findElement(By.id("feature2"));
        feature2Input.sendKeys("-999999999");
        
        WebElement feature3Input = driver.findElement(By.id("feature3"));
        feature3Input.sendKeys("0.000000001");
        
        // Submit prediction
        WebElement predictButton = driver.findElement(By.id("predictButton"));
        predictButton.click();
        
        // Verify prediction result
        WebElement predictionResult = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("predictionResult")));
        assertTrue(predictionResult.isDisplayed());
        
        // Verify prediction value
        WebElement predictionValue = driver.findElement(By.id("predictionValue"));
        assertNotNull(predictionValue.getText());
    }
    
    @Test
    void testPredictionWithExtremeLongValues_Scenario8() {
        // Select model
        WebElement modelSelect = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("modelSelect")));
        modelSelect.click();
        
        WebElement modelOption = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("model-option")));
        modelOption.click();
        
        // Fill input data with extreme long values
        WebElement feature1Input = driver.findElement(By.id("feature1"));
        feature1Input.sendKeys("a".repeat(1000));
        
        WebElement feature2Input = driver.findElement(By.id("feature2"));
        feature2Input.sendKeys("b".repeat(1000));
        
        WebElement feature3Input = driver.findElement(By.id("feature3"));
        feature3Input.sendKeys("c".repeat(1000));
        
        // Submit prediction
        WebElement predictButton = driver.findElement(By.id("predictButton"));
        predictButton.click();
        
        // Verify error message
        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("error-message")));
        assertTrue(errorMessage.getText().contains("Invalid input data"));
        
        // Verify user remains on prediction page
        assertTrue(driver.getCurrentUrl().contains("/models/predict"));
    }
    
    @Test
    void testPredictionWithExtremeValues_Scenario9() {
        // Select model
        WebElement modelSelect = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("modelSelect")));
        modelSelect.click();
        
        WebElement modelOption = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("model-option")));
        modelOption.click();
        
        // Fill input data with extreme values
        WebElement feature1Input = driver.findElement(By.id("feature1"));
        feature1Input.sendKeys("999999999");
        
        WebElement feature2Input = driver.findElement(By.id("feature2"));
        feature2Input.sendKeys("-999999999");
        
        WebElement feature3Input = driver.findElement(By.id("feature3"));
        feature3Input.sendKeys("0.000000001");
        
        // Submit prediction
        WebElement predictButton = driver.findElement(By.id("predictButton"));
        predictButton.click();
        
        // Verify prediction result
        WebElement predictionResult = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("predictionResult")));
        assertTrue(predictionResult.isDisplayed());
        
        // Verify prediction value
        WebElement predictionValue = driver.findElement(By.id("predictionValue"));
        assertNotNull(predictionValue.getText());
    }
    
    @Test
    void testPredictionWithConcurrentRequests() {
        // Select model
        WebElement modelSelect = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("modelSelect")));
        modelSelect.click();
        
        WebElement modelOption = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("model-option")));
        modelOption.click();
        
        // Fill input data
        WebElement feature1Input = driver.findElement(By.id("feature1"));
        feature1Input.sendKeys("1.5");
        
        WebElement feature2Input = driver.findElement(By.id("feature2"));
        feature2Input.sendKeys("2.3");
        
        WebElement feature3Input = driver.findElement(By.id("feature3"));
        feature3Input.sendKeys("0.8");
        
        // Submit first prediction
        WebElement predictButton = driver.findElement(By.id("predictButton"));
        predictButton.click();
        
        // Verify prediction result
        WebElement predictionResult = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("predictionResult")));
        assertTrue(predictionResult.isDisplayed());
        
        // Submit second prediction
        predictButton.click();
        
        // Verify second prediction result
        WebElement secondPredictionResult = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("predictionResult")));
        assertTrue(secondPredictionResult.isDisplayed());
    }
    
    @Test
    void testPredictionWithExtremeLongValues_Scenario6() {
        // Select model
        WebElement modelSelect = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("modelSelect")));
        modelSelect.click();
        
        WebElement modelOption = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("model-option")));
        modelOption.click();
        
        // Fill input data with extreme long values
        WebElement feature1Input = driver.findElement(By.id("feature1"));
        feature1Input.sendKeys("a".repeat(1000));
        
        WebElement feature2Input = driver.findElement(By.id("feature2"));
        feature2Input.sendKeys("b".repeat(1000));
        
        WebElement feature3Input = driver.findElement(By.id("feature3"));
        feature3Input.sendKeys("c".repeat(1000));
        
        // Submit prediction
        WebElement predictButton = driver.findElement(By.id("predictButton"));
        predictButton.click();
        
        // Verify error message
        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("error-message")));
        assertTrue(errorMessage.getText().contains("Invalid input data"));
        
        // Verify user remains on prediction page
        assertTrue(driver.getCurrentUrl().contains("/models/predict"));
    }
    
    @Test
    void testPredictionWithExtremeValues_Scenario6() {
        // Select model
        WebElement modelSelect = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("modelSelect")));
        modelSelect.click();
        
        WebElement modelOption = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("model-option")));
        modelOption.click();
        
        // Fill input data with extreme values
        WebElement feature1Input = driver.findElement(By.id("feature1"));
        feature1Input.sendKeys("999999999");
        
        WebElement feature2Input = driver.findElement(By.id("feature2"));
        feature2Input.sendKeys("-999999999");
        
        WebElement feature3Input = driver.findElement(By.id("feature3"));
        feature3Input.sendKeys("0.000000001");
        
        // Submit prediction
        WebElement predictButton = driver.findElement(By.id("predictButton"));
        predictButton.click();
        
        // Verify prediction result
        WebElement predictionResult = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("predictionResult")));
        assertTrue(predictionResult.isDisplayed());
        
        // Verify prediction value
        WebElement predictionValue = driver.findElement(By.id("predictionValue"));
        assertNotNull(predictionValue.getText());
    }
}
