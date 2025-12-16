package com.example.xaiapp.unit.exception;

import com.example.xaiapp.exception.ModelTrainingException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ModelTrainingException
 * Tests all three constructors, error codes, user messages, and exception chaining
 */
class ModelTrainingExceptionTest {

    @Test
    void testConstructor_messageOnly_createsExceptionWithDefaults() {
        // Arrange
        String message = "Model training failed";
        
        // Act
        ModelTrainingException exception = new ModelTrainingException(message);
        
        // Assert
        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
        assertEquals("MODEL_TRAINING_ERROR", exception.getErrorCode());
        assertEquals("Model training failed. Please check your data and try again.", exception.getUserMessage());
        assertNull(exception.getCause());
    }

    @Test
    void testConstructor_messageWithCause_createsExceptionWithCause() {
        // Arrange
        String message = "Model training failed";
        Throwable cause = new RuntimeException("Root cause");
        
        // Act
        ModelTrainingException exception = new ModelTrainingException(message, cause);
        
        // Assert
        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
        assertEquals("MODEL_TRAINING_ERROR", exception.getErrorCode());
        assertEquals("Model training failed. Please check your data and try again.", exception.getUserMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void testConstructor_messageWithUserMessage_createsExceptionWithCustomUserMessage() {
        // Arrange
        String message = "Model training failed";
        String userMessage = "Custom user message";
        
        // Act
        ModelTrainingException exception = new ModelTrainingException(message, userMessage);
        
        // Assert
        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
        assertEquals("MODEL_TRAINING_ERROR", exception.getErrorCode());
        assertEquals(userMessage, exception.getUserMessage());
        assertNull(exception.getCause());
    }

    @Test
    void testInheritance_extendsXaiException() {
        // Arrange
        ModelTrainingException exception = new ModelTrainingException("message");
        
        // Act & Assert
        assertTrue(exception instanceof com.example.xaiapp.exception.XaiException);
    }

    @Test
    void testWithNullMessage_handlesNullMessage() {
        // Act
        ModelTrainingException exception = new ModelTrainingException(null);
        
        // Assert
        assertNull(exception.getMessage());
        assertEquals("MODEL_TRAINING_ERROR", exception.getErrorCode());
        assertEquals("Model training failed. Please check your data and try again.", exception.getUserMessage());
    }

    @Test
    void testWithEmptyMessage_handlesEmptyMessage() {
        // Act
        ModelTrainingException exception = new ModelTrainingException("");
        
        // Assert
        assertEquals("", exception.getMessage());
        assertEquals("MODEL_TRAINING_ERROR", exception.getErrorCode());
        assertEquals("Model training failed. Please check your data and try again.", exception.getUserMessage());
    }

    @Test
    void testWithNullCause_handlesNullCause() {
        // Act
        ModelTrainingException exception = new ModelTrainingException("message", (Throwable) null);
        
        // Assert
        assertEquals("message", exception.getMessage());
        assertEquals("MODEL_TRAINING_ERROR", exception.getErrorCode());
        assertEquals("Model training failed. Please check your data and try again.", exception.getUserMessage());
        assertNull(exception.getCause());
    }

    @Test
    void testWithNullUserMessage_handlesNullUserMessage() {
        // Act
        ModelTrainingException exception = new ModelTrainingException("message", (String) null);
        
        // Assert
        assertEquals("message", exception.getMessage());
        assertEquals("MODEL_TRAINING_ERROR", exception.getErrorCode());
        assertNull(exception.getUserMessage());
    }

    @Test
    void testWithEmptyUserMessage_handlesEmptyUserMessage() {
        // Act
        ModelTrainingException exception = new ModelTrainingException("message", "");
        
        // Assert
        assertEquals("message", exception.getMessage());
        assertEquals("MODEL_TRAINING_ERROR", exception.getErrorCode());
        assertEquals("", exception.getUserMessage());
    }

    @Test
    void testWithLongMessage_handlesLongMessage() {
        // Arrange
        String longMessage = "This is a very long model training error message that contains many characters and should be handled properly by the exception constructor";
        
        // Act
        ModelTrainingException exception = new ModelTrainingException(longMessage);
        
        // Assert
        assertEquals(longMessage, exception.getMessage());
        assertEquals("MODEL_TRAINING_ERROR", exception.getErrorCode());
        assertEquals("Model training failed. Please check your data and try again.", exception.getUserMessage());
    }

    @Test
    void testWithLongUserMessage_handlesLongUserMessage() {
        // Arrange
        String longUserMessage = "This is a very long user-friendly message that explains the model training error in detail and provides guidance to the user";
        
        // Act
        ModelTrainingException exception = new ModelTrainingException("message", longUserMessage);
        
        // Assert
        assertEquals("message", exception.getMessage());
        assertEquals("MODEL_TRAINING_ERROR", exception.getErrorCode());
        assertEquals(longUserMessage, exception.getUserMessage());
    }

    @Test
    void testWithSpecialCharactersInMessage_handlesSpecialChars() {
        // Arrange
        String specialMessage = "Model training error with special chars: @#$%^&*()_+-=[]{}|;':\",./<>?";
        
        // Act
        ModelTrainingException exception = new ModelTrainingException(specialMessage);
        
        // Assert
        assertEquals(specialMessage, exception.getMessage());
        assertEquals("MODEL_TRAINING_ERROR", exception.getErrorCode());
        assertEquals("Model training failed. Please check your data and try again.", exception.getUserMessage());
    }

    @Test
    void testWithUnicodeCharacters_handlesUnicode() {
        // Arrange
        String unicodeMessage = "模型训练错误：数据无效";
        String unicodeUserMessage = "请检查数据并重试";
        
        // Act
        ModelTrainingException exception = new ModelTrainingException(unicodeMessage, unicodeUserMessage);
        
        // Assert
        assertEquals(unicodeMessage, exception.getMessage());
        assertEquals("MODEL_TRAINING_ERROR", exception.getErrorCode());
        assertEquals(unicodeUserMessage, exception.getUserMessage());
    }

    @Test
    void testWithWhitespaceInMessage_handlesWhitespace() {
        // Arrange
        String messageWithWhitespace = "  Model training error with whitespace  ";
        
        // Act
        ModelTrainingException exception = new ModelTrainingException(messageWithWhitespace);
        
        // Assert
        assertEquals(messageWithWhitespace, exception.getMessage());
        assertEquals("MODEL_TRAINING_ERROR", exception.getErrorCode());
        assertEquals("Model training failed. Please check your data and try again.", exception.getUserMessage());
    }

    @Test
    void testWithNestedCause_handlesNestedCause() {
        // Arrange
        Throwable rootCause = new RuntimeException("Root cause");
        Throwable intermediateCause = new IllegalArgumentException("Intermediate cause", rootCause);
        Throwable topCause = new IllegalStateException("Top cause", intermediateCause);
        
        // Act
        ModelTrainingException exception = new ModelTrainingException("message", topCause);
        
        // Assert
        assertEquals("message", exception.getMessage());
        assertEquals("MODEL_TRAINING_ERROR", exception.getErrorCode());
        assertEquals("Model training failed. Please check your data and try again.", exception.getUserMessage());
        assertEquals(topCause, exception.getCause());
        assertEquals(intermediateCause, exception.getCause().getCause());
        assertEquals(rootCause, exception.getCause().getCause().getCause());
    }

    @Test
    void testToString_containsMessage() {
        // Arrange
        ModelTrainingException exception = new ModelTrainingException("Test message");
        
        // Act
        String toString = exception.toString();
        
        // Assert
        assertTrue(toString.contains("Test message"));
        assertTrue(toString.contains("ModelTrainingException"));
    }

    @Test
    void testWithCauseToString_containsCause() {
        // Arrange
        Throwable cause = new RuntimeException("Cause message");
        ModelTrainingException exception = new ModelTrainingException("Test message", cause);
        
        // Act
        String toString = exception.toString();
        
        // Assert
        assertTrue(toString.contains("Test message"));
        assertTrue(toString.contains("ModelTrainingException"));
    }

    @Test
    void testErrorCodeConsistency_alwaysReturnsSameErrorCode() {
        // Arrange
        ModelTrainingException exception1 = new ModelTrainingException("message1");
        ModelTrainingException exception2 = new ModelTrainingException("message2", new RuntimeException());
        ModelTrainingException exception3 = new ModelTrainingException("message3", "user message");
        
        // Act & Assert
        assertEquals("MODEL_TRAINING_ERROR", exception1.getErrorCode());
        assertEquals("MODEL_TRAINING_ERROR", exception2.getErrorCode());
        assertEquals("MODEL_TRAINING_ERROR", exception3.getErrorCode());
        assertEquals(exception1.getErrorCode(), exception2.getErrorCode());
        assertEquals(exception2.getErrorCode(), exception3.getErrorCode());
    }

    @Test
    void testUserMessageConsistency_messageOnly_alwaysReturnsSameUserMessage() {
        // Arrange
        ModelTrainingException exception1 = new ModelTrainingException("message1");
        ModelTrainingException exception2 = new ModelTrainingException("message2");
        
        // Act & Assert
        assertEquals("Model training failed. Please check your data and try again.", exception1.getUserMessage());
        assertEquals("Model training failed. Please check your data and try again.", exception2.getUserMessage());
        assertEquals(exception1.getUserMessage(), exception2.getUserMessage());
    }

    @Test
    void testUserMessageConsistency_messageWithCause_alwaysReturnsSameUserMessage() {
        // Arrange
        ModelTrainingException exception1 = new ModelTrainingException("message1", new RuntimeException());
        ModelTrainingException exception2 = new ModelTrainingException("message2", new IllegalArgumentException());
        
        // Act & Assert
        assertEquals("Model training failed. Please check your data and try again.", exception1.getUserMessage());
        assertEquals("Model training failed. Please check your data and try again.", exception2.getUserMessage());
        assertEquals(exception1.getUserMessage(), exception2.getUserMessage());
    }

    @Test
    void testUserMessageCustomization_messageWithUserMessage_returnsCustomUserMessage() {
        // Arrange
        ModelTrainingException exception = new ModelTrainingException("message", "Custom user message");
        
        // Act
        String userMessage = exception.getUserMessage();
        
        // Assert
        assertEquals("Custom user message", userMessage);
    }
}
