# Test Implementation Guide

## Overview

This document provides a comprehensive guide to the test implementation for the XAI Application. The test suite includes unit tests, integration tests, API tests, and end-to-end (E2E) tests, all designed to achieve 80% code coverage.

## Test Architecture

### Test Structure

```
backend/src/test/java/com/example/xaiapp/
├── unit/                    # Unit tests
│   ├── service/            # Service layer tests
│   ├── builder/            # Builder pattern tests
│   ├── factory/            # Factory pattern tests
│   ├── strategy/           # Strategy pattern tests
│   └── config/             # Configuration tests
├── integration/            # Integration tests
│   ├── controller/         # Controller integration tests
│   ├── repository/         # Repository integration tests
│   └── security/           # Security integration tests
├── api/                    # API contract tests
│   ├── AuthApiTest.java
│   ├── DatasetApiTest.java
│   ├── ModelApiTest.java
│   └── ErrorHandlingApiTest.java
├── e2e/                    # End-to-end tests
│   ├── UserRegistrationWorkflowTest.java
│   ├── DatasetUploadWorkflowTest.java
│   ├── ModelTrainingWorkflowTest.java
│   └── PredictionWorkflowTest.java
└── util/                   # Test utilities
    ├── TestConstants.java
    ├── TestDataBuilder.java
    ├── SeleniumTestBase.java
    ├── ApiTestBase.java
    ├── ExcelTestReporter.java
    └── CoverageToExcelConverter.java
```

## Test Types

### 1. Unit Tests

**Purpose**: Test individual components in isolation
**Framework**: JUnit 5, Mockito
**Coverage**: Services, builders, factories, strategies, configuration validators

#### Key Features:
- Mock external dependencies
- Test business logic in isolation
- Verify error handling
- Test edge cases and boundary conditions

#### Example:
```java
@Test
void testModelServiceTrainModel() {
    // Arrange
    when(datasetRepository.findById(anyLong())).thenReturn(Optional.of(testDataset));
    when(modelRepository.save(any(MLModel.class))).thenReturn(testModel);
    
    // Act
    ModelDto result = modelService.trainModel(trainRequest);
    
    // Assert
    assertNotNull(result);
    assertEquals(testModel.getModelName(), result.getModelName());
    verify(modelRepository).save(any(MLModel.class));
}
```

### 2. Integration Tests

**Purpose**: Test component interactions with real dependencies
**Framework**: Spring Boot Test, Testcontainers
**Coverage**: Controllers, repositories, security

#### Key Features:
- Use real database (PostgreSQL via Testcontainers)
- Test complete request/response cycles
- Verify database transactions
- Test security configurations

#### Example:
```java
@Test
void testDatasetControllerIntegration() {
    // Arrange
    User testUser = createTestUser();
    userRepository.save(testUser);
    String authToken = authenticateUser(testUser);
    
    // Act
    ResponseEntity<ApiResponse> response = testRestTemplate
        .withBasicAuth("testuser", "password123")
        .postForEntity("/api/datasets/upload", multipartFile, ApiResponse.class);
    
    // Assert
    assertEquals(HttpStatus.CREATED, response.getStatusCode());
    assertTrue(response.getBody().isSuccess());
}
```

### 3. API Tests

**Purpose**: Test API contracts and response formats
**Framework**: REST Assured
**Coverage**: All REST endpoints, error handling, edge cases

#### Key Features:
- Test JSON schema validation
- Verify response structure
- Test error formats
- Test authentication and authorization

#### Example:
```java
@Test
void testRegistrationApiContract() {
    given()
        .contentType(ContentType.JSON)
        .body(registrationRequest)
    .when()
        .post("/api/auth/register")
    .then()
        .statusCode(201)
        .contentType(ContentType.JSON)
        .body("success", equalTo(true))
        .body("data.username", equalTo("testuser"))
        .body("data.password", nullValue());
}
```

### 4. E2E Tests

**Purpose**: Test complete user workflows
**Framework**: Selenium WebDriver
**Coverage**: User registration, dataset upload, model training, prediction

#### Key Features:
- Test complete user journeys
- Verify UI interactions
- Test file uploads
- Test form validations

#### Example:
```java
@Test
void testCompleteUserRegistrationWorkflow() {
    // Step 1: Fill registration form
    WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("username")));
    usernameField.sendKeys("testuser");
    
    // Step 2: Submit registration
    WebElement registerButton = driver.findElement(By.id("registerButton"));
    registerButton.click();
    
    // Step 3: Verify successful registration
    wait.until(ExpectedConditions.urlContains("/login"));
    assertTrue(driver.getCurrentUrl().contains("/login"));
}
```

## Test Utilities

### TestConstants

Centralized constants for test data:
```java
public class TestConstants {
    public static final String TEST_USERNAME = "testuser";
    public static final String TEST_EMAIL = "test@example.com";
    public static final String TEST_PASSWORD = "password123";
    // ... more constants
}
```

### TestDataBuilder

Builder pattern for creating test data:
```java
public class TestDataBuilder {
    public static User createTestUser() {
        return User.builder()
            .username(TEST_USERNAME)
            .email(TEST_EMAIL)
            .password(TEST_PASSWORD)
            .build();
    }
    // ... more builders
}
```

### SeleniumTestBase

Base class for E2E tests:
```java
public abstract class SeleniumTestBase {
    protected WebDriver driver;
    protected WebDriverWait wait;
    protected String baseUrl;
    
    @BeforeEach
    void setUp() {
        // WebDriver setup
    }
    
    @AfterEach
    void tearDown() {
        // Cleanup
    }
}
```

### ApiTestBase

Base class for API tests:
```java
public abstract class ApiTestBase {
    protected String authToken;
    
    protected void createTestUserWorkflow() {
        // Create user and authenticate
    }
}
```

## Test Configuration

### Maven Configuration

```xml
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.8</version>
    <executions>
        <execution>
            <goals>
                <goal>prepare-agent</goal>
            </goals>
        </execution>
        <execution>
            <id>report</id>
            <phase>test</phase>
            <goals>
                <goal>report</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

### Test Profiles

**application-test.properties**:
```properties
spring.datasource.url=jdbc:tc:postgresql:13:///testdb
spring.datasource.driver-class-name=org.testcontainers.jdbc.ContainerDatabaseDriver
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true
```

## Running Tests

### Unit Tests
```bash
mvn test -Dtest="*UnitTest"
```

### Integration Tests
```bash
mvn test -Dtest="*IntegrationTest"
```

### API Tests
```bash
mvn test -Dtest="*ApiTest"
```

### E2E Tests
```bash
mvn test -Dtest="*WorkflowTest"
```

### All Tests
```bash
mvn test
```

### Coverage Report
```bash
mvn jacoco:report
```

## Test Data

### Test Datasets

Located in `src/test/resources/test-datasets/`:
- `test-classification-small.csv` - Small classification dataset
- `test-regression-small.csv` - Small regression dataset
- `test-invalid-empty.csv` - Empty file for error testing
- `test-invalid-missing-header.csv` - Missing header for error testing
- `test-special-chars.csv` - Special characters for robustness testing

### Test Models

Test models are created dynamically using `TestDataBuilder`:
```java
MLModel testModel = TestDataBuilder.createTestModel(testDataset, "Test Model");
```

## Best Practices

### 1. Test Naming
- Use descriptive test method names
- Follow the pattern: `test[MethodName]_[Scenario]_[ExpectedResult]`

### 2. Test Structure
- Follow AAA pattern: Arrange, Act, Assert
- Keep tests focused and single-purpose
- Use meaningful assertions

### 3. Test Data
- Use builders for complex objects
- Keep test data minimal and focused
- Use constants for reusable values

### 4. Assertions
- Use specific assertions
- Test both positive and negative cases
- Verify side effects

### 5. Cleanup
- Clean up test data after each test
- Use `@Transactional` for database tests
- Close resources properly

## Troubleshooting

### Common Issues

1. **Testcontainers not starting**
   - Ensure Docker is running
   - Check Docker daemon status

2. **Selenium WebDriver issues**
   - Update WebDriverManager
   - Check browser compatibility

3. **Database connection issues**
   - Verify Testcontainers configuration
   - Check database URL format

4. **Coverage not reaching 80%**
   - Identify uncovered lines
   - Add tests for missing scenarios
   - Check for dead code

### Debug Tips

1. **Enable debug logging**:
```properties
logging.level.com.example.xaiapp=DEBUG
```

2. **Use test profiles**:
```bash
mvn test -Dspring.profiles.active=test
```

3. **Run specific test classes**:
```bash
mvn test -Dtest=ModelServiceTest
```

## Continuous Integration

### GitHub Actions Configuration

```yaml
name: Test Suite
on: [push, pull_request]
jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - name: Set up JDK 17
        uses: actions/setup-java@v2
        with:
          java-version: '17'
      - name: Run tests
        run: mvn test
      - name: Generate coverage report
        run: mvn jacoco:report
```

## Performance Considerations

### Test Execution Time
- Unit tests: < 1 second each
- Integration tests: < 5 seconds each
- API tests: < 3 seconds each
- E2E tests: < 10 seconds each

### Optimization Tips
- Use `@DirtiesContext` sparingly
- Reuse test data when possible
- Use parallel execution for independent tests
- Mock external services

## Maintenance

### Regular Tasks
1. Update test dependencies
2. Review and update test data
3. Check for flaky tests
4. Update documentation

### Monitoring
- Track test execution time
- Monitor coverage trends
- Identify failing tests
- Review test quality metrics

## Conclusion

This test implementation provides comprehensive coverage of the XAI Application with multiple testing layers. The combination of unit, integration, API, and E2E tests ensures robust validation of all application components while maintaining high code coverage and test quality.

For questions or issues, refer to the troubleshooting section or contact the development team.
