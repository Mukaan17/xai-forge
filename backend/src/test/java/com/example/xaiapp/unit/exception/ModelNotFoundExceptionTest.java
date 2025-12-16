package com.example.xaiapp.unit.exception;

import com.example.xaiapp.exception.ModelNotFoundException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ModelNotFoundException
 * Tests constructors with Long and String parameters, error codes, and user messages
 */
class ModelNotFoundExceptionTest {

    @Test
    void testConstructor_withLongId_createsExceptionWithFormattedMessage() {
        // Arrange
        Long modelId = 123L;
        
        // Act
        ModelNotFoundException exception = new ModelNotFoundException(modelId);
        
        // Assert
        assertNotNull(exception);
        assertEquals("Model with ID " + modelId + " not found", exception.getMessage());
        assertEquals("MODEL_NOT_FOUND", exception.getErrorCode());
        assertEquals("The requested model could not be found. It may have been deleted or you may not have permission to access it.", exception.getUserMessage());
        assertNull(exception.getCause());
    }

    @Test
    void testConstructor_withStringName_createsExceptionWithFormattedMessage() {
        // Arrange
        String modelName = "test-model";
        
        // Act
        ModelNotFoundException exception = new ModelNotFoundException(modelName);
        
        // Assert
        assertNotNull(exception);
        assertEquals("Model with name " + modelName + " not found", exception.getMessage());
        assertEquals("MODEL_NOT_FOUND", exception.getErrorCode());
        assertEquals("The requested model could not be found. It may have been deleted or you may not have permission to access it.", exception.getUserMessage());
        assertNull(exception.getCause());
    }

    @Test
    void testInheritance_extendsXaiException() {
        // Arrange
        ModelNotFoundException exception = new ModelNotFoundException(1L);
        
        // Act & Assert
        assertTrue(exception instanceof com.example.xaiapp.exception.XaiException);
    }

    @Test
    void testWithNullId_handlesNullId() {
        // Act
        ModelNotFoundException exception = new ModelNotFoundException((Long) null);
        
        // Assert
        assertEquals("Model with ID null not found", exception.getMessage());
        assertEquals("MODEL_NOT_FOUND", exception.getErrorCode());
        assertEquals("The requested model could not be found. It may have been deleted or you may not have permission to access it.", exception.getUserMessage());
    }

    @Test
    void testWithNullName_handlesNullName() {
        // Act
        ModelNotFoundException exception = new ModelNotFoundException((String) null);
        
        // Assert
        assertEquals("Model with name null not found", exception.getMessage());
        assertEquals("MODEL_NOT_FOUND", exception.getErrorCode());
        assertEquals("The requested model could not be found. It may have been deleted or you may not have permission to access it.", exception.getUserMessage());
    }

    @Test
    void testWithZeroId_handlesZeroId() {
        // Act
        ModelNotFoundException exception = new ModelNotFoundException(0L);
        
        // Assert
        assertEquals("Model with ID 0 not found", exception.getMessage());
        assertEquals("MODEL_NOT_FOUND", exception.getErrorCode());
        assertEquals("The requested model could not be found. It may have been deleted or you may not have permission to access it.", exception.getUserMessage());
    }

    @Test
    void testWithNegativeId_handlesNegativeId() {
        // Act
        ModelNotFoundException exception = new ModelNotFoundException(-1L);
        
        // Assert
        assertEquals("Model with ID -1 not found", exception.getMessage());
        assertEquals("MODEL_NOT_FOUND", exception.getErrorCode());
        assertEquals("The requested model could not be found. It may have been deleted or you may not have permission to access it.", exception.getUserMessage());
    }

    @Test
    void testWithMaxId_handlesMaxId() {
        // Act
        ModelNotFoundException exception = new ModelNotFoundException(Long.MAX_VALUE);
        
        // Assert
        assertEquals("Model with ID " + Long.MAX_VALUE + " not found", exception.getMessage());
        assertEquals("MODEL_NOT_FOUND", exception.getErrorCode());
        assertEquals("The requested model could not be found. It may have been deleted or you may not have permission to access it.", exception.getUserMessage());
    }

    @Test
    void testWithEmptyName_handlesEmptyName() {
        // Act
        ModelNotFoundException exception = new ModelNotFoundException("");
        
        // Assert
        assertEquals("Model with name  not found", exception.getMessage());
        assertEquals("MODEL_NOT_FOUND", exception.getErrorCode());
        assertEquals("The requested model could not be found. It may have been deleted or you may not have permission to access it.", exception.getUserMessage());
    }

    @Test
    void testWithWhitespaceName_handlesWhitespaceName() {
        // Act
        ModelNotFoundException exception = new ModelNotFoundException("   ");
        
        // Assert
        assertEquals("Model with name     not found", exception.getMessage());
        assertEquals("MODEL_NOT_FOUND", exception.getErrorCode());
        assertEquals("The requested model could not be found. It may have been deleted or you may not have permission to access it.", exception.getUserMessage());
    }

    @Test
    void testWithLongName_handlesLongName() {
        // Arrange
        String longName = "very_long_model_name_with_many_characters_and_numbers_12345";
        
        // Act
        ModelNotFoundException exception = new ModelNotFoundException(longName);
        
        // Assert
        assertEquals("Model with name " + longName + " not found", exception.getMessage());
        assertEquals("MODEL_NOT_FOUND", exception.getErrorCode());
        assertEquals("The requested model could not be found. It may have been deleted or you may not have permission to access it.", exception.getUserMessage());
    }

    @Test
    void testWithSpecialCharactersInName_handlesSpecialChars() {
        // Arrange
        String specialName = "model@domain.com";
        
        // Act
        ModelNotFoundException exception = new ModelNotFoundException(specialName);
        
        // Assert
        assertEquals("Model with name " + specialName + " not found", exception.getMessage());
        assertEquals("MODEL_NOT_FOUND", exception.getErrorCode());
        assertEquals("The requested model could not be found. It may have been deleted or you may not have permission to access it.", exception.getUserMessage());
    }

    @Test
    void testWithUnicodeCharacters_handlesUnicode() {
        // Arrange
        String unicodeName = "模型名称123";
        
        // Act
        ModelNotFoundException exception = new ModelNotFoundException(unicodeName);
        
        // Assert
        assertEquals("Model with name " + unicodeName + " not found", exception.getMessage());
        assertEquals("MODEL_NOT_FOUND", exception.getErrorCode());
        assertEquals("The requested model could not be found. It may have been deleted or you may not have permission to access it.", exception.getUserMessage());
    }

    @Test
    void testMessageFormatting_variousIdValues() {
        // Test different ID values
        Long[] testIds = {1L, 100L, 999L, 1000L, Long.MAX_VALUE};
        
        for (Long id : testIds) {
            // Act
            ModelNotFoundException exception = new ModelNotFoundException(id);
            
            // Assert
            assertEquals("Model with ID " + id + " not found", exception.getMessage());
            assertEquals("MODEL_NOT_FOUND", exception.getErrorCode());
            assertEquals("The requested model could not be found. It may have been deleted or you may not have permission to access it.", exception.getUserMessage());
        }
    }

    @Test
    void testMessageFormatting_variousNameValues() {
        // Test different name values
        String[] testNames = {"model1", "test-model", "model_123", "model@domain.com"};
        
        for (String name : testNames) {
            // Act
            ModelNotFoundException exception = new ModelNotFoundException(name);
            
            // Assert
            assertEquals("Model with name " + name + " not found", exception.getMessage());
            assertEquals("MODEL_NOT_FOUND", exception.getErrorCode());
            assertEquals("The requested model could not be found. It may have been deleted or you may not have permission to access it.", exception.getUserMessage());
        }
    }

    @Test
    void testToString_containsMessage() {
        // Arrange
        ModelNotFoundException exception = new ModelNotFoundException(123L);
        
        // Act
        String toString = exception.toString();
        
        // Assert
        assertTrue(toString.contains("Model with ID 123 not found"));
        assertTrue(toString.contains("ModelNotFoundException"));
    }

    @Test
    void testWithNameToString_containsFormattedMessage() {
        // Arrange
        ModelNotFoundException exception = new ModelNotFoundException("test-model");
        
        // Act
        String toString = exception.toString();
        
        // Assert
        assertTrue(toString.contains("Model with name test-model not found"));
        assertTrue(toString.contains("ModelNotFoundException"));
    }

    @Test
    void testErrorCodeConsistency_alwaysReturnsSameErrorCode() {
        // Arrange
        ModelNotFoundException exception1 = new ModelNotFoundException(1L);
        ModelNotFoundException exception2 = new ModelNotFoundException("model-name");
        
        // Act & Assert
        assertEquals("MODEL_NOT_FOUND", exception1.getErrorCode());
        assertEquals("MODEL_NOT_FOUND", exception2.getErrorCode());
        assertEquals(exception1.getErrorCode(), exception2.getErrorCode());
    }

    @Test
    void testUserMessageConsistency_alwaysReturnsSameUserMessage() {
        // Arrange
        ModelNotFoundException exception1 = new ModelNotFoundException(1L);
        ModelNotFoundException exception2 = new ModelNotFoundException("model-name");
        
        // Act & Assert
        assertEquals("The requested model could not be found. It may have been deleted or you may not have permission to access it.", exception1.getUserMessage());
        assertEquals("The requested model could not be found. It may have been deleted or you may not have permission to access it.", exception2.getUserMessage());
        assertEquals(exception1.getUserMessage(), exception2.getUserMessage());
    }

    @Test
    void testWithWhitespaceInName_handlesWhitespace() {
        // Arrange
        String nameWithWhitespace = " model name ";
        
        // Act
        ModelNotFoundException exception = new ModelNotFoundException(nameWithWhitespace);
        
        // Assert
        assertEquals("Model with name " + nameWithWhitespace + " not found", exception.getMessage());
        assertEquals("MODEL_NOT_FOUND", exception.getErrorCode());
        assertEquals("The requested model could not be found. It may have been deleted or you may not have permission to access it.", exception.getUserMessage());
    }

    @Test
    void testWithNumericStringName_handlesNumericString() {
        // Arrange
        String numericName = "12345";
        
        // Act
        ModelNotFoundException exception = new ModelNotFoundException(numericName);
        
        // Assert
        assertEquals("Model with name " + numericName + " not found", exception.getMessage());
        assertEquals("MODEL_NOT_FOUND", exception.getErrorCode());
        assertEquals("The requested model could not be found. It may have been deleted or you may not have permission to access it.", exception.getUserMessage());
    }
}
