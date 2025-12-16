package com.example.xaiapp.unit.dto;

import com.example.xaiapp.dto.ExplanationResponse;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ExplanationResponse DTO and its nested FeatureContribution class
 * Tests constructors, Lombok methods, and nested class functionality
 */
class ExplanationResponseTest {

    @Test
    void testNoArgsConstructor_createsEmptyResponse() {
        // Act
        ExplanationResponse response = new ExplanationResponse();
        
        // Assert
        assertNotNull(response);
        assertNull(response.getPrediction());
        assertNull(response.getFeatureContributions());
        assertNull(response.getInputData());
        assertNull(response.getExplanationText());
    }

    @Test
    void testAllArgsConstructor_createsCompleteResponse() {
        // Arrange
        String prediction = "Positive";
        List<ExplanationResponse.FeatureContribution> contributions = Arrays.asList(
            new ExplanationResponse.FeatureContribution("feature1", 0.8, "positive"),
            new ExplanationResponse.FeatureContribution("feature2", -0.3, "negative")
        );
        Map<String, String> inputData = new HashMap<>();
        inputData.put("feature1", "1.5");
        inputData.put("feature2", "0.8");
        String explanationText = "The model predicts positive based on feature contributions";
        
        // Act
        ExplanationResponse response = new ExplanationResponse(
            prediction, contributions, inputData, explanationText
        );
        
        // Assert
        assertNotNull(response);
        assertEquals(prediction, response.getPrediction());
        assertEquals(contributions, response.getFeatureContributions());
        assertEquals(inputData, response.getInputData());
        assertEquals(explanationText, response.getExplanationText());
    }

    @Test
    void testSetters_setAllFields() {
        // Arrange
        ExplanationResponse response = new ExplanationResponse();
        String prediction = "Negative";
        List<ExplanationResponse.FeatureContribution> contributions = Arrays.asList(
            new ExplanationResponse.FeatureContribution("feature1", -0.5, "negative")
        );
        Map<String, String> inputData = new HashMap<>();
        inputData.put("feature1", "0.2");
        String explanationText = "Negative prediction due to low feature value";
        
        // Act
        response.setPrediction(prediction);
        response.setFeatureContributions(contributions);
        response.setInputData(inputData);
        response.setExplanationText(explanationText);
        
        // Assert
        assertEquals(prediction, response.getPrediction());
        assertEquals(contributions, response.getFeatureContributions());
        assertEquals(inputData, response.getInputData());
        assertEquals(explanationText, response.getExplanationText());
    }

    @Test
    void testEquals_sameObject_returnsTrue() {
        // Arrange
        ExplanationResponse response = createTestResponse();
        
        // Act & Assert
        assertEquals(response, response);
    }

    @Test
    void testEquals_equalObjects_returnsTrue() {
        // Arrange
        ExplanationResponse response1 = createTestResponse();
        ExplanationResponse response2 = createTestResponse();
        
        // Act & Assert
        assertEquals(response1, response2);
    }

    @Test
    void testEquals_differentObjects_returnsFalse() {
        // Arrange
        ExplanationResponse response1 = createTestResponse();
        ExplanationResponse response2 = createTestResponse();
        response2.setPrediction("Different");
        
        // Act & Assert
        assertNotEquals(response1, response2);
    }

    @Test
    void testEquals_nullObject_returnsFalse() {
        // Arrange
        ExplanationResponse response = createTestResponse();
        
        // Act & Assert
        assertNotEquals(response, null);
    }

    @Test
    void testEquals_differentClass_returnsFalse() {
        // Arrange
        ExplanationResponse response = createTestResponse();
        String other = "not a response";
        
        // Act & Assert
        assertNotEquals(response, other);
    }

    @Test
    void testHashCode_equalObjects_sameHashCode() {
        // Arrange
        ExplanationResponse response1 = createTestResponse();
        ExplanationResponse response2 = createTestResponse();
        
        // Act & Assert
        assertEquals(response1.hashCode(), response2.hashCode());
    }

    @Test
    void testToString_containsAllFields() {
        // Arrange
        ExplanationResponse response = createTestResponse();
        
        // Act
        String toString = response.toString();
        
        // Assert
        assertTrue(toString.contains("Positive"));
        assertTrue(toString.contains("feature1"));
        assertTrue(toString.contains("explanation"));
    }

    @Test
    void testWithNullValues_handlesNullsCorrectly() {
        // Arrange
        ExplanationResponse response = new ExplanationResponse();
        
        // Act
        response.setPrediction(null);
        response.setFeatureContributions(null);
        response.setInputData(null);
        response.setExplanationText(null);
        
        // Assert
        assertNull(response.getPrediction());
        assertNull(response.getFeatureContributions());
        assertNull(response.getInputData());
        assertNull(response.getExplanationText());
    }

    @Test
    void testWithEmptyContributions_handlesEmptyList() {
        // Arrange
        ExplanationResponse response = new ExplanationResponse();
        List<ExplanationResponse.FeatureContribution> emptyContributions = Collections.emptyList();
        
        // Act
        response.setFeatureContributions(emptyContributions);
        
        // Assert
        assertEquals(emptyContributions, response.getFeatureContributions());
        assertTrue(response.getFeatureContributions().isEmpty());
    }

    @Test
    void testWithEmptyInputData_handlesEmptyMap() {
        // Arrange
        ExplanationResponse response = new ExplanationResponse();
        Map<String, String> emptyInputData = new HashMap<>();
        
        // Act
        response.setInputData(emptyInputData);
        
        // Assert
        assertEquals(emptyInputData, response.getInputData());
        assertTrue(response.getInputData().isEmpty());
    }

    // FeatureContribution nested class tests
    @Test
    void testFeatureContributionNoArgsConstructor_createsEmptyContribution() {
        // Act
        ExplanationResponse.FeatureContribution contribution = new ExplanationResponse.FeatureContribution();
        
        // Assert
        assertNotNull(contribution);
        assertNull(contribution.getFeatureName());
        assertNull(contribution.getContribution());
        assertNull(contribution.getDirection());
    }

    @Test
    void testFeatureContributionAllArgsConstructor_createsCompleteContribution() {
        // Arrange
        String featureName = "temperature";
        Double contributionValue = 0.75;
        String direction = "positive";
        
        // Act
        ExplanationResponse.FeatureContribution contribution = new ExplanationResponse.FeatureContribution(
            featureName, contributionValue, direction
        );
        
        // Assert
        assertNotNull(contribution);
        assertEquals(featureName, contribution.getFeatureName());
        assertEquals(contributionValue, contribution.getContribution());
        assertEquals(direction, contribution.getDirection());
    }

    @Test
    void testFeatureContributionSetters_setAllFields() {
        // Arrange
        ExplanationResponse.FeatureContribution contribution = new ExplanationResponse.FeatureContribution();
        String featureName = "humidity";
        Double contributionValue = -0.25;
        String direction = "negative";
        
        // Act
        contribution.setFeatureName(featureName);
        contribution.setContribution(contributionValue);
        contribution.setDirection(direction);
        
        // Assert
        assertEquals(featureName, contribution.getFeatureName());
        assertEquals(contributionValue, contribution.getContribution());
        assertEquals(direction, contribution.getDirection());
    }

    @Test
    void testFeatureContributionEquals_sameObject_returnsTrue() {
        // Arrange
        ExplanationResponse.FeatureContribution contribution = createTestContribution();
        
        // Act & Assert
        assertEquals(contribution, contribution);
    }

    @Test
    void testFeatureContributionEquals_equalObjects_returnsTrue() {
        // Arrange
        ExplanationResponse.FeatureContribution contribution1 = createTestContribution();
        ExplanationResponse.FeatureContribution contribution2 = createTestContribution();
        
        // Act & Assert
        assertEquals(contribution1, contribution2);
    }

    @Test
    void testFeatureContributionEquals_differentObjects_returnsFalse() {
        // Arrange
        ExplanationResponse.FeatureContribution contribution1 = createTestContribution();
        ExplanationResponse.FeatureContribution contribution2 = createTestContribution();
        contribution2.setContribution(0.5);
        
        // Act & Assert
        assertNotEquals(contribution1, contribution2);
    }

    @Test
    void testFeatureContributionEquals_nullObject_returnsFalse() {
        // Arrange
        ExplanationResponse.FeatureContribution contribution = createTestContribution();
        
        // Act & Assert
        assertNotEquals(contribution, null);
    }

    @Test
    void testFeatureContributionEquals_differentClass_returnsFalse() {
        // Arrange
        ExplanationResponse.FeatureContribution contribution = createTestContribution();
        String other = "not a contribution";
        
        // Act & Assert
        assertNotEquals(contribution, other);
    }

    @Test
    void testFeatureContributionHashCode_equalObjects_sameHashCode() {
        // Arrange
        ExplanationResponse.FeatureContribution contribution1 = createTestContribution();
        ExplanationResponse.FeatureContribution contribution2 = createTestContribution();
        
        // Act & Assert
        assertEquals(contribution1.hashCode(), contribution2.hashCode());
    }

    @Test
    void testFeatureContributionToString_containsAllFields() {
        // Arrange
        ExplanationResponse.FeatureContribution contribution = createTestContribution();
        
        // Act
        String toString = contribution.toString();
        
        // Assert
        assertTrue(toString.contains("feature1"));
        assertTrue(toString.contains("0.8"));
        assertTrue(toString.contains("positive"));
    }

    @Test
    void testFeatureContributionWithNullValues_handlesNullsCorrectly() {
        // Arrange
        ExplanationResponse.FeatureContribution contribution = new ExplanationResponse.FeatureContribution();
        
        // Act
        contribution.setFeatureName(null);
        contribution.setContribution(null);
        contribution.setDirection(null);
        
        // Assert
        assertNull(contribution.getFeatureName());
        assertNull(contribution.getContribution());
        assertNull(contribution.getDirection());
    }

    @Test
    void testFeatureContributionWithZeroContribution_handlesZero() {
        // Arrange
        ExplanationResponse.FeatureContribution contribution = new ExplanationResponse.FeatureContribution();
        
        // Act
        contribution.setContribution(0.0);
        
        // Assert
        assertEquals(Double.valueOf(0.0), contribution.getContribution());
    }

    @Test
    void testFeatureContributionWithNegativeContribution_handlesNegative() {
        // Arrange
        ExplanationResponse.FeatureContribution contribution = new ExplanationResponse.FeatureContribution();
        
        // Act
        contribution.setContribution(-1.0);
        
        // Assert
        assertEquals(Double.valueOf(-1.0), contribution.getContribution());
    }

    @Test
    void testFeatureContributionWithMaxContribution_handlesMaxValue() {
        // Arrange
        ExplanationResponse.FeatureContribution contribution = new ExplanationResponse.FeatureContribution();
        
        // Act
        contribution.setContribution(Double.MAX_VALUE);
        
        // Assert
        assertEquals(Double.MAX_VALUE, contribution.getContribution());
    }

    @Test
    void testFeatureContributionWithMinContribution_handlesMinValue() {
        // Arrange
        ExplanationResponse.FeatureContribution contribution = new ExplanationResponse.FeatureContribution();
        
        // Act
        contribution.setContribution(Double.MIN_VALUE);
        
        // Assert
        assertEquals(Double.MIN_VALUE, contribution.getContribution());
    }

    @Test
    void testFeatureContributionWithLongFeatureName_handlesLongStrings() {
        // Arrange
        ExplanationResponse.FeatureContribution contribution = new ExplanationResponse.FeatureContribution();
        String longFeatureName = "very_long_feature_name_with_many_characters_and_numbers_12345";
        
        // Act
        contribution.setFeatureName(longFeatureName);
        
        // Assert
        assertEquals(longFeatureName, contribution.getFeatureName());
    }

    private ExplanationResponse createTestResponse() {
        List<ExplanationResponse.FeatureContribution> contributions = Arrays.asList(
            new ExplanationResponse.FeatureContribution("feature1", 0.8, "positive")
        );
        Map<String, String> inputData = new HashMap<>();
        inputData.put("feature1", "1.5");
        
        return new ExplanationResponse(
            "Positive",
            contributions,
            inputData,
            "Test explanation"
        );
    }

    private ExplanationResponse.FeatureContribution createTestContribution() {
        return new ExplanationResponse.FeatureContribution("feature1", 0.8, "positive");
    }
}
