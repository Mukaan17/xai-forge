package com.example.xaiapp.util;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;
import java.util.List;

/**
 * Base class for Selenium E2E tests
 * Provides common setup, teardown, and utility methods
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public abstract class SeleniumTestBase {
    
    protected WebDriver driver;
    protected WebDriverWait wait;
    
    @LocalServerPort
    protected int port;
    
    protected String baseUrl;
    protected String frontendUrl;
    
    @BeforeEach
    public void setUp() {
        // Setup ChromeDriver
        WebDriverManager.chromedriver().setup();
        
        // Configure Chrome options
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless"); // Run in headless mode for CI/CD
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");
        options.addArguments("--window-size=1920,1080");
        
        // Create WebDriver instance
        driver = new ChromeDriver(options);
        
        // Setup WebDriverWait with timeout
        wait = new WebDriverWait(driver, Duration.ofSeconds(TestConstants.SELENIUM_EXPLICIT_WAIT));
        
        // Setup URLs
        baseUrl = "http://localhost:" + port;
        frontendUrl = TestConstants.FRONTEND_BASE_URL;
        
        // Set implicit wait
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(TestConstants.SELENIUM_IMPLICIT_WAIT));
    }
    
    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
    
    /**
     * Navigate to a specific URL
     */
    protected void navigateTo(String url) {
        driver.get(url);
    }
    
    /**
     * Navigate to the frontend application
     */
    protected void navigateToFrontend() {
        navigateTo(frontendUrl);
    }
    
    /**
     * Navigate to the backend API
     */
    protected void navigateToBackend() {
        navigateTo(baseUrl);
    }
    
    /**
     * Wait for an element to be visible and return it
     */
    protected WebElement waitForElement(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }
    
    /**
     * Wait for an element to be clickable and return it
     */
    protected WebElement waitForClickable(By locator) {
        return wait.until(ExpectedConditions.elementToBeClickable(locator));
    }
    
    /**
     * Wait for an element to be present and return it
     */
    protected WebElement waitForPresent(By locator) {
        return wait.until(ExpectedConditions.presenceOfElementLocated(locator));
    }
    
    /**
     * Wait for text to be present in an element
     */
    protected void waitForText(By locator, String text) {
        wait.until(ExpectedConditions.textToBePresentInElementLocated(locator, text));
    }
    
    /**
     * Wait for URL to contain specific text
     */
    protected void waitForUrlContains(String urlPart) {
        wait.until(ExpectedConditions.urlContains(urlPart));
    }
    
    /**
     * Wait for page title to contain specific text
     */
    protected void waitForTitleContains(String titlePart) {
        wait.until(ExpectedConditions.titleContains(titlePart));
    }
    
    /**
     * Click an element after waiting for it to be clickable
     */
    protected void clickElement(By locator) {
        WebElement element = waitForClickable(locator);
        element.click();
    }
    
    /**
     * Type text into an input field
     */
    protected void typeText(By locator, String text) {
        WebElement element = waitForElement(locator);
        element.clear();
        element.sendKeys(text);
    }
    
    /**
     * Get text from an element
     */
    protected String getText(By locator) {
        WebElement element = waitForElement(locator);
        return element.getText();
    }
    
    /**
     * Get attribute value from an element
     */
    protected String getAttribute(By locator, String attributeName) {
        WebElement element = waitForElement(locator);
        return element.getAttribute(attributeName);
    }
    
    /**
     * Check if an element is displayed
     */
    protected boolean isElementDisplayed(By locator) {
        try {
            WebElement element = driver.findElement(locator);
            return element.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Check if an element exists
     */
    protected boolean isElementPresent(By locator) {
        try {
            driver.findElement(locator);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Get all elements matching a locator
     */
    protected List<WebElement> getElements(By locator) {
        return driver.findElements(locator);
    }
    
    /**
     * Wait for an element to disappear
     */
    protected void waitForElementToDisappear(By locator) {
        wait.until(ExpectedConditions.invisibilityOfElementLocated(locator));
    }
    
    /**
     * Wait for an element to be selected
     */
    protected void waitForElementToBeSelected(By locator) {
        wait.until(ExpectedConditions.elementToBeSelected(locator));
    }
    
    /**
     * Wait for an alert to be present
     */
    protected void waitForAlert() {
        wait.until(ExpectedConditions.alertIsPresent());
    }
    
    /**
     * Take a screenshot (for debugging)
     */
    protected void takeScreenshot(String filename) {
        try {
            ((org.openqa.selenium.TakesScreenshot) driver)
                .getScreenshotAs(org.openqa.selenium.OutputType.FILE);
            // In a real implementation, you might want to save this to a specific directory
            System.out.println("Screenshot taken: " + filename);
        } catch (Exception e) {
            System.err.println("Failed to take screenshot: " + e.getMessage());
        }
    }
    
    /**
     * Scroll to an element
     */
    protected void scrollToElement(By locator) {
        WebElement element = waitForElement(locator);
        ((org.openqa.selenium.JavascriptExecutor) driver)
            .executeScript("arguments[0].scrollIntoView(true);", element);
    }
    
    /**
     * Execute JavaScript
     */
    protected Object executeJavaScript(String script, Object... args) {
        return ((org.openqa.selenium.JavascriptExecutor) driver).executeScript(script, args);
    }
    
    /**
     * Wait for a specific condition with custom timeout
     */
    protected void waitForCondition(org.openqa.selenium.support.ui.ExpectedCondition<?> condition, 
                                   Duration timeout) {
        WebDriverWait customWait = new WebDriverWait(driver, timeout);
        customWait.until(condition);
    }
    
    /**
     * Get current page title
     */
    protected String getPageTitle() {
        return driver.getTitle();
    }
    
    /**
     * Get current URL
     */
    protected String getCurrentUrl() {
        return driver.getCurrentUrl();
    }
    
    /**
     * Navigate back
     */
    protected void navigateBack() {
        driver.navigate().back();
    }
    
    /**
     * Navigate forward
     */
    protected void navigateForward() {
        driver.navigate().forward();
    }
    
    /**
     * Refresh page
     */
    protected void refreshPage() {
        driver.navigate().refresh();
    }
    
    /**
     * Switch to a new window/tab
     */
    protected void switchToNewWindow() {
        for (String windowHandle : driver.getWindowHandles()) {
            driver.switchTo().window(windowHandle);
        }
    }
    
    /**
     * Close current window and switch to the remaining one
     */
    protected void closeCurrentWindow() {
        driver.close();
        switchToNewWindow();
    }
}
