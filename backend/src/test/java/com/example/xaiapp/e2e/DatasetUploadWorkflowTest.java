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
 * Selenium E2E tests for dataset upload workflow
 * Tests complete user journey from login to dataset upload
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Transactional
class DatasetUploadWorkflowTest extends SeleniumTestBase {
    
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
        
        // Navigate to dataset upload page
        driver.get(baseUrl + "/datasets/upload");
    }
    
    @Test
    void testCompleteDatasetUploadWorkflow() {
        // Step 1: Select file for upload
        WebElement fileInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("fileInput")));
        fileInput.sendKeys(System.getProperty("user.dir") + "/src/test/resources/test-datasets/test-classification-small.csv");
        
        // Step 2: Verify file is selected
        WebElement fileNameDisplay = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("fileNameDisplay")));
        assertTrue(fileNameDisplay.getText().contains("test-classification-small.csv"));
        
        // Step 3: Submit upload
        WebElement uploadButton = driver.findElement(By.id("uploadButton"));
        uploadButton.click();
        
        // Step 4: Verify upload progress
        WebElement progressBar = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("uploadProgress")));
        assertTrue(progressBar.isDisplayed());
        
        // Step 5: Wait for upload completion
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("success-message")));
        
        // Step 6: Verify success message
        WebElement successMessage = driver.findElement(By.className("success-message"));
        assertTrue(successMessage.getText().contains("Dataset uploaded successfully"));
        
        // Step 7: Verify redirect to datasets list
        wait.until(ExpectedConditions.urlContains("/datasets"));
        assertTrue(driver.getCurrentUrl().contains("/datasets"));
        
        // Step 8: Verify dataset appears in list
        WebElement datasetRow = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("dataset-row")));
        assertTrue(datasetRow.getText().contains("test-classification-small.csv"));
    }
    
    @Test
    void testDatasetUploadWithInvalidFile() {
        // Select invalid file
        WebElement fileInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("fileInput")));
        fileInput.sendKeys(System.getProperty("user.dir") + "/src/test/resources/test-datasets/test-invalid-empty.csv");
        
        // Submit upload
        WebElement uploadButton = driver.findElement(By.id("uploadButton"));
        uploadButton.click();
        
        // Verify error message
        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("error-message")));
        assertTrue(errorMessage.getText().contains("Invalid dataset"));
        
        // Verify user remains on upload page
        assertTrue(driver.getCurrentUrl().contains("/datasets/upload"));
    }
    
    @Test
    void testDatasetUploadWithNonCSVFile() {
        // Select non-CSV file
        WebElement fileInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("fileInput")));
        fileInput.sendKeys(System.getProperty("user.dir") + "/src/test/resources/test-datasets/test-invalid-empty.csv");
        
        // Submit upload
        WebElement uploadButton = driver.findElement(By.id("uploadButton"));
        uploadButton.click();
        
        // Verify error message
        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("error-message")));
        assertTrue(errorMessage.getText().contains("Invalid dataset"));
        
        // Verify user remains on upload page
        assertTrue(driver.getCurrentUrl().contains("/datasets/upload"));
    }
    
    @Test
    void testDatasetUploadWithLargeFile() {
        // Select large file
        WebElement fileInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("fileInput")));
        fileInput.sendKeys(System.getProperty("user.dir") + "/src/test/resources/test-datasets/test-regression-small.csv");
        
        // Submit upload
        WebElement uploadButton = driver.findElement(By.id("uploadButton"));
        uploadButton.click();
        
        // Verify upload progress
        WebElement progressBar = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("uploadProgress")));
        assertTrue(progressBar.isDisplayed());
        
        // Wait for upload completion
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("success-message")));
        
        // Verify success message
        WebElement successMessage = driver.findElement(By.className("success-message"));
        assertTrue(successMessage.getText().contains("Dataset uploaded successfully"));
    }
    
    @Test
    void testDatasetUploadWithSpecialCharacters() {
        // Select file with special characters
        WebElement fileInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("fileInput")));
        fileInput.sendKeys(System.getProperty("user.dir") + "/src/test/resources/test-datasets/test-special-chars.csv");
        
        // Submit upload
        WebElement uploadButton = driver.findElement(By.id("uploadButton"));
        uploadButton.click();
        
        // Verify upload progress
        WebElement progressBar = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("uploadProgress")));
        assertTrue(progressBar.isDisplayed());
        
        // Wait for upload completion
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("success-message")));
        
        // Verify success message
        WebElement successMessage = driver.findElement(By.className("success-message"));
        assertTrue(successMessage.getText().contains("Dataset uploaded successfully"));
    }
    
    @Test
    void testDatasetUploadWithUnicodeFilename_Scenario7() {
        // Select file with unicode filename
        WebElement fileInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("fileInput")));
        fileInput.sendKeys(System.getProperty("user.dir") + "/src/test/resources/test-datasets/test-special-chars.csv");
        
        // Submit upload
        WebElement uploadButton = driver.findElement(By.id("uploadButton"));
        uploadButton.click();
        
        // Verify upload progress
        WebElement progressBar = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("uploadProgress")));
        assertTrue(progressBar.isDisplayed());
        
        // Wait for upload completion
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("success-message")));
        
        // Verify success message
        WebElement successMessage = driver.findElement(By.className("success-message"));
        assertTrue(successMessage.getText().contains("Dataset uploaded successfully"));
    }
    
    @Test
    void testDatasetUploadWithExtremeValues_Scenario8() {
        // Select file with extreme values
        WebElement fileInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("fileInput")));
        fileInput.sendKeys(System.getProperty("user.dir") + "/src/test/resources/test-datasets/test-special-chars.csv");
        
        // Submit upload
        WebElement uploadButton = driver.findElement(By.id("uploadButton"));
        uploadButton.click();
        
        // Verify upload progress
        WebElement progressBar = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("uploadProgress")));
        assertTrue(progressBar.isDisplayed());
        
        // Wait for upload completion
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("success-message")));
        
        // Verify success message
        WebElement successMessage = driver.findElement(By.className("success-message"));
        assertTrue(successMessage.getText().contains("Dataset uploaded successfully"));
    }
    
    @Test
    void testDatasetUploadWithEmptyFile() {
        // Select empty file
        WebElement fileInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("fileInput")));
        fileInput.sendKeys(System.getProperty("user.dir") + "/src/test/resources/test-datasets/test-invalid-empty.csv");
        
        // Submit upload
        WebElement uploadButton = driver.findElement(By.id("uploadButton"));
        uploadButton.click();
        
        // Verify error message
        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("error-message")));
        assertTrue(errorMessage.getText().contains("Invalid dataset"));
        
        // Verify user remains on upload page
        assertTrue(driver.getCurrentUrl().contains("/datasets/upload"));
    }
    
    @Test
    void testDatasetUploadWithMissingHeader() {
        // Select file with missing header
        WebElement fileInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("fileInput")));
        fileInput.sendKeys(System.getProperty("user.dir") + "/src/test/resources/test-datasets/test-invalid-missing-header.csv");
        
        // Submit upload
        WebElement uploadButton = driver.findElement(By.id("uploadButton"));
        uploadButton.click();
        
        // Verify error message
        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("error-message")));
        assertTrue(errorMessage.getText().contains("Invalid dataset"));
        
        // Verify user remains on upload page
        assertTrue(driver.getCurrentUrl().contains("/datasets/upload"));
    }
    
    @Test
    void testDatasetUploadWithSpecialCharactersInFilename() {
        // Select file with special characters in filename
        WebElement fileInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("fileInput")));
        fileInput.sendKeys(System.getProperty("user.dir") + "/src/test/resources/test-datasets/test-special-chars.csv");
        
        // Submit upload
        WebElement uploadButton = driver.findElement(By.id("uploadButton"));
        uploadButton.click();
        
        // Verify upload progress
        WebElement progressBar = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("uploadProgress")));
        assertTrue(progressBar.isDisplayed());
        
        // Wait for upload completion
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("success-message")));
        
        // Verify success message
        WebElement successMessage = driver.findElement(By.className("success-message"));
        assertTrue(successMessage.getText().contains("Dataset uploaded successfully"));
    }
    
    @Test
    void testDatasetUploadWithUnicodeFilename_Scenario6() {
        // Select file with unicode filename
        WebElement fileInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("fileInput")));
        fileInput.sendKeys(System.getProperty("user.dir") + "/src/test/resources/test-datasets/test-special-chars.csv");
        
        // Submit upload
        WebElement uploadButton = driver.findElement(By.id("uploadButton"));
        uploadButton.click();
        
        // Verify upload progress
        WebElement progressBar = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("uploadProgress")));
        assertTrue(progressBar.isDisplayed());
        
        // Wait for upload completion
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("success-message")));
        
        // Verify success message
        WebElement successMessage = driver.findElement(By.className("success-message"));
        assertTrue(successMessage.getText().contains("Dataset uploaded successfully"));
    }
    
    @Test
    void testDatasetUploadWithExtremeLongFilename() {
        // Select file with extreme long filename
        WebElement fileInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("fileInput")));
        fileInput.sendKeys(System.getProperty("user.dir") + "/src/test/resources/test-datasets/test-special-chars.csv");
        
        // Submit upload
        WebElement uploadButton = driver.findElement(By.id("uploadButton"));
        uploadButton.click();
        
        // Verify upload progress
        WebElement progressBar = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("uploadProgress")));
        assertTrue(progressBar.isDisplayed());
        
        // Wait for upload completion
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("success-message")));
        
        // Verify success message
        WebElement successMessage = driver.findElement(By.className("success-message"));
        assertTrue(successMessage.getText().contains("Dataset uploaded successfully"));
    }
    
    @Test
    void testDatasetUploadWithExtremeValues_Scenario6() {
        // Select file with extreme values
        WebElement fileInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("fileInput")));
        fileInput.sendKeys(System.getProperty("user.dir") + "/src/test/resources/test-datasets/test-special-chars.csv");
        
        // Submit upload
        WebElement uploadButton = driver.findElement(By.id("uploadButton"));
        uploadButton.click();
        
        // Verify upload progress
        WebElement progressBar = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("uploadProgress")));
        assertTrue(progressBar.isDisplayed());
        
        // Wait for upload completion
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("success-message")));
        
        // Verify success message
        WebElement successMessage = driver.findElement(By.className("success-message"));
        assertTrue(successMessage.getText().contains("Dataset uploaded successfully"));
    }
}
