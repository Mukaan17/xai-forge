package com.example.xaiapp.unit.factory;

import com.example.xaiapp.entity.MLModel;
import com.example.xaiapp.factory.ModelFactory;
import com.example.xaiapp.strategy.ClassificationStrategy;
import com.example.xaiapp.strategy.RegressionStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.tribuo.MutableDataset;
import org.tribuo.Model;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.doReturn;

/**
 * Unit tests for ModelFactory
 * Tests model creation strategies and parameter validation
 */
@ExtendWith(MockitoExtension.class)
class ModelFactoryTest {
    
    @Mock
    private ClassificationStrategy classificationStrategy;
    
    @Mock
    private RegressionStrategy regressionStrategy;
    
    private ModelFactory modelFactory;
    
    private MutableDataset<?> testDataset;
    
    @BeforeEach
    void setUp() {
        modelFactory = new ModelFactory(classificationStrategy, regressionStrategy);
        testDataset = mock(MutableDataset.class);
    }
    
    @Test
    void testCreateClassificationModel_Success() throws Exception {
        // Arrange
        Model<?> mockModel = mock(Model.class);
        doReturn(mockModel).when(classificationStrategy).train(any(), any());
        
        // Act
        Model<?> result = modelFactory.createModel(testDataset, MLModel.ModelType.CLASSIFICATION, new java.util.HashMap<>());
        
        // Assert
        assertNotNull(result);
        verify(classificationStrategy).validateDataset(testDataset);
        verify(classificationStrategy).train(testDataset, new java.util.HashMap<>());
    }
    
    @Test
    void testCreateRegressionModel_Success() throws Exception {
        // Arrange
        Model<?> mockModel = mock(Model.class);
        doReturn(mockModel).when(regressionStrategy).train(any(), any());
        
        // Act
        Model<?> result = modelFactory.createModel(testDataset, MLModel.ModelType.REGRESSION, new java.util.HashMap<>());
        
        // Assert
        assertNotNull(result);
        verify(regressionStrategy).validateDataset(testDataset);
        verify(regressionStrategy).train(testDataset, new java.util.HashMap<>());
    }
    
    @Test
    void testCreateModel_NullDataset() throws Exception {
        // Act & Assert
        assertThrows(Exception.class, () -> {
            modelFactory.createModel(null, MLModel.ModelType.CLASSIFICATION, null);
        });
    }
    
    @Test
    void testCreateModel_NullModelType() throws Exception {
        // Act & Assert
        assertThrows(Exception.class, () -> {
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
    void testCreateModel_EmptyDataset() throws Exception {
        // Arrange
        doThrow(new IllegalArgumentException("Dataset cannot be empty")).when(classificationStrategy).validateDataset(testDataset);
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            modelFactory.createModel(testDataset, MLModel.ModelType.CLASSIFICATION, new java.util.HashMap<>());
        });
    }
    
    @Test
    void testCreateModel_DatasetWithNullOutputInfo() throws Exception {
        // Arrange
        doThrow(new NullPointerException("Output info cannot be null")).when(classificationStrategy).validateDataset(testDataset);
        
        // Act & Assert
        assertThrows(NullPointerException.class, () -> {
            modelFactory.createModel(testDataset, MLModel.ModelType.CLASSIFICATION, new java.util.HashMap<>());
        });
    }
    
    @Test
    void testCreateModel_DatasetWithNullDomain() throws Exception {
        // Arrange
        doThrow(new NullPointerException("Domain cannot be null")).when(classificationStrategy).validateDataset(testDataset);
        
        // Act & Assert
        assertThrows(NullPointerException.class, () -> {
            modelFactory.createModel(testDataset, MLModel.ModelType.CLASSIFICATION, new java.util.HashMap<>());
        });
    }
    
    @Test
    void testCreateModel_ClassificationWithRegressionDataset() throws Exception {
        // Arrange
        doThrow(new IllegalArgumentException("Dataset type mismatch")).when(classificationStrategy).validateDataset(testDataset);
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            modelFactory.createModel(testDataset, MLModel.ModelType.CLASSIFICATION, new java.util.HashMap<>());
        });
    }
    
    @Test
    void testCreateModel_RegressionWithClassificationDataset() throws Exception {
        // Arrange
        doThrow(new IllegalArgumentException("Dataset type mismatch")).when(regressionStrategy).validateDataset(testDataset);
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            modelFactory.createModel(testDataset, MLModel.ModelType.REGRESSION, null);
        });
    }
    
    @Test
    void testCreateModel_LargeDataset() throws Exception {
        // Arrange
        Model<?> mockModel = mock(Model.class);
        when(testDataset.size()).thenReturn(10000);
        doReturn(mockModel).when(classificationStrategy).train(any(), any());
        
        // Act
        Model<?> result = modelFactory.createModel(testDataset, MLModel.ModelType.CLASSIFICATION, new java.util.HashMap<>());
        
        // Assert
        assertNotNull(result);
        verify(classificationStrategy).validateDataset(testDataset);
        verify(classificationStrategy).train(testDataset, new java.util.HashMap<>());
    }
    
    @Test
    void testCreateModel_SingleRowDataset() throws Exception {
        // Arrange
        when(testDataset.size()).thenReturn(1);
        doThrow(new IllegalArgumentException("Dataset too small")).when(classificationStrategy).validateDataset(testDataset);
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            modelFactory.createModel(testDataset, MLModel.ModelType.CLASSIFICATION, new java.util.HashMap<>());
        });
    }
    
    @Test
    void testCreateModel_MinimumValidDataset() throws Exception {
        // Arrange
        Model<?> mockModel = mock(Model.class);
        when(testDataset.size()).thenReturn(2);
        doReturn(mockModel).when(classificationStrategy).train(any(), any());
        
        // Act
        Model<?> result = modelFactory.createModel(testDataset, MLModel.ModelType.CLASSIFICATION, new java.util.HashMap<>());
        
        // Assert
        assertNotNull(result);
        verify(classificationStrategy).validateDataset(testDataset);
        verify(classificationStrategy).train(testDataset, new java.util.HashMap<>());
    }
    
    @Test
    void testCreateModel_WithCustomParameters() throws Exception {
        // Arrange
        Model<?> mockModel = mock(Model.class);
        java.util.Map<String, Object> customParams = new java.util.HashMap<>();
        customParams.put("maxIterations", 100);
        when(testDataset.size()).thenReturn(100);
        doReturn(mockModel).when(classificationStrategy).train(any(), any());
        
        // Act
        Model<?> result = modelFactory.createModel(testDataset, MLModel.ModelType.CLASSIFICATION, customParams);
        
        // Assert
        assertNotNull(result);
        verify(classificationStrategy).validateDataset(testDataset);
        verify(classificationStrategy).train(testDataset, customParams);
    }
    
    @Test
    void testCreateModel_RegressionWithNumericTarget() throws Exception {
        // Arrange
        Model<?> mockModel = mock(Model.class);
        when(testDataset.size()).thenReturn(100);
        doReturn(mockModel).when(regressionStrategy).train(any(), any());
        
        // Act
        Model<?> result = modelFactory.createModel(testDataset, MLModel.ModelType.REGRESSION, new java.util.HashMap<>());
        
        // Assert
        assertNotNull(result);
        verify(regressionStrategy).validateDataset(testDataset);
        verify(regressionStrategy).train(testDataset, new java.util.HashMap<>());
    }
    
    @Test
    void testCreateModel_ClassificationWithCategoricalTarget() throws Exception {
        // Arrange
        Model<?> mockModel = mock(Model.class);
        when(testDataset.size()).thenReturn(100);
        doReturn(mockModel).when(classificationStrategy).train(any(), any());
        
        // Act
        Model<?> result = modelFactory.createModel(testDataset, MLModel.ModelType.CLASSIFICATION, new java.util.HashMap<>());
        
        // Assert
        assertNotNull(result);
        verify(classificationStrategy).validateDataset(testDataset);
        verify(classificationStrategy).train(testDataset, new java.util.HashMap<>());
    }
    
    @Test
    void testCreateModel_ExtremeLargeDataset() throws Exception {
        // Arrange
        Model<?> mockModel = mock(Model.class);
        when(testDataset.size()).thenReturn(Integer.MAX_VALUE);
        doReturn(mockModel).when(classificationStrategy).train(any(), any());
        
        // Act
        Model<?> result = modelFactory.createModel(testDataset, MLModel.ModelType.CLASSIFICATION, new java.util.HashMap<>());
        
        // Assert
        assertNotNull(result);
        verify(classificationStrategy).validateDataset(testDataset);
        verify(classificationStrategy).train(testDataset, new java.util.HashMap<>());
    }
    
    @Test
    void testCreateModel_WithMockedModel() throws Exception {
        // Arrange
        Model<?> mockModel = mock(Model.class);
        when(testDataset.size()).thenReturn(100);
        doReturn(mockModel).when(classificationStrategy).train(any(), any());
        
        // Act
        Model<?> result = modelFactory.createModel(testDataset, MLModel.ModelType.CLASSIFICATION, new java.util.HashMap<>());
        
        // Assert
        assertNotNull(result);
        verify(classificationStrategy).validateDataset(testDataset);
        verify(classificationStrategy).train(testDataset, new java.util.HashMap<>());
    }
    
    @Test
    void testCreateModel_WithInvalidOutputInfo() throws Exception {
        // Arrange
        doThrow(new IllegalArgumentException("Invalid output info")).when(classificationStrategy).validateDataset(testDataset);
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            modelFactory.createModel(testDataset, MLModel.ModelType.CLASSIFICATION, new java.util.HashMap<>());
        });
    }
    
    @Test
    void testCreateModel_WithExceptionDuringCreation() throws Exception {
        // Arrange
        when(testDataset.size()).thenReturn(100);
        doThrow(new RuntimeException("Simulated error")).when(classificationStrategy).validateDataset(testDataset);
        
        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            modelFactory.createModel(testDataset, MLModel.ModelType.CLASSIFICATION, new java.util.HashMap<>());
        });
    }
}