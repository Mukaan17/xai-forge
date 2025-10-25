/**
 * @Author: Mukhil Sundararaj
 * @Date:   2025-10-24 18:59:00
 * @Last Modified by:   Mukhil Sundararaj
 * @Last Modified time: 2025-10-24 19:05:35
 */
package com.example.xaiapp.unit.factory;

import com.example.xaiapp.entity.MLModel;
import com.example.xaiapp.factory.AlgorithmFactory;
import com.example.xaiapp.util.TestConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.tribuo.MutableDataset;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for AlgorithmFactory
 * Tests algorithm selection and dataset loading for classification and regression
 */
@ExtendWith(MockitoExtension.class)
class AlgorithmFactoryTest {
    
    @InjectMocks
    private AlgorithmFactory algorithmFactory;
    
    private Path testCsvPath;
    private String testTargetVariable;
    private List<String> testFeatureNames;
    
    @BeforeEach
    void setUp() {
        testCsvPath = Paths.get(TestConstants.TEST_DATASET_PATH);
        testTargetVariable = TestConstants.TEST_TARGET_VARIABLE;
        testFeatureNames = Arrays.asList(TestConstants.TEST_FEATURE_NAMES);
    }
    
    @Test
    void testLoadDatasetFromCSV_Classification() throws Exception {
        // Act
        MutableDataset<?> dataset = algorithmFactory.loadDatasetFromCSV(
            testCsvPath, 
            testTargetVariable, 
            testFeatureNames, 
            MLModel.ModelType.CLASSIFICATION
        );
        
        // Assert
        assertNotNull(dataset);
        assertTrue(dataset.size() > 0);
        // Verify it's a classification dataset
        assertTrue(dataset.getOutputIDInfo().getDomain() instanceof org.tribuo.classification.LabelInfo);
    }
    
    @Test
    void testLoadDatasetFromCSV_Regression() throws Exception {
        // Act
        MutableDataset<?> dataset = algorithmFactory.loadDatasetFromCSV(
            testCsvPath, 
            testTargetVariable, 
            testFeatureNames, 
            MLModel.ModelType.REGRESSION
        );
        
        // Assert
        assertNotNull(dataset);
        assertTrue(dataset.size() > 0);
        // Verify it's a regression dataset
        assertTrue(dataset.getOutputIDInfo().getDomain() instanceof org.tribuo.regression.Regressor);
    }
    
    @Test
    void testLoadDatasetFromCSV_InvalidModelType() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            algorithmFactory.loadDatasetFromCSV(
                testCsvPath, 
                testTargetVariable, 
                testFeatureNames, 
                null
            );
        });
    }
    
    @Test
    void testLoadDatasetFromCSV_NonExistentFile() {
        // Arrange
        Path nonExistentPath = Paths.get("non-existent-file.csv");
        
        // Act & Assert
        assertThrows(Exception.class, () -> {
            algorithmFactory.loadDatasetFromCSV(
                nonExistentPath, 
                testTargetVariable, 
                testFeatureNames, 
                MLModel.ModelType.CLASSIFICATION
            );
        });
    }
    
    @Test
    void testLoadDatasetFromCSV_EmptyFile() {
        // Arrange
        Path emptyFilePath = Paths.get(TestConstants.TEST_INVALID_DATASET_PATH);
        
        // Act & Assert
        assertThrows(Exception.class, () -> {
            algorithmFactory.loadDatasetFromCSV(
                emptyFilePath, 
                testTargetVariable, 
                testFeatureNames, 
                MLModel.ModelType.CLASSIFICATION
            );
        });
    }
    
    @Test
    void testLoadDatasetFromCSV_MissingHeader() {
        // Arrange
        Path missingHeaderPath = Paths.get("src/test/resources/test-datasets/test-invalid-missing-header.csv");
        
        // Act & Assert
        assertThrows(Exception.class, () -> {
            algorithmFactory.loadDatasetFromCSV(
                missingHeaderPath, 
                testTargetVariable, 
                testFeatureNames, 
                MLModel.ModelType.CLASSIFICATION
            );
        });
    }
    
    @Test
    void testLoadDatasetFromCSV_NullPath() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> {
            algorithmFactory.loadDatasetFromCSV(
                null, 
                testTargetVariable, 
                testFeatureNames, 
                MLModel.ModelType.CLASSIFICATION
            );
        });
    }
    
    @Test
    void testLoadDatasetFromCSV_NullTargetVariable() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> {
            algorithmFactory.loadDatasetFromCSV(
                testCsvPath, 
                null, 
                testFeatureNames, 
                MLModel.ModelType.CLASSIFICATION
            );
        });
    }
    
    @Test
    void testLoadDatasetFromCSV_NullFeatureNames() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> {
            algorithmFactory.loadDatasetFromCSV(
                testCsvPath, 
                testTargetVariable, 
                null, 
                MLModel.ModelType.CLASSIFICATION
            );
        });
    }
    
    @Test
    void testLoadDatasetFromCSV_EmptyFeatureNames() {
        // Arrange
        List<String> emptyFeatures = Arrays.asList();
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            algorithmFactory.loadDatasetFromCSV(
                testCsvPath, 
                testTargetVariable, 
                emptyFeatures, 
                MLModel.ModelType.CLASSIFICATION
            );
        });
    }
    
    @Test
    void testLoadDatasetFromCSV_InvalidTargetVariable() {
        // Arrange
        String invalidTarget = "non_existent_column";
        
        // Act & Assert
        assertThrows(Exception.class, () -> {
            algorithmFactory.loadDatasetFromCSV(
                testCsvPath, 
                invalidTarget, 
                testFeatureNames, 
                MLModel.ModelType.CLASSIFICATION
            );
        });
    }
    
    @Test
    void testLoadDatasetFromCSV_InvalidFeatureNames() {
        // Arrange
        List<String> invalidFeatures = Arrays.asList("non_existent_feature1", "non_existent_feature2");
        
        // Act & Assert
        assertThrows(Exception.class, () -> {
            algorithmFactory.loadDatasetFromCSV(
                testCsvPath, 
                testTargetVariable, 
                invalidFeatures, 
                MLModel.ModelType.CLASSIFICATION
            );
        });
    }
    
    @Test
    void testLoadDatasetFromCSV_SpecialCharacters() throws Exception {
        // Arrange
        Path specialCharsPath = Paths.get("src/test/resources/test-datasets/test-special-chars.csv");
        String specialTarget = "salary";
        List<String> specialFeatures = Arrays.asList("name", "age", "city");
        
        // Act
        MutableDataset<?> dataset = algorithmFactory.loadDatasetFromCSV(
            specialCharsPath, 
            specialTarget, 
            specialFeatures, 
            MLModel.ModelType.REGRESSION
        );
        
        // Assert
        assertNotNull(dataset);
        assertTrue(dataset.size() > 0);
    }
    
    @Test
    void testLoadDatasetFromCSV_LargeDataset() throws Exception {
        // Arrange
        Path largeDatasetPath = Paths.get(TestConstants.TEST_LARGE_DATASET_PATH);
        String largeTarget = "price";
        List<String> largeFeatures = Arrays.asList("bedrooms", "bathrooms", "sqft");
        
        // Act
        MutableDataset<?> dataset = algorithmFactory.loadDatasetFromCSV(
            largeDatasetPath, 
            largeTarget, 
            largeFeatures, 
            MLModel.ModelType.REGRESSION
        );
        
        // Assert
        assertNotNull(dataset);
        assertTrue(dataset.size() > 0);
    }
    
    @Test
    void testLoadDatasetFromCSV_DuplicateFeatureNames() {
        // Arrange
        List<String> duplicateFeatures = Arrays.asList("feature1", "feature1", "feature2");
        
        // Act & Assert
        assertThrows(Exception.class, () -> {
            algorithmFactory.loadDatasetFromCSV(
                testCsvPath, 
                testTargetVariable, 
                duplicateFeatures, 
                MLModel.ModelType.CLASSIFICATION
            );
        });
    }
    
    @Test
    void testLoadDatasetFromCSV_EmptyTargetVariable() {
        // Arrange
        String emptyTarget = "";
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            algorithmFactory.loadDatasetFromCSV(
                testCsvPath, 
                emptyTarget, 
                testFeatureNames, 
                MLModel.ModelType.CLASSIFICATION
            );
        });
    }
    
    @Test
    void testLoadDatasetFromCSV_WhitespaceTargetVariable() {
        // Arrange
        String whitespaceTarget = "   ";
        
        // Act & Assert
        assertThrows(Exception.class, () -> {
            algorithmFactory.loadDatasetFromCSV(
                testCsvPath, 
                whitespaceTarget, 
                testFeatureNames, 
                MLModel.ModelType.CLASSIFICATION
            );
        });
    }
    
    @Test
    void testLoadDatasetFromCSV_WhitespaceFeatureNames() {
        // Arrange
        List<String> whitespaceFeatures = Arrays.asList("   ", "\t", "\n");
        
        // Act & Assert
        assertThrows(Exception.class, () -> {
            algorithmFactory.loadDatasetFromCSV(
                testCsvPath, 
                testTargetVariable, 
                whitespaceFeatures, 
                MLModel.ModelType.CLASSIFICATION
            );
        });
    }
    
    @Test
    void testLoadDatasetFromCSV_MixedValidInvalidFeatures() {
        // Arrange
        List<String> mixedFeatures = Arrays.asList("feature1", "invalid_feature", "feature3");
        
        // Act & Assert
        assertThrows(Exception.class, () -> {
            algorithmFactory.loadDatasetFromCSV(
                testCsvPath, 
                testTargetVariable, 
                mixedFeatures, 
                MLModel.ModelType.CLASSIFICATION
            );
        });
    }
    
    @Test
    void testLoadDatasetFromCSV_CorruptedFile() {
        // Arrange
        Path corruptedPath = Paths.get("src/test/resources/test-datasets/test-invalid-empty.csv");
        
        // Act & Assert
        assertThrows(Exception.class, () -> {
            algorithmFactory.loadDatasetFromCSV(
                corruptedPath, 
                testTargetVariable, 
                testFeatureNames, 
                MLModel.ModelType.CLASSIFICATION
            );
        });
    }
    
    @Test
    void testLoadDatasetFromCSV_UnicodeContent() throws Exception {
        // Arrange
        Path unicodePath = Paths.get("src/test/resources/test-datasets/test-special-chars.csv");
        String unicodeTarget = "salary";
        List<String> unicodeFeatures = Arrays.asList("name", "age", "city");
        
        // Act
        MutableDataset<?> dataset = algorithmFactory.loadDatasetFromCSV(
            unicodePath, 
            unicodeTarget, 
            unicodeFeatures, 
            MLModel.ModelType.REGRESSION
        );
        
        // Assert
        assertNotNull(dataset);
        assertTrue(dataset.size() > 0);
    }
    
    @Test
    void testLoadDatasetFromCSV_ExtremeValues() throws Exception {
        // Arrange
        Path extremePath = Paths.get(TestConstants.TEST_LARGE_DATASET_PATH);
        String extremeTarget = "price";
        List<String> extremeFeatures = Arrays.asList("bedrooms", "bathrooms", "sqft");
        
        // Act
        MutableDataset<?> dataset = algorithmFactory.loadDatasetFromCSV(
            extremePath, 
            extremeTarget, 
            extremeFeatures, 
            MLModel.ModelType.REGRESSION
        );
        
        // Assert
        assertNotNull(dataset);
        assertTrue(dataset.size() > 0);
    }
}
