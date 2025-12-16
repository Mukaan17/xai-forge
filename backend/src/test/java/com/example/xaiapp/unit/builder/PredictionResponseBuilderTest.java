package com.example.xaiapp.unit.builder;

import com.example.xaiapp.builder.PredictionResponseBuilder;
import com.example.xaiapp.dto.PredictionResponse;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for PredictionResponseBuilder
 * Tests fluent builder API, validation, and method chaining
 */
class PredictionResponseBuilderTest {
    
    @Test
    void testBuildCompleteResponse() {
        // Arrange
        String prediction = "True";
        Map<String, Object> confidenceScores = new HashMap<>();
        confidenceScores.put("True", 0.85);
        confidenceScores.put("False", 0.15);
        Map<String, String> inputData = new HashMap<>();
        inputData.put("feature1", "1.5");
        inputData.put("feature2", "2.3");
        
        // Act
        PredictionResponse response = PredictionResponseBuilder.builder()
            .setPrediction(prediction)
            .setProbabilities(confidenceScores)
            .setInputData(inputData)
            .build();
        
        // Assert
        assertNotNull(response);
        assertEquals(prediction, response.getPrediction());
        assertEquals(confidenceScores, response.getProbabilities());
        assertEquals(inputData, response.getInputData());
    }
    
    @Test
    void testBuildMinimalResponse() {
        // Arrange
        String prediction = "False";
        
        // Act
        PredictionResponse response = PredictionResponseBuilder.builder()
            .setPrediction(prediction)
            .setConfidence(0.5) // Add required confidence
            .setInputData(new HashMap<>()) // Add required input data
            .build();
        
        // Assert
        assertNotNull(response);
        assertEquals(prediction, response.getPrediction());
        assertNotNull(response.getProbabilities());
        assertNotNull(response.getInputData());
    }
    
    @Test
    void testBuildWithNullValues() {
        // Act & Assert - This should throw an exception due to validation
        assertThrows(IllegalArgumentException.class, () -> {
            PredictionResponseBuilder.builder()
                .setPrediction(null)
                .setConfidence(0.5)
                .setInputData(new HashMap<>())
                .build();
        });
        
        // Test passes if exception is thrown
    }
    
    @Test
    void testMethodChaining() {
        // Act
        PredictionResponseBuilder builder = PredictionResponseBuilder.builder()
            .setPrediction("Test")
            .setProbabilities(new HashMap<>())
            .setInputData(new HashMap<>());
        
        // Assert
        assertNotNull(builder);
        // Verify that all methods return the builder instance for chaining
        assertSame(builder, builder.setPrediction("Test2"));
        assertSame(builder, builder.setProbabilities(new HashMap<>()));
        assertSame(builder, builder.setInputData(new HashMap<>()));
    }
    
    @Test
    void testBuildWithEmptyMaps() {
        // Arrange
        Map<String, Object> emptyConfidence = new HashMap<>();
        Map<String, String> emptyInput = new HashMap<>();
        
        // Act
        PredictionResponse response = PredictionResponseBuilder.builder()
            .setPrediction("Test")
            .setProbabilities(emptyConfidence)
            .setInputData(emptyInput)
            .setConfidence(0.5) // Add required confidence
            .build();
        
        // Assert
        assertNotNull(response);
        assertEquals("Test", response.getPrediction());
        assertNotNull(response.getProbabilities());
        assertTrue(response.getProbabilities().isEmpty());
        assertNotNull(response.getInputData());
        assertTrue(response.getInputData().isEmpty());
    }
    
    @Test
    void testBuildWithLargeConfidenceScores() {
        // Arrange
        Map<String, Object> largeConfidence = new HashMap<>();
        for (int i = 0; i < 100; i++) {
            largeConfidence.put("class" + i, Math.random());
        }
        
        // Act
        PredictionResponse response = PredictionResponseBuilder.builder()
            .setPrediction("Test")
            .setProbabilities(largeConfidence)
            .setConfidence(0.5) // Add required confidence
            .setInputData(new HashMap<>()) // Add required input data
            .build();
        
        // Assert
        assertNotNull(response);
        assertEquals("Test", response.getPrediction());
        assertEquals(largeConfidence, response.getProbabilities());
        assertEquals(100, response.getProbabilities().size());
    }
    
    @Test
    void testBuildWithSpecialCharacters() {
        // Arrange
        String specialPrediction = "Class with special chars: @#$%^&*()";
        Map<String, Object> specialConfidence = new HashMap<>();
        specialConfidence.put("Class with spaces", 0.5);
        specialConfidence.put("Class-with-dashes", 0.3);
        specialConfidence.put("Class_with_underscores", 0.2);
        
        Map<String, String> specialInput = new HashMap<>();
        specialInput.put("feature with spaces", "value with spaces");
        specialInput.put("feature-with-dashes", "value-with-dashes");
        specialInput.put("feature_with_underscores", "value_with_underscores");
        
        // Act
        PredictionResponse response = PredictionResponseBuilder.builder()
            .setPrediction(specialPrediction)
            .setProbabilities(specialConfidence)
            .setInputData(specialInput)
            .setConfidence(0.5) // Add required confidence
            .build();
        
        // Assert
        assertNotNull(response);
        assertEquals(specialPrediction, response.getPrediction());
        assertEquals(specialConfidence, response.getProbabilities());
        assertEquals(specialInput, response.getInputData());
    }
    
    @Test
    void testBuildWithUnicodeCharacters() {
        // Arrange
        String unicodePrediction = "预测结果";
        Map<String, Object> unicodeConfidence = new HashMap<>();
        unicodeConfidence.put("是", 0.6);
        unicodeConfidence.put("否", 0.4);
        
        Map<String, String> unicodeInput = new HashMap<>();
        unicodeInput.put("特征1", "值1");
        unicodeInput.put("特征2", "值2");
        
        // Act
        PredictionResponse response = PredictionResponseBuilder.builder()
            .setPrediction(unicodePrediction)
            .setProbabilities(unicodeConfidence)
            .setInputData(unicodeInput)
            .setConfidence(0.5) // Add required confidence
            .build();
        
        // Assert
        assertNotNull(response);
        assertEquals(unicodePrediction, response.getPrediction());
        assertEquals(unicodeConfidence, response.getProbabilities());
        assertEquals(unicodeInput, response.getInputData());
    }
    
    @Test
    void testBuildWithNumericPredictions() {
        // Arrange
        String numericPrediction = "123.45";
        Map<String, Object> numericConfidence = new HashMap<>();
        numericConfidence.put("100.0", 0.3);
        numericConfidence.put("200.0", 0.4);
        numericConfidence.put("300.0", 0.3);
        
        Map<String, String> numericInput = new HashMap<>();
        numericInput.put("numeric_feature", "123.45");
        numericInput.put("integer_feature", "42");
        
        // Act
        PredictionResponse response = PredictionResponseBuilder.builder()
            .setPrediction(numericPrediction)
            .setProbabilities(numericConfidence)
            .setInputData(numericInput)
            .setConfidence(0.5) // Add required confidence
            .build();
        
        // Assert
        assertNotNull(response);
        assertEquals(numericPrediction, response.getPrediction());
        assertEquals(numericConfidence, response.getProbabilities());
        assertEquals(numericInput, response.getInputData());
    }
    
    @Test
    void testBuildWithBooleanPredictions() {
        // Arrange
        String booleanPrediction = "true";
        Map<String, Object> booleanConfidence = new HashMap<>();
        booleanConfidence.put("true", 0.8);
        booleanConfidence.put("false", 0.2);
        
        Map<String, String> booleanInput = new HashMap<>();
        booleanInput.put("boolean_feature", "true");
        booleanInput.put("flag_feature", "false");
        
        // Act
        PredictionResponse response = PredictionResponseBuilder.builder()
            .setPrediction(booleanPrediction)
            .setProbabilities(booleanConfidence)
            .setInputData(booleanInput)
            .setConfidence(0.5) // Add required confidence
            .build();
        
        // Assert
        assertNotNull(response);
        assertEquals(booleanPrediction, response.getPrediction());
        assertEquals(booleanConfidence, response.getProbabilities());
        assertEquals(booleanInput, response.getInputData());
    }
    
    @Test
    void testBuildWithExtremeValues() {
        // Arrange
        String extremePrediction = "0.0000001";
        Map<String, Object> extremeConfidence = new HashMap<>();
        extremeConfidence.put("0.0", 0.0000001);
        extremeConfidence.put("1.0", 0.9999999);
        
        Map<String, String> extremeInput = new HashMap<>();
        extremeInput.put("tiny_feature", "0.0000001");
        extremeInput.put("huge_feature", "999999999.99");
        
        // Act
        PredictionResponse response = PredictionResponseBuilder.builder()
            .setPrediction(extremePrediction)
            .setProbabilities(extremeConfidence)
            .setInputData(extremeInput)
            .setConfidence(0.5) // Add required confidence
            .build();
        
        // Assert
        assertNotNull(response);
        assertEquals(extremePrediction, response.getPrediction());
        assertEquals(extremeConfidence, response.getProbabilities());
        assertEquals(extremeInput, response.getInputData());
    }
    
    @Test
    void testBuildWithNullKeys() {
        // Arrange
        Map<String, Object> confidenceWithNullKey = new HashMap<>();
        confidenceWithNullKey.put(null, 0.5);
        confidenceWithNullKey.put("valid_key", 0.5);
        
        Map<String, String> inputWithNullKey = new HashMap<>();
        inputWithNullKey.put(null, "null_value");
        inputWithNullKey.put("valid_key", "valid_value");
        
        // Act
        PredictionResponse response = PredictionResponseBuilder.builder()
            .setPrediction("Test")
            .setProbabilities(confidenceWithNullKey)
            .setInputData(inputWithNullKey)
            .setConfidence(0.5) // Add required confidence
            .build();
        
        // Assert
        assertNotNull(response);
        assertEquals("Test", response.getPrediction());
        assertEquals(confidenceWithNullKey, response.getProbabilities());
        assertEquals(inputWithNullKey, response.getInputData());
    }
    
    @Test
    void testBuildWithNullValuesInMaps() {
        // Arrange
        Map<String, Object> confidenceWithNullValues = new HashMap<>();
        confidenceWithNullValues.put("key1", null);
        confidenceWithNullValues.put("key2", 0.5);
        
        Map<String, String> inputWithNullValues = new HashMap<>();
        inputWithNullValues.put("key1", null);
        inputWithNullValues.put("key2", "valid_value");
        
        // Act
        PredictionResponse response = PredictionResponseBuilder.builder()
            .setPrediction("Test")
            .setProbabilities(confidenceWithNullValues)
            .setInputData(inputWithNullValues)
            .setConfidence(0.5) // Add required confidence
            .build();
        
        // Assert
        assertNotNull(response);
        assertEquals("Test", response.getPrediction());
        assertEquals(confidenceWithNullValues, response.getProbabilities());
        assertEquals(inputWithNullValues, response.getInputData());
    }
}
