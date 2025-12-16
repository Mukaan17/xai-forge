package com.example.xaiapp.unit.exception;

import com.example.xaiapp.exception.XaiException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for XaiException base class
 * Tests all constructors, error codes, user messages, and exception chaining
 */
class XaiExceptionTest {

    @Test
    void testConstructor_messageOnly_createsExceptionWithDefaults() {
        // Arrange
        String message = "Test error message";
        
        // Act
        XaiException exception = new XaiException(message);
        
        // Assert
        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
        assertEquals("XAI_ERROR", exception.getErrorCode());
        assertEquals("An unexpected error occurred", exception.getUserMessage());
        assertNull(exception.getCause());
    }

    @Test
    void testConstructor_messageWithCause_createsExceptionWithCause() {
        // Arrange
        String message = "Test error message";
        Throwable cause = new RuntimeException("Root cause");
        
        // Act
        XaiException exception = new XaiException(message, cause);
        
        // Assert
        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
        assertEquals("XAI_ERROR", exception.getErrorCode());
        assertEquals("An unexpected error occurred", exception.getUserMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void testConstructor_withErrorCodeAndMessages_createsExceptionWithCustomValues() {
        // Arrange
        String errorCode = "CUSTOM_ERROR";
        String message = "Custom error message";
        String userMessage = "Custom user message";
        
        // Act
        XaiException exception = new XaiException(errorCode, message, userMessage);
        
        // Assert
        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
        assertEquals(errorCode, exception.getErrorCode());
        assertEquals(userMessage, exception.getUserMessage());
        assertNull(exception.getCause());
    }

    @Test
    void testConstructor_withErrorCodeMessagesAndCause_createsExceptionWithAllValues() {
        // Arrange
        String errorCode = "CUSTOM_ERROR";
        String message = "Custom error message";
        String userMessage = "Custom user message";
        Throwable cause = new IllegalArgumentException("Root cause");
        
        // Act
        XaiException exception = new XaiException(errorCode, message, userMessage, cause);
        
        // Assert
        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
        assertEquals(errorCode, exception.getErrorCode());
        assertEquals(userMessage, exception.getUserMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void testGetErrorCode_returnsCorrectErrorCode() {
        // Arrange
        String errorCode = "TEST_ERROR";
        XaiException exception = new XaiException(errorCode, "message", "user message");
        
        // Act
        String result = exception.getErrorCode();
        
        // Assert
        assertEquals(errorCode, result);
    }

    @Test
    void testGetUserMessage_returnsCorrectUserMessage() {
        // Arrange
        String userMessage = "User-friendly message";
        XaiException exception = new XaiException("ERROR", "message", userMessage);
        
        // Act
        String result = exception.getUserMessage();
        
        // Assert
        assertEquals(userMessage, result);
    }

    @Test
    void testWithNullMessage_handlesNullMessage() {
        // Act
        XaiException exception = new XaiException(null);
        
        // Assert
        assertNull(exception.getMessage());
        assertEquals("XAI_ERROR", exception.getErrorCode());
        assertEquals("An unexpected error occurred", exception.getUserMessage());
    }

    @Test
    void testWithEmptyMessage_handlesEmptyMessage() {
        // Act
        XaiException exception = new XaiException("");
        
        // Assert
        assertEquals("", exception.getMessage());
        assertEquals("XAI_ERROR", exception.getErrorCode());
        assertEquals("An unexpected error occurred", exception.getUserMessage());
    }

    @Test
    void testWithNullCause_handlesNullCause() {
        // Act
        XaiException exception = new XaiException("message", null);
        
        // Assert
        assertEquals("message", exception.getMessage());
        assertEquals("XAI_ERROR", exception.getErrorCode());
        assertEquals("An unexpected error occurred", exception.getUserMessage());
        assertNull(exception.getCause());
    }

    @Test
    void testWithNullErrorCode_handlesNullErrorCode() {
        // Act
        XaiException exception = new XaiException(null, "message", "user message");
        
        // Assert
        assertEquals("message", exception.getMessage());
        assertNull(exception.getErrorCode());
        assertEquals("user message", exception.getUserMessage());
    }

    @Test
    void testWithNullUserMessage_handlesNullUserMessage() {
        // Act
        XaiException exception = new XaiException("ERROR", "message", null);
        
        // Assert
        assertEquals("message", exception.getMessage());
        assertEquals("ERROR", exception.getErrorCode());
        assertNull(exception.getUserMessage());
    }

    @Test
    void testWithLongMessage_handlesLongMessage() {
        // Arrange
        String longMessage = "This is a very long error message that contains many characters and should be handled properly by the exception constructor";
        
        // Act
        XaiException exception = new XaiException(longMessage);
        
        // Assert
        assertEquals(longMessage, exception.getMessage());
        assertEquals("XAI_ERROR", exception.getErrorCode());
        assertEquals("An unexpected error occurred", exception.getUserMessage());
    }

    @Test
    void testWithLongUserMessage_handlesLongUserMessage() {
        // Arrange
        String longUserMessage = "This is a very long user-friendly message that explains the error in detail and provides guidance to the user";
        
        // Act
        XaiException exception = new XaiException("ERROR", "message", longUserMessage);
        
        // Assert
        assertEquals("message", exception.getMessage());
        assertEquals("ERROR", exception.getErrorCode());
        assertEquals(longUserMessage, exception.getUserMessage());
    }

    @Test
    void testWithSpecialCharactersInMessage_handlesSpecialChars() {
        // Arrange
        String specialMessage = "Error with special chars: @#$%^&*()_+-=[]{}|;':\",./<>?";
        
        // Act
        XaiException exception = new XaiException(specialMessage);
        
        // Assert
        assertEquals(specialMessage, exception.getMessage());
        assertEquals("XAI_ERROR", exception.getErrorCode());
        assertEquals("An unexpected error occurred", exception.getUserMessage());
    }

    @Test
    void testWithUnicodeCharacters_handlesUnicode() {
        // Arrange
        String unicodeMessage = "错误消息：用户输入无效";
        String unicodeUserMessage = "请检查您的输入并重试";
        
        // Act
        XaiException exception = new XaiException("ERROR", unicodeMessage, unicodeUserMessage);
        
        // Assert
        assertEquals(unicodeMessage, exception.getMessage());
        assertEquals("ERROR", exception.getErrorCode());
        assertEquals(unicodeUserMessage, exception.getUserMessage());
    }

    @Test
    void testWithWhitespaceInMessage_handlesWhitespace() {
        // Arrange
        String messageWithWhitespace = "  Error message with whitespace  ";
        
        // Act
        XaiException exception = new XaiException(messageWithWhitespace);
        
        // Assert
        assertEquals(messageWithWhitespace, exception.getMessage());
        assertEquals("XAI_ERROR", exception.getErrorCode());
        assertEquals("An unexpected error occurred", exception.getUserMessage());
    }

    @Test
    void testWithNestedCause_handlesNestedCause() {
        // Arrange
        Throwable rootCause = new RuntimeException("Root cause");
        Throwable intermediateCause = new IllegalArgumentException("Intermediate cause", rootCause);
        Throwable topCause = new IllegalStateException("Top cause", intermediateCause);
        
        // Act
        XaiException exception = new XaiException("message", topCause);
        
        // Assert
        assertEquals("message", exception.getMessage());
        assertEquals("XAI_ERROR", exception.getErrorCode());
        assertEquals("An unexpected error occurred", exception.getUserMessage());
        assertEquals(topCause, exception.getCause());
        assertEquals(intermediateCause, exception.getCause().getCause());
        assertEquals(rootCause, exception.getCause().getCause().getCause());
    }

    @Test
    void testWithEmptyErrorCode_handlesEmptyErrorCode() {
        // Act
        XaiException exception = new XaiException("", "message", "user message");
        
        // Assert
        assertEquals("message", exception.getMessage());
        assertEquals("", exception.getErrorCode());
        assertEquals("user message", exception.getUserMessage());
    }

    @Test
    void testWithEmptyUserMessage_handlesEmptyUserMessage() {
        // Act
        XaiException exception = new XaiException("ERROR", "message", "");
        
        // Assert
        assertEquals("message", exception.getMessage());
        assertEquals("ERROR", exception.getErrorCode());
        assertEquals("", exception.getUserMessage());
    }

    @Test
    void testInheritance_extendsRuntimeException() {
        // Arrange
        XaiException exception = new XaiException("message");
        
        // Act & Assert
        assertTrue(exception instanceof RuntimeException);
    }

    @Test
    void testToString_containsMessage() {
        // Arrange
        XaiException exception = new XaiException("Test message");
        
        // Act
        String toString = exception.toString();
        
        // Assert
        assertTrue(toString.contains("Test message"));
        assertTrue(toString.contains("XaiException"));
    }

    @Test
    void testWithCauseToString_containsCause() {
        // Arrange
        Throwable cause = new RuntimeException("Cause message");
        XaiException exception = new XaiException("Test message", cause);
        
        // Act
        String toString = exception.toString();
        
        // Assert
        assertTrue(toString.contains("Test message"));
        assertTrue(toString.contains("XaiException"));
    }
}
