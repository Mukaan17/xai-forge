package com.example.xaiapp.integration.repository;

import com.example.xaiapp.entity.Dataset;
import com.example.xaiapp.entity.MLModel;
import com.example.xaiapp.entity.User;
import com.example.xaiapp.repository.DatasetRepository;
import com.example.xaiapp.repository.MLModelRepository;
import com.example.xaiapp.repository.UserRepository;
import com.example.xaiapp.util.TestConstants;
import com.example.xaiapp.util.TestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for MLModelRepository
 * Tests model queries and relationship integrity
 */
@DataJpaTest
@ActiveProfiles("test")
class MLModelRepositoryTest {
    
    @Autowired
    private TestEntityManager entityManager;
    
    @Autowired
    private MLModelRepository modelRepository;
    
    @Autowired
    private DatasetRepository datasetRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    private User testUser;
    private Dataset testDataset;
    private MLModel testModel;
    
    @BeforeEach
    void setUp() {
        modelRepository.deleteAll();
        datasetRepository.deleteAll();
        userRepository.deleteAll();
        
        testUser = TestDataBuilder.createTestUser();
        userRepository.save(testUser);
        
        testDataset = TestDataBuilder.createTestDataset(testUser);
        datasetRepository.save(testDataset);
        
        testModel = TestDataBuilder.createTestModel(testDataset);
    }
    
    @Test
    void testFindByDatasetOwnerId_Success() {
        // Arrange
        modelRepository.save(testModel);
        
        // Act
        List<MLModel> result = modelRepository.findByDatasetOwnerId(testUser.getId());
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testModel.getModelName(), result.get(0).getModelName());
        assertEquals(testUser.getId(), result.get(0).getDataset().getOwner().getId());
    }
    
    @Test
    void testFindByDatasetOwnerId_EmptyList() {
        // Act
        List<MLModel> result = modelRepository.findByDatasetOwnerId(testUser.getId());
        
        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
    
    @Test
    void testFindByDatasetOwnerId_NonExistentUser() {
        // Act
        List<MLModel> result = modelRepository.findByDatasetOwnerId(999L);
        
        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
    
    @Test
    void testFindByDatasetOwnerId_MultipleModels() {
        // Arrange
        MLModel model1 = TestDataBuilder.createTestModel(testDataset, "Model 1");
        MLModel model2 = TestDataBuilder.createTestModel(testDataset, "Model 2");
        modelRepository.save(model1);
        modelRepository.save(model2);
        
        // Act
        List<MLModel> result = modelRepository.findByDatasetOwnerId(testUser.getId());
        
        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
    }
    
    @Test
    void testFindByDatasetOwnerId_WithDifferentUsers() {
        // Arrange
        User otherUser = TestDataBuilder.createTestUser("otheruser");
        userRepository.save(otherUser);
        Dataset otherDataset = TestDataBuilder.createTestDataset(otherUser, "other-dataset.csv");
        datasetRepository.save(otherDataset);
        
        MLModel userModel = TestDataBuilder.createTestModel(testDataset, "User Model");
        MLModel otherModel = TestDataBuilder.createTestModel(otherDataset, "Other Model");
        modelRepository.save(userModel);
        modelRepository.save(otherModel);
        
        // Act
        List<MLModel> userResult = modelRepository.findByDatasetOwnerId(testUser.getId());
        List<MLModel> otherResult = modelRepository.findByDatasetOwnerId(otherUser.getId());
        
        // Assert
        assertNotNull(userResult);
        assertEquals(1, userResult.size());
        assertEquals("User Model", userResult.get(0).getModelName());
        
        assertNotNull(otherResult);
        assertEquals(1, otherResult.size());
        assertEquals("Other Model", otherResult.get(0).getModelName());
    }
    
    @Test
    void testFindByIdAndDatasetOwnerId_Success() {
        // Arrange
        modelRepository.save(testModel);
        
        // Act
        Optional<MLModel> result = modelRepository.findByIdAndDatasetOwnerId(testModel.getId(), testUser.getId());
        
        // Assert
        assertTrue(result.isPresent());
        assertEquals(testModel.getId(), result.get().getId());
        assertEquals(testModel.getModelName(), result.get().getModelName());
        assertEquals(testUser.getId(), result.get().getDataset().getOwner().getId());
    }
    
    @Test
    void testFindByIdAndDatasetOwnerId_NotFound() {
        // Act
        Optional<MLModel> result = modelRepository.findByIdAndDatasetOwnerId(999L, testUser.getId());
        
        // Assert
        assertFalse(result.isPresent());
    }
    
    @Test
    void testFindByIdAndDatasetOwnerId_WrongOwner() {
        // Arrange
        User otherUser = TestDataBuilder.createTestUser("otheruser");
        userRepository.save(otherUser);
        modelRepository.save(testModel);
        
        // Act
        Optional<MLModel> result = modelRepository.findByIdAndDatasetOwnerId(testModel.getId(), otherUser.getId());
        
        // Assert
        assertFalse(result.isPresent());
    }
    
    @Test
    void testFindByIdAndDatasetOwnerId_NullId() {
        // Act
        Optional<MLModel> result = modelRepository.findByIdAndDatasetOwnerId(null, testUser.getId());
        
        // Assert
        assertFalse(result.isPresent());
    }
    
    @Test
    void testFindByIdAndDatasetOwnerId_NullOwnerId() {
        // Arrange
        modelRepository.save(testModel);
        
        // Act
        Optional<MLModel> result = modelRepository.findByIdAndDatasetOwnerId(testModel.getId(), null);
        
        // Assert
        assertFalse(result.isPresent());
    }
    
    @Test
    void testFindByDataset_Success() {
        // Arrange
        modelRepository.save(testModel);
        
        // Act
        Optional<MLModel> result = modelRepository.findByDataset(testDataset);
        
        // Assert
        assertTrue(result.isPresent());
        assertEquals(testModel.getId(), result.get().getId());
        assertEquals(testModel.getModelName(), result.get().getModelName());
        assertEquals(testDataset.getId(), result.get().getDataset().getId());
    }
    
    @Test
    void testFindByDataset_NotFound() {
        // Act
        Optional<MLModel> result = modelRepository.findByDataset(testDataset);
        
        // Assert
        assertFalse(result.isPresent());
    }
    
    @Test
    void testFindByDataset_NullDataset() {
        // Act
        Optional<MLModel> result = modelRepository.findByDataset(null);
        
        // Assert
        assertFalse(result.isPresent());
    }
    
    @Test
    void testSave_WithValidData() {
        // Act
        MLModel savedModel = modelRepository.save(testModel);
        
        // Assert
        assertNotNull(savedModel.getId());
        assertEquals(testModel.getModelName(), savedModel.getModelName());
        assertEquals(testModel.getModelType(), savedModel.getModelType());
        assertEquals(testModel.getTargetVariable(), savedModel.getTargetVariable());
        assertEquals(testModel.getFeatureNames(), savedModel.getFeatureNames());
        assertEquals(testModel.getAccuracy(), savedModel.getAccuracy());
        assertEquals(testDataset.getId(), savedModel.getDataset().getId());
    }
    
    @Test
    void testSave_WithSpecialCharacters() {
        // Arrange
        MLModel specialModel = TestDataBuilder.createTestModel(testDataset, "Model with spaces");
        specialModel.setSerializedModelPath("/path/with spaces/model.model");
        
        // Act
        MLModel savedModel = modelRepository.save(specialModel);
        
        // Assert
        assertNotNull(savedModel.getId());
        assertEquals("Model with spaces", savedModel.getModelName());
        assertEquals("/path/with spaces/model.model", savedModel.getSerializedModelPath());
    }
    
    @Test
    void testSave_WithUnicodeCharacters() {
        // Arrange
        MLModel unicodeModel = TestDataBuilder.createTestModel(testDataset, "模型名称");
        unicodeModel.setSerializedModelPath("/path/模型.model");
        
        // Act
        MLModel savedModel = modelRepository.save(unicodeModel);
        
        // Assert
        assertNotNull(savedModel.getId());
        assertEquals("模型名称", savedModel.getModelName());
        assertEquals("/path/模型.model", savedModel.getSerializedModelPath());
    }
    
    @Test
    void testSave_WithLongValues() {
        // Arrange
        MLModel longModel = TestDataBuilder.createTestModel(testDataset, "a".repeat(100));
        longModel.setSerializedModelPath("/path/" + "a".repeat(100) + ".model");
        
        // Act
        MLModel savedModel = modelRepository.save(longModel);
        
        // Assert
        assertNotNull(savedModel.getId());
        assertEquals("a".repeat(100), savedModel.getModelName());
        assertEquals("/path/" + "a".repeat(100) + ".model", savedModel.getSerializedModelPath());
    }
    
    @Test
    void testSave_WithNullValues() {
        // Arrange
        MLModel nullModel = new MLModel();
        nullModel.setModelName(null);
        nullModel.setModelType(null);
        nullModel.setSerializedModelPath(null);
        nullModel.setTargetVariable(null);
        nullModel.setFeatureNames(null);
        nullModel.setDataset(null);
        nullModel.setAccuracy(null);
        
        // Act & Assert
        assertThrows(Exception.class, () -> {
            modelRepository.save(nullModel);
        });
    }
    
    @Test
    void testSave_WithEmptyValues() {
        // Arrange
        MLModel emptyModel = new MLModel();
        emptyModel.setModelName("");
        emptyModel.setModelType(MLModel.ModelType.CLASSIFICATION);
        emptyModel.setSerializedModelPath("");
        emptyModel.setTargetVariable("");
        emptyModel.setFeatureNames(List.of());
        emptyModel.setDataset(testDataset);
        emptyModel.setAccuracy(0.0);
        
        // Act & Assert
        assertThrows(Exception.class, () -> {
            modelRepository.save(emptyModel);
        });
    }
    
    @Test
    void testSave_WithWhitespaceValues() {
        // Arrange
        MLModel whitespaceModel = new MLModel();
        whitespaceModel.setModelName("   ");
        whitespaceModel.setModelType(MLModel.ModelType.CLASSIFICATION);
        whitespaceModel.setSerializedModelPath("   ");
        whitespaceModel.setTargetVariable("   ");
        whitespaceModel.setFeatureNames(List.of());
        whitespaceModel.setDataset(testDataset);
        whitespaceModel.setAccuracy(0.0);
        
        // Act & Assert
        assertThrows(Exception.class, () -> {
            modelRepository.save(whitespaceModel);
        });
    }
    
    @Test
    void testSave_WithExtremeValues() {
        // Arrange
        MLModel extremeModel = TestDataBuilder.createTestModel(testDataset, "a".repeat(1000));
        extremeModel.setSerializedModelPath("/path/" + "a".repeat(1000) + ".model");
        extremeModel.setAccuracy(Double.MAX_VALUE);
        
        // Act & Assert
        assertThrows(Exception.class, () -> {
            modelRepository.save(extremeModel);
        });
    }
    
    @Test
    void testSave_WithNegativeAccuracy() {
        // Arrange
        MLModel negativeAccuracyModel = TestDataBuilder.createTestModel(testDataset, "negative-accuracy");
        negativeAccuracyModel.setAccuracy(-1.0);
        
        // Act & Assert
        assertThrows(Exception.class, () -> {
            modelRepository.save(negativeAccuracyModel);
        });
    }
    
    @Test
    void testSave_WithAccuracyGreaterThanOne() {
        // Arrange
        MLModel highAccuracyModel = TestDataBuilder.createTestModel(testDataset, "high-accuracy");
        highAccuracyModel.setAccuracy(1.5);
        
        // Act & Assert
        assertThrows(Exception.class, () -> {
            modelRepository.save(highAccuracyModel);
        });
    }
    
    @Test
    void testSave_WithNullDataset() {
        // Arrange
        MLModel nullDatasetModel = TestDataBuilder.createTestModel(testDataset, "null-dataset");
        nullDatasetModel.setDataset(null);
        
        // Act & Assert
        assertThrows(Exception.class, () -> {
            modelRepository.save(nullDatasetModel);
        });
    }
    
    @Test
    void testSave_WithNullModelType() {
        // Arrange
        MLModel nullModelTypeModel = TestDataBuilder.createTestModel(testDataset, "null-model-type");
        nullModelTypeModel.setModelType(null);
        
        // Act & Assert
        assertThrows(Exception.class, () -> {
            modelRepository.save(nullModelTypeModel);
        });
    }
    
    @Test
    void testSave_WithNullFeatureNames() {
        // Arrange
        MLModel nullFeatureNamesModel = TestDataBuilder.createTestModel(testDataset, "null-feature-names");
        nullFeatureNamesModel.setFeatureNames(null);
        
        // Act & Assert
        assertThrows(Exception.class, () -> {
            modelRepository.save(nullFeatureNamesModel);
        });
    }
    
    @Test
    void testSave_WithEmptyFeatureNames() {
        // Arrange
        MLModel emptyFeatureNamesModel = TestDataBuilder.createTestModel(testDataset, "empty-feature-names");
        emptyFeatureNamesModel.setFeatureNames(List.of());
        
        // Act & Assert
        assertThrows(Exception.class, () -> {
            modelRepository.save(emptyFeatureNamesModel);
        });
    }
    
    @Test
    void testSave_WithWhitespaceFeatureNames() {
        // Arrange
        MLModel whitespaceFeatureNamesModel = TestDataBuilder.createTestModel(testDataset, "whitespace-feature-names");
        whitespaceFeatureNamesModel.setFeatureNames(List.of("   ", "   ", "   "));
        
        // Act & Assert
        assertThrows(Exception.class, () -> {
            modelRepository.save(whitespaceFeatureNamesModel);
        });
    }
    
    @Test
    void testSave_WithNullFeatureNameValues() {
        // Arrange
        MLModel nullFeatureNameValuesModel = TestDataBuilder.createTestModel(testDataset, "null-feature-name-values");
        nullFeatureNameValuesModel.setFeatureNames(List.of(null, null, null));
        
        // Act & Assert
        assertThrows(Exception.class, () -> {
            modelRepository.save(nullFeatureNameValuesModel);
        });
    }
    
    @Test
    void testSave_WithEmptyFeatureNameValues() {
        // Arrange
        MLModel emptyFeatureNameValuesModel = TestDataBuilder.createTestModel(testDataset, "empty-feature-name-values");
        emptyFeatureNameValuesModel.setFeatureNames(List.of("", "", ""));
        
        // Act & Assert
        assertThrows(Exception.class, () -> {
            modelRepository.save(emptyFeatureNameValuesModel);
        });
    }
    
    @Test
    void testSave_WithSpecialCharactersInFeatureNames() {
        // Arrange
        MLModel specialFeatureNamesModel = TestDataBuilder.createTestModel(testDataset, "special-feature-names");
        specialFeatureNamesModel.setFeatureNames(List.of("feature with spaces", "feature-with-dashes", "feature_with_underscores"));
        
        // Act
        MLModel savedModel = modelRepository.save(specialFeatureNamesModel);
        
        // Assert
        assertNotNull(savedModel.getId());
        assertEquals(List.of("feature with spaces", "feature-with-dashes", "feature_with_underscores"), savedModel.getFeatureNames());
    }
    
    @Test
    void testSave_WithUnicodeInFeatureNames() {
        // Arrange
        MLModel unicodeFeatureNamesModel = TestDataBuilder.createTestModel(testDataset, "unicode-feature-names");
        unicodeFeatureNamesModel.setFeatureNames(List.of("特征1", "特征2", "特征3"));
        
        // Act
        MLModel savedModel = modelRepository.save(unicodeFeatureNamesModel);
        
        // Assert
        assertNotNull(savedModel.getId());
        assertEquals(List.of("特征1", "特征2", "特征3"), savedModel.getFeatureNames());
    }
    
    @Test
    void testSave_WithLongFeatureNames() {
        // Arrange
        MLModel longFeatureNamesModel = TestDataBuilder.createTestModel(testDataset, "long-feature-names");
        longFeatureNamesModel.setFeatureNames(List.of("a".repeat(100), "b".repeat(100), "c".repeat(100)));
        
        // Act & Assert
        assertThrows(Exception.class, () -> {
            modelRepository.save(longFeatureNamesModel);
        });
    }

    @Test
    void testSave_WithDuplicateFeatureNames() {
        // Arrange
        MLModel duplicateFeatureNamesModel = TestDataBuilder.createTestModel(testDataset, "duplicate-feature-names");
        duplicateFeatureNamesModel.setFeatureNames(List.of("feature1", "feature1", "feature2"));
        
        // Act
        MLModel savedModel = modelRepository.save(duplicateFeatureNamesModel);
        
        // Assert
        assertNotNull(savedModel.getId());
        assertEquals(List.of("feature1", "feature1", "feature2"), savedModel.getFeatureNames());
    }
    
    @Test
    void testSave_WithNullModelName() {
        // Arrange
        MLModel nullModelNameModel = TestDataBuilder.createTestModel(testDataset, "null-model-name");
        nullModelNameModel.setModelName(null);
        
        // Act & Assert
        assertThrows(Exception.class, () -> {
            modelRepository.save(nullModelNameModel);
        });
    }
    
    @Test
    void testSave_WithEmptyModelName() {
        // Arrange
        MLModel emptyModelNameModel = TestDataBuilder.createTestModel(testDataset, "empty-model-name");
        emptyModelNameModel.setModelName("");
        
        // Act & Assert
        assertThrows(Exception.class, () -> {
            modelRepository.save(emptyModelNameModel);
        });
    }
    
    @Test
    void testSave_WithWhitespaceModelName() {
        // Arrange
        MLModel whitespaceModelNameModel = TestDataBuilder.createTestModel(testDataset, "whitespace-model-name");
        whitespaceModelNameModel.setModelName("   ");
        
        // Act & Assert
        assertThrows(Exception.class, () -> {
            modelRepository.save(whitespaceModelNameModel);
        });
    }
    
    @Test
    void testSave_WithNullSerializedModelPath() {
        // Arrange
        MLModel nullSerializedModelPathModel = TestDataBuilder.createTestModel(testDataset, "null-serialized-model-path");
        nullSerializedModelPathModel.setSerializedModelPath(null);
        
        // Act & Assert
        assertThrows(Exception.class, () -> {
            modelRepository.save(nullSerializedModelPathModel);
        });
    }
    
    @Test
    void testSave_WithEmptySerializedModelPath() {
        // Arrange
        MLModel emptySerializedModelPathModel = TestDataBuilder.createTestModel(testDataset, "empty-serialized-model-path");
        emptySerializedModelPathModel.setSerializedModelPath("");
        
        // Act & Assert
        assertThrows(Exception.class, () -> {
            modelRepository.save(emptySerializedModelPathModel);
        });
    }
    
    @Test
    void testSave_WithWhitespaceSerializedModelPath() {
        // Arrange
        MLModel whitespaceSerializedModelPathModel = TestDataBuilder.createTestModel(testDataset, "whitespace-serialized-model-path");
        whitespaceSerializedModelPathModel.setSerializedModelPath("   ");
        
        // Act & Assert
        assertThrows(Exception.class, () -> {
            modelRepository.save(whitespaceSerializedModelPathModel);
        });
    }
    
    @Test
    void testSave_WithNullTargetVariable() {
        // Arrange
        MLModel nullTargetVariableModel = TestDataBuilder.createTestModel(testDataset, "null-target-variable");
        nullTargetVariableModel.setTargetVariable(null);
        
        // Act & Assert
        assertThrows(Exception.class, () -> {
            modelRepository.save(nullTargetVariableModel);
        });
    }
    
    @Test
    void testSave_WithEmptyTargetVariable() {
        // Arrange
        MLModel emptyTargetVariableModel = TestDataBuilder.createTestModel(testDataset, "empty-target-variable");
        emptyTargetVariableModel.setTargetVariable("");
        
        // Act & Assert
        assertThrows(Exception.class, () -> {
            modelRepository.save(emptyTargetVariableModel);
        });
    }
    
    @Test
    void testSave_WithWhitespaceTargetVariable() {
        // Arrange
        MLModel whitespaceTargetVariableModel = TestDataBuilder.createTestModel(testDataset, "whitespace-target-variable");
        whitespaceTargetVariableModel.setTargetVariable("   ");
        
        // Act & Assert
        assertThrows(Exception.class, () -> {
            modelRepository.save(whitespaceTargetVariableModel);
        });
    }
}
    
  