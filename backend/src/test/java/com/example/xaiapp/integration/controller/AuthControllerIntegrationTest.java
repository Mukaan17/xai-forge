package com.example.xaiapp.integration.controller;

import com.example.xaiapp.entity.User;
import com.example.xaiapp.repository.UserRepository;
import com.example.xaiapp.util.TestConstants;
import com.example.xaiapp.util.TestDataBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for AuthController
 * Tests full authentication flow with real database and HTTP requests
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Transactional
class AuthControllerIntegrationTest {
    
    @LocalServerPort
    private int port;
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private String baseUrl;
    
    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port;
        userRepository.deleteAll(); // Clean up before each test
    }
    
    @Test
    void testRegisterUser_Success() {
        // Arrange
        Map<String, String> registrationRequest = Map.of(
            "username", TestConstants.TEST_USERNAME,
            "email", TestConstants.TEST_EMAIL,
            "password", TestConstants.TEST_PASSWORD
        );
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        HttpEntity<Map<String, String>> request = new HttpEntity<>(registrationRequest, headers);
        
        // Act
        ResponseEntity<String> response = restTemplate.exchange(
            baseUrl + TestConstants.AUTH_REGISTER_ENDPOINT,
            HttpMethod.POST,
            request,
            String.class
        );
        
        // Assert
        assertEquals(201, response.getStatusCode().value());
        assertTrue(response.getBody().contains("registered successfully"));
        
        // Verify user was created in database
        User savedUser = userRepository.findByUsername(TestConstants.TEST_USERNAME).orElse(null);
        assertNotNull(savedUser);
        assertEquals(TestConstants.TEST_EMAIL, savedUser.getEmail());
        assertNotNull(savedUser.getPassword()); // Should be encrypted
        assertNotEquals(TestConstants.TEST_PASSWORD, savedUser.getPassword()); // Should not be plain text
    }
    
    @Test
    void testRegisterUser_DuplicateUsername() {
        // Arrange - Create existing user
        User existingUser = TestDataBuilder.createTestUser();
        userRepository.save(existingUser);
        
        Map<String, String> registrationRequest = Map.of(
            "username", TestConstants.TEST_USERNAME,
            "email", "different@example.com",
            "password", TestConstants.TEST_PASSWORD
        );
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        HttpEntity<Map<String, String>> request = new HttpEntity<>(registrationRequest, headers);
        
        // Act
        ResponseEntity<String> response = restTemplate.exchange(
            baseUrl + TestConstants.AUTH_REGISTER_ENDPOINT,
            HttpMethod.POST,
            request,
            String.class
        );
        
        // Assert
        assertEquals(400, response.getStatusCode().value());
        assertTrue(response.getBody().contains("Username already exists"));
    }
    
    @Test
    void testRegisterUser_DuplicateEmail() {
        // Arrange - Create existing user
        User existingUser = TestDataBuilder.createTestUser();
        userRepository.save(existingUser);
        
        Map<String, String> registrationRequest = Map.of(
            "username", "differentuser",
            "email", TestConstants.TEST_EMAIL,
            "password", TestConstants.TEST_PASSWORD
        );
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        HttpEntity<Map<String, String>> request = new HttpEntity<>(registrationRequest, headers);
        
        // Act
        ResponseEntity<String> response = restTemplate.exchange(
            baseUrl + TestConstants.AUTH_REGISTER_ENDPOINT,
            HttpMethod.POST,
            request,
            String.class
        );
        
        // Assert
        assertEquals(400, response.getStatusCode().value());
        assertTrue(response.getBody().contains("Email already exists"));
    }
    
    @Test
    void testRegisterUser_InvalidEmail() {
        // Arrange
        Map<String, String> registrationRequest = Map.of(
            "username", TestConstants.TEST_USERNAME,
            "email", "invalid-email",
            "password", TestConstants.TEST_PASSWORD
        );
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        HttpEntity<Map<String, String>> request = new HttpEntity<>(registrationRequest, headers);
        
        // Act
        ResponseEntity<String> response = restTemplate.exchange(
            baseUrl + TestConstants.AUTH_REGISTER_ENDPOINT,
            HttpMethod.POST,
            request,
            String.class
        );
        
        // Assert
        assertEquals(400, response.getStatusCode().value());
        assertTrue(response.getBody().contains("Invalid email format"));
    }
    
    @Test
    void testRegisterUser_WeakPassword() {
        // Arrange
        Map<String, String> registrationRequest = Map.of(
            "username", TestConstants.TEST_USERNAME,
            "email", TestConstants.TEST_EMAIL,
            "password", "123" // Weak password
        );
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        HttpEntity<Map<String, String>> request = new HttpEntity<>(registrationRequest, headers);
        
        // Act
        ResponseEntity<String> response = restTemplate.exchange(
            baseUrl + TestConstants.AUTH_REGISTER_ENDPOINT,
            HttpMethod.POST,
            request,
            String.class
        );
        
        // Assert
        assertEquals(400, response.getStatusCode().value());
        assertTrue(response.getBody().contains("Password must be at least 8 characters"));
    }
    
    @Test
    void testRegisterUser_MissingFields() {
        // Arrange
        Map<String, String> registrationRequest = Map.of(
            "username", TestConstants.TEST_USERNAME
            // Missing email and password
        );
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        HttpEntity<Map<String, String>> request = new HttpEntity<>(registrationRequest, headers);
        
        // Act
        ResponseEntity<String> response = restTemplate.exchange(
            baseUrl + TestConstants.AUTH_REGISTER_ENDPOINT,
            HttpMethod.POST,
            request,
            String.class
        );
        
        // Assert
        assertEquals(400, response.getStatusCode().value());
        assertTrue(response.getBody().contains("Required fields missing"));
    }
    
    @Test
    void testLoginUser_Success() {
        // Arrange - Create user first
        User testUser = TestDataBuilder.createTestUser();
        userRepository.save(testUser);
        
        Map<String, String> loginRequest = Map.of(
            "username", TestConstants.TEST_USERNAME,
            "password", TestConstants.TEST_PASSWORD
        );
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        HttpEntity<Map<String, String>> request = new HttpEntity<>(loginRequest, headers);
        
        // Act
        ResponseEntity<String> response = restTemplate.exchange(
            baseUrl + TestConstants.AUTH_LOGIN_ENDPOINT,
            HttpMethod.POST,
            request,
            String.class
        );
        
        // Assert
        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().contains("accessToken"));
        assertTrue(response.getBody().contains("Bearer"));
    }
    
    @Test
    void testLoginUser_InvalidCredentials() {
        // Arrange
        Map<String, String> loginRequest = Map.of(
            "username", TestConstants.TEST_USERNAME,
            "password", "wrongpassword"
        );
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        HttpEntity<Map<String, String>> request = new HttpEntity<>(loginRequest, headers);
        
        // Act
        ResponseEntity<String> response = restTemplate.exchange(
            baseUrl + TestConstants.AUTH_LOGIN_ENDPOINT,
            HttpMethod.POST,
            request,
            String.class
        );
        
        // Assert
        assertEquals(401, response.getStatusCode().value());
        assertTrue(response.getBody().contains("Invalid credentials"));
    }
    
    @Test
    void testLoginUser_NonExistentUser() {
        // Arrange
        Map<String, String> loginRequest = Map.of(
            "username", "nonexistent",
            "password", TestConstants.TEST_PASSWORD
        );
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        HttpEntity<Map<String, String>> request = new HttpEntity<>(loginRequest, headers);
        
        // Act
        ResponseEntity<String> response = restTemplate.exchange(
            baseUrl + TestConstants.AUTH_LOGIN_ENDPOINT,
            HttpMethod.POST,
            request,
            String.class
        );
        
        // Assert
        assertEquals(401, response.getStatusCode().value());
        assertTrue(response.getBody().contains("Invalid credentials"));
    }
    
    @Test
    void testLoginUser_EmptyCredentials() {
        // Arrange
        Map<String, String> loginRequest = Map.of(
            "username", "",
            "password", ""
        );
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        HttpEntity<Map<String, String>> request = new HttpEntity<>(loginRequest, headers);
        
        // Act
        ResponseEntity<String> response = restTemplate.exchange(
            baseUrl + TestConstants.AUTH_LOGIN_ENDPOINT,
            HttpMethod.POST,
            request,
            String.class
        );
        
        // Assert
        assertEquals(400, response.getStatusCode().value());
        assertTrue(response.getBody().contains("Username and password are required"));
    }
    
    @Test
    void testLoginUser_NullCredentials() {
        // Arrange
        Map<String, String> loginRequest = Map.of(
            "username", null,
            "password", null
        );
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        HttpEntity<Map<String, String>> request = new HttpEntity<>(loginRequest, headers);
        
        // Act
        ResponseEntity<String> response = restTemplate.exchange(
            baseUrl + TestConstants.AUTH_LOGIN_ENDPOINT,
            HttpMethod.POST,
            request,
            String.class
        );
        
        // Assert
        assertEquals(400, response.getStatusCode().value());
        assertTrue(response.getBody().contains("Username and password are required"));
    }
    
    @Test
    void testLoginUser_WithEmail() {
        // Arrange - Create user first
        User testUser = TestDataBuilder.createTestUser();
        userRepository.save(testUser);
        
        Map<String, String> loginRequest = Map.of(
            "username", TestConstants.TEST_EMAIL, // Login with email instead of username
            "password", TestConstants.TEST_PASSWORD
        );
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        HttpEntity<Map<String, String>> request = new HttpEntity<>(loginRequest, headers);
        
        // Act
        ResponseEntity<String> response = restTemplate.exchange(
            baseUrl + TestConstants.AUTH_LOGIN_ENDPOINT,
            HttpMethod.POST,
            request,
            String.class
        );
        
        // Assert
        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().contains("accessToken"));
    }
    
    @Test
    void testLoginUser_CaseInsensitiveUsername() {
        // Arrange - Create user first
        User testUser = TestDataBuilder.createTestUser();
        userRepository.save(testUser);
        
        Map<String, String> loginRequest = Map.of(
            "username", TestConstants.TEST_USERNAME.toUpperCase(), // Different case
            "password", TestConstants.TEST_PASSWORD
        );
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        HttpEntity<Map<String, String>> request = new HttpEntity<>(loginRequest, headers);
        
        // Act
        ResponseEntity<String> response = restTemplate.exchange(
            baseUrl + TestConstants.AUTH_LOGIN_ENDPOINT,
            HttpMethod.POST,
            request,
            String.class
        );
        
        // Assert
        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().contains("accessToken"));
    }
    
    @Test
    void testLoginUser_WithSpecialCharacters() {
        // Arrange - Create user with special characters
        User specialUser = TestDataBuilder.createTestUser("user@domain.com");
        specialUser.setEmail("user@domain.com");
        userRepository.save(specialUser);
        
        Map<String, String> loginRequest = Map.of(
            "username", "user@domain.com",
            "password", TestConstants.TEST_PASSWORD
        );
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        HttpEntity<Map<String, String>> request = new HttpEntity<>(loginRequest, headers);
        
        // Act
        ResponseEntity<String> response = restTemplate.exchange(
            baseUrl + TestConstants.AUTH_LOGIN_ENDPOINT,
            HttpMethod.POST,
            request,
            String.class
        );
        
        // Assert
        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().contains("accessToken"));
    }
    
    @Test
    void testLoginUser_WithUnicodeCharacters() {
        // Arrange - Create user with unicode characters
        User unicodeUser = TestDataBuilder.createTestUser("测试用户");
        unicodeUser.setEmail("test@测试.com");
        userRepository.save(unicodeUser);
        
        Map<String, String> loginRequest = Map.of(
            "username", "测试用户",
            "password", TestConstants.TEST_PASSWORD
        );
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        HttpEntity<Map<String, String>> request = new HttpEntity<>(loginRequest, headers);
        
        // Act
        ResponseEntity<String> response = restTemplate.exchange(
            baseUrl + TestConstants.AUTH_LOGIN_ENDPOINT,
            HttpMethod.POST,
            request,
            String.class
        );
        
        // Assert
        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().contains("accessToken"));
    }
    
    @Test
    void testLoginUser_WithWhitespace() {
        // Arrange - Create user first
        User testUser = TestDataBuilder.createTestUser();
        userRepository.save(testUser);
        
        Map<String, String> loginRequest = Map.of(
            "username", " " + TestConstants.TEST_USERNAME + " ", // With whitespace
            "password", TestConstants.TEST_PASSWORD
        );
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        HttpEntity<Map<String, String>> request = new HttpEntity<>(loginRequest, headers);
        
        // Act
        ResponseEntity<String> response = restTemplate.exchange(
            baseUrl + TestConstants.AUTH_LOGIN_ENDPOINT,
            HttpMethod.POST,
            request,
            String.class
        );
        
        // Assert
        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().contains("accessToken"));
    }
    
    @Test
    void testLoginUser_WithLongCredentials() {
        // Arrange - Create user first
        User testUser = TestDataBuilder.createTestUser();
        userRepository.save(testUser);
        
        Map<String, String> loginRequest = Map.of(
            "username", TestConstants.TEST_USERNAME,
            "password", "a".repeat(1000) // Very long password
        );
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        HttpEntity<Map<String, String>> request = new HttpEntity<>(loginRequest, headers);
        
        // Act
        ResponseEntity<String> response = restTemplate.exchange(
            baseUrl + TestConstants.AUTH_LOGIN_ENDPOINT,
            HttpMethod.POST,
            request,
            String.class
        );
        
        // Assert
        assertEquals(401, response.getStatusCode().value());
        assertTrue(response.getBody().contains("Invalid credentials"));
    }
    
    @Test
    void testLoginUser_WithExtremeValues() {
        // Arrange - Create user first
        User testUser = TestDataBuilder.createTestUser();
        userRepository.save(testUser);
        
        Map<String, String> loginRequest = Map.of(
            "username", TestConstants.TEST_USERNAME,
            "password", TestConstants.TEST_PASSWORD
        );
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        HttpEntity<Map<String, String>> request = new HttpEntity<>(loginRequest, headers);
        
        // Act
        ResponseEntity<String> response = restTemplate.exchange(
            baseUrl + TestConstants.AUTH_LOGIN_ENDPOINT,
            HttpMethod.POST,
            request,
            String.class
        );
        
        // Assert
        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().contains("accessToken"));
    }
}
