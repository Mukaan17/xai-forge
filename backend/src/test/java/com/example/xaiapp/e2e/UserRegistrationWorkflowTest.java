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
 * Selenium E2E tests for user registration workflow
 * Tests complete user journey from registration to dashboard access
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Transactional
class UserRegistrationWorkflowTest extends SeleniumTestBase {
    
    @BeforeEach
    public void setUp() {
        super.setUp();
        // Navigate to registration page
        driver.get(baseUrl + "/register");
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }
    
    @Test
    void testCompleteUserRegistrationWorkflow() {
        // Step 1: Fill registration form
        WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("username")));
        usernameField.sendKeys(TestConstants.TEST_USERNAME);
        
        WebElement emailField = driver.findElement(By.id("email"));
        emailField.sendKeys(TestConstants.TEST_EMAIL);
        
        WebElement passwordField = driver.findElement(By.id("password"));
        passwordField.sendKeys(TestConstants.TEST_PASSWORD);
        
        WebElement confirmPasswordField = driver.findElement(By.id("confirmPassword"));
        confirmPasswordField.sendKeys(TestConstants.TEST_PASSWORD);
        
        // Step 2: Submit registration
        WebElement registerButton = driver.findElement(By.id("registerButton"));
        registerButton.click();
        
        // Step 3: Verify successful registration and redirect to login
        wait.until(ExpectedConditions.urlContains("/login"));
        assertTrue(driver.getCurrentUrl().contains("/login"));
        
        // Step 4: Verify success message is displayed
        WebElement successMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("success-message")));
        assertTrue(successMessage.getText().contains("Registration successful"));
        
        // Step 5: Login with new credentials
        WebElement loginUsernameField = driver.findElement(By.id("username"));
        loginUsernameField.sendKeys(TestConstants.TEST_USERNAME);
        
        WebElement loginPasswordField = driver.findElement(By.id("password"));
        loginPasswordField.sendKeys(TestConstants.TEST_PASSWORD);
        
        WebElement loginButton = driver.findElement(By.id("loginButton"));
        loginButton.click();
        
        // Step 6: Verify successful login and redirect to dashboard
        wait.until(ExpectedConditions.urlContains("/dashboard"));
        assertTrue(driver.getCurrentUrl().contains("/dashboard"));
        
        // Step 7: Verify dashboard elements are present
        WebElement welcomeMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("welcome-message")));
        assertTrue(welcomeMessage.getText().contains("Welcome"));
        
        WebElement userMenu = driver.findElement(By.className("user-menu"));
        assertTrue(userMenu.isDisplayed());
    }
    
    @Test
    void testUserRegistrationWithInvalidEmail() {
        // Fill registration form with invalid email
        WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("username")));
        usernameField.sendKeys(TestConstants.TEST_USERNAME);
        
        WebElement emailField = driver.findElement(By.id("email"));
        emailField.sendKeys("invalid-email");
        
        WebElement passwordField = driver.findElement(By.id("password"));
        passwordField.sendKeys(TestConstants.TEST_PASSWORD);
        
        WebElement confirmPasswordField = driver.findElement(By.id("confirmPassword"));
        confirmPasswordField.sendKeys(TestConstants.TEST_PASSWORD);
        
        // Submit registration
        WebElement registerButton = driver.findElement(By.id("registerButton"));
        registerButton.click();
        
        // Verify error message is displayed
        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("error-message")));
        assertTrue(errorMessage.getText().contains("Invalid email format"));
        
        // Verify user remains on registration page
        assertTrue(driver.getCurrentUrl().contains("/register"));
    }
    
    @Test
    void testUserRegistrationWithWeakPassword() {
        // Fill registration form with weak password
        WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("username")));
        usernameField.sendKeys(TestConstants.TEST_USERNAME);
        
        WebElement emailField = driver.findElement(By.id("email"));
        emailField.sendKeys(TestConstants.TEST_EMAIL);
        
        WebElement passwordField = driver.findElement(By.id("password"));
        passwordField.sendKeys("123"); // Weak password
        
        WebElement confirmPasswordField = driver.findElement(By.id("confirmPassword"));
        confirmPasswordField.sendKeys("123");
        
        // Submit registration
        WebElement registerButton = driver.findElement(By.id("registerButton"));
        registerButton.click();
        
        // Verify error message is displayed
        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("error-message")));
        assertTrue(errorMessage.getText().contains("Password must be at least 8 characters"));
        
        // Verify user remains on registration page
        assertTrue(driver.getCurrentUrl().contains("/register"));
    }
    
    @Test
    void testUserRegistrationWithMismatchedPasswords() {
        // Fill registration form with mismatched passwords
        WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("username")));
        usernameField.sendKeys(TestConstants.TEST_USERNAME);
        
        WebElement emailField = driver.findElement(By.id("email"));
        emailField.sendKeys(TestConstants.TEST_EMAIL);
        
        WebElement passwordField = driver.findElement(By.id("password"));
        passwordField.sendKeys(TestConstants.TEST_PASSWORD);
        
        WebElement confirmPasswordField = driver.findElement(By.id("confirmPassword"));
        confirmPasswordField.sendKeys("different-password");
        
        // Submit registration
        WebElement registerButton = driver.findElement(By.id("registerButton"));
        registerButton.click();
        
        // Verify error message is displayed
        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("error-message")));
        assertTrue(errorMessage.getText().contains("Passwords do not match"));
        
        // Verify user remains on registration page
        assertTrue(driver.getCurrentUrl().contains("/register"));
    }
    
    @Test
    void testUserRegistrationWithEmptyFields() {
        // Submit registration with empty fields
        WebElement registerButton = driver.findElement(By.id("registerButton"));
        registerButton.click();
        
        // Verify error messages are displayed for each field
        WebElement usernameError = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("username-error")));
        assertTrue(usernameError.getText().contains("Username is required"));
        
        WebElement emailError = driver.findElement(By.id("email-error"));
        assertTrue(emailError.getText().contains("Email is required"));
        
        WebElement passwordError = driver.findElement(By.id("password-error"));
        assertTrue(passwordError.getText().contains("Password is required"));
        
        // Verify user remains on registration page
        assertTrue(driver.getCurrentUrl().contains("/register"));
    }
    
    @Test
    void testUserRegistrationWithSpecialCharacters() {
        // Fill registration form with special characters
        WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("username")));
        usernameField.sendKeys("user@domain.com");
        
        WebElement emailField = driver.findElement(By.id("email"));
        emailField.sendKeys("user@domain.com");
        
        WebElement passwordField = driver.findElement(By.id("password"));
        passwordField.sendKeys(TestConstants.TEST_PASSWORD);
        
        WebElement confirmPasswordField = driver.findElement(By.id("confirmPassword"));
        confirmPasswordField.sendKeys(TestConstants.TEST_PASSWORD);
        
        // Submit registration
        WebElement registerButton = driver.findElement(By.id("registerButton"));
        registerButton.click();
        
        // Verify successful registration
        wait.until(ExpectedConditions.urlContains("/login"));
        assertTrue(driver.getCurrentUrl().contains("/login"));
        
        // Verify success message is displayed
        WebElement successMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("success-message")));
        assertTrue(successMessage.getText().contains("Registration successful"));
    }
    
    @Test
    void testUserRegistrationWithUnicodeCharacters() {
        // Fill registration form with unicode characters
        WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("username")));
        usernameField.sendKeys("测试用户");
        
        WebElement emailField = driver.findElement(By.id("email"));
        emailField.sendKeys("test@测试.com");
        
        WebElement passwordField = driver.findElement(By.id("password"));
        passwordField.sendKeys(TestConstants.TEST_PASSWORD);
        
        WebElement confirmPasswordField = driver.findElement(By.id("confirmPassword"));
        confirmPasswordField.sendKeys(TestConstants.TEST_PASSWORD);
        
        // Submit registration
        WebElement registerButton = driver.findElement(By.id("registerButton"));
        registerButton.click();
        
        // Verify successful registration
        wait.until(ExpectedConditions.urlContains("/login"));
        assertTrue(driver.getCurrentUrl().contains("/login"));
        
        // Verify success message is displayed
        WebElement successMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("success-message")));
        assertTrue(successMessage.getText().contains("Registration successful"));
    }
    
    @Test
    void testUserRegistrationWithExtremeValues_Scenario2() {
        // Fill registration form with extreme values
        WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("username")));
        usernameField.sendKeys("a".repeat(1000));
        
        WebElement emailField = driver.findElement(By.id("email"));
        emailField.sendKeys("a".repeat(1000) + "@example.com");
        
        WebElement passwordField = driver.findElement(By.id("password"));
        passwordField.sendKeys("a".repeat(1000));
        
        WebElement confirmPasswordField = driver.findElement(By.id("confirmPassword"));
        confirmPasswordField.sendKeys("a".repeat(1000));
        
        // Submit registration
        WebElement registerButton = driver.findElement(By.id("registerButton"));
        registerButton.click();
        
        // Verify error message is displayed
        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("error-message")));
        assertTrue(errorMessage.getText().contains("Invalid parameters"));
        
        // Verify user remains on registration page
        assertTrue(driver.getCurrentUrl().contains("/register"));
    }
    
    @Test
    void testUserRegistrationWithWhitespaceFields() {
        // Fill registration form with whitespace fields
        WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("username")));
        usernameField.sendKeys("   ");
        
        WebElement emailField = driver.findElement(By.id("email"));
        emailField.sendKeys("   ");
        
        WebElement passwordField = driver.findElement(By.id("password"));
        passwordField.sendKeys("   ");
        
        WebElement confirmPasswordField = driver.findElement(By.id("confirmPassword"));
        confirmPasswordField.sendKeys("   ");
        
        // Submit registration
        WebElement registerButton = driver.findElement(By.id("registerButton"));
        registerButton.click();
        
        // Verify error messages are displayed
        WebElement usernameError = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("username-error")));
        assertTrue(usernameError.getText().contains("Username is required"));
        
        WebElement emailError = driver.findElement(By.id("email-error"));
        assertTrue(emailError.getText().contains("Email is required"));
        
        WebElement passwordError = driver.findElement(By.id("password-error"));
        assertTrue(passwordError.getText().contains("Password is required"));
        
        // Verify user remains on registration page
        assertTrue(driver.getCurrentUrl().contains("/register"));
    }
    
    @Test
    void testUserRegistrationWithNullFields() {
        // Fill registration form with null fields
        WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("username")));
        usernameField.sendKeys("");
        
        WebElement emailField = driver.findElement(By.id("email"));
        emailField.sendKeys("");
        
        WebElement passwordField = driver.findElement(By.id("password"));
        passwordField.sendKeys("");
        
        WebElement confirmPasswordField = driver.findElement(By.id("confirmPassword"));
        confirmPasswordField.sendKeys("");
        
        // Submit registration
        WebElement registerButton = driver.findElement(By.id("registerButton"));
        registerButton.click();
        
        // Verify error messages are displayed
        WebElement usernameError = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("username-error")));
        assertTrue(usernameError.getText().contains("Username is required"));
        
        WebElement emailError = driver.findElement(By.id("email-error"));
        assertTrue(emailError.getText().contains("Email is required"));
        
        WebElement passwordError = driver.findElement(By.id("password-error"));
        assertTrue(passwordError.getText().contains("Password is required"));
        
        // Verify user remains on registration page
        assertTrue(driver.getCurrentUrl().contains("/register"));
    }
    
    @Test
    void testUserRegistrationWithExtremeLongValues() {
        // Fill registration form with extreme long values
        WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("username")));
        usernameField.sendKeys("a".repeat(1000));
        
        WebElement emailField = driver.findElement(By.id("email"));
        emailField.sendKeys("a".repeat(1000) + "@example.com");
        
        WebElement passwordField = driver.findElement(By.id("password"));
        passwordField.sendKeys("a".repeat(1000));
        
        WebElement confirmPasswordField = driver.findElement(By.id("confirmPassword"));
        confirmPasswordField.sendKeys("a".repeat(1000));
        
        // Submit registration
        WebElement registerButton = driver.findElement(By.id("registerButton"));
        registerButton.click();
        
        // Verify error message is displayed
        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("error-message")));
        assertTrue(errorMessage.getText().contains("Invalid parameters"));
        
        // Verify user remains on registration page
        assertTrue(driver.getCurrentUrl().contains("/register"));
    }
    
    @Test
    void testUserRegistrationWithExtremeValues_Scenario6() {
        // Fill registration form with extreme values
        WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("username")));
        usernameField.sendKeys("a".repeat(1000));
        
        WebElement emailField = driver.findElement(By.id("email"));
        emailField.sendKeys("a".repeat(1000) + "@example.com");
        
        WebElement passwordField = driver.findElement(By.id("password"));
        passwordField.sendKeys("a".repeat(1000));
        
        WebElement confirmPasswordField = driver.findElement(By.id("confirmPassword"));
        confirmPasswordField.sendKeys("a".repeat(1000));
        
        // Submit registration
        WebElement registerButton = driver.findElement(By.id("registerButton"));
        registerButton.click();
        
        // Verify error message is displayed
        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("error-message")));
        assertTrue(errorMessage.getText().contains("Invalid parameters"));
        
        // Verify user remains on registration page
        assertTrue(driver.getCurrentUrl().contains("/register"));
    }
}
