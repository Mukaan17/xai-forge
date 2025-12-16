package com.example.xaiapp.unit.exception;

import com.example.xaiapp.exception.ResourceExhaustedException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ResourceExhaustedException
 * Tests constructors, error codes, user messages, and message formatting
 */
class ResourceExhaustedExceptionTest {

    @Test
    void testConstructor_messageOnly_createsExceptionWithDefaults() {
        // Arrange
        String message = "System resources exhausted";
        
        // Act
        ResourceExhaustedException exception = new ResourceExhaustedException(message);
        
        // Assert
        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
        assertEquals("RESOURCE_EXHAUSTED", exception.getErrorCode());
        assertEquals("System resources are currently unavailable. Please try again later.", exception.getUserMessage());
        assertNull(exception.getCause());
    }

    @Test
    void testConstructor_resourceAndLimit_createsExceptionWithFormattedMessage() {
        // Arrange
        String resource = "memory";
        String limit = "512MB";
        
        // Act
        ResourceExhaustedException exception = new ResourceExhaustedException(resource, limit);
        
        // Assert
        assertNotNull(exception);
        assertEquals(resource + " limit exceeded: " + limit, exception.getMessage());
        assertEquals("RESOURCE_EXHAUSTED", exception.getErrorCode());
        assertEquals("The system has reached its " + resource + " limit. Please try again later or contact support.", exception.getUserMessage());
        assertNull(exception.getCause());
    }

    @Test
    void testInheritance_extendsXaiException() {
        // Arrange
        ResourceExhaustedException exception = new ResourceExhaustedException("message");
        
        // Act & Assert
        assertTrue(exception instanceof com.example.xaiapp.exception.XaiException);
    }

    @Test
    void testWithNullMessage_handlesNullMessage() {
        // Act
        ResourceExhaustedException exception = new ResourceExhaustedException(null);
        
        // Assert
        assertNull(exception.getMessage());
        assertEquals("RESOURCE_EXHAUSTED", exception.getErrorCode());
        assertEquals("System resources are currently unavailable. Please try again later.", exception.getUserMessage());
    }

    @Test
    void testWithEmptyMessage_handlesEmptyMessage() {
        // Act
        ResourceExhaustedException exception = new ResourceExhaustedException("");
        
        // Assert
        assertEquals("", exception.getMessage());
        assertEquals("RESOURCE_EXHAUSTED", exception.getErrorCode());
        assertEquals("System resources are currently unavailable. Please try again later.", exception.getUserMessage());
    }

    @Test
    void testWithNullResource_handlesNullResource() {
        // Act
        ResourceExhaustedException exception = new ResourceExhaustedException(null, "limit");
        
        // Assert
        assertEquals("null limit exceeded: limit", exception.getMessage());
        assertEquals("RESOURCE_EXHAUSTED", exception.getErrorCode());
        assertEquals("The system has reached its null limit. Please try again later or contact support.", exception.getUserMessage());
    }

    @Test
    void testWithNullLimit_handlesNullLimit() {
        // Act
        ResourceExhaustedException exception = new ResourceExhaustedException("resource", null);
        
        // Assert
        assertEquals("resource limit exceeded: null", exception.getMessage());
        assertEquals("RESOURCE_EXHAUSTED", exception.getErrorCode());
        assertEquals("The system has reached its resource limit. Please try again later or contact support.", exception.getUserMessage());
    }

    @Test
    void testWithEmptyResource_handlesEmptyResource() {
        // Act
        ResourceExhaustedException exception = new ResourceExhaustedException("", "limit");
        
        // Assert
        assertEquals(" limit exceeded: limit", exception.getMessage());
        assertEquals("RESOURCE_EXHAUSTED", exception.getErrorCode());
        assertEquals("The system has reached its  limit. Please try again later or contact support.", exception.getUserMessage());
    }

    @Test
    void testWithEmptyLimit_handlesEmptyLimit() {
        // Act
        ResourceExhaustedException exception = new ResourceExhaustedException("resource", "");
        
        // Assert
        assertEquals("resource limit exceeded: ", exception.getMessage());
        assertEquals("RESOURCE_EXHAUSTED", exception.getErrorCode());
        assertEquals("The system has reached its resource limit. Please try again later or contact support.", exception.getUserMessage());
    }

    @Test
    void testWithLongMessage_handlesLongMessage() {
        // Arrange
        String longMessage = "This is a very long resource exhausted error message that contains many characters and should be handled properly by the exception constructor";
        
        // Act
        ResourceExhaustedException exception = new ResourceExhaustedException(longMessage);
        
        // Assert
        assertEquals(longMessage, exception.getMessage());
        assertEquals("RESOURCE_EXHAUSTED", exception.getErrorCode());
        assertEquals("System resources are currently unavailable. Please try again later.", exception.getUserMessage());
    }

    @Test
    void testWithLongResourceAndLimit_handlesLongStrings() {
        // Arrange
        String longResource = "very_long_resource_name_with_many_characters_and_numbers_12345";
        String longLimit = "very_long_limit_value_with_many_characters_and_numbers_12345";
        
        // Act
        ResourceExhaustedException exception = new ResourceExhaustedException(longResource, longLimit);
        
        // Assert
        assertEquals(longResource + " limit exceeded: " + longLimit, exception.getMessage());
        assertEquals("RESOURCE_EXHAUSTED", exception.getErrorCode());
        assertEquals("The system has reached its " + longResource + " limit. Please try again later or contact support.", exception.getUserMessage());
    }

    @Test
    void testWithSpecialCharactersInMessage_handlesSpecialChars() {
        // Arrange
        String specialMessage = "Resource error with special chars: @#$%^&*()_+-=[]{}|;':\",./<>?";
        
        // Act
        ResourceExhaustedException exception = new ResourceExhaustedException(specialMessage);
        
        // Assert
        assertEquals(specialMessage, exception.getMessage());
        assertEquals("RESOURCE_EXHAUSTED", exception.getErrorCode());
        assertEquals("System resources are currently unavailable. Please try again later.", exception.getUserMessage());
    }

    @Test
    void testWithSpecialCharactersInResourceAndLimit_handlesSpecialChars() {
        // Arrange
        String specialResource = "resource@domain.com";
        String specialLimit = "limit#123";
        
        // Act
        ResourceExhaustedException exception = new ResourceExhaustedException(specialResource, specialLimit);
        
        // Assert
        assertEquals(specialResource + " limit exceeded: " + specialLimit, exception.getMessage());
        assertEquals("RESOURCE_EXHAUSTED", exception.getErrorCode());
        assertEquals("The system has reached its " + specialResource + " limit. Please try again later or contact support.", exception.getUserMessage());
    }

    @Test
    void testWithUnicodeCharacters_handlesUnicode() {
        // Arrange
        String unicodeMessage = "资源耗尽：系统资源不足";
        String unicodeResource = "内存";
        String unicodeLimit = "512MB";
        
        // Act
        ResourceExhaustedException exception1 = new ResourceExhaustedException(unicodeMessage);
        ResourceExhaustedException exception2 = new ResourceExhaustedException(unicodeResource, unicodeLimit);
        
        // Assert
        assertEquals(unicodeMessage, exception1.getMessage());
        assertEquals("RESOURCE_EXHAUSTED", exception1.getErrorCode());
        assertEquals("System resources are currently unavailable. Please try again later.", exception1.getUserMessage());
        
        assertEquals(unicodeResource + " limit exceeded: " + unicodeLimit, exception2.getMessage());
        assertEquals("RESOURCE_EXHAUSTED", exception2.getErrorCode());
        assertEquals("The system has reached its " + unicodeResource + " limit. Please try again later or contact support.", exception2.getUserMessage());
    }

    @Test
    void testWithWhitespaceInMessage_handlesWhitespace() {
        // Arrange
        String messageWithWhitespace = "  Resource error with whitespace  ";
        
        // Act
        ResourceExhaustedException exception = new ResourceExhaustedException(messageWithWhitespace);
        
        // Assert
        assertEquals(messageWithWhitespace, exception.getMessage());
        assertEquals("RESOURCE_EXHAUSTED", exception.getErrorCode());
        assertEquals("System resources are currently unavailable. Please try again later.", exception.getUserMessage());
    }

    @Test
    void testWithWhitespaceInResourceAndLimit_handlesWhitespace() {
        // Arrange
        String resourceWithWhitespace = " resource name ";
        String limitWithWhitespace = " limit value ";
        
        // Act
        ResourceExhaustedException exception = new ResourceExhaustedException(resourceWithWhitespace, limitWithWhitespace);
        
        // Assert
        assertEquals(resourceWithWhitespace + " limit exceeded: " + limitWithWhitespace, exception.getMessage());
        assertEquals("RESOURCE_EXHAUSTED", exception.getErrorCode());
        assertEquals("The system has reached its " + resourceWithWhitespace + " limit. Please try again later or contact support.", exception.getUserMessage());
    }

    @Test
    void testMessageFormatting_variousResourceLimitCombinations() {
        // Test different combinations
        String[][] testCases = {
            {"memory", "512MB"},
            {"disk", "1GB"},
            {"cpu", "80%"},
            {"connections", "100"},
            {"threads", "50"}
        };
        
        for (String[] testCase : testCases) {
            String resource = testCase[0];
            String limit = testCase[1];
            
            // Act
            ResourceExhaustedException exception = new ResourceExhaustedException(resource, limit);
            
            // Assert
            assertEquals(resource + " limit exceeded: " + limit, exception.getMessage());
            assertEquals("RESOURCE_EXHAUSTED", exception.getErrorCode());
            assertEquals("The system has reached its " + resource + " limit. Please try again later or contact support.", exception.getUserMessage());
        }
    }

    @Test
    void testToString_containsMessage() {
        // Arrange
        ResourceExhaustedException exception = new ResourceExhaustedException("Test message");
        
        // Act
        String toString = exception.toString();
        
        // Assert
        assertTrue(toString.contains("Test message"));
        assertTrue(toString.contains("ResourceExhaustedException"));
    }

    @Test
    void testWithResourceLimitToString_containsFormattedMessage() {
        // Arrange
        ResourceExhaustedException exception = new ResourceExhaustedException("memory", "512MB");
        
        // Act
        String toString = exception.toString();
        
        // Assert
        assertTrue(toString.contains("memory limit exceeded: 512MB"));
        assertTrue(toString.contains("ResourceExhaustedException"));
    }

    @Test
    void testErrorCodeConsistency_alwaysReturnsSameErrorCode() {
        // Arrange
        ResourceExhaustedException exception1 = new ResourceExhaustedException("message1");
        ResourceExhaustedException exception2 = new ResourceExhaustedException("resource", "limit");
        
        // Act & Assert
        assertEquals("RESOURCE_EXHAUSTED", exception1.getErrorCode());
        assertEquals("RESOURCE_EXHAUSTED", exception2.getErrorCode());
        assertEquals(exception1.getErrorCode(), exception2.getErrorCode());
    }

    @Test
    void testUserMessageConsistency_messageOnly_alwaysReturnsSameUserMessage() {
        // Arrange
        ResourceExhaustedException exception1 = new ResourceExhaustedException("message1");
        ResourceExhaustedException exception2 = new ResourceExhaustedException("message2");
        
        // Act & Assert
        assertEquals("System resources are currently unavailable. Please try again later.", exception1.getUserMessage());
        assertEquals("System resources are currently unavailable. Please try again later.", exception2.getUserMessage());
        assertEquals(exception1.getUserMessage(), exception2.getUserMessage());
    }

    @Test
    void testUserMessageFormatting_resourceLimit_returnsFormattedUserMessage() {
        // Arrange
        ResourceExhaustedException exception = new ResourceExhaustedException("memory", "512MB");
        
        // Act
        String userMessage = exception.getUserMessage();
        
        // Assert
        assertEquals("The system has reached its memory limit. Please try again later or contact support.", userMessage);
    }
}
