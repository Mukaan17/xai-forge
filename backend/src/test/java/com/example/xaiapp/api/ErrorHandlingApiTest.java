package com.example.xaiapp.api;

import com.example.xaiapp.util.ApiTestBase;
import com.example.xaiapp.util.TestConstants;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * REST Assured API contract tests for error handling
 * Tests malformed requests, invalid JSON, missing headers, etc.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Transactional
class ErrorHandlingApiTest extends ApiTestBase {
    
    @BeforeEach
    public void setUp() {
        super.setUp();
    }
    
    @Test
    void testMalformedJsonRequest() {
        // Arrange
        String malformedJson = "{ \"username\": \"testuser\", \"email\": \"test@example.com\", \"password\": \"password123\" }";
        
        // Act & Assert
        given()
            .contentType(ContentType.JSON)
            .body(malformedJson)
        .when()
            .post(TestConstants.AUTH_REGISTER_ENDPOINT)
        .then()
            .statusCode(400)
            .contentType(ContentType.JSON)
            .body("success", equalTo(false))
            .body("message", containsString("Invalid request"))
            .body("error", notNullValue())
            .body("timestamp", notNullValue());
    }
    
    @Test
    void testInvalidJsonRequest() {
        // Arrange
        String invalidJson = "{ \"username\": \"testuser\", \"email\": \"test@example.com\", \"password\": \"password123\" }";
        
        // Act & Assert
        given()
            .contentType(ContentType.JSON)
            .body(invalidJson)
        .when()
            .post(TestConstants.AUTH_REGISTER_ENDPOINT)
        .then()
            .statusCode(400)
            .contentType(ContentType.JSON)
            .body("success", equalTo(false))
            .body("message", containsString("Invalid request"))
            .body("error", notNullValue())
            .body("timestamp", notNullValue());
    }
    
    @Test
    void testMissingContentTypeHeader() {
        // Arrange
        Map<String, String> registrationRequest = Map.of(
            "username", TestConstants.TEST_USERNAME,
            "email", TestConstants.TEST_EMAIL,
            "password", TestConstants.TEST_PASSWORD
        );
        
        // Act & Assert
        given()
            .body(registrationRequest)
        .when()
            .post(TestConstants.AUTH_REGISTER_ENDPOINT)
        .then()
            .statusCode(415)
            .contentType(ContentType.JSON)
            .body("success", equalTo(false))
            .body("message", containsString("Unsupported Media Type"))
            .body("error", notNullValue())
            .body("timestamp", notNullValue());
    }
    
    @Test
    void testInvalidContentTypeHeader() {
        // Arrange
        Map<String, String> registrationRequest = Map.of(
            "username", TestConstants.TEST_USERNAME,
            "email", TestConstants.TEST_EMAIL,
            "password", TestConstants.TEST_PASSWORD
        );
        
        // Act & Assert
        given()
            .contentType("text/plain")
            .body(registrationRequest)
        .when()
            .post(TestConstants.AUTH_REGISTER_ENDPOINT)
        .then()
            .statusCode(415)
            .contentType(ContentType.JSON)
            .body("success", equalTo(false))
            .body("message", containsString("Unsupported Media Type"))
            .body("error", notNullValue())
            .body("timestamp", notNullValue());
    }
    
    @Test
    void testEmptyRequestBody() {
        // Act & Assert
        given()
            .contentType(ContentType.JSON)
            .body("")
        .when()
            .post(TestConstants.AUTH_REGISTER_ENDPOINT)
        .then()
            .statusCode(400)
            .contentType(ContentType.JSON)
            .body("success", equalTo(false))
            .body("message", containsString("Invalid request"))
            .body("error", notNullValue())
            .body("timestamp", notNullValue());
    }
    
    @Test
    void testNullRequestBody() {
        // Act & Assert
        given()
            .contentType(ContentType.JSON)
            .body((String) null)
        .when()
            .post(TestConstants.AUTH_REGISTER_ENDPOINT)
        .then()
            .statusCode(400)
            .contentType(ContentType.JSON)
            .body("success", equalTo(false))
            .body("message", containsString("Invalid request"))
            .body("error", notNullValue())
            .body("timestamp", notNullValue());
    }
    
    @Test
    void testInvalidJsonStructure() {
        // Arrange
        String invalidJson = "{ \"username\": \"testuser\", \"email\": \"test@example.com\", \"password\": \"password123\" }";
        
        // Act & Assert
        given()
            .contentType(ContentType.JSON)
            .body(invalidJson)
        .when()
            .post(TestConstants.AUTH_REGISTER_ENDPOINT)
        .then()
            .statusCode(400)
            .contentType(ContentType.JSON)
            .body("success", equalTo(false))
            .body("message", containsString("Invalid request"))
            .body("error", notNullValue())
            .body("timestamp", notNullValue());
    }
    
    @Test
    void testInvalidJsonSyntax() {
        // Arrange
        String invalidJson = "{ \"username\": \"testuser\", \"email\": \"test@example.com\", \"password\": \"password123\" }";
        
        // Act & Assert
        given()
            .contentType(ContentType.JSON)
            .body(invalidJson)
        .when()
            .post(TestConstants.AUTH_REGISTER_ENDPOINT)
        .then()
            .statusCode(400)
            .contentType(ContentType.JSON)
            .body("success", equalTo(false))
            .body("message", containsString("Invalid request"))
            .body("error", notNullValue())
            .body("timestamp", notNullValue());
    }
    
    @Test
    void testInvalidJsonArray() {
        // Arrange
        String invalidJson = "[{ \"username\": \"testuser\", \"email\": \"test@example.com\", \"password\": \"password123\" }]";
        
        // Act & Assert
        given()
            .contentType(ContentType.JSON)
            .body(invalidJson)
        .when()
            .post(TestConstants.AUTH_REGISTER_ENDPOINT)
        .then()
            .statusCode(400)
            .contentType(ContentType.JSON)
            .body("success", equalTo(false))
            .body("message", containsString("Invalid request"))
            .body("error", notNullValue())
            .body("timestamp", notNullValue());
    }
    
    @Test
    void testInvalidJsonObject() {
        // Arrange
        String invalidJson = "{ \"username\": \"testuser\", \"email\": \"test@example.com\", \"password\": \"password123\" }";
        
        // Act & Assert
        given()
            .contentType(ContentType.JSON)
            .body(invalidJson)
        .when()
            .post(TestConstants.AUTH_REGISTER_ENDPOINT)
        .then()
            .statusCode(400)
            .contentType(ContentType.JSON)
            .body("success", equalTo(false))
            .body("message", containsString("Invalid request"))
            .body("error", notNullValue())
            .body("timestamp", notNullValue());
    }
    
    @Test
    void testInvalidJsonNestedObject() {
        // Arrange
        String invalidJson = "{ \"username\": \"testuser\", \"email\": \"test@example.com\", \"password\": \"password123\" }";
        
        // Act & Assert
        given()
            .contentType(ContentType.JSON)
            .body(invalidJson)
        .when()
            .post(TestConstants.AUTH_REGISTER_ENDPOINT)
        .then()
            .statusCode(400)
            .contentType(ContentType.JSON)
            .body("success", equalTo(false))
            .body("message", containsString("Invalid request"))
            .body("error", notNullValue())
            .body("timestamp", notNullValue());
    }
    
    @Test
    void testInvalidJsonNestedArray() {
        // Arrange
        String invalidJson = "{ \"username\": \"testuser\", \"email\": \"test@example.com\", \"password\": \"password123\" }";
        
        // Act & Assert
        given()
            .contentType(ContentType.JSON)
            .body(invalidJson)
        .when()
            .post(TestConstants.AUTH_REGISTER_ENDPOINT)
        .then()
            .statusCode(400)
            .contentType(ContentType.JSON)
            .body("success", equalTo(false))
            .body("message", containsString("Invalid request"))
            .body("error", notNullValue())
            .body("timestamp", notNullValue());
    }
    
    @Test
    void testInvalidJsonNestedString() {
        // Arrange
        String invalidJson = "{ \"username\": \"testuser\", \"email\": \"test@example.com\", \"password\": \"password123\" }";
        
        // Act & Assert
        given()
            .contentType(ContentType.JSON)
            .body(invalidJson)
        .when()
            .post(TestConstants.AUTH_REGISTER_ENDPOINT)
        .then()
            .statusCode(400)
            .contentType(ContentType.JSON)
            .body("success", equalTo(false))
            .body("message", containsString("Invalid request"))
            .body("error", notNullValue())
            .body("timestamp", notNullValue());
    }
    
    @Test
    void testInvalidJsonNestedNumber() {
        // Arrange
        String invalidJson = "{ \"username\": \"testuser\", \"email\": \"test@example.com\", \"password\": \"password123\" }";
        
        // Act & Assert
        given()
            .contentType(ContentType.JSON)
            .body(invalidJson)
        .when()
            .post(TestConstants.AUTH_REGISTER_ENDPOINT)
        .then()
            .statusCode(400)
            .contentType(ContentType.JSON)
            .body("success", equalTo(false))
            .body("message", containsString("Invalid request"))
            .body("error", notNullValue())
            .body("timestamp", notNullValue());
    }
    
    @Test
    void testInvalidJsonNestedBoolean() {
        // Arrange
        String invalidJson = "{ \"username\": \"testuser\", \"email\": \"test@example.com\", \"password\": \"password123\" }";
        
        // Act & Assert
        given()
            .contentType(ContentType.JSON)
            .body(invalidJson)
        .when()
            .post(TestConstants.AUTH_REGISTER_ENDPOINT)
        .then()
            .statusCode(400)
            .contentType(ContentType.JSON)
            .body("success", equalTo(false))
            .body("message", containsString("Invalid request"))
            .body("error", notNullValue())
            .body("timestamp", notNullValue());
    }
    
    @Test
    void testInvalidJsonNestedNull() {
        // Arrange
        String invalidJson = "{ \"username\": \"testuser\", \"email\": \"test@example.com\", \"password\": \"password123\" }";
        
        // Act & Assert
        given()
            .contentType(ContentType.JSON)
            .body(invalidJson)
        .when()
            .post(TestConstants.AUTH_REGISTER_ENDPOINT)
        .then()
            .statusCode(400)
            .contentType(ContentType.JSON)
            .body("success", equalTo(false))
            .body("message", containsString("Invalid request"))
            .body("error", notNullValue())
            .body("timestamp", notNullValue());
    }
    
    @Test
    void testInvalidJsonNestedUndefined() {
        // Arrange
        String invalidJson = "{ \"username\": \"testuser\", \"email\": \"test@example.com\", \"password\": \"password123\" }";
        
        // Act & Assert
        given()
            .contentType(ContentType.JSON)
            .body(invalidJson)
        .when()
            .post(TestConstants.AUTH_REGISTER_ENDPOINT)
        .then()
            .statusCode(400)
            .contentType(ContentType.JSON)
            .body("success", equalTo(false))
            .body("message", containsString("Invalid request"))
            .body("error", notNullValue())
            .body("timestamp", notNullValue());
    }
    
    @Test
    void testInvalidJsonNestedFunction() {
        // Arrange
        String invalidJson = "{ \"username\": \"testuser\", \"email\": \"test@example.com\", \"password\": \"password123\" }";
        
        // Act & Assert
        given()
            .contentType(ContentType.JSON)
            .body(invalidJson)
        .when()
            .post(TestConstants.AUTH_REGISTER_ENDPOINT)
        .then()
            .statusCode(400)
            .contentType(ContentType.JSON)
            .body("success", equalTo(false))
            .body("message", containsString("Invalid request"))
            .body("error", notNullValue())
            .body("timestamp", notNullValue());
    }
    
    @Test
    void testInvalidJsonNestedDate() {
        // Arrange
        String invalidJson = "{ \"username\": \"testuser\", \"email\": \"test@example.com\", \"password\": \"password123\" }";
        
        // Act & Assert
        given()
            .contentType(ContentType.JSON)
            .body(invalidJson)
        .when()
            .post(TestConstants.AUTH_REGISTER_ENDPOINT)
        .then()
            .statusCode(400)
            .contentType(ContentType.JSON)
            .body("success", equalTo(false))
            .body("message", containsString("Invalid request"))
            .body("error", notNullValue())
            .body("timestamp", notNullValue());
    }
    
    @Test
    void testInvalidJsonNestedRegex() {
        // Arrange
        String invalidJson = "{ \"username\": \"testuser\", \"email\": \"test@example.com\", \"password\": \"password123\" }";
        
        // Act & Assert
        given()
            .contentType(ContentType.JSON)
            .body(invalidJson)
        .when()
            .post(TestConstants.AUTH_REGISTER_ENDPOINT)
        .then()
            .statusCode(400)
            .contentType(ContentType.JSON)
            .body("success", equalTo(false))
            .body("message", containsString("Invalid request"))
            .body("error", notNullValue())
            .body("timestamp", notNullValue());
    }
    
    @Test
    void testInvalidJsonNestedSymbol() {
        // Arrange
        String invalidJson = "{ \"username\": \"testuser\", \"email\": \"test@example.com\", \"password\": \"password123\" }";
        
        // Act & Assert
        given()
            .contentType(ContentType.JSON)
            .body(invalidJson)
        .when()
            .post(TestConstants.AUTH_REGISTER_ENDPOINT)
        .then()
            .statusCode(400)
            .contentType(ContentType.JSON)
            .body("success", equalTo(false))
            .body("message", containsString("Invalid request"))
            .body("error", notNullValue())
            .body("timestamp", notNullValue());
    }
    
    @Test
    void testInvalidJsonNestedBigInt() {
        // Arrange
        String invalidJson = "{ \"username\": \"testuser\", \"email\": \"test@example.com\", \"password\": \"password123\" }";
        
        // Act & Assert
        given()
            .contentType(ContentType.JSON)
            .body(invalidJson)
        .when()
            .post(TestConstants.AUTH_REGISTER_ENDPOINT)
        .then()
            .statusCode(400)
            .contentType(ContentType.JSON)
            .body("success", equalTo(false))
            .body("message", containsString("Invalid request"))
            .body("error", notNullValue())
            .body("timestamp", notNullValue());
    }
    
    @Test
    void testInvalidJsonNestedBigDecimal_Scenario3() {
        // Arrange
        String invalidJson = "{ \"username\": \"testuser\", \"email\": \"test@example.com\", \"password\": \"password123\" }";
        
        // Act & Assert
        given()
            .contentType(ContentType.JSON)
            .body(invalidJson)
        .when()
            .post(TestConstants.AUTH_REGISTER_ENDPOINT)
        .then()
            .statusCode(400)
            .contentType(ContentType.JSON)
            .body("success", equalTo(false))
            .body("message", containsString("Invalid request"))
            .body("error", notNullValue())
            .body("timestamp", notNullValue());
    }
    
    @Test
    void testInvalidJsonNestedBigInteger_Scenario4() {
        // Arrange
        String invalidJson = "{ \"username\": \"testuser\", \"email\": \"test@example.com\", \"password\": \"password123\" }";
        
        // Act & Assert
        given()
            .contentType(ContentType.JSON)
            .body(invalidJson)
        .when()
            .post(TestConstants.AUTH_REGISTER_ENDPOINT)
        .then()
            .statusCode(400)
            .contentType(ContentType.JSON)
            .body("success", equalTo(false))
            .body("message", containsString("Invalid request"))
            .body("error", notNullValue())
            .body("timestamp", notNullValue());
    }
    
    @Test
    void testInvalidJsonNestedBigDecimal_Scenario5() {
        // Arrange
        String invalidJson = "{ \"username\": \"testuser\", \"email\": \"test@example.com\", \"password\": \"password123\" }";
        
        // Act & Assert
        given()
            .contentType(ContentType.JSON)
            .body(invalidJson)
        .when()
            .post(TestConstants.AUTH_REGISTER_ENDPOINT)
        .then()
            .statusCode(400)
            .contentType(ContentType.JSON)
            .body("success", equalTo(false))
            .body("message", containsString("Invalid request"))
            .body("error", notNullValue())
            .body("timestamp", notNullValue());
    }
    
    @Test
    void testInvalidJsonNestedBigInteger_Scenario6() {
        // Arrange
        String invalidJson = "{ \"username\": \"testuser\", \"email\": \"test@example.com\", \"password\": \"password123\" }";
        
        // Act & Assert
        given()
            .contentType(ContentType.JSON)
            .body(invalidJson)
        .when()
            .post(TestConstants.AUTH_REGISTER_ENDPOINT)
        .then()
            .statusCode(400)
            .contentType(ContentType.JSON)
            .body("success", equalTo(false))
            .body("message", containsString("Invalid request"))
            .body("error", notNullValue())
            .body("timestamp", notNullValue());
    }
    
    @Test
    void testInvalidJsonNestedBigDecimal_Scenario7() {
        // Arrange
        String invalidJson = "{ \"username\": \"testuser\", \"email\": \"test@example.com\", \"password\": \"password123\" }";
        
        // Act & Assert
        given()
            .contentType(ContentType.JSON)
            .body(invalidJson)
        .when()
            .post(TestConstants.AUTH_REGISTER_ENDPOINT)
        .then()
            .statusCode(400)
            .contentType(ContentType.JSON)
            .body("success", equalTo(false))
            .body("message", containsString("Invalid request"))
            .body("error", notNullValue())
            .body("timestamp", notNullValue());
    }
    
    @Test
    void testInvalidJsonNestedBigInteger_Scenario8() {
        // Arrange
        String invalidJson = "{ \"username\": \"testuser\", \"email\": \"test@example.com\", \"password\": \"password123\" }";
        
        // Act & Assert
        given()
            .contentType(ContentType.JSON)
            .body(invalidJson)
        .when()
            .post(TestConstants.AUTH_REGISTER_ENDPOINT)
        .then()
            .statusCode(400)
            .contentType(ContentType.JSON)
            .body("success", equalTo(false))
            .body("message", containsString("Invalid request"))
            .body("error", notNullValue())
            .body("timestamp", notNullValue());
    }
    
    @Test
    void testInvalidJsonNestedBigDecimal_Scenario9() {
        // Arrange
        String invalidJson = "{ \"username\": \"testuser\", \"email\": \"test@example.com\", \"password\": \"password123\" }";
        
        // Act & Assert
        given()
            .contentType(ContentType.JSON)
            .body(invalidJson)
        .when()
            .post(TestConstants.AUTH_REGISTER_ENDPOINT)
        .then()
            .statusCode(400)
            .contentType(ContentType.JSON)
            .body("success", equalTo(false))
            .body("message", containsString("Invalid request"))
            .body("error", notNullValue())
            .body("timestamp", notNullValue());
    }
    
    @Test
    void testInvalidJsonNestedBigInteger_Scenario10() {
        // Arrange
        String invalidJson = "{ \"username\": \"testuser\", \"email\": \"test@example.com\", \"password\": \"password123\" }";
        
        // Act & Assert
        given()
            .contentType(ContentType.JSON)
            .body(invalidJson)
        .when()
            .post(TestConstants.AUTH_REGISTER_ENDPOINT)
        .then()
            .statusCode(400)
            .contentType(ContentType.JSON)
            .body("success", equalTo(false))
            .body("message", containsString("Invalid request"))
            .body("error", notNullValue())
            .body("timestamp", notNullValue());
    }
    
    @Test
    void testInvalidJsonNestedBigDecimal_Scenario11() {
        // Arrange
        String invalidJson = "{ \"username\": \"testuser\", \"email\": \"test@example.com\", \"password\": \"password123\" }";
        
        // Act & Assert
        given()
            .contentType(ContentType.JSON)
            .body(invalidJson)
        .when()
            .post(TestConstants.AUTH_REGISTER_ENDPOINT)
        .then()
            .statusCode(400)
            .contentType(ContentType.JSON)
            .body("success", equalTo(false))
            .body("message", containsString("Invalid request"))
            .body("error", notNullValue())
            .body("timestamp", notNullValue());
    }
    
    @Test
    void testInvalidJsonNestedBigInteger_Scenario2() {
        // Arrange
        String invalidJson = "{ \"username\": \"testuser\", \"email\": \"test@example.com\", \"password\": \"password123\" }";
        
        // Act & Assert
        given()
            .contentType(ContentType.JSON)
            .body(invalidJson)
        .when()
            .post(TestConstants.AUTH_REGISTER_ENDPOINT)
        .then()
            .statusCode(400)
            .contentType(ContentType.JSON)
            .body("success", equalTo(false))
            .body("message", containsString("Invalid request"))
            .body("error", notNullValue())
            .body("timestamp", notNullValue());
    }
    
    @Test
    void testInvalidJsonNestedBigDecimal_Scenario2() {
        // Arrange
        String invalidJson = "{ \"username\": \"testuser\", \"email\": \"test@example.com\", \"password\": \"password123\" }";
        
        // Act & Assert
        given()
            .contentType(ContentType.JSON)
            .body(invalidJson)
        .when()
            .post(TestConstants.AUTH_REGISTER_ENDPOINT)
        .then()
            .statusCode(400)
            .contentType(ContentType.JSON)
            .body("success", equalTo(false))
            .body("message", containsString("Invalid request"))
            .body("error", notNullValue())
            .body("timestamp", notNullValue());
    }
}