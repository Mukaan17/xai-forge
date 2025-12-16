package com.example.xaiapp.unit.exception;

import com.example.xaiapp.exception.DatasetException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for DatasetException
 * Tests constructors, error codes, user messages, and exception chaining
 */
class DatasetExceptionTest {

    @Test
    void testConstructor_messageOnly_createsExceptionWithDefaults() {
        // Arrange
        String message = "Dataset processing failed";
        
        // Act
        DatasetException exception = new DatasetException(message);
        
        // Assert
        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
        assertEquals("DATASET_ERROR", exception.getErrorCode());
        assertEquals("There was an error processing your dataset. Please check the file format and try again.", exception.getUserMessage());
        assertNull(exception.getCause());
    }

    @Test
    void testConstructor_messageWithCause_createsExceptionWithCause() {
        // Arrange
        String message = "Dataset processing failed";
        Throwable cause = new RuntimeException("Root cause");
        
        // Act
        DatasetException exception = new DatasetException(message, cause);
        
        // Assert
        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
        assertEquals("DATASET_ERROR", exception.getErrorCode());
        assertEquals("There was an error processing your dataset. Please check the file format and try again.", exception.getUserMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void testConstructor_messageWithUserMessage_createsExceptionWithCustomUserMessage() {
        // Arrange
        String message = "Dataset processing failed";
        String userMessage = "Custom user message";
        
        // Act
        DatasetException exception = new DatasetException(message, userMessage);
        
        // Assert
        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
        assertEquals("DATASET_ERROR", exception.getErrorCode());
        assertEquals(userMessage, exception.getUserMessage());
        assertNull(exception.getCause());
    }

    @Test
    void testInheritance_extendsXaiException() {
        // Arrange
        DatasetException exception = new DatasetException("message");
        
        // Act & Assert
        assertTrue(exception instanceof com.example.xaiapp.exception.XaiException);
    }

    @Test
    void testWithNullMessage_handlesNullMessage() {
        // Act
        DatasetException exception = new DatasetException(null);
        
        // Assert
        assertNull(exception.getMessage());
        assertEquals("DATASET_ERROR", exception.getErrorCode());
        assertEquals("There was an error processing your dataset. Please check the file format and try again.", exception.getUserMessage());
    }

    @Test
    void testWithEmptyMessage_handlesEmptyMessage() {
        // Act
        DatasetException exception = new DatasetException("");
        
        // Assert
        assertEquals("", exception.getMessage());
        assertEquals("DATASET_ERROR", exception.getErrorCode());
        assertEquals("There was an error processing your dataset. Please check the file format and try again.", exception.getUserMessage());
    }

    @Test
    void testWithNullCause_handlesNullCause() {
        // Act
        DatasetException exception = new DatasetException("message", (Throwable) null);
        
        // Assert
        assertEquals("message", exception.getMessage());
        assertEquals("DATASET_ERROR", exception.getErrorCode());
        assertEquals("There was an error processing your dataset. Please check the file format and try again.", exception.getUserMessage());
        assertNull(exception.getCause());
    }

    @Test
    void testWithNullUserMessage_handlesNullUserMessage() {
        // Act
        DatasetException exception = new DatasetException("message", (String) null);
        
        // Assert
        assertEquals("message", exception.getMessage());
        assertEquals("DATASET_ERROR", exception.getErrorCode());
        assertNull(exception.getUserMessage());
    }

    @Test
    void testWithEmptyUserMessage_handlesEmptyUserMessage() {
        // Act
        DatasetException exception = new DatasetException("message", "");
        
        // Assert
        assertEquals("message", exception.getMessage());
        assertEquals("DATASET_ERROR", exception.getErrorCode());
        assertEquals("", exception.getUserMessage());
    }

    @Test
    void testWithLongMessage_handlesLongMessage() {
        // Arrange
        String longMessage = "This is a very long dataset error message that contains many characters and should be handled properly by the exception constructor";
        
        // Act
        DatasetException exception = new DatasetException(longMessage);
        
        // Assert
        assertEquals(longMessage, exception.getMessage());
        assertEquals("DATASET_ERROR", exception.getErrorCode());
        assertEquals("There was an error processing your dataset. Please check the file format and try again.", exception.getUserMessage());
    }

    @Test
    void testWithLongUserMessage_handlesLongUserMessage() {
        // Arrange
        String longUserMessage = "This is a very long user-friendly message that explains the dataset error in detail and provides guidance to the user";
        
        // Act
        DatasetException exception = new DatasetException("message", longUserMessage);
        
        // Assert
        assertEquals("message", exception.getMessage());
        assertEquals("DATASET_ERROR", exception.getErrorCode());
        assertEquals(longUserMessage, exception.getUserMessage());
    }

    @Test
    void testWithSpecialCharactersInMessage_handlesSpecialChars() {
        // Arrange
        String specialMessage = "Dataset error with special chars: @#$%^&*()_+-=[]{}|;':\",./<>?";
        
        // Act
        DatasetException exception = new DatasetException(specialMessage);
        
        // Assert
        assertEquals(specialMessage, exception.getMessage());
        assertEquals("DATASET_ERROR", exception.getErrorCode());
        assertEquals("There was an error processing your dataset. Please check the file format and try again.", exception.getUserMessage());
    }

    @Test
    void testWithUnicodeCharacters_handlesUnicode() {
        // Arrange
        String unicodeMessage = "数据集错误：文件格式无效";
        String unicodeUserMessage = "请检查文件格式并重试";
        
        // Act
        DatasetException exception = new DatasetException(unicodeMessage, unicodeUserMessage);
        
        // Assert
        assertEquals(unicodeMessage, exception.getMessage());
        assertEquals("DATASET_ERROR", exception.getErrorCode());
        assertEquals(unicodeUserMessage, exception.getUserMessage());
    }

    @Test
    void testWithWhitespaceInMessage_handlesWhitespace() {
        // Arrange
        String messageWithWhitespace = "  Dataset error with whitespace  ";
        
        // Act
        DatasetException exception = new DatasetException(messageWithWhitespace);
        
        // Assert
        assertEquals(messageWithWhitespace, exception.getMessage());
        assertEquals("DATASET_ERROR", exception.getErrorCode());
        assertEquals("There was an error processing your dataset. Please check the file format and try again.", exception.getUserMessage());
    }

    @Test
    void testWithNestedCause_handlesNestedCause() {
        // Arrange
        Throwable rootCause = new RuntimeException("Root cause");
        Throwable intermediateCause = new IllegalArgumentException("Intermediate cause", rootCause);
        Throwable topCause = new IllegalStateException("Top cause", intermediateCause);
        
        // Act
        DatasetException exception = new DatasetException("message", topCause);
        
        // Assert
        assertEquals("message", exception.getMessage());
        assertEquals("DATASET_ERROR", exception.getErrorCode());
        assertEquals("There was an error processing your dataset. Please check the file format and try again.", exception.getUserMessage());
        assertEquals(topCause, exception.getCause());
        assertEquals(intermediateCause, exception.getCause().getCause());
        assertEquals(rootCause, exception.getCause().getCause().getCause());
    }

    @Test
    void testToString_containsMessage() {
        // Arrange
        DatasetException exception = new DatasetException("Test message");
        
        // Act
        String toString = exception.toString();
        
        // Assert
        assertTrue(toString.contains("Test message"));
        assertTrue(toString.contains("DatasetException"));
    }

    @Test
    void testWithCauseToString_containsCause() {
        // Arrange
        Throwable cause = new RuntimeException("Cause message");
        DatasetException exception = new DatasetException("Test message", cause);
        
        // Act
        String toString = exception.toString();
        
        // Assert
        assertTrue(toString.contains("Test message"));
        assertTrue(toString.contains("DatasetException"));
    }

    @Test
    void testErrorCodeConsistency_alwaysReturnsSameErrorCode() {
        // Arrange
        DatasetException exception1 = new DatasetException("message1");
        DatasetException exception2 = new DatasetException("message2", new RuntimeException());
        DatasetException exception3 = new DatasetException("message3", "user message");
        
        // Act & Assert
        assertEquals("DATASET_ERROR", exception1.getErrorCode());
        assertEquals("DATASET_ERROR", exception2.getErrorCode());
        assertEquals("DATASET_ERROR", exception3.getErrorCode());
        assertEquals(exception1.getErrorCode(), exception2.getErrorCode());
        assertEquals(exception2.getErrorCode(), exception3.getErrorCode());
    }

    @Test
    void testUserMessageConsistency_messageOnly_alwaysReturnsSameUserMessage() {
        // Arrange
        DatasetException exception1 = new DatasetException("message1");
        DatasetException exception2 = new DatasetException("message2");
        
        // Act & Assert
        assertEquals("There was an error processing your dataset. Please check the file format and try again.", exception1.getUserMessage());
        assertEquals("There was an error processing your dataset. Please check the file format and try again.", exception2.getUserMessage());
        assertEquals(exception1.getUserMessage(), exception2.getUserMessage());
    }

    @Test
    void testUserMessageConsistency_messageWithCause_alwaysReturnsSameUserMessage() {
        // Arrange
        DatasetException exception1 = new DatasetException("message1", new RuntimeException());
        DatasetException exception2 = new DatasetException("message2", new IllegalArgumentException());
        
        // Act & Assert
        assertEquals("There was an error processing your dataset. Please check the file format and try again.", exception1.getUserMessage());
        assertEquals("There was an error processing your dataset. Please check the file format and try again.", exception2.getUserMessage());
        assertEquals(exception1.getUserMessage(), exception2.getUserMessage());
    }

    @Test
    void testUserMessageCustomization_messageWithUserMessage_returnsCustomUserMessage() {
        // Arrange
        DatasetException exception = new DatasetException("message", "Custom user message");
        
        // Act
        String userMessage = exception.getUserMessage();
        
        // Assert
        assertEquals("Custom user message", userMessage);
    }
}
