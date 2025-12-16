package com.example.xaiapp.unit.entity;

import com.example.xaiapp.entity.MLModel;
import com.example.xaiapp.entity.Dataset;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for MLModel entity
 * Tests constructors, Lombok methods, @PrePersist, enum, JPA relationships, and equals/hashCode
 */
class MLModelTest {

    @Test
    void testNoArgsConstructor_createsEmptyModel() {
        // Act
        MLModel model = new MLModel();
        
        // Assert
        assertNotNull(model);
        assertNull(model.getId());
        assertNull(model.getModelName());
        assertNull(model.getModelType());
        assertNull(model.getSerializedModelPath());
        assertNull(model.getTrainingDate());
        assertNull(model.getTargetVariable());
        assertNull(model.getFeatureNames());
        assertNull(model.getDataset());
        assertNull(model.getAccuracy());
        assertNull(model.getModelMetadata());
    }

    @Test
    void testAllArgsConstructor_createsCompleteModel() {
        // Arrange
        Long id = 1L;
        String modelName = "Test Model";
        MLModel.ModelType modelType = MLModel.ModelType.CLASSIFICATION;
        String serializedModelPath = "/models/test.model";
        LocalDateTime trainingDate = LocalDateTime.now();
        String targetVariable = "target";
        List<String> featureNames = Arrays.asList("feature1", "feature2");
        Dataset dataset = new Dataset();
        dataset.setId(1L);
        Double accuracy = 0.85;
        String modelMetadata = "Test metadata";
        
        // Act
        MLModel model = new MLModel(id, modelName, modelType, serializedModelPath, trainingDate, 
            targetVariable, featureNames, dataset, accuracy, modelMetadata);
        
        // Assert
        assertNotNull(model);
        assertEquals(id, model.getId());
        assertEquals(modelName, model.getModelName());
        assertEquals(modelType, model.getModelType());
        assertEquals(serializedModelPath, model.getSerializedModelPath());
        assertEquals(trainingDate, model.getTrainingDate());
        assertEquals(targetVariable, model.getTargetVariable());
        assertEquals(featureNames, model.getFeatureNames());
        assertEquals(dataset, model.getDataset());
        assertEquals(accuracy, model.getAccuracy());
        assertEquals(modelMetadata, model.getModelMetadata());
    }

    @Test
    void testSetters_setAllFields() {
        // Arrange
        MLModel model = new MLModel();
        Long id = 2L;
        String modelName = "New Model";
        MLModel.ModelType modelType = MLModel.ModelType.REGRESSION;
        String serializedModelPath = "/models/new.model";
        LocalDateTime trainingDate = LocalDateTime.of(2024, 1, 15, 10, 30);
        String targetVariable = "outcome";
        List<String> featureNames = Arrays.asList("feature1", "feature2", "feature3");
        Dataset dataset = new Dataset();
        dataset.setId(2L);
        Double accuracy = 0.92;
        String modelMetadata = "New metadata";
        
        // Act
        model.setId(id);
        model.setModelName(modelName);
        model.setModelType(modelType);
        model.setSerializedModelPath(serializedModelPath);
        model.setTrainingDate(trainingDate);
        model.setTargetVariable(targetVariable);
        model.setFeatureNames(featureNames);
        model.setDataset(dataset);
        model.setAccuracy(accuracy);
        model.setModelMetadata(modelMetadata);
        
        // Assert
        assertEquals(id, model.getId());
        assertEquals(modelName, model.getModelName());
        assertEquals(modelType, model.getModelType());
        assertEquals(serializedModelPath, model.getSerializedModelPath());
        assertEquals(trainingDate, model.getTrainingDate());
        assertEquals(targetVariable, model.getTargetVariable());
        assertEquals(featureNames, model.getFeatureNames());
        assertEquals(dataset, model.getDataset());
        assertEquals(accuracy, model.getAccuracy());
        assertEquals(modelMetadata, model.getModelMetadata());
    }

    @Test
    void testEquals_sameObject_returnsTrue() {
        // Arrange
        MLModel model = createTestModel();
        
        // Act & Assert
        assertEquals(model, model);
    }

    @Test
    void testEquals_equalObjects_returnsTrue() {
        // Arrange
        MLModel model1 = createTestModel();
        MLModel model2 = createTestModel();
        
        // Act & Assert
        assertEquals(model1, model2);
    }

    @Test
    void testEquals_differentObjects_returnsFalse() {
        // Arrange
        MLModel model1 = createTestModel();
        MLModel model2 = createTestModel();
        model2.setModelName("Different");
        
        // Act & Assert
        assertNotEquals(model1, model2);
    }

    @Test
    void testEquals_nullObject_returnsFalse() {
        // Arrange
        MLModel model = createTestModel();
        
        // Act & Assert
        assertNotEquals(model, null);
    }

    @Test
    void testEquals_differentClass_returnsFalse() {
        // Arrange
        MLModel model = createTestModel();
        String other = "not a model";
        
        // Act & Assert
        assertNotEquals(model, other);
    }

    @Test
    void testHashCode_equalObjects_sameHashCode() {
        // Arrange
        MLModel model1 = createTestModel();
        MLModel model2 = createTestModel();
        
        // Act & Assert
        assertEquals(model1.hashCode(), model2.hashCode());
    }

    @Test
    void testToString_containsAllFields() {
        // Arrange
        MLModel model = createTestModel();
        
        // Act
        String toString = model.toString();
        
        // Assert
        assertTrue(toString.contains("1"));
        assertTrue(toString.contains("Test Model"));
        assertTrue(toString.contains("CLASSIFICATION"));
        assertTrue(toString.contains("/models/test.model"));
        assertTrue(toString.contains("target"));
        assertTrue(toString.contains("feature1"));
        assertTrue(toString.contains("0.85"));
    }

    @Test
    void testModelTypeEnum_values() {
        // Test enum values
        assertEquals("CLASSIFICATION", MLModel.ModelType.CLASSIFICATION.name());
        assertEquals("REGRESSION", MLModel.ModelType.REGRESSION.name());
    }

    @Test
    void testModelTypeEnum_ordinal() {
        // Test enum ordinal values
        assertEquals(0, MLModel.ModelType.CLASSIFICATION.ordinal());
        assertEquals(1, MLModel.ModelType.REGRESSION.ordinal());
    }

    @Test
    void testModelTypeEnum_valueOf() {
        // Test enum valueOf method
        assertEquals(MLModel.ModelType.CLASSIFICATION, MLModel.ModelType.valueOf("CLASSIFICATION"));
        assertEquals(MLModel.ModelType.REGRESSION, MLModel.ModelType.valueOf("REGRESSION"));
    }

    @Test
    void testModelTypeEnum_valuesMethod_returnsAllEnumValues() {
        // Test enum values method
        MLModel.ModelType[] values = MLModel.ModelType.values();
        assertEquals(2, values.length);
        assertTrue(Arrays.asList(values).contains(MLModel.ModelType.CLASSIFICATION));
        assertTrue(Arrays.asList(values).contains(MLModel.ModelType.REGRESSION));
    }

    @Test
    void testWithNullValues_handlesNullsCorrectly() {
        // Arrange
        MLModel model = new MLModel();
        
        // Act
        model.setId(null);
        model.setModelName(null);
        model.setModelType(null);
        model.setSerializedModelPath(null);
        model.setTrainingDate(null);
        model.setTargetVariable(null);
        model.setFeatureNames(null);
        model.setDataset(null);
        model.setAccuracy(null);
        model.setModelMetadata(null);
        
        // Assert
        assertNull(model.getId());
        assertNull(model.getModelName());
        assertNull(model.getModelType());
        assertNull(model.getSerializedModelPath());
        assertNull(model.getTrainingDate());
        assertNull(model.getTargetVariable());
        assertNull(model.getFeatureNames());
        assertNull(model.getDataset());
        assertNull(model.getAccuracy());
        assertNull(model.getModelMetadata());
    }

    @Test
    void testWithEmptyFeatureNames_handlesEmptyList() {
        // Arrange
        MLModel model = new MLModel();
        List<String> emptyFeatures = Collections.emptyList();
        
        // Act
        model.setFeatureNames(emptyFeatures);
        
        // Assert
        assertEquals(emptyFeatures, model.getFeatureNames());
        assertTrue(model.getFeatureNames().isEmpty());
    }

    @Test
    void testWithZeroAccuracy_handlesZero() {
        // Arrange
        MLModel model = new MLModel();
        
        // Act
        model.setAccuracy(0.0);
        
        // Assert
        assertEquals(Double.valueOf(0.0), model.getAccuracy());
    }

    @Test
    void testWithNegativeAccuracy_handlesNegative() {
        // Arrange
        MLModel model = new MLModel();
        
        // Act
        model.setAccuracy(-0.5);
        
        // Assert
        assertEquals(Double.valueOf(-0.5), model.getAccuracy());
    }

    @Test
    void testWithMaxAccuracy_handlesMaxValue() {
        // Arrange
        MLModel model = new MLModel();
        
        // Act
        model.setAccuracy(Double.MAX_VALUE);
        
        // Assert
        assertEquals(Double.MAX_VALUE, model.getAccuracy());
    }

    @Test
    void testWithMinAccuracy_handlesMinValue() {
        // Arrange
        MLModel model = new MLModel();
        
        // Act
        model.setAccuracy(Double.MIN_VALUE);
        
        // Assert
        assertEquals(Double.MIN_VALUE, model.getAccuracy());
    }

    @Test
    void testWithLongModelName_handlesLongStrings() {
        // Arrange
        MLModel model = new MLModel();
        String longModelName = "very_long_model_name_with_many_characters_and_numbers_12345";
        
        // Act
        model.setModelName(longModelName);
        
        // Assert
        assertEquals(longModelName, model.getModelName());
    }

    @Test
    void testWithLongSerializedModelPath_handlesLongStrings() {
        // Arrange
        MLModel model = new MLModel();
        String longPath = "/very/long/path/to/model/with/many/directories/and/characters/12345.model";
        
        // Act
        model.setSerializedModelPath(longPath);
        
        // Assert
        assertEquals(longPath, model.getSerializedModelPath());
    }

    @Test
    void testWithLongTargetVariable_handlesLongStrings() {
        // Arrange
        MLModel model = new MLModel();
        String longTargetVariable = "very_long_target_variable_with_many_characters_and_numbers_12345";
        
        // Act
        model.setTargetVariable(longTargetVariable);
        
        // Assert
        assertEquals(longTargetVariable, model.getTargetVariable());
    }

    @Test
    void testWithManyFeatureNames_handlesLargeLists() {
        // Arrange
        MLModel model = new MLModel();
        List<String> manyFeatures = Arrays.asList(
            "feature1", "feature2", "feature3", "feature4", "feature5",
            "feature6", "feature7", "feature8", "feature9", "feature10"
        );
        
        // Act
        model.setFeatureNames(manyFeatures);
        
        // Assert
        assertEquals(manyFeatures, model.getFeatureNames());
        assertEquals(10, model.getFeatureNames().size());
    }

    @Test
    void testWithLongModelMetadata_handlesLongStrings() {
        // Arrange
        MLModel model = new MLModel();
        String longMetadata = "This is a very long model metadata string that contains many characters and should be handled properly by the entity";
        
        // Act
        model.setModelMetadata(longMetadata);
        
        // Assert
        assertEquals(longMetadata, model.getModelMetadata());
    }

    @Test
    void testWithSpecialCharactersInModelName_handlesSpecialChars() {
        // Arrange
        MLModel model = new MLModel();
        String specialModelName = "model@domain.com";
        
        // Act
        model.setModelName(specialModelName);
        
        // Assert
        assertEquals(specialModelName, model.getModelName());
    }

    @Test
    void testWithSpecialCharactersInTargetVariable_handlesSpecialChars() {
        // Arrange
        MLModel model = new MLModel();
        String specialTargetVariable = "target@domain.com";
        
        // Act
        model.setTargetVariable(specialTargetVariable);
        
        // Assert
        assertEquals(specialTargetVariable, model.getTargetVariable());
    }

    @Test
    void testWithUnicodeCharacters_handlesUnicode() {
        // Arrange
        MLModel model = new MLModel();
        String unicodeModelName = "模型名称123";
        String unicodeTargetVariable = "目标变量456";
        
        // Act
        model.setModelName(unicodeModelName);
        model.setTargetVariable(unicodeTargetVariable);
        
        // Assert
        assertEquals(unicodeModelName, model.getModelName());
        assertEquals(unicodeTargetVariable, model.getTargetVariable());
    }

    @Test
    void testWithWhitespaceInModelName_handlesWhitespace() {
        // Arrange
        MLModel model = new MLModel();
        String modelNameWithWhitespace = " model name ";
        
        // Act
        model.setModelName(modelNameWithWhitespace);
        
        // Assert
        assertEquals(modelNameWithWhitespace, model.getModelName());
    }

    @Test
    void testWithWhitespaceInTargetVariable_handlesWhitespace() {
        // Arrange
        MLModel model = new MLModel();
        String targetVariableWithWhitespace = " target variable ";
        
        // Act
        model.setTargetVariable(targetVariableWithWhitespace);
        
        // Assert
        assertEquals(targetVariableWithWhitespace, model.getTargetVariable());
    }

    @Test
    void testWithDatasetRelationship_handlesDatasetRelationship() {
        // Arrange
        MLModel model = new MLModel();
        Dataset dataset = new Dataset();
        dataset.setId(1L);
        dataset.setFileName("test.csv");
        
        // Act
        model.setDataset(dataset);
        
        // Assert
        assertEquals(dataset, model.getDataset());
        assertEquals(Long.valueOf(1), model.getDataset().getId());
        assertEquals("test.csv", model.getDataset().getFileName());
    }

    @Test
    void testWithDifferentModelTypes_handlesBothTypes() {
        // Test both model types
        MLModel.ModelType[] modelTypes = {MLModel.ModelType.CLASSIFICATION, MLModel.ModelType.REGRESSION};
        
        for (MLModel.ModelType modelType : modelTypes) {
            // Arrange
            MLModel model = new MLModel();
            
            // Act
            model.setModelType(modelType);
            
            // Assert
            assertEquals(modelType, model.getModelType());
        }
    }

    @Test
    void testWithDifferentAccuracies_handlesDifferentAccuracies() {
        // Test different accuracy values
        Double[] testAccuracies = {0.0, 0.1, 0.5, 0.85, 0.99, 1.0};
        
        for (Double accuracy : testAccuracies) {
            // Arrange
            MLModel model = new MLModel();
            
            // Act
            model.setAccuracy(accuracy);
            
            // Assert
            assertEquals(accuracy, model.getAccuracy());
        }
    }

    @Test
    void testWithDifferentTrainingDates_handlesDifferentDates() {
        // Test different training dates
        LocalDateTime[] testDates = {
            LocalDateTime.of(2024, 1, 1, 0, 0),
            LocalDateTime.of(2024, 6, 15, 12, 30),
            LocalDateTime.of(2024, 12, 31, 23, 59),
            LocalDateTime.now()
        };
        
        for (LocalDateTime date : testDates) {
            // Arrange
            MLModel model = new MLModel();
            
            // Act
            model.setTrainingDate(date);
            
            // Assert
            assertEquals(date, model.getTrainingDate());
        }
    }

    @Test
    void testWithNaNAccuracy_handlesNaN() {
        // Arrange
        MLModel model = new MLModel();
        
        // Act
        model.setAccuracy(Double.NaN);
        
        // Assert
        assertEquals(Double.NaN, model.getAccuracy());
    }

    @Test
    void testWithInfinityAccuracy_handlesInfinity() {
        // Arrange
        MLModel model = new MLModel();
        
        // Act
        model.setAccuracy(Double.POSITIVE_INFINITY);
        
        // Assert
        assertEquals(Double.POSITIVE_INFINITY, model.getAccuracy());
    }

    private MLModel createTestModel() {
        MLModel model = new MLModel();
        model.setId(1L);
        model.setModelName("Test Model");
        model.setModelType(MLModel.ModelType.CLASSIFICATION);
        model.setSerializedModelPath("/models/test.model");
        model.setTrainingDate(LocalDateTime.of(2024, 1, 15, 10, 30));
        model.setTargetVariable("target");
        model.setFeatureNames(Arrays.asList("feature1", "feature2"));
        model.setAccuracy(0.85);
        model.setModelMetadata("Test metadata");
        
        Dataset dataset = new Dataset();
        dataset.setId(1L);
        dataset.setFileName("test.csv");
        model.setDataset(dataset);
        
        return model;
    }
}
