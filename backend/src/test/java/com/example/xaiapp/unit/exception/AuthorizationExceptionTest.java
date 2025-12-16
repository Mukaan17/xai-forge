package com.example.xaiapp.unit.exception;

import com.example.xaiapp.exception.AuthorizationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for AuthorizationException
 * Tests constructors, error codes, user messages, and message formatting
 */
class AuthorizationExceptionTest {

    @Test
    void testConstructor_messageOnly_createsExceptionWithDefaults() {
        // Arrange
        String message = "Access denied";
        
        // Act
        AuthorizationException exception = new AuthorizationException(message);
        
        // Assert
        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
        assertEquals("AUTHORIZATION_ERROR", exception.getErrorCode());
        assertEquals("You don't have permission to access this resource.", exception.getUserMessage());
        assertNull(exception.getCause());
    }

    @Test
    void testConstructor_resourceAndAction_createsExceptionWithFormattedMessage() {
        // Arrange
        String resource = "dataset";
        String action = "delete";
        
        // Act
        AuthorizationException exception = new AuthorizationException(resource, action);
        
        // Assert
        assertNotNull(exception);
        assertEquals("User not authorized to " + action + " " + resource, exception.getMessage());
        assertEquals("AUTHORIZATION_ERROR", exception.getErrorCode());
        assertEquals("You don't have permission to " + action + " this " + resource + ".", exception.getUserMessage());
        assertNull(exception.getCause());
    }

    @Test
    void testInheritance_extendsXaiException() {
        // Arrange
        AuthorizationException exception = new AuthorizationException("message");
        
        // Act & Assert
        assertTrue(exception instanceof com.example.xaiapp.exception.XaiException);
    }

    @Test
    void testWithNullMessage_handlesNullMessage() {
        // Act
        AuthorizationException exception = new AuthorizationException(null);
        
        // Assert
        assertNull(exception.getMessage());
        assertEquals("AUTHORIZATION_ERROR", exception.getErrorCode());
        assertEquals("You don't have permission to access this resource.", exception.getUserMessage());
    }

    @Test
    void testWithEmptyMessage_handlesEmptyMessage() {
        // Act
        AuthorizationException exception = new AuthorizationException("");
        
        // Assert
        assertEquals("", exception.getMessage());
        assertEquals("AUTHORIZATION_ERROR", exception.getErrorCode());
        assertEquals("You don't have permission to access this resource.", exception.getUserMessage());
    }

    @Test
    void testWithNullResource_handlesNullResource() {
        // Act
        AuthorizationException exception = new AuthorizationException(null, "action");
        
        // Assert
        assertEquals("User not authorized to action null", exception.getMessage());
        assertEquals("AUTHORIZATION_ERROR", exception.getErrorCode());
        assertEquals("You don't have permission to action this null.", exception.getUserMessage());
    }

    @Test
    void testWithNullAction_handlesNullAction() {
        // Act
        AuthorizationException exception = new AuthorizationException("resource", null);
        
        // Assert
        assertEquals("User not authorized to null resource", exception.getMessage());
        assertEquals("AUTHORIZATION_ERROR", exception.getErrorCode());
        assertEquals("You don't have permission to null this resource.", exception.getUserMessage());
    }

    @Test
    void testWithEmptyResource_handlesEmptyResource() {
        // Act
        AuthorizationException exception = new AuthorizationException("", "action");
        
        // Assert
        assertEquals("User not authorized to action ", exception.getMessage());
        assertEquals("AUTHORIZATION_ERROR", exception.getErrorCode());
        assertEquals("You don't have permission to action this .", exception.getUserMessage());
    }

    @Test
    void testWithEmptyAction_handlesEmptyAction() {
        // Act
        AuthorizationException exception = new AuthorizationException("resource", "");
        
        // Assert
        assertEquals("User not authorized to  resource", exception.getMessage());
        assertEquals("AUTHORIZATION_ERROR", exception.getErrorCode());
        assertEquals("You don't have permission to  this resource.", exception.getUserMessage());
    }

    @Test
    void testWithLongMessage_handlesLongMessage() {
        // Arrange
        String longMessage = "This is a very long authorization error message that contains many characters and should be handled properly by the exception constructor";
        
        // Act
        AuthorizationException exception = new AuthorizationException(longMessage);
        
        // Assert
        assertEquals(longMessage, exception.getMessage());
        assertEquals("AUTHORIZATION_ERROR", exception.getErrorCode());
        assertEquals("You don't have permission to access this resource.", exception.getUserMessage());
    }

    @Test
    void testWithLongResourceAndAction_handlesLongStrings() {
        // Arrange
        String longResource = "very_long_resource_name_with_many_characters_and_numbers_12345";
        String longAction = "very_long_action_name_with_many_characters_and_numbers_12345";
        
        // Act
        AuthorizationException exception = new AuthorizationException(longResource, longAction);
        
        // Assert
        assertEquals("User not authorized to " + longAction + " " + longResource, exception.getMessage());
        assertEquals("AUTHORIZATION_ERROR", exception.getErrorCode());
        assertEquals("You don't have permission to " + longAction + " this " + longResource + ".", exception.getUserMessage());
    }

    @Test
    void testWithSpecialCharactersInMessage_handlesSpecialChars() {
        // Arrange
        String specialMessage = "Auth error with special chars: @#$%^&*()_+-=[]{}|;':\",./<>?";
        
        // Act
        AuthorizationException exception = new AuthorizationException(specialMessage);
        
        // Assert
        assertEquals(specialMessage, exception.getMessage());
        assertEquals("AUTHORIZATION_ERROR", exception.getErrorCode());
        assertEquals("You don't have permission to access this resource.", exception.getUserMessage());
    }

    @Test
    void testWithSpecialCharactersInResourceAndAction_handlesSpecialChars() {
        // Arrange
        String specialResource = "resource@domain.com";
        String specialAction = "action#123";
        
        // Act
        AuthorizationException exception = new AuthorizationException(specialResource, specialAction);
        
        // Assert
        assertEquals("User not authorized to " + specialAction + " " + specialResource, exception.getMessage());
        assertEquals("AUTHORIZATION_ERROR", exception.getErrorCode());
        assertEquals("You don't have permission to " + specialAction + " this " + specialResource + ".", exception.getUserMessage());
    }

    @Test
    void testWithUnicodeCharacters_handlesUnicode() {
        // Arrange
        String unicodeMessage = "授权失败：用户没有权限";
        String unicodeResource = "数据集";
        String unicodeAction = "删除";
        
        // Act
        AuthorizationException exception1 = new AuthorizationException(unicodeMessage);
        AuthorizationException exception2 = new AuthorizationException(unicodeResource, unicodeAction);
        
        // Assert
        assertEquals(unicodeMessage, exception1.getMessage());
        assertEquals("AUTHORIZATION_ERROR", exception1.getErrorCode());
        assertEquals("You don't have permission to access this resource.", exception1.getUserMessage());
        
        assertEquals("User not authorized to " + unicodeAction + " " + unicodeResource, exception2.getMessage());
        assertEquals("AUTHORIZATION_ERROR", exception2.getErrorCode());
        assertEquals("You don't have permission to " + unicodeAction + " this " + unicodeResource + ".", exception2.getUserMessage());
    }

    @Test
    void testWithWhitespaceInMessage_handlesWhitespace() {
        // Arrange
        String messageWithWhitespace = "  Authorization error with whitespace  ";
        
        // Act
        AuthorizationException exception = new AuthorizationException(messageWithWhitespace);
        
        // Assert
        assertEquals(messageWithWhitespace, exception.getMessage());
        assertEquals("AUTHORIZATION_ERROR", exception.getErrorCode());
        assertEquals("You don't have permission to access this resource.", exception.getUserMessage());
    }

    @Test
    void testWithWhitespaceInResourceAndAction_handlesWhitespace() {
        // Arrange
        String resourceWithWhitespace = " resource name ";
        String actionWithWhitespace = " action name ";
        
        // Act
        AuthorizationException exception = new AuthorizationException(resourceWithWhitespace, actionWithWhitespace);
        
        // Assert
        assertEquals("User not authorized to " + actionWithWhitespace + " " + resourceWithWhitespace, exception.getMessage());
        assertEquals("AUTHORIZATION_ERROR", exception.getErrorCode());
        assertEquals("You don't have permission to " + actionWithWhitespace + " this " + resourceWithWhitespace + ".", exception.getUserMessage());
    }

    @Test
    void testMessageFormatting_variousResourceActionCombinations() {
        // Test different combinations
        String[][] testCases = {
            {"dataset", "read"},
            {"model", "write"},
            {"user", "update"},
            {"file", "download"},
            {"report", "generate"}
        };
        
        for (String[] testCase : testCases) {
            String resource = testCase[0];
            String action = testCase[1];
            
            // Act
            AuthorizationException exception = new AuthorizationException(resource, action);
            
            // Assert
            assertEquals("User not authorized to " + action + " " + resource, exception.getMessage());
            assertEquals("AUTHORIZATION_ERROR", exception.getErrorCode());
            assertEquals("You don't have permission to " + action + " this " + resource + ".", exception.getUserMessage());
        }
    }

    @Test
    void testToString_containsMessage() {
        // Arrange
        AuthorizationException exception = new AuthorizationException("Test message");
        
        // Act
        String toString = exception.toString();
        
        // Assert
        assertTrue(toString.contains("Test message"));
        assertTrue(toString.contains("AuthorizationException"));
    }

    @Test
    void testWithResourceActionToString_containsFormattedMessage() {
        // Arrange
        AuthorizationException exception = new AuthorizationException("dataset", "delete");
        
        // Act
        String toString = exception.toString();
        
        // Assert
        assertTrue(toString.contains("User not authorized to delete dataset"));
        assertTrue(toString.contains("AuthorizationException"));
    }

    @Test
    void testErrorCodeConsistency_alwaysReturnsSameErrorCode() {
        // Arrange
        AuthorizationException exception1 = new AuthorizationException("message1");
        AuthorizationException exception2 = new AuthorizationException("resource", "action");
        
        // Act & Assert
        assertEquals("AUTHORIZATION_ERROR", exception1.getErrorCode());
        assertEquals("AUTHORIZATION_ERROR", exception2.getErrorCode());
        assertEquals(exception1.getErrorCode(), exception2.getErrorCode());
    }

    @Test
    void testUserMessageConsistency_messageOnly_alwaysReturnsSameUserMessage() {
        // Arrange
        AuthorizationException exception1 = new AuthorizationException("message1");
        AuthorizationException exception2 = new AuthorizationException("message2");
        
        // Act & Assert
        assertEquals("You don't have permission to access this resource.", exception1.getUserMessage());
        assertEquals("You don't have permission to access this resource.", exception2.getUserMessage());
        assertEquals(exception1.getUserMessage(), exception2.getUserMessage());
    }

    @Test
    void testUserMessageFormatting_resourceAction_returnsFormattedUserMessage() {
        // Arrange
        AuthorizationException exception = new AuthorizationException("dataset", "delete");
        
        // Act
        String userMessage = exception.getUserMessage();
        
        // Assert
        assertEquals("You don't have permission to delete this dataset.", userMessage);
    }
}
