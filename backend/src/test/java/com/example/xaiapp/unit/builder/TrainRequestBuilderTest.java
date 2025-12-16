package com.example.xaiapp.unit.builder;

import com.example.xaiapp.builder.TrainRequestBuilder;
import com.example.xaiapp.dto.TrainRequestDto;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for TrainRequestBuilder
 * Tests fluent builder API, validation, and immutability
 */
class TrainRequestBuilderTest {
    
    @Test
    void testBuildCompleteRequest() {
        // Arrange
        Long datasetId = 1L;
        String modelName = "Test Model";
        String modelType = "CLASSIFICATION";
        String targetVariable = "target";
        List<String> featureNames = Arrays.asList("feature1", "feature2", "feature3");
        
        // Act
        TrainRequestDto request = TrainRequestBuilder.builder()
            .setDatasetId(datasetId)
            .setModelName(modelName)
            .setModelType(modelType)
            .setTargetVariable(targetVariable)
            .setFeatureNames(featureNames)
            .build();
        
        // Assert
        assertNotNull(request);
        assertEquals(datasetId, request.getDatasetId());
        assertEquals(modelName, request.getModelName());
        assertEquals(modelType, request.getModelType());
        assertEquals(targetVariable, request.getTargetVariable());
        assertEquals(featureNames, request.getFeatureNames());
    }
    
    @Test
    void testBuildMinimalRequest() {
        // Arrange
        Long datasetId = 1L;
        String modelName = "Test Model";
        
        // Act & Assert - This should throw an exception due to validation
        assertThrows(IllegalArgumentException.class, () -> {
            TrainRequestBuilder.builder()
                .setDatasetId(datasetId)
                .setModelName(modelName)
                .build();
        });
        
        // Test passes if exception is thrown
    }
    
    @Test
    void testBuildWithNullValues() {
        // Act & Assert - This should throw an exception due to validation
        assertThrows(IllegalArgumentException.class, () -> {
            TrainRequestBuilder.builder()
                .setDatasetId(null)
                .setModelName(null)
                .setModelType((String) null)
                .setTargetVariable(null)
                .setFeatureNames(null)
                .build();
        });
        
        // Test passes if exception is thrown
    }
    
    @Test
    void testMethodChaining() {
        // Act
        TrainRequestBuilder builder = TrainRequestBuilder.builder()
            .setDatasetId(1L)
            .setModelName("Test")
            .setModelType("CLASSIFICATION")
            .setTargetVariable("target")
            .setFeatureNames(Arrays.asList("feature1"));
        
        // Assert
        assertNotNull(builder);
        // Verify that all methods return the builder instance for chaining
        assertSame(builder, builder.setDatasetId(2L));
        assertSame(builder, builder.setModelName("Test2"));
        assertSame(builder, builder.setModelType("REGRESSION"));
        assertSame(builder, builder.setTargetVariable("target2"));
        assertSame(builder, builder.setFeatureNames(Arrays.asList("feature2")));
    }
    
    @Test
    void testBuildWithEmptyFeatureNames() {
        // Arrange
        List<String> emptyFeatures = Arrays.asList();
        
        // Act & Assert - This should throw an exception due to validation
        assertThrows(IllegalArgumentException.class, () -> {
            TrainRequestBuilder.builder()
                .setDatasetId(1L)
                .setModelName("Test Model")
                .setModelType("CLASSIFICATION")
                .setTargetVariable("target")
                .setFeatureNames(emptyFeatures)
                .build();
        });
        
        // Test passes if exception is thrown
    }
    
    @Test
    void testBuildWithLargeFeatureList() {
        // Arrange
        List<String> largeFeatureList = Arrays.asList(
            "feature1", "feature2", "feature3", "feature4", "feature5",
            "feature6", "feature7", "feature8", "feature9", "feature10"
        );
        
        // Act
        TrainRequestDto request = TrainRequestBuilder.builder()
            .setDatasetId(1L)
            .setModelName("Large Feature Model")
            .setModelType("CLASSIFICATION")
            .setTargetVariable("target")
            .setFeatureNames(largeFeatureList)
            .build();
        
        // Assert
        assertNotNull(request);
        assertEquals(1L, request.getDatasetId());
        assertEquals("Large Feature Model", request.getModelName());
        assertEquals("CLASSIFICATION", request.getModelType());
        assertEquals("target", request.getTargetVariable());
        assertEquals(largeFeatureList, request.getFeatureNames());
        assertEquals(10, request.getFeatureNames().size());
    }
    
    @Test
    void testBuildWithSpecialCharacters() {
        // Arrange
        String specialModelName = "Model with special chars: @#$%^&*()";
        String specialTargetVariable = "target_with_underscores";
        List<String> specialFeatures = Arrays.asList(
            "feature with spaces",
            "feature-with-dashes",
            "feature_with_underscores"
        );
        
        // Act
        TrainRequestDto request = TrainRequestBuilder.builder()
            .setDatasetId(1L)
            .setModelName(specialModelName)
            .setModelType("CLASSIFICATION")
            .setTargetVariable(specialTargetVariable)
            .setFeatureNames(specialFeatures)
            .build();
        
        // Assert
        assertNotNull(request);
        assertEquals(1L, request.getDatasetId());
        assertEquals(specialModelName, request.getModelName());
        assertEquals("CLASSIFICATION", request.getModelType());
        assertEquals(specialTargetVariable, request.getTargetVariable());
        assertEquals(specialFeatures, request.getFeatureNames());
    }
    
    @Test
    void testBuildWithUnicodeCharacters() {
        // Arrange
        String unicodeModelName = "模型名称";
        String unicodeTargetVariable = "目标变量";
        List<String> unicodeFeatures = Arrays.asList("特征1", "特征2", "特征3");
        
        // Act
        TrainRequestDto request = TrainRequestBuilder.builder()
            .setDatasetId(1L)
            .setModelName(unicodeModelName)
            .setModelType("CLASSIFICATION")
            .setTargetVariable(unicodeTargetVariable)
            .setFeatureNames(unicodeFeatures)
            .build();
        
        // Assert
        assertNotNull(request);
        assertEquals(1L, request.getDatasetId());
        assertEquals(unicodeModelName, request.getModelName());
        assertEquals("CLASSIFICATION", request.getModelType());
        assertEquals(unicodeTargetVariable, request.getTargetVariable());
        assertEquals(unicodeFeatures, request.getFeatureNames());
    }
    
    @Test
    void testBuildWithNumericValues() {
        // Arrange
        String numericModelName = "Model123";
        String numericTargetVariable = "target_456";
        List<String> numericFeatures = Arrays.asList("feature_1", "feature_2", "feature_3");
        
        // Act
        TrainRequestDto request = TrainRequestBuilder.builder()
            .setDatasetId(123L)
            .setModelName(numericModelName)
            .setModelType("REGRESSION")
            .setTargetVariable(numericTargetVariable)
            .setFeatureNames(numericFeatures)
            .build();
        
        // Assert
        assertNotNull(request);
        assertEquals(123L, request.getDatasetId());
        assertEquals(numericModelName, request.getModelName());
        assertEquals("REGRESSION", request.getModelType());
        assertEquals(numericTargetVariable, request.getTargetVariable());
        assertEquals(numericFeatures, request.getFeatureNames());
    }
    
    @Test
    void testBuildWithBooleanValues() {
        // Arrange
        String booleanModelName = "BooleanModel";
        String booleanTargetVariable = "is_true";
        List<String> booleanFeatures = Arrays.asList("flag1", "flag2", "flag3");
        
        // Act
        TrainRequestDto request = TrainRequestBuilder.builder()
            .setDatasetId(1L)
            .setModelName(booleanModelName)
            .setModelType("CLASSIFICATION")
            .setTargetVariable(booleanTargetVariable)
            .setFeatureNames(booleanFeatures)
            .build();
        
        // Assert
        assertNotNull(request);
        assertEquals(1L, request.getDatasetId());
        assertEquals(booleanModelName, request.getModelName());
        assertEquals("CLASSIFICATION", request.getModelType());
        assertEquals(booleanTargetVariable, request.getTargetVariable());
        assertEquals(booleanFeatures, request.getFeatureNames());
    }
    
    @Test
    void testBuildWithExtremeValues() {
        // Arrange
        Long extremeDatasetId = Long.MAX_VALUE;
        String extremeModelName = "A".repeat(1000); // Very long string
        String extremeTargetVariable = "target_" + "x".repeat(100);
        List<String> extremeFeatures = Arrays.asList(
            "feature_" + "x".repeat(50),
            "feature_" + "y".repeat(50),
            "feature_" + "z".repeat(50)
        );
        
        // Act
        TrainRequestDto request = TrainRequestBuilder.builder()
            .setDatasetId(extremeDatasetId)
            .setModelName(extremeModelName)
            .setModelType("CLASSIFICATION")
            .setTargetVariable(extremeTargetVariable)
            .setFeatureNames(extremeFeatures)
            .build();
        
        // Assert
        assertNotNull(request);
        assertEquals(extremeDatasetId, request.getDatasetId());
        assertEquals(extremeModelName, request.getModelName());
        assertEquals("CLASSIFICATION", request.getModelType());
        assertEquals(extremeTargetVariable, request.getTargetVariable());
        assertEquals(extremeFeatures, request.getFeatureNames());
    }
    
    @Test
    void testBuildWithNegativeDatasetId() {
        // Arrange
        Long negativeDatasetId = -1L;
        
        // Act & Assert - This should throw an exception due to validation
        assertThrows(IllegalArgumentException.class, () -> {
            TrainRequestBuilder.builder()
                .setDatasetId(negativeDatasetId)
                .setModelName("Test Model")
                .setModelType("CLASSIFICATION")
                .setTargetVariable("target")
                .setFeatureNames(Arrays.asList("feature1"))
                .build();
        });
        
        // Test passes if exception is thrown
    }
    
    @Test
    void testBuildWithZeroDatasetId() {
        // Arrange
        Long zeroDatasetId = 0L;
        
        // Act & Assert - This should throw an exception due to validation
        assertThrows(IllegalArgumentException.class, () -> {
            TrainRequestBuilder.builder()
                .setDatasetId(zeroDatasetId)
                .setModelName("Test Model")
                .setModelType("CLASSIFICATION")
                .setTargetVariable("target")
                .setFeatureNames(Arrays.asList("feature1"))
                .build();
        });
        
        // Test passes if exception is thrown
    }
    
    @Test
    void testBuildWithEmptyStringValues() {
        // Arrange
        String emptyModelName = "";
        String emptyTargetVariable = "";
        List<String> emptyFeatures = Arrays.asList("", "", "");
        
        // Act & Assert - This should throw an exception due to validation
        assertThrows(IllegalArgumentException.class, () -> {
            TrainRequestBuilder.builder()
                .setDatasetId(1L)
                .setModelName(emptyModelName)
                .setModelType("CLASSIFICATION")
                .setTargetVariable(emptyTargetVariable)
                .setFeatureNames(emptyFeatures)
                .build();
        });
        
        // Test passes if exception is thrown
    }
    
    @Test
    void testBuildWithWhitespaceValues() {
        // Arrange
        String whitespaceModelName = "   ";
        String whitespaceTargetVariable = "\t\n";
        List<String> whitespaceFeatures = Arrays.asList(" ", "\t", "\n");
        
        // Act
        TrainRequestDto request = TrainRequestBuilder.builder()
            .setDatasetId(1L)
            .setModelName(whitespaceModelName)
            .setModelType("CLASSIFICATION")
            .setTargetVariable(whitespaceTargetVariable)
            .setFeatureNames(whitespaceFeatures)
            .build();
        
        // Assert
        assertNotNull(request);
        assertEquals(1L, request.getDatasetId());
        assertEquals(whitespaceModelName, request.getModelName());
        assertEquals("CLASSIFICATION", request.getModelType());
        assertEquals(whitespaceTargetVariable, request.getTargetVariable());
        assertEquals(whitespaceFeatures, request.getFeatureNames());
    }
    
    @Test
    void testBuildWithDuplicateFeatureNames() {
        // Arrange
        List<String> duplicateFeatures = Arrays.asList("feature1", "feature1", "feature2", "feature2");
        
        // Act
        TrainRequestDto request = TrainRequestBuilder.builder()
            .setDatasetId(1L)
            .setModelName("Test Model")
            .setModelType("CLASSIFICATION")
            .setTargetVariable("target")
            .setFeatureNames(duplicateFeatures)
            .build();
        
        // Assert
        assertNotNull(request);
        assertEquals(1L, request.getDatasetId());
        assertEquals("Test Model", request.getModelName());
        assertEquals("CLASSIFICATION", request.getModelType());
        assertEquals("target", request.getTargetVariable());
        assertEquals(duplicateFeatures, request.getFeatureNames());
        assertEquals(4, request.getFeatureNames().size());
    }
}
