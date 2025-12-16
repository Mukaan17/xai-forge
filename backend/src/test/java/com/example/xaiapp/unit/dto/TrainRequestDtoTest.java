package com.example.xaiapp.unit.dto;

import com.example.xaiapp.dto.TrainRequestDto;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for TrainRequestDto
 * Tests constructors, Lombok methods, and validation annotations
 */
class TrainRequestDtoTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testNoArgsConstructor_createsEmptyDto() {
        // Act
        TrainRequestDto dto = new TrainRequestDto();
        
        // Assert
        assertNotNull(dto);
        assertNull(dto.getDatasetId());
        assertNull(dto.getModelName());
        assertNull(dto.getTargetVariable());
        assertNull(dto.getFeatureNames());
        assertNull(dto.getModelType());
    }

    @Test
    void testAllArgsConstructor_createsCompleteDto() {
        // Arrange
        Long datasetId = 1L;
        String modelName = "Test Model";
        String targetVariable = "target";
        List<String> featureNames = Arrays.asList("feature1", "feature2");
        String modelType = "CLASSIFICATION";
        
        // Act
        TrainRequestDto dto = new TrainRequestDto(
            datasetId, modelName, targetVariable, featureNames, modelType
        );
        
        // Assert
        assertNotNull(dto);
        assertEquals(datasetId, dto.getDatasetId());
        assertEquals(modelName, dto.getModelName());
        assertEquals(targetVariable, dto.getTargetVariable());
        assertEquals(featureNames, dto.getFeatureNames());
        assertEquals(modelType, dto.getModelType());
    }

    @Test
    void testSetters_setAllFields() {
        // Arrange
        TrainRequestDto dto = new TrainRequestDto();
        Long datasetId = 2L;
        String modelName = "New Model";
        String targetVariable = "outcome";
        List<String> featureNames = Arrays.asList("feature1", "feature2", "feature3");
        String modelType = "REGRESSION";
        
        // Act
        dto.setDatasetId(datasetId);
        dto.setModelName(modelName);
        dto.setTargetVariable(targetVariable);
        dto.setFeatureNames(featureNames);
        dto.setModelType(modelType);
        
        // Assert
        assertEquals(datasetId, dto.getDatasetId());
        assertEquals(modelName, dto.getModelName());
        assertEquals(targetVariable, dto.getTargetVariable());
        assertEquals(featureNames, dto.getFeatureNames());
        assertEquals(modelType, dto.getModelType());
    }

    @Test
    void testEquals_sameObject_returnsTrue() {
        // Arrange
        TrainRequestDto dto = createTestDto();
        
        // Act & Assert
        assertEquals(dto, dto);
    }

    @Test
    void testEquals_equalObjects_returnsTrue() {
        // Arrange
        TrainRequestDto dto1 = createTestDto();
        TrainRequestDto dto2 = createTestDto();
        
        // Act & Assert
        assertEquals(dto1, dto2);
    }

    @Test
    void testEquals_differentObjects_returnsFalse() {
        // Arrange
        TrainRequestDto dto1 = createTestDto();
        TrainRequestDto dto2 = createTestDto();
        dto2.setModelName("Different");
        
        // Act & Assert
        assertNotEquals(dto1, dto2);
    }

    @Test
    void testEquals_nullObject_returnsFalse() {
        // Arrange
        TrainRequestDto dto = createTestDto();
        
        // Act & Assert
        assertNotEquals(dto, null);
    }

    @Test
    void testEquals_differentClass_returnsFalse() {
        // Arrange
        TrainRequestDto dto = createTestDto();
        String other = "not a dto";
        
        // Act & Assert
        assertNotEquals(dto, other);
    }

    @Test
    void testHashCode_equalObjects_sameHashCode() {
        // Arrange
        TrainRequestDto dto1 = createTestDto();
        TrainRequestDto dto2 = createTestDto();
        
        // Act & Assert
        assertEquals(dto1.hashCode(), dto2.hashCode());
    }

    @Test
    void testToString_containsAllFields() {
        // Arrange
        TrainRequestDto dto = createTestDto();
        
        // Act
        String toString = dto.toString();
        
        // Assert
        assertTrue(toString.contains("1"));
        assertTrue(toString.contains("Test Model"));
        assertTrue(toString.contains("target"));
        assertTrue(toString.contains("feature1"));
        assertTrue(toString.contains("CLASSIFICATION"));
    }

    @Test
    void testValidation_validDto_noViolations() {
        // Arrange
        TrainRequestDto dto = new TrainRequestDto(
            1L, "Valid Model", "target", Arrays.asList("feature1", "feature2"), "CLASSIFICATION"
        );
        
        // Act
        Set<ConstraintViolation<TrainRequestDto>> violations = validator.validate(dto);
        
        // Assert
        assertTrue(violations.isEmpty());
    }

    @Test
    void testValidation_nullDatasetId_violation() {
        // Arrange
        TrainRequestDto dto = new TrainRequestDto(
            null, "Model", "target", Arrays.asList("feature1"), "CLASSIFICATION"
        );
        
        // Act
        Set<ConstraintViolation<TrainRequestDto>> violations = validator.validate(dto);
        
        // Assert
        assertEquals(1, violations.size());
        ConstraintViolation<TrainRequestDto> violation = violations.iterator().next();
        assertEquals("datasetId", violation.getPropertyPath().toString());
        assertEquals("Dataset ID is required", violation.getMessage());
    }

    @Test
    void testValidation_nullModelName_violation() {
        // Arrange
        TrainRequestDto dto = new TrainRequestDto(
            1L, null, "target", Arrays.asList("feature1"), "CLASSIFICATION"
        );
        
        // Act
        Set<ConstraintViolation<TrainRequestDto>> violations = validator.validate(dto);
        
        // Assert
        assertEquals(1, violations.size());
        ConstraintViolation<TrainRequestDto> violation = violations.iterator().next();
        assertEquals("modelName", violation.getPropertyPath().toString());
        assertEquals("Model name is required", violation.getMessage());
    }

    @Test
    void testValidation_blankModelName_violation() {
        // Arrange
        TrainRequestDto dto = new TrainRequestDto(
            1L, "", "target", Arrays.asList("feature1"), "CLASSIFICATION"
        );
        
        // Act
        Set<ConstraintViolation<TrainRequestDto>> violations = validator.validate(dto);
        
        // Assert
        assertEquals(1, violations.size());
        ConstraintViolation<TrainRequestDto> violation = violations.iterator().next();
        assertEquals("modelName", violation.getPropertyPath().toString());
        assertEquals("Model name is required", violation.getMessage());
    }

    @Test
    void testValidation_whitespaceModelName_violation() {
        // Arrange
        TrainRequestDto dto = new TrainRequestDto(
            1L, "   ", "target", Arrays.asList("feature1"), "CLASSIFICATION"
        );
        
        // Act
        Set<ConstraintViolation<TrainRequestDto>> violations = validator.validate(dto);
        
        // Assert
        assertEquals(1, violations.size());
        ConstraintViolation<TrainRequestDto> violation = violations.iterator().next();
        assertEquals("modelName", violation.getPropertyPath().toString());
        assertEquals("Model name is required", violation.getMessage());
    }

    @Test
    void testValidation_nullTargetVariable_violation() {
        // Arrange
        TrainRequestDto dto = new TrainRequestDto(
            1L, "Model", null, Arrays.asList("feature1"), "CLASSIFICATION"
        );
        
        // Act
        Set<ConstraintViolation<TrainRequestDto>> violations = validator.validate(dto);
        
        // Assert
        assertEquals(1, violations.size());
        ConstraintViolation<TrainRequestDto> violation = violations.iterator().next();
        assertEquals("targetVariable", violation.getPropertyPath().toString());
        assertEquals("Target variable is required", violation.getMessage());
    }

    @Test
    void testValidation_blankTargetVariable_violation() {
        // Arrange
        TrainRequestDto dto = new TrainRequestDto(
            1L, "Model", "", Arrays.asList("feature1"), "CLASSIFICATION"
        );
        
        // Act
        Set<ConstraintViolation<TrainRequestDto>> violations = validator.validate(dto);
        
        // Assert
        assertEquals(1, violations.size());
        ConstraintViolation<TrainRequestDto> violation = violations.iterator().next();
        assertEquals("targetVariable", violation.getPropertyPath().toString());
        assertEquals("Target variable is required", violation.getMessage());
    }

    @Test
    void testValidation_nullFeatureNames_violation() {
        // Arrange
        TrainRequestDto dto = new TrainRequestDto(
            1L, "Model", "target", null, "CLASSIFICATION"
        );
        
        // Act
        Set<ConstraintViolation<TrainRequestDto>> violations = validator.validate(dto);
        
        // Assert
        assertEquals(1, violations.size());
        ConstraintViolation<TrainRequestDto> violation = violations.iterator().next();
        assertEquals("featureNames", violation.getPropertyPath().toString());
        assertEquals("Feature names are required", violation.getMessage());
    }

    @Test
    void testValidation_emptyFeatureNames_violation() {
        // Arrange
        TrainRequestDto dto = new TrainRequestDto(
            1L, "Model", "target", Collections.emptyList(), "CLASSIFICATION"
        );
        
        // Act
        Set<ConstraintViolation<TrainRequestDto>> violations = validator.validate(dto);
        
        // Assert
        assertEquals(1, violations.size());
        ConstraintViolation<TrainRequestDto> violation = violations.iterator().next();
        assertEquals("featureNames", violation.getPropertyPath().toString());
        assertEquals("Feature names are required", violation.getMessage());
    }

    @Test
    void testValidation_nullModelType_violation() {
        // Arrange
        TrainRequestDto dto = new TrainRequestDto(
            1L, "Model", "target", Arrays.asList("feature1"), null
        );
        
        // Act
        Set<ConstraintViolation<TrainRequestDto>> violations = validator.validate(dto);
        
        // Assert
        assertEquals(1, violations.size());
        ConstraintViolation<TrainRequestDto> violation = violations.iterator().next();
        assertEquals("modelType", violation.getPropertyPath().toString());
        assertEquals("Model type is required", violation.getMessage());
    }

    @Test
    void testValidation_blankModelType_violation() {
        // Arrange
        TrainRequestDto dto = new TrainRequestDto(
            1L, "Model", "target", Arrays.asList("feature1"), ""
        );
        
        // Act
        Set<ConstraintViolation<TrainRequestDto>> violations = validator.validate(dto);
        
        // Assert
        assertEquals(1, violations.size());
        ConstraintViolation<TrainRequestDto> violation = violations.iterator().next();
        assertEquals("modelType", violation.getPropertyPath().toString());
        assertEquals("Model type is required", violation.getMessage());
    }

    @Test
    void testValidation_multipleViolations_returnsAllViolations() {
        // Arrange
        TrainRequestDto dto = new TrainRequestDto(
            null, "", "", Collections.emptyList(), ""
        );
        
        // Act
        Set<ConstraintViolation<TrainRequestDto>> violations = validator.validate(dto);
        
        // Assert
        assertEquals(5, violations.size());
        
        boolean hasDatasetIdViolation = violations.stream()
            .anyMatch(v -> "datasetId".equals(v.getPropertyPath().toString()));
        boolean hasModelNameViolation = violations.stream()
            .anyMatch(v -> "modelName".equals(v.getPropertyPath().toString()));
        boolean hasTargetVariableViolation = violations.stream()
            .anyMatch(v -> "targetVariable".equals(v.getPropertyPath().toString()));
        boolean hasFeatureNamesViolation = violations.stream()
            .anyMatch(v -> "featureNames".equals(v.getPropertyPath().toString()));
        boolean hasModelTypeViolation = violations.stream()
            .anyMatch(v -> "modelType".equals(v.getPropertyPath().toString()));
            
        assertTrue(hasDatasetIdViolation);
        assertTrue(hasModelNameViolation);
        assertTrue(hasTargetVariableViolation);
        assertTrue(hasFeatureNamesViolation);
        assertTrue(hasModelTypeViolation);
    }

    @Test
    void testWithNullValues_handlesNullsCorrectly() {
        // Arrange
        TrainRequestDto dto = new TrainRequestDto();
        
        // Act
        dto.setDatasetId(null);
        dto.setModelName(null);
        dto.setTargetVariable(null);
        dto.setFeatureNames(null);
        dto.setModelType(null);
        
        // Assert
        assertNull(dto.getDatasetId());
        assertNull(dto.getModelName());
        assertNull(dto.getTargetVariable());
        assertNull(dto.getFeatureNames());
        assertNull(dto.getModelType());
    }

    @Test
    void testWithEmptyStrings_handlesEmptyStrings() {
        // Arrange
        TrainRequestDto dto = new TrainRequestDto();
        
        // Act
        dto.setModelName("");
        dto.setTargetVariable("");
        dto.setModelType("");
        
        // Assert
        assertEquals("", dto.getModelName());
        assertEquals("", dto.getTargetVariable());
        assertEquals("", dto.getModelType());
    }

    @Test
    void testWithEmptyFeatureNames_handlesEmptyList() {
        // Arrange
        TrainRequestDto dto = new TrainRequestDto();
        List<String> emptyFeatures = Collections.emptyList();
        
        // Act
        dto.setFeatureNames(emptyFeatures);
        
        // Assert
        assertEquals(emptyFeatures, dto.getFeatureNames());
        assertTrue(dto.getFeatureNames().isEmpty());
    }

    @Test
    void testWithZeroDatasetId_handlesZero() {
        // Arrange
        TrainRequestDto dto = new TrainRequestDto();
        
        // Act
        dto.setDatasetId(0L);
        
        // Assert
        assertEquals(Long.valueOf(0), dto.getDatasetId());
    }

    @Test
    void testWithNegativeDatasetId_handlesNegativeValues() {
        // Arrange
        TrainRequestDto dto = new TrainRequestDto();
        
        // Act
        dto.setDatasetId(-1L);
        
        // Assert
        assertEquals(Long.valueOf(-1), dto.getDatasetId());
    }

    @Test
    void testWithMaxDatasetId_handlesMaxValue() {
        // Arrange
        TrainRequestDto dto = new TrainRequestDto();
        
        // Act
        dto.setDatasetId(Long.MAX_VALUE);
        
        // Assert
        assertEquals(Long.MAX_VALUE, dto.getDatasetId());
    }

    @Test
    void testWithLongModelName_handlesLongStrings() {
        // Arrange
        TrainRequestDto dto = new TrainRequestDto();
        String longModelName = "very_long_model_name_with_many_characters_and_numbers_12345";
        
        // Act
        dto.setModelName(longModelName);
        
        // Assert
        assertEquals(longModelName, dto.getModelName());
    }

    @Test
    void testWithLongTargetVariable_handlesLongStrings() {
        // Arrange
        TrainRequestDto dto = new TrainRequestDto();
        String longTargetVariable = "very_long_target_variable_with_many_characters_and_numbers_12345";
        
        // Act
        dto.setTargetVariable(longTargetVariable);
        
        // Assert
        assertEquals(longTargetVariable, dto.getTargetVariable());
    }

    @Test
    void testWithManyFeatureNames_handlesLargeLists() {
        // Arrange
        TrainRequestDto dto = new TrainRequestDto();
        List<String> manyFeatures = Arrays.asList(
            "feature1", "feature2", "feature3", "feature4", "feature5",
            "feature6", "feature7", "feature8", "feature9", "feature10"
        );
        
        // Act
        dto.setFeatureNames(manyFeatures);
        
        // Assert
        assertEquals(manyFeatures, dto.getFeatureNames());
        assertEquals(10, dto.getFeatureNames().size());
    }

    @Test
    void testWithSpecialCharactersInModelName_handlesSpecialChars() {
        // Arrange
        TrainRequestDto dto = new TrainRequestDto();
        String specialModelName = "model@domain.com";
        
        // Act
        dto.setModelName(specialModelName);
        
        // Assert
        assertEquals(specialModelName, dto.getModelName());
    }

    @Test
    void testWithSpecialCharactersInTargetVariable_handlesSpecialChars() {
        // Arrange
        TrainRequestDto dto = new TrainRequestDto();
        String specialTargetVariable = "target@domain.com";
        
        // Act
        dto.setTargetVariable(specialTargetVariable);
        
        // Assert
        assertEquals(specialTargetVariable, dto.getTargetVariable());
    }

    @Test
    void testWithUnicodeCharacters_handlesUnicode() {
        // Arrange
        TrainRequestDto dto = new TrainRequestDto();
        String unicodeModelName = "模型名称123";
        String unicodeTargetVariable = "目标变量456";
        
        // Act
        dto.setModelName(unicodeModelName);
        dto.setTargetVariable(unicodeTargetVariable);
        
        // Assert
        assertEquals(unicodeModelName, dto.getModelName());
        assertEquals(unicodeTargetVariable, dto.getTargetVariable());
    }

    @Test
    void testWithWhitespaceInModelName_handlesWhitespace() {
        // Arrange
        TrainRequestDto dto = new TrainRequestDto();
        String modelNameWithWhitespace = " model name ";
        
        // Act
        dto.setModelName(modelNameWithWhitespace);
        
        // Assert
        assertEquals(modelNameWithWhitespace, dto.getModelName());
    }

    @Test
    void testWithWhitespaceInTargetVariable_handlesWhitespace() {
        // Arrange
        TrainRequestDto dto = new TrainRequestDto();
        String targetVariableWithWhitespace = " target variable ";
        
        // Act
        dto.setTargetVariable(targetVariableWithWhitespace);
        
        // Assert
        assertEquals(targetVariableWithWhitespace, dto.getTargetVariable());
    }

    @Test
    void testWithValidModelTypes_handlesValidTypes() {
        // Arrange
        TrainRequestDto dto = new TrainRequestDto();
        
        // Act & Assert
        dto.setModelType("CLASSIFICATION");
        assertEquals("CLASSIFICATION", dto.getModelType());
        
        dto.setModelType("REGRESSION");
        assertEquals("REGRESSION", dto.getModelType());
        
        dto.setModelType("classification");
        assertEquals("classification", dto.getModelType());
        
        dto.setModelType("regression");
        assertEquals("regression", dto.getModelType());
    }

    private TrainRequestDto createTestDto() {
        return new TrainRequestDto(
            1L,
            "Test Model",
            "target",
            Arrays.asList("feature1", "feature2"),
            "CLASSIFICATION"
        );
    }
}
