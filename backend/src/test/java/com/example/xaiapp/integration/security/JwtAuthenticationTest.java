package com.example.xaiapp.integration.security;

import com.example.xaiapp.entity.User;
import com.example.xaiapp.repository.UserRepository;
import com.example.xaiapp.util.TestConstants;
import com.example.xaiapp.util.TestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for JWT authentication
 * Tests JWT filter chain and token validation
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Transactional
class JwtAuthenticationTest {
    
    @LocalServerPort
    private int port;
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Autowired
    private UserRepository userRepository;
    
    private String baseUrl;
    private String validToken;
    private String expiredToken;
    private String malformedToken;
    
    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port;
        userRepository.deleteAll(); // Clean up before each test
        
        // Create test user and get valid token
        User testUser = TestDataBuilder.createTestUser();
        userRepository.save(testUser);
        validToken = getValidToken();
        
        // Create test tokens
        expiredToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0dXNlciIsImlhdCI6MTUxNjIzOTAyMiwiZXhwIjoxNTE2MjM5MDIyfQ.invalid";
        malformedToken = "invalid.token.here";
    }
    
    private String getValidToken() {
        // Login to get valid JWT token
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        
        String loginRequest = String.format(
            "{\"username\":\"%s\",\"password\":\"%s\"}",
            TestConstants.TEST_USERNAME,
            TestConstants.TEST_PASSWORD
        );
        
        HttpEntity<String> request = new HttpEntity<>(loginRequest, headers);
        ResponseEntity<String> response = restTemplate.exchange(
            baseUrl + TestConstants.AUTH_LOGIN_ENDPOINT,
            HttpMethod.POST,
            request,
            String.class
        );
        
        // Extract token from response
        String responseBody = response.getBody();
        if (responseBody != null && responseBody.contains("accessToken")) {
            return responseBody.substring(
                responseBody.indexOf("\"accessToken\":\"") + 16,
                responseBody.indexOf("\"", responseBody.indexOf("\"accessToken\":\"") + 16)
            );
        }
        return null;
    }
    
    @Test
    void testValidJwtPasses() {
        // Arrange
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + validToken);
        HttpEntity<String> request = new HttpEntity<>(headers);
        
        // Act
        ResponseEntity<String> response = restTemplate.exchange(
            baseUrl + TestConstants.DATASETS_LIST_ENDPOINT,
            HttpMethod.GET,
            request,
            String.class
        );
        
        // Assert
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
    }
    
    @Test
    void testExpiredJwtRejected() {
        // Arrange
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + expiredToken);
        HttpEntity<String> request = new HttpEntity<>(headers);
        
        // Act
        ResponseEntity<String> response = restTemplate.exchange(
            baseUrl + TestConstants.DATASETS_LIST_ENDPOINT,
            HttpMethod.GET,
            request,
            String.class
        );
        
        // Assert
        assertEquals(401, response.getStatusCode().value());
        assertTrue(response.getBody().contains("JWT expired"));
    }
    
    @Test
    void testMalformedJwtRejected() {
        // Arrange
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + malformedToken);
        HttpEntity<String> request = new HttpEntity<>(headers);
        
        // Act
        ResponseEntity<String> response = restTemplate.exchange(
            baseUrl + TestConstants.DATASETS_LIST_ENDPOINT,
            HttpMethod.GET,
            request,
            String.class
        );
        
        // Assert
        assertEquals(401, response.getStatusCode().value());
        assertTrue(response.getBody().contains("Invalid JWT"));
    }
    
    @Test
    void testMissingJwtRedirected() {
        // Arrange
        HttpHeaders headers = new HttpHeaders();
        // No Authorization header
        HttpEntity<String> request = new HttpEntity<>(headers);
        
        // Act
        ResponseEntity<String> response = restTemplate.exchange(
            baseUrl + TestConstants.DATASETS_LIST_ENDPOINT,
            HttpMethod.GET,
            request,
            String.class
        );
        
        // Assert
        assertEquals(401, response.getStatusCode().value());
        assertTrue(response.getBody().contains("Unauthorized"));
    }
    
    @Test
    void testInvalidJwtFormat() {
        // Arrange
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "InvalidFormat " + validToken);
        HttpEntity<String> request = new HttpEntity<>(headers);
        
        // Act
        ResponseEntity<String> response = restTemplate.exchange(
            baseUrl + TestConstants.DATASETS_LIST_ENDPOINT,
            HttpMethod.GET,
            request,
            String.class
        );
        
        // Assert
        assertEquals(401, response.getStatusCode().value());
        assertTrue(response.getBody().contains("Invalid JWT format"));
    }
    
    @Test
    void testJwtWithoutBearerPrefix() {
        // Arrange
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", validToken); // Missing "Bearer " prefix
        HttpEntity<String> request = new HttpEntity<>(headers);
        
        // Act
        ResponseEntity<String> response = restTemplate.exchange(
            baseUrl + TestConstants.DATASETS_LIST_ENDPOINT,
            HttpMethod.GET,
            request,
            String.class
        );
        
        // Assert
        assertEquals(401, response.getStatusCode().value());
        assertTrue(response.getBody().contains("Invalid JWT format"));
    }
    
    @Test
    void testJwtWithInvalidSignature() {
        // Arrange
        String invalidSignatureToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0dXNlciIsImlhdCI6MTUxNjIzOTAyMiwiZXhwIjoxNTE2MjM5MDIyfQ.invalid-signature";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + invalidSignatureToken);
        HttpEntity<String> request = new HttpEntity<>(headers);
        
        // Act
        ResponseEntity<String> response = restTemplate.exchange(
            baseUrl + TestConstants.DATASETS_LIST_ENDPOINT,
            HttpMethod.GET,
            request,
            String.class
        );
        
        // Assert
        assertEquals(401, response.getStatusCode().value());
        assertTrue(response.getBody().contains("Invalid JWT signature"));
    }
    
    @Test
    void testJwtWithInvalidAlgorithm() {
        // Arrange
        String invalidAlgorithmToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0dXNlciIsImlhdCI6MTUxNjIzOTAyMiwiZXhwIjoxNTE2MjM5MDIyfQ.invalid-algorithm";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + invalidAlgorithmToken);
        HttpEntity<String> request = new HttpEntity<>(headers);
        
        // Act
        ResponseEntity<String> response = restTemplate.exchange(
            baseUrl + TestConstants.DATASETS_LIST_ENDPOINT,
            HttpMethod.GET,
            request,
            String.class
        );
        
        // Assert
        assertEquals(401, response.getStatusCode().value());
        assertTrue(response.getBody().contains("Invalid JWT algorithm"));
    }
    
    @Test
    void testJwtWithMissingClaims() {
        // Arrange
        String missingClaimsToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpYXQiOjE1MTYyMzkwMjJ9.missing-claims";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + missingClaimsToken);
        HttpEntity<String> request = new HttpEntity<>(headers);
        
        // Act
        ResponseEntity<String> response = restTemplate.exchange(
            baseUrl + TestConstants.DATASETS_LIST_ENDPOINT,
            HttpMethod.GET,
            request,
            String.class
        );
        
        // Assert
        assertEquals(401, response.getStatusCode().value());
        assertTrue(response.getBody().contains("Invalid JWT claims"));
    }
    
    @Test
    void testJwtWithInvalidClaims() {
        // Arrange
        String invalidClaimsToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIiLCJpYXQiOjE1MTYyMzkwMjIsImV4cCI6MTUxNjIzOTAyMn0.invalid-claims";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + invalidClaimsToken);
        HttpEntity<String> request = new HttpEntity<>(headers);
        
        // Act
        ResponseEntity<String> response = restTemplate.exchange(
            baseUrl + TestConstants.DATASETS_LIST_ENDPOINT,
            HttpMethod.GET,
            request,
            String.class
        );
        
        // Assert
        assertEquals(401, response.getStatusCode().value());
        assertTrue(response.getBody().contains("Invalid JWT claims"));
    }
    
    @Test
    void testJwtWithExpiredClaims() {
        // Arrange
        String expiredClaimsToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0dXNlciIsImlhdCI6MTUxNjIzOTAyMiwiZXhwIjoxNTE2MjM5MDIyfQ.expired-claims";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + expiredClaimsToken);
        HttpEntity<String> request = new HttpEntity<>(headers);
        
        // Act
        ResponseEntity<String> response = restTemplate.exchange(
            baseUrl + TestConstants.DATASETS_LIST_ENDPOINT,
            HttpMethod.GET,
            request,
            String.class
        );
        
        // Assert
        assertEquals(401, response.getStatusCode().value());
        assertTrue(response.getBody().contains("JWT expired"));
    }
    
    @Test
    void testJwtWithInvalidIssuer() {
        // Arrange
        String invalidIssuerToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0dXNlciIsImlhdCI6MTUxNjIzOTAyMiwiZXhwIjoxNTE2MjM5MDIyLCJpc3MiOiJpbnZhbGlkLWlzc3VlciJ9.invalid-issuer";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + invalidIssuerToken);
        HttpEntity<String> request = new HttpEntity<>(headers);
        
        // Act
        ResponseEntity<String> response = restTemplate.exchange(
            baseUrl + TestConstants.DATASETS_LIST_ENDPOINT,
            HttpMethod.GET,
            request,
            String.class
        );
        
        // Assert
        assertEquals(401, response.getStatusCode().value());
        assertTrue(response.getBody().contains("Invalid JWT issuer"));
    }
    
    @Test
    void testJwtWithInvalidAudience() {
        // Arrange
        String invalidAudienceToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0dXNlciIsImlhdCI6MTUxNjIzOTAyMiwiZXhwIjoxNTE2MjM5MDIyLCJhdWQiOiJpbnZhbGlkLWF1ZGllbmNlIn0.invalid-audience";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + invalidAudienceToken);
        HttpEntity<String> request = new HttpEntity<>(headers);
        
        // Act
        ResponseEntity<String> response = restTemplate.exchange(
            baseUrl + TestConstants.DATASETS_LIST_ENDPOINT,
            HttpMethod.GET,
            request,
            String.class
        );
        
        // Assert
        assertEquals(401, response.getStatusCode().value());
        assertTrue(response.getBody().contains("Invalid JWT audience"));
    }
    
    @Test
    void testJwtWithInvalidNotBefore_Scenario2() {
        // Arrange
        String invalidNotBeforeToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0dXNlciIsImlhdCI6MTUxNjIzOTAyMiwiZXhwIjoxNTE2MjM5MDIyLCJuYmYiOjE1MTYyMzkwMjJ9.invalid-not-before";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + invalidNotBeforeToken);
        HttpEntity<String> request = new HttpEntity<>(headers);
        
        // Act
        ResponseEntity<String> response = restTemplate.exchange(
            baseUrl + TestConstants.DATASETS_LIST_ENDPOINT,
            HttpMethod.GET,
            request,
            String.class
        );
        
        // Assert
        assertEquals(401, response.getStatusCode().value());
        assertTrue(response.getBody().contains("JWT not yet valid"));
    }
    
    @Test
    void testJwtWithInvalidIssuedAt_Scenario2() {
        // Arrange
        String invalidIssuedAtToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0dXNlciIsImlhdCI6MTUxNjIzOTAyMiwiZXhwIjoxNTE2MjM5MDIyLCJpYXQiOjE1MTYyMzkwMjJ9.invalid-issued-at";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + invalidIssuedAtToken);
        HttpEntity<String> request = new HttpEntity<>(headers);
        
        // Act
        ResponseEntity<String> response = restTemplate.exchange(
            baseUrl + TestConstants.DATASETS_LIST_ENDPOINT,
            HttpMethod.GET,
            request,
            String.class
        );
        
        // Assert
        assertEquals(401, response.getStatusCode().value());
        assertTrue(response.getBody().contains("Invalid JWT issued at"));
    }
    
    @Test
    void testJwtWithInvalidJwtId_Scenario2() {
        // Arrange
        String invalidJwtIdToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0dXNlciIsImlhdCI6MTUxNjIzOTAyMiwiZXhwIjoxNTE2MjM5MDIyLCJqdGkiOiJpbnZhbGlkLWp3dC1pZCJ9.invalid-jwt-id";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + invalidJwtIdToken);
        HttpEntity<String> request = new HttpEntity<>(headers);
        
        // Act
        ResponseEntity<String> response = restTemplate.exchange(
            baseUrl + TestConstants.DATASETS_LIST_ENDPOINT,
            HttpMethod.GET,
            request,
            String.class
        );
        
        // Assert
        assertEquals(401, response.getStatusCode().value());
        assertTrue(response.getBody().contains("Invalid JWT ID"));
    }
    
    @Test
    void testJwtWithInvalidSubject_Scenario5() {
        // Arrange
        String invalidSubjectToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIiLCJpYXQiOjE1MTYyMzkwMjIsImV4cCI6MTUxNjIzOTAyMn0.invalid-subject";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + invalidSubjectToken);
        HttpEntity<String> request = new HttpEntity<>(headers);
        
        // Act
        ResponseEntity<String> response = restTemplate.exchange(
            baseUrl + TestConstants.DATASETS_LIST_ENDPOINT,
            HttpMethod.GET,
            request,
            String.class
        );
        
        // Assert
        assertEquals(401, response.getStatusCode().value());
        assertTrue(response.getBody().contains("Invalid JWT subject"));
    }
    
    @Test
    void testJwtWithInvalidExpiration_Scenario3() {
        // Arrange
        String invalidExpirationToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0dXNlciIsImlhdCI6MTUxNjIzOTAyMiwiZXhwIjoxNTE2MjM5MDIyfQ.invalid-expiration";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + invalidExpirationToken);
        HttpEntity<String> request = new HttpEntity<>(headers);
        
        // Act
        ResponseEntity<String> response = restTemplate.exchange(
            baseUrl + TestConstants.DATASETS_LIST_ENDPOINT,
            HttpMethod.GET,
            request,
            String.class
        );
        
        // Assert
        assertEquals(401, response.getStatusCode().value());
        assertTrue(response.getBody().contains("JWT expired"));
    }
    
    @Test
    void testJwtWithInvalidNotBefore_Scenario5() {
        // Arrange
        String invalidNotBeforeToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0dXNlciIsImlhdCI6MTUxNjIzOTAyMiwiZXhwIjoxNTE2MjM5MDIyLCJuYmYiOjE1MTYyMzkwMjJ9.invalid-not-before";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + invalidNotBeforeToken);
        HttpEntity<String> request = new HttpEntity<>(headers);
        
        // Act
        ResponseEntity<String> response = restTemplate.exchange(
            baseUrl + TestConstants.DATASETS_LIST_ENDPOINT,
            HttpMethod.GET,
            request,
            String.class
        );
        
        // Assert
        assertEquals(401, response.getStatusCode().value());
        assertTrue(response.getBody().contains("JWT not yet valid"));
    }
    
    @Test
    void testJwtWithInvalidIssuedAt_Scenario5() {
        // Arrange
        String invalidIssuedAtToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0dXNlciIsImlhdCI6MTUxNjIzOTAyMiwiZXhwIjoxNTE2MjM5MDIyLCJpYXQiOjE1MTYyMzkwMjJ9.invalid-issued-at";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + invalidIssuedAtToken);
        HttpEntity<String> request = new HttpEntity<>(headers);
        
        // Act
        ResponseEntity<String> response = restTemplate.exchange(
            baseUrl + TestConstants.DATASETS_LIST_ENDPOINT,
            HttpMethod.GET,
            request,
            String.class
        );
        
        // Assert
        assertEquals(401, response.getStatusCode().value());
        assertTrue(response.getBody().contains("Invalid JWT issued at"));
    }
    
    @Test
    void testJwtWithInvalidJwtId_Scenario5() {
        // Arrange
        String invalidJwtIdToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0dXNlciIsImlhdCI6MTUxNjIzOTAyMiwiZXhwIjoxNTE2MjM5MDIyLCJqdGkiOiJpbnZhbGlkLWp3dC1pZCJ9.invalid-jwt-id";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + invalidJwtIdToken);
        HttpEntity<String> request = new HttpEntity<>(headers);
        
        // Act
        ResponseEntity<String> response = restTemplate.exchange(
            baseUrl + TestConstants.DATASETS_LIST_ENDPOINT,
            HttpMethod.GET,
            request,
            String.class
        );
        
        // Assert
        assertEquals(401, response.getStatusCode().value());
        assertTrue(response.getBody().contains("Invalid JWT ID"));
    }
    
    @Test
    void testJwtWithInvalidSubject_Scenario2() {
        // Arrange
        String invalidSubjectToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIiLCJpYXQiOjE1MTYyMzkwMjIsImV4cCI6MTUxNjIzOTAyMn0.invalid-subject";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + invalidSubjectToken);
        HttpEntity<String> request = new HttpEntity<>(headers);
        
        // Act
        ResponseEntity<String> response = restTemplate.exchange(
            baseUrl + TestConstants.DATASETS_LIST_ENDPOINT,
            HttpMethod.GET,
            request,
            String.class
        );
        
        // Assert
        assertEquals(401, response.getStatusCode().value());
        assertTrue(response.getBody().contains("Invalid JWT subject"));
    }
    
    @Test
    void testJwtWithInvalidExpiration_Scenario2() {
        // Arrange
        String invalidExpirationToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0dXNlciIsImlhdCI6MTUxNjIzOTAyMiwiZXhwIjoxNTE2MjM5MDIyfQ.invalid-expiration";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + invalidExpirationToken);
        HttpEntity<String> request = new HttpEntity<>(headers);
        
        // Act
        ResponseEntity<String> response = restTemplate.exchange(
            baseUrl + TestConstants.DATASETS_LIST_ENDPOINT,
            HttpMethod.GET,
            request,
            String.class
        );
        
        // Assert
        assertEquals(401, response.getStatusCode().value());
        assertTrue(response.getBody().contains("JWT expired"));
    }
}
