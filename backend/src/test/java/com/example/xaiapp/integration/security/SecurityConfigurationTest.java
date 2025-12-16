package com.example.xaiapp.integration.security;

import com.example.xaiapp.util.TestConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for SecurityConfiguration
 * Tests security rules and CORS configuration
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Transactional
class SecurityConfigurationTest {
    
    @LocalServerPort
    private int port;
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    private String baseUrl;
    
    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port;
    }
    
    @Test
    void testPublicEndpointsAccessible() {
        // Test registration endpoint
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        
        String registrationRequest = String.format(
            "{\"username\":\"%s\",\"email\":\"%s\",\"password\":\"%s\"}",
            TestConstants.TEST_USERNAME,
            TestConstants.TEST_EMAIL,
            TestConstants.TEST_PASSWORD
        );
        
        HttpEntity<String> request = new HttpEntity<>(registrationRequest, headers);
        ResponseEntity<String> response = restTemplate.exchange(
            baseUrl + TestConstants.AUTH_REGISTER_ENDPOINT,
            HttpMethod.POST,
            request,
            String.class
        );
        
        // Assert
        assertEquals(201, response.getStatusCode().value());
        assertTrue(response.getBody().contains("registered successfully"));
    }
    
    @Test
    void testPublicEndpointsAccessible_Login() {
        // Test login endpoint
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
        
        // Assert
        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().contains("accessToken"));
    }
    
    @Test
    void testProtectedEndpointsRequireAuth() {
        // Test protected endpoint without authentication
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> request = new HttpEntity<>(headers);
        
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
    void testProtectedEndpointsRequireAuth_Models() {
        // Test protected endpoint without authentication
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> request = new HttpEntity<>(headers);
        
        ResponseEntity<String> response = restTemplate.exchange(
            baseUrl + TestConstants.MODELS_LIST_ENDPOINT,
            HttpMethod.GET,
            request,
            String.class
        );
        
        // Assert
        assertEquals(401, response.getStatusCode().value());
        assertTrue(response.getBody().contains("Unauthorized"));
    }
    
    @Test
    void testProtectedEndpointsRequireAuth_Upload() {
        // Test protected endpoint without authentication
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> request = new HttpEntity<>(headers);
        
        ResponseEntity<String> response = restTemplate.exchange(
            baseUrl + TestConstants.DATASETS_UPLOAD_ENDPOINT,
            HttpMethod.POST,
            request,
            String.class
        );
        
        // Assert
        assertEquals(401, response.getStatusCode().value());
        assertTrue(response.getBody().contains("Unauthorized"));
    }
    
    @Test
    void testProtectedEndpointsRequireAuth_Train() {
        // Test protected endpoint without authentication
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> request = new HttpEntity<>(headers);
        
        ResponseEntity<String> response = restTemplate.exchange(
            baseUrl + TestConstants.MODELS_TRAIN_ENDPOINT,
            HttpMethod.POST,
            request,
            String.class
        );
        
        // Assert
        assertEquals(401, response.getStatusCode().value());
        assertTrue(response.getBody().contains("Unauthorized"));
    }
    
    @Test
    void testProtectedEndpointsRequireAuth_Predict() {
        // Test protected endpoint without authentication
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> request = new HttpEntity<>(headers);
        
        ResponseEntity<String> response = restTemplate.exchange(
            baseUrl + TestConstants.MODELS_PREDICT_ENDPOINT.replace("{id}", "1"),
            HttpMethod.POST,
            request,
            String.class
        );
        
        // Assert
        assertEquals(401, response.getStatusCode().value());
        assertTrue(response.getBody().contains("Unauthorized"));
    }
    
    @Test
    void testProtectedEndpointsRequireAuth_Explain() {
        // Test protected endpoint without authentication
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> request = new HttpEntity<>(headers);
        
        ResponseEntity<String> response = restTemplate.exchange(
            baseUrl + TestConstants.MODELS_EXPLAIN_ENDPOINT.replace("{id}", "1"),
            HttpMethod.POST,
            request,
            String.class
        );
        
        // Assert
        assertEquals(401, response.getStatusCode().value());
        assertTrue(response.getBody().contains("Unauthorized"));
    }
    
    @Test
    void testProtectedEndpointsRequireAuth_Delete() {
        // Test protected endpoint without authentication
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> request = new HttpEntity<>(headers);
        
        ResponseEntity<String> response = restTemplate.exchange(
            baseUrl + TestConstants.DATASETS_LIST_ENDPOINT + "/1",
            HttpMethod.DELETE,
            request,
            String.class
        );
        
        // Assert
        assertEquals(401, response.getStatusCode().value());
        assertTrue(response.getBody().contains("Unauthorized"));
    }
    
    @Test
    void testProtectedEndpointsRequireAuth_DeleteModel() {
        // Test protected endpoint without authentication
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> request = new HttpEntity<>(headers);
        
        ResponseEntity<String> response = restTemplate.exchange(
            baseUrl + TestConstants.MODELS_LIST_ENDPOINT + "/1",
            HttpMethod.DELETE,
            request,
            String.class
        );
        
        // Assert
        assertEquals(401, response.getStatusCode().value());
        assertTrue(response.getBody().contains("Unauthorized"));
    }
    
    @Test
    void testCorsConfiguration_AllowedOrigins() {
        // Test CORS with allowed origin
        HttpHeaders headers = new HttpHeaders();
        headers.set("Origin", "http://localhost:3000");
        headers.set("Access-Control-Request-Method", "GET");
        headers.set("Access-Control-Request-Headers", "Authorization");
        
        HttpEntity<String> request = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(
            baseUrl + TestConstants.DATASETS_LIST_ENDPOINT,
            HttpMethod.OPTIONS,
            request,
            String.class
        );
        
        // Assert
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getHeaders().get("Access-Control-Allow-Origin"));
    }
    
    @Test
    void testCorsConfiguration_AllowedMethods() {
        // Test CORS with allowed methods
        HttpHeaders headers = new HttpHeaders();
        headers.set("Origin", "http://localhost:3000");
        headers.set("Access-Control-Request-Method", "POST");
        headers.set("Access-Control-Request-Headers", "Authorization");
        
        HttpEntity<String> request = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(
            baseUrl + TestConstants.DATASETS_UPLOAD_ENDPOINT,
            HttpMethod.OPTIONS,
            request,
            String.class
        );
        
        // Assert
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getHeaders().get("Access-Control-Allow-Methods"));
    }
    
    @Test
    void testCorsConfiguration_AllowedHeaders() {
        // Test CORS with allowed headers
        HttpHeaders headers = new HttpHeaders();
        headers.set("Origin", "http://localhost:3000");
        headers.set("Access-Control-Request-Method", "GET");
        headers.set("Access-Control-Request-Headers", "Authorization, Content-Type");
        
        HttpEntity<String> request = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(
            baseUrl + TestConstants.DATASETS_LIST_ENDPOINT,
            HttpMethod.OPTIONS,
            request,
            String.class
        );
        
        // Assert
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getHeaders().get("Access-Control-Allow-Headers"));
    }
    
    @Test
    void testCorsConfiguration_DisallowedOrigin() {
        // Test CORS with disallowed origin
        HttpHeaders headers = new HttpHeaders();
        headers.set("Origin", "http://malicious-site.com");
        headers.set("Access-Control-Request-Method", "GET");
        headers.set("Access-Control-Request-Headers", "Authorization");
        
        HttpEntity<String> request = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(
            baseUrl + TestConstants.DATASETS_LIST_ENDPOINT,
            HttpMethod.OPTIONS,
            request,
            String.class
        );
        
        // Assert
        assertEquals(200, response.getStatusCode().value());
        // CORS should still work but with restricted access
    }
    
    @Test
    void testCorsConfiguration_DisallowedMethod() {
        // Test CORS with disallowed method
        HttpHeaders headers = new HttpHeaders();
        headers.set("Origin", "http://localhost:3000");
        headers.set("Access-Control-Request-Method", "PUT");
        headers.set("Access-Control-Request-Headers", "Authorization");
        
        HttpEntity<String> request = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(
            baseUrl + TestConstants.DATASETS_LIST_ENDPOINT,
            HttpMethod.OPTIONS,
            request,
            String.class
        );
        
        // Assert
        assertEquals(200, response.getStatusCode().value());
        // CORS should still work but with restricted access
    }
    
    @Test
    void testCorsConfiguration_DisallowedHeaders() {
        // Test CORS with disallowed headers
        HttpHeaders headers = new HttpHeaders();
        headers.set("Origin", "http://localhost:3000");
        headers.set("Access-Control-Request-Method", "GET");
        headers.set("Access-Control-Request-Headers", "X-Custom-Header");
        
        HttpEntity<String> request = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(
            baseUrl + TestConstants.DATASETS_LIST_ENDPOINT,
            HttpMethod.OPTIONS,
            request,
            String.class
        );
        
        // Assert
        assertEquals(200, response.getStatusCode().value());
        // CORS should still work but with restricted access
    }
    
    @Test
    void testCorsConfiguration_NoOrigin() {
        // Test CORS without origin header
        HttpHeaders headers = new HttpHeaders();
        headers.set("Access-Control-Request-Method", "GET");
        headers.set("Access-Control-Request-Headers", "Authorization");
        
        HttpEntity<String> request = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(
            baseUrl + TestConstants.DATASETS_LIST_ENDPOINT,
            HttpMethod.OPTIONS,
            request,
            String.class
        );
        
        // Assert
        assertEquals(200, response.getStatusCode().value());
        // CORS should still work but with restricted access
    }
    
    @Test
    void testCorsConfiguration_NoMethod() {
        // Test CORS without method header
        HttpHeaders headers = new HttpHeaders();
        headers.set("Origin", "http://localhost:3000");
        headers.set("Access-Control-Request-Headers", "Authorization");
        
        HttpEntity<String> request = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(
            baseUrl + TestConstants.DATASETS_LIST_ENDPOINT,
            HttpMethod.OPTIONS,
            request,
            String.class
        );
        
        // Assert
        assertEquals(200, response.getStatusCode().value());
        // CORS should still work but with restricted access
    }
    
    @Test
    void testCorsConfiguration_NoHeaders() {
        // Test CORS without headers
        HttpHeaders headers = new HttpHeaders();
        headers.set("Origin", "http://localhost:3000");
        headers.set("Access-Control-Request-Method", "GET");
        
        HttpEntity<String> request = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(
            baseUrl + TestConstants.DATASETS_LIST_ENDPOINT,
            HttpMethod.OPTIONS,
            request,
            String.class
        );
        
        // Assert
        assertEquals(200, response.getStatusCode().value());
        // CORS should still work but with restricted access
    }
    
    @Test
    void testCorsConfiguration_EmptyOrigin() {
        // Test CORS with empty origin
        HttpHeaders headers = new HttpHeaders();
        headers.set("Origin", "");
        headers.set("Access-Control-Request-Method", "GET");
        headers.set("Access-Control-Request-Headers", "Authorization");
        
        HttpEntity<String> request = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(
            baseUrl + TestConstants.DATASETS_LIST_ENDPOINT,
            HttpMethod.OPTIONS,
            request,
            String.class
        );
        
        // Assert
        assertEquals(200, response.getStatusCode().value());
        // CORS should still work but with restricted access
    }
    
    @Test
    void testCorsConfiguration_EmptyMethod() {
        // Test CORS with empty method
        HttpHeaders headers = new HttpHeaders();
        headers.set("Origin", "http://localhost:3000");
        headers.set("Access-Control-Request-Method", "");
        headers.set("Access-Control-Request-Headers", "Authorization");
        
        HttpEntity<String> request = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(
            baseUrl + TestConstants.DATASETS_LIST_ENDPOINT,
            HttpMethod.OPTIONS,
            request,
            String.class
        );
        
        // Assert
        assertEquals(200, response.getStatusCode().value());
        // CORS should still work but with restricted access
    }
    
    @Test
    void testCorsConfiguration_EmptyHeaders() {
        // Test CORS with empty headers
        HttpHeaders headers = new HttpHeaders();
        headers.set("Origin", "http://localhost:3000");
        headers.set("Access-Control-Request-Method", "GET");
        headers.set("Access-Control-Request-Headers", "");
        
        HttpEntity<String> request = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(
            baseUrl + TestConstants.DATASETS_LIST_ENDPOINT,
            HttpMethod.OPTIONS,
            request,
            String.class
        );
        
        // Assert
        assertEquals(200, response.getStatusCode().value());
        // CORS should still work but with restricted access
    }
    
    @Test
    void testCorsConfiguration_WhitespaceOrigin() {
        // Test CORS with whitespace origin
        HttpHeaders headers = new HttpHeaders();
        headers.set("Origin", "   ");
        headers.set("Access-Control-Request-Method", "GET");
        headers.set("Access-Control-Request-Headers", "Authorization");
        
        HttpEntity<String> request = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(
            baseUrl + TestConstants.DATASETS_LIST_ENDPOINT,
            HttpMethod.OPTIONS,
            request,
            String.class
        );
        
        // Assert
        assertEquals(200, response.getStatusCode().value());
        // CORS should still work but with restricted access
    }
    
    @Test
    void testCorsConfiguration_WhitespaceMethod() {
        // Test CORS with whitespace method
        HttpHeaders headers = new HttpHeaders();
        headers.set("Origin", "http://localhost:3000");
        headers.set("Access-Control-Request-Method", "   ");
        headers.set("Access-Control-Request-Headers", "Authorization");
        
        HttpEntity<String> request = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(
            baseUrl + TestConstants.DATASETS_LIST_ENDPOINT,
            HttpMethod.OPTIONS,
            request,
            String.class
        );
        
        // Assert
        assertEquals(200, response.getStatusCode().value());
        // CORS should still work but with restricted access
    }
    
    @Test
    void testCorsConfiguration_WhitespaceHeaders() {
        // Test CORS with whitespace headers
        HttpHeaders headers = new HttpHeaders();
        headers.set("Origin", "http://localhost:3000");
        headers.set("Access-Control-Request-Method", "GET");
        headers.set("Access-Control-Request-Headers", "   ");
        
        HttpEntity<String> request = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(
            baseUrl + TestConstants.DATASETS_LIST_ENDPOINT,
            HttpMethod.OPTIONS,
            request,
            String.class
        );
        
        // Assert
        assertEquals(200, response.getStatusCode().value());
        // CORS should still work but with restricted access
    }
    
    @Test
    void testCorsConfiguration_ExtremeLongOrigin() {
        // Test CORS with extreme long origin
        HttpHeaders headers = new HttpHeaders();
        headers.set("Origin", "http://" + "a".repeat(1000) + ".com");
        headers.set("Access-Control-Request-Method", "GET");
        headers.set("Access-Control-Request-Headers", "Authorization");
        
        HttpEntity<String> request = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(
            baseUrl + TestConstants.DATASETS_LIST_ENDPOINT,
            HttpMethod.OPTIONS,
            request,
            String.class
        );
        
        // Assert
        assertEquals(200, response.getStatusCode().value());
        // CORS should still work but with restricted access
    }
    
    @Test
    void testCorsConfiguration_ExtremeLongMethod() {
        // Test CORS with extreme long method
        HttpHeaders headers = new HttpHeaders();
        headers.set("Origin", "http://localhost:3000");
        headers.set("Access-Control-Request-Method", "a".repeat(1000));
        headers.set("Access-Control-Request-Headers", "Authorization");
        
        HttpEntity<String> request = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(
            baseUrl + TestConstants.DATASETS_LIST_ENDPOINT,
            HttpMethod.OPTIONS,
            request,
            String.class
        );
        
        // Assert
        assertEquals(200, response.getStatusCode().value());
        // CORS should still work but with restricted access
    }
    
    @Test
    void testCorsConfiguration_ExtremeLongHeaders() {
        // Test CORS with extreme long headers
        HttpHeaders headers = new HttpHeaders();
        headers.set("Origin", "http://localhost:3000");
        headers.set("Access-Control-Request-Method", "GET");
        headers.set("Access-Control-Request-Headers", "a".repeat(1000));
        
        HttpEntity<String> request = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(
            baseUrl + TestConstants.DATASETS_LIST_ENDPOINT,
            HttpMethod.OPTIONS,
            request,
            String.class
        );
        
        // Assert
        assertEquals(200, response.getStatusCode().value());
        // CORS should still work but with restricted access
    }
}
