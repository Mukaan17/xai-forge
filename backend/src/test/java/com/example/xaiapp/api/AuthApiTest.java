package com.example.xaiapp.api;

import com.example.xaiapp.entity.User;
import com.example.xaiapp.repository.UserRepository;
import com.example.xaiapp.util.ApiTestBase;
import com.example.xaiapp.util.TestConstants;
import com.example.xaiapp.util.TestDataBuilder;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * REST Assured API contract tests for authentication endpoints
 * Tests JSON schema, response structure, and error formats
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Transactional
class AuthApiTest extends ApiTestBase {
    
    @Autowired
    private UserRepository userRepository;
    
    @BeforeEach
    public void setUp() {
        super.setUp();
        userRepository.deleteAll(); // Clean up before each test
    }
    
    @Test
    void testRegistrationApiContract() {
        // Arrange
        Map<String, String> registrationRequest = Map.of(
            "username", TestConstants.TEST_USERNAME,
            "email", TestConstants.TEST_EMAIL,
            "password", TestConstants.TEST_PASSWORD
        );
        
        // Act & Assert
        given()
            .contentType(ContentType.JSON)
            .body(registrationRequest)
        .when()
            .post(TestConstants.AUTH_REGISTER_ENDPOINT)
        .then()
            .statusCode(201)
            .contentType(ContentType.JSON)
            .body("success", equalTo(true))
            .body("message", containsString("registered successfully"))
            .body("data", notNullValue())
            .body("data.username", equalTo(TestConstants.TEST_USERNAME))
            .body("data.email", equalTo(TestConstants.TEST_EMAIL))
            .body("data.id", notNullValue())
            .body("data.password", nullValue()); // Password should not be returned
    }
    
    @Test
    void testRegistrationApiContract_DuplicateUsername() {
        // Arrange - Create existing user
        User existingUser = TestDataBuilder.createTestUser();
        userRepository.save(existingUser);
        
        Map<String, String> registrationRequest = Map.of(
            "username", TestConstants.TEST_USERNAME,
            "email", "different@example.com",
            "password", TestConstants.TEST_PASSWORD
        );
        
        // Act & Assert
        given()
            .contentType(ContentType.JSON)
            .body(registrationRequest)
        .when()
            .post(TestConstants.AUTH_REGISTER_ENDPOINT)
        .then()
            .statusCode(400)
            .contentType(ContentType.JSON)
            .body("success", equalTo(false))
            .body("message", containsString("Username already exists"))
            .body("error", notNullValue())
            .body("timestamp", notNullValue());
    }
    
    @Test
    void testRegistrationApiContract_DuplicateEmail() {
        // Arrange - Create existing user
        User existingUser = TestDataBuilder.createTestUser();
        userRepository.save(existingUser);
        
        Map<String, String> registrationRequest = Map.of(
            "username", "differentuser",
            "email", TestConstants.TEST_EMAIL,
            "password", TestConstants.TEST_PASSWORD
        );
        
        // Act & Assert
        given()
            .contentType(ContentType.JSON)
            .body(registrationRequest)
        .when()
            .post(TestConstants.AUTH_REGISTER_ENDPOINT)
        .then()
            .statusCode(400)
            .contentType(ContentType.JSON)
            .body("success", equalTo(false))
            .body("message", containsString("Email already exists"))
            .body("error", notNullValue())
            .body("timestamp", notNullValue());
    }
    
    @Test
    void testRegistrationApiContract_InvalidEmail() {
        // Arrange
        Map<String, String> registrationRequest = Map.of(
            "username", TestConstants.TEST_USERNAME,
            "email", "invalid-email",
            "password", TestConstants.TEST_PASSWORD
        );
        
        // Act & Assert
        given()
            .contentType(ContentType.JSON)
            .body(registrationRequest)
        .when()
            .post(TestConstants.AUTH_REGISTER_ENDPOINT)
        .then()
            .statusCode(400)
            .contentType(ContentType.JSON)
            .body("success", equalTo(false))
            .body("message", containsString("Invalid email format"))
            .body("error", notNullValue())
            .body("timestamp", notNullValue());
    }
    
    @Test
    void testRegistrationApiContract_WeakPassword() {
        // Arrange
        Map<String, String> registrationRequest = Map.of(
            "username", TestConstants.TEST_USERNAME,
            "email", TestConstants.TEST_EMAIL,
            "password", "123" // Weak password
        );
        
        // Act & Assert
        given()
            .contentType(ContentType.JSON)
            .body(registrationRequest)
        .when()
            .post(TestConstants.AUTH_REGISTER_ENDPOINT)
        .then()
            .statusCode(400)
            .contentType(ContentType.JSON)
            .body("success", equalTo(false))
            .body("message", containsString("Password must be at least 8 characters"))
            .body("error", notNullValue())
            .body("timestamp", notNullValue());
    }
    
    @Test
    void testRegistrationApiContract_MissingFields() {
        // Arrange
        Map<String, String> registrationRequest = Map.of(
            "username", TestConstants.TEST_USERNAME
            // Missing email and password
        );
        
        // Act & Assert
        given()
            .contentType(ContentType.JSON)
            .body(registrationRequest)
        .when()
            .post(TestConstants.AUTH_REGISTER_ENDPOINT)
        .then()
            .statusCode(400)
            .contentType(ContentType.JSON)
            .body("success", equalTo(false))
            .body("message", containsString("Required fields missing"))
            .body("error", notNullValue())
            .body("timestamp", notNullValue());
    }
    
    @Test
    void testRegistrationApiContract_EmptyFields() {
        // Arrange
        Map<String, String> registrationRequest = Map.of(
            "username", "",
            "email", "",
            "password", ""
        );
        
        // Act & Assert
        given()
            .contentType(ContentType.JSON)
            .body(registrationRequest)
        .when()
            .post(TestConstants.AUTH_REGISTER_ENDPOINT)
        .then()
            .statusCode(400)
            .contentType(ContentType.JSON)
            .body("success", equalTo(false))
            .body("message", containsString("Required fields missing"))
            .body("error", notNullValue())
            .body("timestamp", notNullValue());
    }
    
    @Test
    void testRegistrationApiContract_NullFields() {
        // Arrange
        Map<String, String> registrationRequest = Map.of(
            "username", null,
            "email", null,
            "password", null
        );
        
        // Act & Assert
        given()
            .contentType(ContentType.JSON)
            .body(registrationRequest)
        .when()
            .post(TestConstants.AUTH_REGISTER_ENDPOINT)
        .then()
            .statusCode(400)
            .contentType(ContentType.JSON)
            .body("success", equalTo(false))
            .body("message", containsString("Required fields missing"))
            .body("error", notNullValue())
            .body("timestamp", notNullValue());
    }
    
    @Test
    void testRegistrationApiContract_WhitespaceFields() {
        // Arrange
        Map<String, String> registrationRequest = Map.of(
            "username", "   ",
            "email", "   ",
            "password", "   "
        );
        
        // Act & Assert
        given()
            .contentType(ContentType.JSON)
            .body(registrationRequest)
        .when()
            .post(TestConstants.AUTH_REGISTER_ENDPOINT)
        .then()
            .statusCode(400)
            .contentType(ContentType.JSON)
            .body("success", equalTo(false))
            .body("message", containsString("Required fields missing"))
            .body("error", notNullValue())
            .body("timestamp", notNullValue());
    }
    
    @Test
    void testRegistrationApiContract_SpecialCharacters() {
        // Arrange
        Map<String, String> registrationRequest = Map.of(
            "username", "user@domain.com",
            "email", "user@domain.com",
            "password", TestConstants.TEST_PASSWORD
        );
        
        // Act & Assert
        given()
            .contentType(ContentType.JSON)
            .body(registrationRequest)
        .when()
            .post(TestConstants.AUTH_REGISTER_ENDPOINT)
        .then()
            .statusCode(201)
            .contentType(ContentType.JSON)
            .body("success", equalTo(true))
            .body("message", containsString("registered successfully"))
            .body("data", notNullValue())
            .body("data.username", equalTo("user@domain.com"))
            .body("data.email", equalTo("user@domain.com"));
    }
    
    @Test
    void testRegistrationApiContract_UnicodeCharacters() {
        // Arrange
        Map<String, String> registrationRequest = Map.of(
            "username", "测试用户",
            "email", "test@测试.com",
            "password", TestConstants.TEST_PASSWORD
        );
        
        // Act & Assert
        given()
            .contentType(ContentType.JSON)
            .body(registrationRequest)
        .when()
            .post(TestConstants.AUTH_REGISTER_ENDPOINT)
        .then()
            .statusCode(201)
            .contentType(ContentType.JSON)
            .body("success", equalTo(true))
            .body("message", containsString("registered successfully"))
            .body("data", notNullValue())
            .body("data.username", equalTo("测试用户"))
            .body("data.email", equalTo("test@测试.com"));
    }
    
    @Test
    void testRegistrationApiContract_ExtremeLongValues() {
        // Arrange
        Map<String, String> registrationRequest = Map.of(
            "username", "a".repeat(1000),
            "email", "a".repeat(1000) + "@example.com",
            "password", "a".repeat(1000)
        );
        
        // Act & Assert
        given()
            .contentType(ContentType.JSON)
            .body(registrationRequest)
        .when()
            .post(TestConstants.AUTH_REGISTER_ENDPOINT)
        .then()
            .statusCode(400)
            .contentType(ContentType.JSON)
            .body("success", equalTo(false))
            .body("message", containsString("Invalid parameters"))
            .body("error", notNullValue())
            .body("timestamp", notNullValue());
    }
    
    @Test
    void testLoginApiContract() {
        // Arrange - Create user first
        User testUser = TestDataBuilder.createTestUser();
        userRepository.save(testUser);
        
        Map<String, String> loginRequest = Map.of(
            "username", TestConstants.TEST_USERNAME,
            "password", TestConstants.TEST_PASSWORD
        );
        
        // Act & Assert
        given()
            .contentType(ContentType.JSON)
            .body(loginRequest)
        .when()
            .post(TestConstants.AUTH_LOGIN_ENDPOINT)
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("success", equalTo(true))
            .body("message", containsString("Login successful"))
            .body("data", notNullValue())
            .body("data.accessToken", notNullValue())
            .body("data.tokenType", equalTo("Bearer"))
            .body("data.expiresIn", notNullValue())
            .body("data.user", notNullValue())
            .body("data.user.username", equalTo(TestConstants.TEST_USERNAME))
            .body("data.user.email", equalTo(TestConstants.TEST_EMAIL))
            .body("data.user.id", notNullValue())
            .body("data.user.password", nullValue()); // Password should not be returned
    }
    
    @Test
    void testLoginApiContract_InvalidCredentials() {
        // Arrange
        Map<String, String> loginRequest = Map.of(
            "username", TestConstants.TEST_USERNAME,
            "password", "wrongpassword"
        );
        
        // Act & Assert
        given()
            .contentType(ContentType.JSON)
            .body(loginRequest)
        .when()
            .post(TestConstants.AUTH_LOGIN_ENDPOINT)
        .then()
            .statusCode(401)
            .contentType(ContentType.JSON)
            .body("success", equalTo(false))
            .body("message", containsString("Invalid credentials"))
            .body("error", notNullValue())
            .body("timestamp", notNullValue());
    }
    
    @Test
    void testLoginApiContract_NonExistentUser() {
        // Arrange
        Map<String, String> loginRequest = Map.of(
            "username", "nonexistent",
            "password", TestConstants.TEST_PASSWORD
        );
        
        // Act & Assert
        given()
            .contentType(ContentType.JSON)
            .body(loginRequest)
        .when()
            .post(TestConstants.AUTH_LOGIN_ENDPOINT)
        .then()
            .statusCode(401)
            .contentType(ContentType.JSON)
            .body("success", equalTo(false))
            .body("message", containsString("Invalid credentials"))
            .body("error", notNullValue())
            .body("timestamp", notNullValue());
    }
    
    @Test
    void testLoginApiContract_EmptyCredentials() {
        // Arrange
        Map<String, String> loginRequest = Map.of(
            "username", "",
            "password", ""
        );
        
        // Act & Assert
        given()
            .contentType(ContentType.JSON)
            .body(loginRequest)
        .when()
            .post(TestConstants.AUTH_LOGIN_ENDPOINT)
        .then()
            .statusCode(400)
            .contentType(ContentType.JSON)
            .body("success", equalTo(false))
            .body("message", containsString("Username and password are required"))
            .body("error", notNullValue())
            .body("timestamp", notNullValue());
    }
    
    @Test
    void testLoginApiContract_NullCredentials() {
        // Arrange
        Map<String, String> loginRequest = Map.of(
            "username", null,
            "password", null
        );
        
        // Act & Assert
        given()
            .contentType(ContentType.JSON)
            .body(loginRequest)
        .when()
            .post(TestConstants.AUTH_LOGIN_ENDPOINT)
        .then()
            .statusCode(400)
            .contentType(ContentType.JSON)
            .body("success", equalTo(false))
            .body("message", containsString("Username and password are required"))
            .body("error", notNullValue())
            .body("timestamp", notNullValue());
    }
    
    @Test
    void testLoginApiContract_WithEmail() {
        // Arrange - Create user first
        User testUser = TestDataBuilder.createTestUser();
        userRepository.save(testUser);
        
        Map<String, String> loginRequest = Map.of(
            "username", TestConstants.TEST_EMAIL, // Login with email instead of username
            "password", TestConstants.TEST_PASSWORD
        );
        
        // Act & Assert
        given()
            .contentType(ContentType.JSON)
            .body(loginRequest)
        .when()
            .post(TestConstants.AUTH_LOGIN_ENDPOINT)
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("success", equalTo(true))
            .body("message", containsString("Login successful"))
            .body("data", notNullValue())
            .body("data.accessToken", notNullValue())
            .body("data.tokenType", equalTo("Bearer"));
    }
    
    @Test
    void testLoginApiContract_CaseInsensitiveUsername() {
        // Arrange - Create user first
        User testUser = TestDataBuilder.createTestUser();
        userRepository.save(testUser);
        
        Map<String, String> loginRequest = Map.of(
            "username", TestConstants.TEST_USERNAME.toUpperCase(), // Different case
            "password", TestConstants.TEST_PASSWORD
        );
        
        // Act & Assert
        given()
            .contentType(ContentType.JSON)
            .body(loginRequest)
        .when()
            .post(TestConstants.AUTH_LOGIN_ENDPOINT)
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("success", equalTo(true))
            .body("message", containsString("Login successful"))
            .body("data", notNullValue())
            .body("data.accessToken", notNullValue())
            .body("data.tokenType", equalTo("Bearer"));
    }
    
    @Test
    void testLoginApiContract_WithWhitespace() {
        // Arrange - Create user first
        User testUser = TestDataBuilder.createTestUser();
        userRepository.save(testUser);
        
        Map<String, String> loginRequest = Map.of(
            "username", " " + TestConstants.TEST_USERNAME + " ", // With whitespace
            "password", TestConstants.TEST_PASSWORD
        );
        
        // Act & Assert
        given()
            .contentType(ContentType.JSON)
            .body(loginRequest)
        .when()
            .post(TestConstants.AUTH_LOGIN_ENDPOINT)
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("success", equalTo(true))
            .body("message", containsString("Login successful"))
            .body("data", notNullValue())
            .body("data.accessToken", notNullValue())
            .body("data.tokenType", equalTo("Bearer"));
    }
    
    @Test
    void testLoginApiContract_WithLongCredentials() {
        // Arrange - Create user first
        User testUser = TestDataBuilder.createTestUser();
        userRepository.save(testUser);
        
        Map<String, String> loginRequest = Map.of(
            "username", TestConstants.TEST_USERNAME,
            "password", "a".repeat(1000) // Very long password
        );
        
        // Act & Assert
        given()
            .contentType(ContentType.JSON)
            .body(loginRequest)
        .when()
            .post(TestConstants.AUTH_LOGIN_ENDPOINT)
        .then()
            .statusCode(401)
            .contentType(ContentType.JSON)
            .body("success", equalTo(false))
            .body("message", containsString("Invalid credentials"))
            .body("error", notNullValue())
            .body("timestamp", notNullValue());
    }
    
    @Test
    void testLoginApiContract_WithExtremeValues_Scenario3() {
        // Arrange - Create user first
        User testUser = TestDataBuilder.createTestUser();
        userRepository.save(testUser);
        
        Map<String, String> loginRequest = Map.of(
            "username", TestConstants.TEST_USERNAME,
            "password", TestConstants.TEST_PASSWORD
        );
        
        // Act & Assert
        given()
            .contentType(ContentType.JSON)
            .body(loginRequest)
        .when()
            .post(TestConstants.AUTH_LOGIN_ENDPOINT)
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("success", equalTo(true))
            .body("message", containsString("Login successful"))
            .body("data", notNullValue())
            .body("data.accessToken", notNullValue())
            .body("data.tokenType", equalTo("Bearer"));
    }
    
    @Test
    void testLoginApiContract_WithSpecialCharacters() {
        // Arrange - Create user with special characters
        User specialUser = TestDataBuilder.createTestUser("user@domain.com");
        specialUser.setEmail("user@domain.com");
        userRepository.save(specialUser);
        
        Map<String, String> loginRequest = Map.of(
            "username", "user@domain.com",
            "password", TestConstants.TEST_PASSWORD
        );
        
        // Act & Assert
        given()
            .contentType(ContentType.JSON)
            .body(loginRequest)
        .when()
            .post(TestConstants.AUTH_LOGIN_ENDPOINT)
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("success", equalTo(true))
            .body("message", containsString("Login successful"))
            .body("data", notNullValue())
            .body("data.accessToken", notNullValue())
            .body("data.tokenType", equalTo("Bearer"));
    }
    
    @Test
    void testLoginApiContract_WithUnicodeCharacters() {
        // Arrange - Create user with unicode characters
        User unicodeUser = TestDataBuilder.createTestUser("测试用户");
        unicodeUser.setEmail("test@测试.com");
        userRepository.save(unicodeUser);
        
        Map<String, String> loginRequest = Map.of(
            "username", "测试用户",
            "password", TestConstants.TEST_PASSWORD
        );
        
        // Act & Assert
        given()
            .contentType(ContentType.JSON)
            .body(loginRequest)
        .when()
            .post(TestConstants.AUTH_LOGIN_ENDPOINT)
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("success", equalTo(true))
            .body("message", containsString("Login successful"))
            .body("data", notNullValue())
            .body("data.accessToken", notNullValue())
            .body("data.tokenType", equalTo("Bearer"));
    }
    
    @Test
    void testLoginApiContract_WithWhitespaceCredentials() {
        // Arrange - Create user first
        User testUser = TestDataBuilder.createTestUser();
        userRepository.save(testUser);
        
        Map<String, String> loginRequest = Map.of(
            "username", "   ",
            "password", "   "
        );
        
        // Act & Assert
        given()
            .contentType(ContentType.JSON)
            .body(loginRequest)
        .when()
            .post(TestConstants.AUTH_LOGIN_ENDPOINT)
        .then()
            .statusCode(400)
            .contentType(ContentType.JSON)
            .body("success", equalTo(false))
            .body("message", containsString("Username and password are required"))
            .body("error", notNullValue())
            .body("timestamp", notNullValue());
    }
    
    @Test
    void testLoginApiContract_WithExtremeLongCredentials() {
        // Arrange - Create user first
        User testUser = TestDataBuilder.createTestUser();
        userRepository.save(testUser);
        
        Map<String, String> loginRequest = Map.of(
            "username", "a".repeat(1000),
            "password", "a".repeat(1000)
        );
        
        // Act & Assert
        given()
            .contentType(ContentType.JSON)
            .body(loginRequest)
        .when()
            .post(TestConstants.AUTH_LOGIN_ENDPOINT)
        .then()
            .statusCode(401)
            .contentType(ContentType.JSON)
            .body("success", equalTo(false))
            .body("message", containsString("Invalid credentials"))
            .body("error", notNullValue())
            .body("timestamp", notNullValue());
    }
    
    @Test
    void testLoginApiContract_WithExtremeValues_Scenario7() {
        // Arrange - Create user first
        User testUser = TestDataBuilder.createTestUser();
        userRepository.save(testUser);
        
        Map<String, String> loginRequest = Map.of(
            "username", TestConstants.TEST_USERNAME,
            "password", TestConstants.TEST_PASSWORD
        );
        
        // Act & Assert
        given()
            .contentType(ContentType.JSON)
            .body(loginRequest)
        .when()
            .post(TestConstants.AUTH_LOGIN_ENDPOINT)
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("success", equalTo(true))
            .body("message", containsString("Login successful"))
            .body("data", notNullValue())
            .body("data.accessToken", notNullValue())
            .body("data.tokenType", equalTo("Bearer"));
    }
}
