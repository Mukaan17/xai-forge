package com.example.xaiapp.unit.dto;

import com.example.xaiapp.dto.ApiResponse;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ApiResponse DTO
 * Tests constructors, Lombok methods, static factory methods, and generics
 */
class ApiResponseTest {

    @Test
    void testNoArgsConstructor_createsEmptyResponse() {
        // Act
        ApiResponse<String> response = new ApiResponse<>();
        
        // Assert
        assertNotNull(response);
        assertFalse(response.isSuccess());
        assertNull(response.getMessage());
        assertNull(response.getData());
    }

    @Test
    void testAllArgsConstructor_createsCompleteResponse() {
        // Arrange
        boolean success = true;
        String message = "Test message";
        String data = "Test data";
        
        // Act
        ApiResponse<String> response = new ApiResponse<>(success, message, data);
        
        // Assert
        assertNotNull(response);
        assertEquals(success, response.isSuccess());
        assertEquals(message, response.getMessage());
        assertEquals(data, response.getData());
    }

    @Test
    void testSetters_setAllFields() {
        // Arrange
        ApiResponse<String> response = new ApiResponse<>();
        boolean success = false;
        String message = "Error message";
        String data = "Error data";
        
        // Act
        response.setSuccess(success);
        response.setMessage(message);
        response.setData(data);
        
        // Assert
        assertEquals(success, response.isSuccess());
        assertEquals(message, response.getMessage());
        assertEquals(data, response.getData());
    }

    @Test
    void testEquals_sameObject_returnsTrue() {
        // Arrange
        ApiResponse<String> response = new ApiResponse<>(true, "test", "data");
        
        // Act & Assert
        assertEquals(response, response);
    }

    @Test
    void testEquals_equalObjects_returnsTrue() {
        // Arrange
        ApiResponse<String> response1 = new ApiResponse<>(true, "test", "data");
        ApiResponse<String> response2 = new ApiResponse<>(true, "test", "data");
        
        // Act & Assert
        assertEquals(response1, response2);
    }

    @Test
    void testEquals_differentObjects_returnsFalse() {
        // Arrange
        ApiResponse<String> response1 = new ApiResponse<>(true, "test", "data");
        ApiResponse<String> response2 = new ApiResponse<>(false, "test", "data");
        
        // Act & Assert
        assertNotEquals(response1, response2);
    }

    @Test
    void testEquals_nullObject_returnsFalse() {
        // Arrange
        ApiResponse<String> response = new ApiResponse<>(true, "test", "data");
        
        // Act & Assert
        assertNotEquals(response, null);
    }

    @Test
    void testEquals_differentClass_returnsFalse() {
        // Arrange
        ApiResponse<String> response = new ApiResponse<>(true, "test", "data");
        String other = "not a response";
        
        // Act & Assert
        assertNotEquals(response, other);
    }

    @Test
    void testHashCode_equalObjects_sameHashCode() {
        // Arrange
        ApiResponse<String> response1 = new ApiResponse<>(true, "test", "data");
        ApiResponse<String> response2 = new ApiResponse<>(true, "test", "data");
        
        // Act & Assert
        assertEquals(response1.hashCode(), response2.hashCode());
    }

    @Test
    void testToString_containsAllFields() {
        // Arrange
        ApiResponse<String> response = new ApiResponse<>(true, "test message", "test data");
        
        // Act
        String toString = response.toString();
        
        // Assert
        assertTrue(toString.contains("true"));
        assertTrue(toString.contains("test message"));
        assertTrue(toString.contains("test data"));
    }

    @Test
    void testSuccessFactoryMethod_messageOnly_createsSuccessResponse() {
        // Act
        ApiResponse<String> response = ApiResponse.success("Operation successful");
        
        // Assert
        assertTrue(response.isSuccess());
        assertEquals("Operation successful", response.getMessage());
        assertNull(response.getData());
    }

    @Test
    void testSuccessFactoryMethod_withData_createsSuccessResponseWithData() {
        // Arrange
        String testData = "Test result";
        
        // Act
        ApiResponse<String> response = ApiResponse.success("Operation successful", testData);
        
        // Assert
        assertTrue(response.isSuccess());
        assertEquals("Operation successful", response.getMessage());
        assertEquals(testData, response.getData());
    }

    @Test
    void testErrorFactoryMethod_messageOnly_createsErrorResponse() {
        // Act
        ApiResponse<String> response = ApiResponse.error("Operation failed");
        
        // Assert
        assertFalse(response.isSuccess());
        assertEquals("Operation failed", response.getMessage());
        assertNull(response.getData());
    }

    @Test
    void testErrorFactoryMethod_withData_createsErrorResponseWithData() {
        // Arrange
        String errorData = "Error details";
        
        // Act
        ApiResponse<String> response = ApiResponse.error("Operation failed", errorData);
        
        // Assert
        assertFalse(response.isSuccess());
        assertEquals("Operation failed", response.getMessage());
        assertEquals(errorData, response.getData());
    }

    @Test
    void testGenericTypeHandling_withIntegerData() {
        // Act
        ApiResponse<Integer> response = ApiResponse.success("Count retrieved", 42);
        
        // Assert
        assertTrue(response.isSuccess());
        assertEquals("Count retrieved", response.getMessage());
        assertEquals(Integer.valueOf(42), response.getData());
    }

    @Test
    void testGenericTypeHandling_withNullData() {
        // Act
        ApiResponse<String> response = ApiResponse.success("No data", null);
        
        // Assert
        assertTrue(response.isSuccess());
        assertEquals("No data", response.getMessage());
        assertNull(response.getData());
    }

    @Test
    void testGenericTypeHandling_withComplexObject() {
        // Arrange
        TestObject testObject = new TestObject("test", 123);
        
        // Act
        ApiResponse<TestObject> response = ApiResponse.success("Object retrieved", testObject);
        
        // Assert
        assertTrue(response.isSuccess());
        assertEquals("Object retrieved", response.getMessage());
        assertEquals(testObject, response.getData());
    }

    // Helper class for testing
    private static class TestObject {
        private String name;
        private int value;
        
        public TestObject(String name, int value) {
            this.name = name;
            this.value = value;
        }
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            TestObject that = (TestObject) obj;
            return value == that.value && java.util.Objects.equals(name, that.name);
        }
        
        @Override
        public int hashCode() {
            return java.util.Objects.hash(name, value);
        }
    }
}
