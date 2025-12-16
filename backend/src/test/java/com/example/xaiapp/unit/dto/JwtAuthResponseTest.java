package com.example.xaiapp.unit.dto;

import com.example.xaiapp.dto.JwtAuthResponse;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for JwtAuthResponse DTO
 * Tests constructors, Lombok methods, and default tokenType value
 */
class JwtAuthResponseTest {

    @Test
    void testNoArgsConstructor_createsEmptyResponse() {
        // Act
        JwtAuthResponse response = new JwtAuthResponse();
        
        // Assert
        assertNotNull(response);
        assertNull(response.getAccessToken());
        assertEquals("Bearer", response.getTokenType()); // Default value
        assertNull(response.getUserId());
        assertNull(response.getUsername());
    }

    @Test
    void testAllArgsConstructor_createsCompleteResponse() {
        // Arrange
        String accessToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...";
        String tokenType = "Bearer";
        Long userId = 123L;
        String username = "testuser";
        
        // Act
        JwtAuthResponse response = new JwtAuthResponse(accessToken, tokenType, userId, username);
        
        // Assert
        assertNotNull(response);
        assertEquals(accessToken, response.getAccessToken());
        assertEquals(tokenType, response.getTokenType());
        assertEquals(userId, response.getUserId());
        assertEquals(username, response.getUsername());
    }

    @Test
    void testSetters_setAllFields() {
        // Arrange
        JwtAuthResponse response = new JwtAuthResponse();
        String accessToken = "new_token_123";
        String tokenType = "JWT";
        Long userId = 456L;
        String username = "newuser";
        
        // Act
        response.setAccessToken(accessToken);
        response.setTokenType(tokenType);
        response.setUserId(userId);
        response.setUsername(username);
        
        // Assert
        assertEquals(accessToken, response.getAccessToken());
        assertEquals(tokenType, response.getTokenType());
        assertEquals(userId, response.getUserId());
        assertEquals(username, response.getUsername());
    }

    @Test
    void testEquals_sameObject_returnsTrue() {
        // Arrange
        JwtAuthResponse response = createTestResponse();
        
        // Act & Assert
        assertEquals(response, response);
    }

    @Test
    void testEquals_equalObjects_returnsTrue() {
        // Arrange
        JwtAuthResponse response1 = createTestResponse();
        JwtAuthResponse response2 = createTestResponse();
        
        // Act & Assert
        assertEquals(response1, response2);
    }

    @Test
    void testEquals_differentObjects_returnsFalse() {
        // Arrange
        JwtAuthResponse response1 = createTestResponse();
        JwtAuthResponse response2 = createTestResponse();
        response2.setUsername("different");
        
        // Act & Assert
        assertNotEquals(response1, response2);
    }

    @Test
    void testEquals_nullObject_returnsFalse() {
        // Arrange
        JwtAuthResponse response = createTestResponse();
        
        // Act & Assert
        assertNotEquals(response, null);
    }

    @Test
    void testEquals_differentClass_returnsFalse() {
        // Arrange
        JwtAuthResponse response = createTestResponse();
        String other = "not a response";
        
        // Act & Assert
        assertNotEquals(response, other);
    }

    @Test
    void testHashCode_equalObjects_sameHashCode() {
        // Arrange
        JwtAuthResponse response1 = createTestResponse();
        JwtAuthResponse response2 = createTestResponse();
        
        // Act & Assert
        assertEquals(response1.hashCode(), response2.hashCode());
    }

    @Test
    void testToString_containsAllFields() {
        // Arrange
        JwtAuthResponse response = createTestResponse();
        
        // Act
        String toString = response.toString();
        
        // Assert
        assertTrue(toString.contains("test_token"));
        assertTrue(toString.contains("Bearer"));
        assertTrue(toString.contains("123"));
        assertTrue(toString.contains("testuser"));
    }

    @Test
    void testWithNullValues_handlesNullsCorrectly() {
        // Arrange
        JwtAuthResponse response = new JwtAuthResponse();
        
        // Act
        response.setAccessToken(null);
        response.setTokenType(null);
        response.setUserId(null);
        response.setUsername(null);
        
        // Assert
        assertNull(response.getAccessToken());
        assertNull(response.getTokenType());
        assertNull(response.getUserId());
        assertNull(response.getUsername());
    }

    @Test
    void testWithEmptyToken_handlesEmptyString() {
        // Arrange
        JwtAuthResponse response = new JwtAuthResponse();
        
        // Act
        response.setAccessToken("");
        
        // Assert
        assertEquals("", response.getAccessToken());
    }

    @Test
    void testWithEmptyUsername_handlesEmptyString() {
        // Arrange
        JwtAuthResponse response = new JwtAuthResponse();
        
        // Act
        response.setUsername("");
        
        // Assert
        assertEquals("", response.getUsername());
    }

    @Test
    void testWithZeroUserId_handlesZero() {
        // Arrange
        JwtAuthResponse response = new JwtAuthResponse();
        
        // Act
        response.setUserId(0L);
        
        // Assert
        assertEquals(Long.valueOf(0), response.getUserId());
    }

    @Test
    void testWithNegativeUserId_handlesNegativeValues() {
        // Arrange
        JwtAuthResponse response = new JwtAuthResponse();
        
        // Act
        response.setUserId(-1L);
        
        // Assert
        assertEquals(Long.valueOf(-1), response.getUserId());
    }

    @Test
    void testWithMaxUserId_handlesMaxValue() {
        // Arrange
        JwtAuthResponse response = new JwtAuthResponse();
        
        // Act
        response.setUserId(Long.MAX_VALUE);
        
        // Assert
        assertEquals(Long.MAX_VALUE, response.getUserId());
    }

    @Test
    void testWithLongToken_handlesLongStrings() {
        // Arrange
        JwtAuthResponse response = new JwtAuthResponse();
        String longToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";
        
        // Act
        response.setAccessToken(longToken);
        
        // Assert
        assertEquals(longToken, response.getAccessToken());
    }

    @Test
    void testWithLongUsername_handlesLongStrings() {
        // Arrange
        JwtAuthResponse response = new JwtAuthResponse();
        String longUsername = "very_long_username_with_many_characters_and_numbers_12345";
        
        // Act
        response.setUsername(longUsername);
        
        // Assert
        assertEquals(longUsername, response.getUsername());
    }

    @Test
    void testWithCustomTokenType_handlesCustomTypes() {
        // Arrange
        JwtAuthResponse response = new JwtAuthResponse();
        String customTokenType = "Custom";
        
        // Act
        response.setTokenType(customTokenType);
        
        // Assert
        assertEquals(customTokenType, response.getTokenType());
    }

    @Test
    void testWithSpecialCharactersInUsername_handlesSpecialChars() {
        // Arrange
        JwtAuthResponse response = new JwtAuthResponse();
        String specialUsername = "user@domain.com";
        
        // Act
        response.setUsername(specialUsername);
        
        // Assert
        assertEquals(specialUsername, response.getUsername());
    }

    @Test
    void testWithSpecialCharactersInToken_handlesSpecialChars() {
        // Arrange
        JwtAuthResponse response = new JwtAuthResponse();
        String specialToken = "token-with-special.chars+more";
        
        // Act
        response.setAccessToken(specialToken);
        
        // Assert
        assertEquals(specialToken, response.getAccessToken());
    }

    @Test
    void testWithUnicodeCharacters_handlesUnicode() {
        // Arrange
        JwtAuthResponse response = new JwtAuthResponse();
        String unicodeUsername = "用户123";
        
        // Act
        response.setUsername(unicodeUsername);
        
        // Assert
        assertEquals(unicodeUsername, response.getUsername());
    }

    @Test
    void testWithWhitespaceInToken_handlesWhitespace() {
        // Arrange
        JwtAuthResponse response = new JwtAuthResponse();
        String tokenWithWhitespace = " token with spaces ";
        
        // Act
        response.setAccessToken(tokenWithWhitespace);
        
        // Assert
        assertEquals(tokenWithWhitespace, response.getAccessToken());
    }

    @Test
    void testWithWhitespaceInUsername_handlesWhitespace() {
        // Arrange
        JwtAuthResponse response = new JwtAuthResponse();
        String usernameWithWhitespace = " user name ";
        
        // Act
        response.setUsername(usernameWithWhitespace);
        
        // Assert
        assertEquals(usernameWithWhitespace, response.getUsername());
    }

    private JwtAuthResponse createTestResponse() {
        return new JwtAuthResponse("test_token", "Bearer", 123L, "testuser");
    }
}
