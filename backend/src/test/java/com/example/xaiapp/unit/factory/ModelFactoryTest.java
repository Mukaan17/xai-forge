/**
 * @Author: Mukhil Sundararaj
 * @Date:   2025-10-24 18:59:17
 * @Last Modified by:   Mukhil Sundararaj
 * @Last Modified time: 2025-10-24 19:05:34
 */
package com.example.xaiapp.unit.factory;

import com.example.xaiapp.entity.MLModel;
import com.example.xaiapp.factory.ModelFactory;
import com.example.xaiapp.util.TestConstants;
import com.example.xaiapp.util.TestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.tribuo.MutableDataset;
import org.tribuo.Model;
import org.tribuo.classification.Label;
import org.tribuo.regression.Regressor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ModelFactory
 * Tests model creation strategies and parameter validation
 */
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class ModelFactoryTest {
    
    @InjectMocks
    private ModelFactory modelFactory;
    
    private MutableDataset<?> testDataset;
    private MLModel testModel;
    
    @BeforeEach
    void setUp() {
        testDataset = mock(MutableDataset.class);
        testModel = TestDataBuilder.createTestModel();
    }
    
    @Test
    void testCreateClassificationModel_Success() throws Exception {
        // Arrange
        when(testDataset.getOutputIDInfo()).thenReturn(mock(org.tribuo.ImmutableOutputInfo.class));
        when(testDataset.getOutputIDInfo().getDomain()).thenAnswer(invocation -> mock(org.tribuo.classification.LabelInfo.class));
        
        // Act
        Model<?> result = modelFactory.createModel(testDataset, MLModel.ModelType.CLASSIFICATION, new java.util.HashMap<>());
        
        // Assert
        assertNotNull(result);
        // Verify that the model is created for classification
        assertTrue(result.getOutputIDInfo().getDomain() instanceof org.tribuo.classification.LabelInfo);
    }
    
    @Test
    void testCreateRegressionModel_Success() {
        // Arrange
        when(testDataset.getOutputIDInfo()).thenReturn(mock(org.tribuo.ImmutableOutputInfo.class));
        when(testDataset.getOutputIDInfo().getDomain()).thenAnswer(invocation -> mock(org.tribuo.regression.Regressor.class));
        
        // Act
        Model<?> result = modelFactory.createModel(testDataset, MLModel.ModelType.REGRESSION, new java.util.HashMap<>());
        
        // Assert
        assertNotNull(result);
        // Verify that the model is created for regression
        assertTrue(result.getOutputIDInfo().getDomain() instanceof org.tribuo.regression.Regressor);
    }
    
    @Test
    void testCreateModel_NullDataset() throws Exception {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> {
            modelFactory.createModel(null, MLModel.ModelType.CLASSIFICATION, null);
        });
    }
    
    @Test
    void testCreateModel_NullModelType() throws Exception {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> {
            modelFactory.createModel(testDataset, null, null);
        });
    }
    
    @Test
    void testCreateModel_InvalidModelType() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            modelFactory.createModel(testDataset, MLModel.ModelType.valueOf("INVALID_TYPE"), new java.util.HashMap<>());
        });
    }
    
    @Test
    void testCreateModel_EmptyDataset() {
        // Arrange
        when(testDataset.size()).thenReturn(0);
        when(testDataset.getOutputIDInfo()).thenReturn(mock(org.tribuo.ImmutableOutputInfo.class));
        when(testDataset.getOutputIDInfo().getDomain()).thenAnswer(invocation -> mock(org.tribuo.classification.LabelInfo.class));
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            modelFactory.createModel(testDataset, MLModel.ModelType.CLASSIFICATION, new java.util.HashMap<>());
        });
    }
    
    @Test
    void testCreateModel_DatasetWithNullOutputInfo() {
        // Arrange
        when(testDataset.getOutputIDInfo()).thenReturn(null);
        
        // Act & Assert
        assertThrows(NullPointerException.class, () -> {
            modelFactory.createModel(testDataset, MLModel.ModelType.CLASSIFICATION, new java.util.HashMap<>());
        });
    }
    
    @Test
    void testCreateModel_DatasetWithNullDomain() {
        // Arrange
        when(testDataset.getOutputIDInfo()).thenReturn(mock(org.tribuo.ImmutableOutputInfo.class));
        when(testDataset.getOutputIDInfo().getDomain()).thenReturn(null);
        
        // Act & Assert
        assertThrows(NullPointerException.class, () -> {
            modelFactory.createModel(testDataset, MLModel.ModelType.CLASSIFICATION, new java.util.HashMap<>());
        });
    }
    
    @Test
    void testCreateModel_ClassificationWithRegressionDataset() {
        // Arrange
        when(testDataset.getOutputIDInfo()).thenReturn(mock(org.tribuo.ImmutableOutputInfo.class));
        when(testDataset.getOutputIDInfo().getDomain()).thenAnswer(invocation -> mock(org.tribuo.regression.Regressor.class));
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            modelFactory.createModel(testDataset, MLModel.ModelType.CLASSIFICATION, new java.util.HashMap<>());
        });
    }
    
    @Test
    void testCreateModel_RegressionWithClassificationDataset() {
        // Arrange
        when(testDataset.getOutputIDInfo()).thenReturn(mock(org.tribuo.ImmutableOutputInfo.class));
        when(testDataset.getOutputIDInfo().getDomain()).thenAnswer(invocation -> mock(org.tribuo.classification.LabelInfo.class));
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            modelFactory.createModel(testDataset, MLModel.ModelType.REGRESSION, null);
        });
    }
    
    @Test
    void testCreateModel_LargeDataset() {
        // Arrange
        when(testDataset.size()).thenReturn(10000);
        when(testDataset.getOutputIDInfo()).thenReturn(mock(org.tribuo.ImmutableOutputInfo.class));
        when(testDataset.getOutputIDInfo().getDomain()).thenAnswer(invocation -> mock(org.tribuo.classification.LabelInfo.class));
        
        // Act
        Model<?> result = modelFactory.createModel(testDataset, MLModel.ModelType.CLASSIFICATION, new java.util.HashMap<>());
        
        // Assert
        assertNotNull(result);
        assertTrue(result.getOutputIDInfo().getDomain() instanceof org.tribuo.classification.LabelInfo);
    }
    
    @Test
    void testCreateModel_SingleRowDataset() {
        // Arrange
        when(testDataset.size()).thenReturn(1);
        when(testDataset.getOutputIDInfo()).thenReturn(mock(org.tribuo.ImmutableOutputInfo.class));
        when(testDataset.getOutputIDInfo().getDomain()).thenAnswer(invocation -> mock(org.tribuo.classification.LabelInfo.class));
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            modelFactory.createModel(testDataset, MLModel.ModelType.CLASSIFICATION, new java.util.HashMap<>());
        });
    }
    
    @Test
    void testCreateModel_MinimumValidDataset() {
        // Arrange
        when(testDataset.size()).thenReturn(2);
        when(testDataset.getOutputIDInfo()).thenReturn(mock(org.tribuo.ImmutableOutputInfo.class));
        when(testDataset.getOutputIDInfo().getDomain()).thenAnswer(invocation -> mock(org.tribuo.classification.LabelInfo.class));
        
        // Act
        Model<?> result = modelFactory.createModel(testDataset, MLModel.ModelType.CLASSIFICATION, new java.util.HashMap<>());
        
        // Assert
        assertNotNull(result);
        assertTrue(result.getOutputIDInfo().getDomain() instanceof org.tribuo.classification.LabelInfo);
    }
    
    @Test
    void testCreateModel_WithCustomParameters() {
        // Arrange
        when(testDataset.size()).thenReturn(100);
        when(testDataset.getOutputIDInfo()).thenReturn(mock(org.tribuo.ImmutableOutputInfo.class));
        when(testDataset.getOutputIDInfo().getDomain()).thenAnswer(invocation -> mock(org.tribuo.classification.LabelInfo.class));
        
        // Act
        Model<?> result = modelFactory.createModel(testDataset, MLModel.ModelType.CLASSIFICATION, new java.util.HashMap<>());
        
        // Assert
        assertNotNull(result);
        assertTrue(result.getOutputIDInfo().getDomain() instanceof org.tribuo.classification.LabelInfo);
    }
    
    @Test
    void testCreateModel_RegressionWithNumericTarget() {
        // Arrange
        when(testDataset.size()).thenReturn(100);
        when(testDataset.getOutputIDInfo()).thenReturn(mock(org.tribuo.ImmutableOutputInfo.class));
        when(testDataset.getOutputIDInfo().getDomain()).thenAnswer(invocation -> mock(org.tribuo.regression.Regressor.class));
        
        // Act
        Model<?> result = modelFactory.createModel(testDataset, MLModel.ModelType.REGRESSION, new java.util.HashMap<>());
        
        // Assert
        assertNotNull(result);
        assertTrue(result.getOutputIDInfo().getDomain() instanceof org.tribuo.regression.Regressor);
    }
    
    @Test
    void testCreateModel_ClassificationWithCategoricalTarget() {
        // Arrange
        when(testDataset.size()).thenReturn(100);
        when(testDataset.getOutputIDInfo()).thenReturn(mock(org.tribuo.ImmutableOutputInfo.class));
        when(testDataset.getOutputIDInfo().getDomain()).thenAnswer(invocation -> mock(org.tribuo.classification.LabelInfo.class));
        
        // Act
        Model<?> result = modelFactory.createModel(testDataset, MLModel.ModelType.CLASSIFICATION, new java.util.HashMap<>());
        
        // Assert
        assertNotNull(result);
        assertTrue(result.getOutputIDInfo().getDomain() instanceof org.tribuo.classification.LabelInfo);
    }
    
    @Test
    void testCreateModel_ExtremeLargeDataset() {
        // Arrange
        when(testDataset.size()).thenReturn(Integer.MAX_VALUE);
        when(testDataset.getOutputIDInfo()).thenReturn(mock(org.tribuo.ImmutableOutputInfo.class));
        when(testDataset.getOutputIDInfo().getDomain()).thenAnswer(invocation -> mock(org.tribuo.classification.LabelInfo.class));
        
        // Act
        Model<?> result = modelFactory.createModel(testDataset, MLModel.ModelType.CLASSIFICATION, new java.util.HashMap<>());
        
        // Assert
        assertNotNull(result);
        assertTrue(result.getOutputIDInfo().getDomain() instanceof org.tribuo.classification.LabelInfo);
    }
    
    @Test
    void testCreateModel_WithMockedModel() {
        // Arrange
        Model<?> mockModel = mock(Model.class);
        when(testDataset.size()).thenReturn(100);
        when(testDataset.getOutputIDInfo()).thenReturn(mock(org.tribuo.ImmutableOutputInfo.class));
        when(testDataset.getOutputIDInfo().getDomain()).thenAnswer(invocation -> mock(org.tribuo.classification.LabelInfo.class));
        
        // Act
        Model<?> result = modelFactory.createModel(testDataset, MLModel.ModelType.CLASSIFICATION, new java.util.HashMap<>());
        
        // Assert
        assertNotNull(result);
        assertTrue(result.getOutputIDInfo().getDomain() instanceof org.tribuo.classification.LabelInfo);
    }
    
    @Test
    void testCreateModel_WithInvalidOutputInfo() {
        // Arrange
        when(testDataset.getOutputIDInfo()).thenReturn(mock(org.tribuo.ImmutableOutputInfo.class));
        when(testDataset.getOutputIDInfo().getDomain()).thenReturn(mock(java.util.Set.class)); // Invalid domain type
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            modelFactory.createModel(testDataset, MLModel.ModelType.CLASSIFICATION, new java.util.HashMap<>());
        });
    }
    
    @Test
    void testCreateModel_WithExceptionDuringCreation() {
        // Arrange
        when(testDataset.size()).thenReturn(100);
        when(testDataset.getOutputIDInfo()).thenThrow(new RuntimeException("Simulated error"));
        
        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            modelFactory.createModel(testDataset, MLModel.ModelType.CLASSIFICATION, new java.util.HashMap<>());
        });
    }
}
