package com.example.xaiapp.unit.exception;

import com.example.xaiapp.exception.AuthenticationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for AuthenticationException
 * Tests constructors, error codes, user messages, and exception chaining
 */
class AuthenticationExceptionTest {

    @Test
    void testConstructor_messageOnly_createsExceptionWithDefaults() {
        // Arrange
        String message = "Authentication failed";
        
        // Act
        AuthenticationException exception = new AuthenticationException(message);
        
        // Assert
        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
        assertEquals("AUTHENTICATION_ERROR", exception.getErrorCode());
        assertEquals("Authentication failed. Please check your credentials and try again.", exception.getUserMessage());
        assertNull(exception.getCause());
    }

    @Test
    void testConstructor_messageWithCause_createsExceptionWithCause() {
        // Arrange
        String message = "Authentication failed";
        Throwable cause = new RuntimeException("Root cause");
        
        // Act
        AuthenticationException exception = new AuthenticationException(message, cause);
        
        // Assert
        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
        assertEquals("AUTHENTICATION_ERROR", exception.getErrorCode());
        assertEquals("Authentication failed. Please check your credentials and try again.", exception.getUserMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void testInheritance_extendsXaiException() {
        // Arrange
        AuthenticationException exception = new AuthenticationException("message");
        
        // Act & Assert
        assertTrue(exception instanceof com.example.xaiapp.exception.XaiException);
    }

    @Test
    void testWithNullMessage_handlesNullMessage() {
        // Act
        AuthenticationException exception = new AuthenticationException(null);
        
        // Assert
        assertNull(exception.getMessage());
        assertEquals("AUTHENTICATION_ERROR", exception.getErrorCode());
        assertEquals("Authentication failed. Please check your credentials and try again.", exception.getUserMessage());
    }

    @Test
    void testWithEmptyMessage_handlesEmptyMessage() {
        // Act
        AuthenticationException exception = new AuthenticationException("");
        
        // Assert
        assertEquals("", exception.getMessage());
        assertEquals("AUTHENTICATION_ERROR", exception.getErrorCode());
        assertEquals("Authentication failed. Please check your credentials and try again.", exception.getUserMessage());
    }

    @Test
    void testWithNullCause_handlesNullCause() {
        // Act
        AuthenticationException exception = new AuthenticationException("message", null);
        
        // Assert
        assertEquals("message", exception.getMessage());
        assertEquals("AUTHENTICATION_ERROR", exception.getErrorCode());
        assertEquals("Authentication failed. Please check your credentials and try again.", exception.getUserMessage());
        assertNull(exception.getCause());
    }

    @Test
    void testWithLongMessage_handlesLongMessage() {
        // Arrange
        String longMessage = "This is a very long authentication error message that contains many characters and should be handled properly by the exception constructor";
        
        // Act
        AuthenticationException exception = new AuthenticationException(longMessage);
        
        // Assert
        assertEquals(longMessage, exception.getMessage());
        assertEquals("AUTHENTICATION_ERROR", exception.getErrorCode());
        assertEquals("Authentication failed. Please check your credentials and try again.", exception.getUserMessage());
    }

    @Test
    void testWithSpecialCharactersInMessage_handlesSpecialChars() {
        // Arrange
        String specialMessage = "Auth error with special chars: @#$%^&*()_+-=[]{}|;':\",./<>?";
        
        // Act
        AuthenticationException exception = new AuthenticationException(specialMessage);
        
        // Assert
        assertEquals(specialMessage, exception.getMessage());
        assertEquals("AUTHENTICATION_ERROR", exception.getErrorCode());
        assertEquals("Authentication failed. Please check your credentials and try again.", exception.getUserMessage());
    }

    @Test
    void testWithUnicodeCharacters_handlesUnicode() {
        // Arrange
        String unicodeMessage = "认证失败：用户凭据无效";
        
        // Act
        AuthenticationException exception = new AuthenticationException(unicodeMessage);
        
        // Assert
        assertEquals(unicodeMessage, exception.getMessage());
        assertEquals("AUTHENTICATION_ERROR", exception.getErrorCode());
        assertEquals("Authentication failed. Please check your credentials and try again.", exception.getUserMessage());
    }

    @Test
    void testWithWhitespaceInMessage_handlesWhitespace() {
        // Arrange
        String messageWithWhitespace = "  Authentication error with whitespace  ";
        
        // Act
        AuthenticationException exception = new AuthenticationException(messageWithWhitespace);
        
        // Assert
        assertEquals(messageWithWhitespace, exception.getMessage());
        assertEquals("AUTHENTICATION_ERROR", exception.getErrorCode());
        assertEquals("Authentication failed. Please check your credentials and try again.", exception.getUserMessage());
    }

    @Test
    void testWithNestedCause_handlesNestedCause() {
        // Arrange
        Throwable rootCause = new RuntimeException("Root cause");
        Throwable intermediateCause = new IllegalArgumentException("Intermediate cause", rootCause);
        Throwable topCause = new IllegalStateException("Top cause", intermediateCause);
        
        // Act
        AuthenticationException exception = new AuthenticationException("message", topCause);
        
        // Assert
        assertEquals("message", exception.getMessage());
        assertEquals("AUTHENTICATION_ERROR", exception.getErrorCode());
        assertEquals("Authentication failed. Please check your credentials and try again.", exception.getUserMessage());
        assertEquals(topCause, exception.getCause());
        assertEquals(intermediateCause, exception.getCause().getCause());
        assertEquals(rootCause, exception.getCause().getCause().getCause());
    }

    @Test
    void testToString_containsMessage() {
        // Arrange
        AuthenticationException exception = new AuthenticationException("Test message");
        
        // Act
        String toString = exception.toString();
        
        // Assert
        assertTrue(toString.contains("Test message"));
        assertTrue(toString.contains("AuthenticationException"));
    }

    @Test
    void testWithCauseToString_containsCause() {
        // Arrange
        Throwable cause = new RuntimeException("Cause message");
        AuthenticationException exception = new AuthenticationException("Test message", cause);
        
        // Act
        String toString = exception.toString();
        
        // Assert
        assertTrue(toString.contains("Test message"));
        assertTrue(toString.contains("AuthenticationException"));
    }

    @Test
    void testErrorCodeConsistency_alwaysReturnsSameErrorCode() {
        // Arrange
        AuthenticationException exception1 = new AuthenticationException("message1");
        AuthenticationException exception2 = new AuthenticationException("message2", new RuntimeException());
        
        // Act & Assert
        assertEquals("AUTHENTICATION_ERROR", exception1.getErrorCode());
        assertEquals("AUTHENTICATION_ERROR", exception2.getErrorCode());
        assertEquals(exception1.getErrorCode(), exception2.getErrorCode());
    }

    @Test
    void testUserMessageConsistency_alwaysReturnsSameUserMessage() {
        // Arrange
        AuthenticationException exception1 = new AuthenticationException("message1");
        AuthenticationException exception2 = new AuthenticationException("message2", new RuntimeException());
        
        // Act & Assert
        assertEquals("Authentication failed. Please check your credentials and try again.", exception1.getUserMessage());
        assertEquals("Authentication failed. Please check your credentials and try again.", exception2.getUserMessage());
        assertEquals(exception1.getUserMessage(), exception2.getUserMessage());
    }
}
