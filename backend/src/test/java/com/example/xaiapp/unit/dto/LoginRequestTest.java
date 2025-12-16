package com.example.xaiapp.unit.dto;

import com.example.xaiapp.dto.LoginRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for LoginRequest DTO
 * Tests constructors, Lombok methods, and validation annotations
 */
class LoginRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testNoArgsConstructor_createsEmptyRequest() {
        // Act
        LoginRequest request = new LoginRequest();
        
        // Assert
        assertNotNull(request);
        assertNull(request.getUsername());
        assertNull(request.getPassword());
    }

    @Test
    void testAllArgsConstructor_createsCompleteRequest() {
        // Arrange
        String username = "testuser";
        String password = "password123";
        
        // Act
        LoginRequest request = new LoginRequest(username, password);
        
        // Assert
        assertNotNull(request);
        assertEquals(username, request.getUsername());
        assertEquals(password, request.getPassword());
    }

    @Test
    void testSetters_setAllFields() {
        // Arrange
        LoginRequest request = new LoginRequest();
        String username = "newuser";
        String password = "newpassword";
        
        // Act
        request.setUsername(username);
        request.setPassword(password);
        
        // Assert
        assertEquals(username, request.getUsername());
        assertEquals(password, request.getPassword());
    }

    @Test
    void testEquals_sameObject_returnsTrue() {
        // Arrange
        LoginRequest request = createTestRequest();
        
        // Act & Assert
        assertEquals(request, request);
    }

    @Test
    void testEquals_equalObjects_returnsTrue() {
        // Arrange
        LoginRequest request1 = createTestRequest();
        LoginRequest request2 = createTestRequest();
        
        // Act & Assert
        assertEquals(request1, request2);
    }

    @Test
    void testEquals_differentObjects_returnsFalse() {
        // Arrange
        LoginRequest request1 = createTestRequest();
        LoginRequest request2 = createTestRequest();
        request2.setUsername("different");
        
        // Act & Assert
        assertNotEquals(request1, request2);
    }

    @Test
    void testEquals_nullObject_returnsFalse() {
        // Arrange
        LoginRequest request = createTestRequest();
        
        // Act & Assert
        assertNotEquals(request, null);
    }

    @Test
    void testEquals_differentClass_returnsFalse() {
        // Arrange
        LoginRequest request = createTestRequest();
        String other = "not a request";
        
        // Act & Assert
        assertNotEquals(request, other);
    }

    @Test
    void testHashCode_equalObjects_sameHashCode() {
        // Arrange
        LoginRequest request1 = createTestRequest();
        LoginRequest request2 = createTestRequest();
        
        // Act & Assert
        assertEquals(request1.hashCode(), request2.hashCode());
    }

    @Test
    void testToString_containsAllFields() {
        // Arrange
        LoginRequest request = createTestRequest();
        
        // Act
        String toString = request.toString();
        
        // Assert
        assertTrue(toString.contains("testuser"));
        assertTrue(toString.contains("password123"));
    }

    @Test
    void testValidation_validRequest_noViolations() {
        // Arrange
        LoginRequest request = new LoginRequest("validuser", "validpassword");
        
        // Act
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);
        
        // Assert
        assertTrue(violations.isEmpty());
    }

    @Test
    void testValidation_nullUsername_violation() {
        // Arrange
        LoginRequest request = new LoginRequest(null, "password");
        
        // Act
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);
        
        // Assert
        assertEquals(1, violations.size());
        ConstraintViolation<LoginRequest> violation = violations.iterator().next();
        assertEquals("username", violation.getPropertyPath().toString());
        assertEquals("Username is required", violation.getMessage());
    }

    @Test
    void testValidation_blankUsername_violation() {
        // Arrange
        LoginRequest request = new LoginRequest("", "password");
        
        // Act
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);
        
        // Assert
        assertEquals(1, violations.size());
        ConstraintViolation<LoginRequest> violation = violations.iterator().next();
        assertEquals("username", violation.getPropertyPath().toString());
        assertEquals("Username is required", violation.getMessage());
    }

    @Test
    void testValidation_whitespaceUsername_violation() {
        // Arrange
        LoginRequest request = new LoginRequest("   ", "password");
        
        // Act
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);
        
        // Assert
        assertEquals(1, violations.size());
        ConstraintViolation<LoginRequest> violation = violations.iterator().next();
        assertEquals("username", violation.getPropertyPath().toString());
        assertEquals("Username is required", violation.getMessage());
    }

    @Test
    void testValidation_nullPassword_violation() {
        // Arrange
        LoginRequest request = new LoginRequest("username", null);
        
        // Act
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);
        
        // Assert
        assertEquals(1, violations.size());
        ConstraintViolation<LoginRequest> violation = violations.iterator().next();
        assertEquals("password", violation.getPropertyPath().toString());
        assertEquals("Password is required", violation.getMessage());
    }

    @Test
    void testValidation_blankPassword_violation() {
        // Arrange
        LoginRequest request = new LoginRequest("username", "");
        
        // Act
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);
        
        // Assert
        assertEquals(1, violations.size());
        ConstraintViolation<LoginRequest> violation = violations.iterator().next();
        assertEquals("password", violation.getPropertyPath().toString());
        assertEquals("Password is required", violation.getMessage());
    }

    @Test
    void testValidation_whitespacePassword_violation() {
        // Arrange
        LoginRequest request = new LoginRequest("username", "   ");
        
        // Act
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);
        
        // Assert
        assertEquals(1, violations.size());
        ConstraintViolation<LoginRequest> violation = violations.iterator().next();
        assertEquals("password", violation.getPropertyPath().toString());
        assertEquals("Password is required", violation.getMessage());
    }

    @Test
    void testValidation_bothFieldsNull_multipleViolations() {
        // Arrange
        LoginRequest request = new LoginRequest(null, null);
        
        // Act
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);
        
        // Assert
        assertEquals(2, violations.size());
        
        boolean hasUsernameViolation = violations.stream()
            .anyMatch(v -> "username".equals(v.getPropertyPath().toString()));
        boolean hasPasswordViolation = violations.stream()
            .anyMatch(v -> "password".equals(v.getPropertyPath().toString()));
            
        assertTrue(hasUsernameViolation);
        assertTrue(hasPasswordViolation);
    }

    @Test
    void testValidation_bothFieldsBlank_multipleViolations() {
        // Arrange
        LoginRequest request = new LoginRequest("", "");
        
        // Act
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);
        
        // Assert
        assertEquals(2, violations.size());
        
        boolean hasUsernameViolation = violations.stream()
            .anyMatch(v -> "username".equals(v.getPropertyPath().toString()));
        boolean hasPasswordViolation = violations.stream()
            .anyMatch(v -> "password".equals(v.getPropertyPath().toString()));
            
        assertTrue(hasUsernameViolation);
        assertTrue(hasPasswordViolation);
    }

    @Test
    void testWithNullValues_handlesNullsCorrectly() {
        // Arrange
        LoginRequest request = new LoginRequest();
        
        // Act
        request.setUsername(null);
        request.setPassword(null);
        
        // Assert
        assertNull(request.getUsername());
        assertNull(request.getPassword());
    }

    @Test
    void testWithEmptyStrings_handlesEmptyStrings() {
        // Arrange
        LoginRequest request = new LoginRequest();
        
        // Act
        request.setUsername("");
        request.setPassword("");
        
        // Assert
        assertEquals("", request.getUsername());
        assertEquals("", request.getPassword());
    }

    @Test
    void testWithLongUsername_handlesLongStrings() {
        // Arrange
        LoginRequest request = new LoginRequest();
        String longUsername = "very_long_username_with_many_characters_and_numbers_12345";
        
        // Act
        request.setUsername(longUsername);
        
        // Assert
        assertEquals(longUsername, request.getUsername());
    }

    @Test
    void testWithLongPassword_handlesLongStrings() {
        // Arrange
        LoginRequest request = new LoginRequest();
        String longPassword = "very_long_password_with_many_characters_and_numbers_12345";
        
        // Act
        request.setPassword(longPassword);
        
        // Assert
        assertEquals(longPassword, request.getPassword());
    }

    @Test
    void testWithSpecialCharactersInUsername_handlesSpecialChars() {
        // Arrange
        LoginRequest request = new LoginRequest();
        String specialUsername = "user@domain.com";
        
        // Act
        request.setUsername(specialUsername);
        
        // Assert
        assertEquals(specialUsername, request.getUsername());
    }

    @Test
    void testWithSpecialCharactersInPassword_handlesSpecialChars() {
        // Arrange
        LoginRequest request = new LoginRequest();
        String specialPassword = "pass@word#123$";
        
        // Act
        request.setPassword(specialPassword);
        
        // Assert
        assertEquals(specialPassword, request.getPassword());
    }

    @Test
    void testWithUnicodeCharacters_handlesUnicode() {
        // Arrange
        LoginRequest request = new LoginRequest();
        String unicodeUsername = "用户123";
        String unicodePassword = "密码456";
        
        // Act
        request.setUsername(unicodeUsername);
        request.setPassword(unicodePassword);
        
        // Assert
        assertEquals(unicodeUsername, request.getUsername());
        assertEquals(unicodePassword, request.getPassword());
    }

    @Test
    void testWithWhitespaceInUsername_handlesWhitespace() {
        // Arrange
        LoginRequest request = new LoginRequest();
        String usernameWithWhitespace = " user name ";
        
        // Act
        request.setUsername(usernameWithWhitespace);
        
        // Assert
        assertEquals(usernameWithWhitespace, request.getUsername());
    }

    @Test
    void testWithWhitespaceInPassword_handlesWhitespace() {
        // Arrange
        LoginRequest request = new LoginRequest();
        String passwordWithWhitespace = " pass word ";
        
        // Act
        request.setPassword(passwordWithWhitespace);
        
        // Assert
        assertEquals(passwordWithWhitespace, request.getPassword());
    }

    private LoginRequest createTestRequest() {
        return new LoginRequest("testuser", "password123");
    }
}
