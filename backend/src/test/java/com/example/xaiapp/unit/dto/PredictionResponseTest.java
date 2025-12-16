package com.example.xaiapp.unit.dto;

import com.example.xaiapp.dto.PredictionResponse;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for PredictionResponse DTO
 * Tests constructors, Lombok methods, and all fields
 */
class PredictionResponseTest {

    @Test
    void testNoArgsConstructor_createsEmptyResponse() {
        // Act
        PredictionResponse response = new PredictionResponse();
        
        // Assert
        assertNotNull(response);
        assertNull(response.getPrediction());
        assertNull(response.getConfidence());
        assertNull(response.getProbabilities());
        assertNull(response.getInputData());
    }

    @Test
    void testAllArgsConstructor_createsCompleteResponse() {
        // Arrange
        String prediction = "Positive";
        Double confidence = 0.85;
        Map<String, Object> probabilities = new HashMap<>();
        probabilities.put("Positive", 0.85);
        probabilities.put("Negative", 0.15);
        Map<String, String> inputData = new HashMap<>();
        inputData.put("feature1", "1.5");
        inputData.put("feature2", "2.3");
        
        // Act
        PredictionResponse response = new PredictionResponse(
            prediction, confidence, probabilities, inputData
        );
        
        // Assert
        assertNotNull(response);
        assertEquals(prediction, response.getPrediction());
        assertEquals(confidence, response.getConfidence());
        assertEquals(probabilities, response.getProbabilities());
        assertEquals(inputData, response.getInputData());
    }

    @Test
    void testSetters_setAllFields() {
        // Arrange
        PredictionResponse response = new PredictionResponse();
        String prediction = "Negative";
        Double confidence = 0.75;
        Map<String, Object> probabilities = new HashMap<>();
        probabilities.put("Negative", 0.75);
        probabilities.put("Positive", 0.25);
        Map<String, String> inputData = new HashMap<>();
        inputData.put("feature1", "0.5");
        
        // Act
        response.setPrediction(prediction);
        response.setConfidence(confidence);
        response.setProbabilities(probabilities);
        response.setInputData(inputData);
        
        // Assert
        assertEquals(prediction, response.getPrediction());
        assertEquals(confidence, response.getConfidence());
        assertEquals(probabilities, response.getProbabilities());
        assertEquals(inputData, response.getInputData());
    }

    @Test
    void testEquals_sameObject_returnsTrue() {
        // Arrange
        PredictionResponse response = createTestResponse();
        
        // Act & Assert
        assertEquals(response, response);
    }

    @Test
    void testEquals_equalObjects_returnsTrue() {
        // Arrange
        PredictionResponse response1 = createTestResponse();
        PredictionResponse response2 = createTestResponse();
        
        // Act & Assert
        assertEquals(response1, response2);
    }

    @Test
    void testEquals_differentObjects_returnsFalse() {
        // Arrange
        PredictionResponse response1 = createTestResponse();
        PredictionResponse response2 = createTestResponse();
        response2.setPrediction("Different");
        
        // Act & Assert
        assertNotEquals(response1, response2);
    }

    @Test
    void testEquals_nullObject_returnsFalse() {
        // Arrange
        PredictionResponse response = createTestResponse();
        
        // Act & Assert
        assertNotEquals(response, null);
    }

    @Test
    void testEquals_differentClass_returnsFalse() {
        // Arrange
        PredictionResponse response = createTestResponse();
        String other = "not a response";
        
        // Act & Assert
        assertNotEquals(response, other);
    }

    @Test
    void testHashCode_equalObjects_sameHashCode() {
        // Arrange
        PredictionResponse response1 = createTestResponse();
        PredictionResponse response2 = createTestResponse();
        
        // Act & Assert
        assertEquals(response1.hashCode(), response2.hashCode());
    }

    @Test
    void testToString_containsAllFields() {
        // Arrange
        PredictionResponse response = createTestResponse();
        
        // Act
        String toString = response.toString();
        
        // Assert
        assertTrue(toString.contains("Positive"));
        assertTrue(toString.contains("0.85"));
        assertTrue(toString.contains("feature1"));
    }

    @Test
    void testWithNullValues_handlesNullsCorrectly() {
        // Arrange
        PredictionResponse response = new PredictionResponse();
        
        // Act
        response.setPrediction(null);
        response.setConfidence(null);
        response.setProbabilities(null);
        response.setInputData(null);
        
        // Assert
        assertNull(response.getPrediction());
        assertNull(response.getConfidence());
        assertNull(response.getProbabilities());
        assertNull(response.getInputData());
    }

    @Test
    void testWithEmptyProbabilities_handlesEmptyMap() {
        // Arrange
        PredictionResponse response = new PredictionResponse();
        Map<String, Object> emptyProbabilities = new HashMap<>();
        
        // Act
        response.setProbabilities(emptyProbabilities);
        
        // Assert
        assertEquals(emptyProbabilities, response.getProbabilities());
        assertTrue(response.getProbabilities().isEmpty());
    }

    @Test
    void testWithEmptyInputData_handlesEmptyMap() {
        // Arrange
        PredictionResponse response = new PredictionResponse();
        Map<String, String> emptyInputData = new HashMap<>();
        
        // Act
        response.setInputData(emptyInputData);
        
        // Assert
        assertEquals(emptyInputData, response.getInputData());
        assertTrue(response.getInputData().isEmpty());
    }

    @Test
    void testWithZeroConfidence_handlesZero() {
        // Arrange
        PredictionResponse response = new PredictionResponse();
        
        // Act
        response.setConfidence(0.0);
        
        // Assert
        assertEquals(Double.valueOf(0.0), response.getConfidence());
    }

    @Test
    void testWithNegativeConfidence_handlesNegative() {
        // Arrange
        PredictionResponse response = new PredictionResponse();
        
        // Act
        response.setConfidence(-0.5);
        
        // Assert
        assertEquals(Double.valueOf(-0.5), response.getConfidence());
    }

    @Test
    void testWithMaxConfidence_handlesMaxValue() {
        // Arrange
        PredictionResponse response = new PredictionResponse();
        
        // Act
        response.setConfidence(Double.MAX_VALUE);
        
        // Assert
        assertEquals(Double.MAX_VALUE, response.getConfidence());
    }

    @Test
    void testWithMinConfidence_handlesMinValue() {
        // Arrange
        PredictionResponse response = new PredictionResponse();
        
        // Act
        response.setConfidence(Double.MIN_VALUE);
        
        // Assert
        assertEquals(Double.MIN_VALUE, response.getConfidence());
    }

    @Test
    void testWithLongPrediction_handlesLongStrings() {
        // Arrange
        PredictionResponse response = new PredictionResponse();
        String longPrediction = "very_long_prediction_with_many_characters_and_numbers_12345";
        
        // Act
        response.setPrediction(longPrediction);
        
        // Assert
        assertEquals(longPrediction, response.getPrediction());
    }

    @Test
    void testWithManyProbabilities_handlesLargeMaps() {
        // Arrange
        PredictionResponse response = new PredictionResponse();
        Map<String, Object> manyProbabilities = new HashMap<>();
        for (int i = 0; i < 10; i++) {
            manyProbabilities.put("class" + i, 0.1);
        }
        
        // Act
        response.setProbabilities(manyProbabilities);
        
        // Assert
        assertEquals(manyProbabilities, response.getProbabilities());
        assertEquals(10, response.getProbabilities().size());
    }

    @Test
    void testWithManyInputData_handlesLargeMaps() {
        // Arrange
        PredictionResponse response = new PredictionResponse();
        Map<String, String> manyInputData = new HashMap<>();
        for (int i = 0; i < 10; i++) {
            manyInputData.put("feature" + i, "value" + i);
        }
        
        // Act
        response.setInputData(manyInputData);
        
        // Assert
        assertEquals(manyInputData, response.getInputData());
        assertEquals(10, response.getInputData().size());
    }

    @Test
    void testWithSpecialCharactersInPrediction_handlesSpecialChars() {
        // Arrange
        PredictionResponse response = new PredictionResponse();
        String specialPrediction = "prediction@domain.com";
        
        // Act
        response.setPrediction(specialPrediction);
        
        // Assert
        assertEquals(specialPrediction, response.getPrediction());
    }

    @Test
    void testWithUnicodeCharacters_handlesUnicode() {
        // Arrange
        PredictionResponse response = new PredictionResponse();
        String unicodePrediction = "预测结果123";
        
        // Act
        response.setPrediction(unicodePrediction);
        
        // Assert
        assertEquals(unicodePrediction, response.getPrediction());
    }

    @Test
    void testWithWhitespaceInPrediction_handlesWhitespace() {
        // Arrange
        PredictionResponse response = new PredictionResponse();
        String predictionWithWhitespace = " prediction result ";
        
        // Act
        response.setPrediction(predictionWithWhitespace);
        
        // Assert
        assertEquals(predictionWithWhitespace, response.getPrediction());
    }

    @Test
    void testWithComplexProbabilities_handlesComplexValues() {
        // Arrange
        PredictionResponse response = new PredictionResponse();
        Map<String, Object> complexProbabilities = new HashMap<>();
        complexProbabilities.put("class1", 0.5);
        complexProbabilities.put("class2", 0.3);
        complexProbabilities.put("class3", 0.2);
        
        // Act
        response.setProbabilities(complexProbabilities);
        
        // Assert
        assertEquals(complexProbabilities, response.getProbabilities());
        assertEquals(0.5, response.getProbabilities().get("class1"));
        assertEquals(0.3, response.getProbabilities().get("class2"));
        assertEquals(0.2, response.getProbabilities().get("class3"));
    }

    @Test
    void testWithComplexInputData_handlesComplexValues() {
        // Arrange
        PredictionResponse response = new PredictionResponse();
        Map<String, String> complexInputData = new HashMap<>();
        complexInputData.put("feature1", "1.5");
        complexInputData.put("feature2", "2.3");
        complexInputData.put("feature3", "0.8");
        
        // Act
        response.setInputData(complexInputData);
        
        // Assert
        assertEquals(complexInputData, response.getInputData());
        assertEquals("1.5", response.getInputData().get("feature1"));
        assertEquals("2.3", response.getInputData().get("feature2"));
        assertEquals("0.8", response.getInputData().get("feature3"));
    }

    @Test
    void testWithNaNConfidence_handlesNaN() {
        // Arrange
        PredictionResponse response = new PredictionResponse();
        
        // Act
        response.setConfidence(Double.NaN);
        
        // Assert
        assertEquals(Double.NaN, response.getConfidence());
    }

    @Test
    void testWithInfinityConfidence_handlesInfinity() {
        // Arrange
        PredictionResponse response = new PredictionResponse();
        
        // Act
        response.setConfidence(Double.POSITIVE_INFINITY);
        
        // Assert
        assertEquals(Double.POSITIVE_INFINITY, response.getConfidence());
    }

    private PredictionResponse createTestResponse() {
        Map<String, Object> probabilities = new HashMap<>();
        probabilities.put("Positive", 0.85);
        probabilities.put("Negative", 0.15);
        
        Map<String, String> inputData = new HashMap<>();
        inputData.put("feature1", "1.5");
        inputData.put("feature2", "2.3");
        
        return new PredictionResponse("Positive", 0.85, probabilities, inputData);
    }
}
