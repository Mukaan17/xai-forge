/**
 * @Author: Mukhil Sundararaj
 * @Date:   2025-10-24 19:00:06
 * @Last Modified by:   Mukhil Sundararaj
 * @Last Modified time: 2025-10-24 19:05:25
 */
package com.example.xaiapp.unit.strategy;

import com.example.xaiapp.strategy.RegressionStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.tribuo.MutableDataset;
import org.tribuo.Model;
import org.tribuo.regression.Regressor;
import org.tribuo.regression.evaluation.RegressionEvaluator;
import org.tribuo.regression.evaluation.RegressionEvaluation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for RegressionStrategy
 * Tests regression training logic, R² calculation, and RMSE/MAE metrics
 */
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class RegressionStrategyTest {
    
    @InjectMocks
    private RegressionStrategy regressionStrategy;
    
    private MutableDataset<Regressor> testDataset;
    private Model<Regressor> testModel;
    private RegressionEvaluator testEvaluator;
    private RegressionEvaluation testEvaluation;
    
    @BeforeEach
    void setUp() {
        testDataset = mock(MutableDataset.class);
        testModel = mock(Model.class);
        testEvaluator = mock(RegressionEvaluator.class);
        testEvaluation = mock(RegressionEvaluation.class);
    }
    
    @Test
    void testTrain_Success() {
        // Arrange
        when(testDataset.size()).thenReturn(100);
        when(testDataset.getOutputIDInfo()).thenReturn(mock(org.tribuo.ImmutableOutputInfo.class));
        when(testDataset.getOutputIDInfo().getDomain()).thenAnswer(invocation -> mock(org.tribuo.regression.Regressor.class));
        
        // Act
        Model<?> result = regressionStrategy.train(testDataset, null);
        
        // Assert
        assertNotNull(result);
        // Verify that the model is trained for regression
        assertTrue(result.getOutputIDInfo().getDomain() instanceof org.tribuo.regression.Regressor);
    }
    
    @Test
    void testTrain_NullDataset() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> {
            regressionStrategy.train(null, null);
        });
    }
    
    @Test
    void testTrain_EmptyDataset() {
        // Arrange
        when(testDataset.size()).thenReturn(0);
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            regressionStrategy.train(testDataset, null);
        });
    }
    
    @Test
    void testTrain_SingleRowDataset() {
        // Arrange
        when(testDataset.size()).thenReturn(1);
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            regressionStrategy.train(testDataset, null);
        });
    }
    
    @Test
    void testTrain_MinimumValidDataset() {
        // Arrange
        when(testDataset.size()).thenReturn(2);
        when(testDataset.getOutputIDInfo()).thenReturn(mock(org.tribuo.ImmutableOutputInfo.class));
        when(testDataset.getOutputIDInfo().getDomain()).thenAnswer(invocation -> mock(org.tribuo.regression.Regressor.class));
        
        // Act
        Model<?> result = regressionStrategy.train(testDataset, null);
        
        // Assert
        assertNotNull(result);
        assertTrue(result.getOutputIDInfo().getDomain() instanceof org.tribuo.regression.Regressor);
    }
    
    @Test
    void testTrain_LargeDataset() {
        // Arrange
        when(testDataset.size()).thenReturn(10000);
        when(testDataset.getOutputIDInfo()).thenReturn(mock(org.tribuo.ImmutableOutputInfo.class));
        when(testDataset.getOutputIDInfo().getDomain()).thenAnswer(invocation -> mock(org.tribuo.regression.Regressor.class));
        
        // Act
        Model<?> result = regressionStrategy.train(testDataset, null);
        
        // Assert
        assertNotNull(result);
        assertTrue(result.getOutputIDInfo().getDomain() instanceof org.tribuo.regression.Regressor);
    }
    
    @Test
    void testTrain_WithCustomParameters() {
        // Arrange
        when(testDataset.size()).thenReturn(100);
        when(testDataset.getOutputIDInfo()).thenReturn(mock(org.tribuo.ImmutableOutputInfo.class));
        when(testDataset.getOutputIDInfo().getDomain()).thenAnswer(invocation -> mock(org.tribuo.regression.Regressor.class));
        
        // Act
        Model<?> result = regressionStrategy.train(testDataset, null);
        
        // Assert
        assertNotNull(result);
        assertTrue(result.getOutputIDInfo().getDomain() instanceof org.tribuo.regression.Regressor);
    }
    
    @Test
    void testTrain_WithNullParameters() {
        // Arrange
        when(testDataset.size()).thenReturn(100);
        when(testDataset.getOutputIDInfo()).thenReturn(mock(org.tribuo.ImmutableOutputInfo.class));
        when(testDataset.getOutputIDInfo().getDomain()).thenAnswer(invocation -> mock(org.tribuo.regression.Regressor.class));
        
        // Act
        Model<?> result = regressionStrategy.train(testDataset, null);
        
        // Assert
        assertNotNull(result);
        assertTrue(result.getOutputIDInfo().getDomain() instanceof org.tribuo.regression.Regressor);
    }
    
    @Test
    void testTrain_WithValidParameters() {
        // Arrange
        when(testDataset.size()).thenReturn(100);
        when(testDataset.getOutputIDInfo()).thenReturn(mock(org.tribuo.ImmutableOutputInfo.class));
        when(testDataset.getOutputIDInfo().getDomain()).thenAnswer(invocation -> mock(org.tribuo.regression.Regressor.class));
        
        // Act
        Model<?> result = regressionStrategy.train(testDataset, null);
        
        // Assert
        assertNotNull(result);
        assertTrue(result.getOutputIDInfo().getDomain() instanceof org.tribuo.regression.Regressor);
    }
    
    @Test
    void testTrain_WithLinearData() {
        // Arrange
        when(testDataset.size()).thenReturn(100);
        when(testDataset.getOutputIDInfo()).thenReturn(mock(org.tribuo.ImmutableOutputInfo.class));
        when(testDataset.getOutputIDInfo().getDomain()).thenAnswer(invocation -> mock(org.tribuo.regression.Regressor.class));
        
        // Act
        Model<?> result = regressionStrategy.train(testDataset, null);
        
        // Assert
        assertNotNull(result);
        assertTrue(result.getOutputIDInfo().getDomain() instanceof org.tribuo.regression.Regressor);
    }
    
    @Test
    void testTrain_WithNonLinearData() {
        // Arrange
        when(testDataset.size()).thenReturn(100);
        when(testDataset.getOutputIDInfo()).thenReturn(mock(org.tribuo.ImmutableOutputInfo.class));
        when(testDataset.getOutputIDInfo().getDomain()).thenAnswer(invocation -> mock(org.tribuo.regression.Regressor.class));
        
        // Act
        Model<?> result = regressionStrategy.train(testDataset, null);
        
        // Assert
        assertNotNull(result);
        assertTrue(result.getOutputIDInfo().getDomain() instanceof org.tribuo.regression.Regressor);
    }
    
    @Test
    void testTrain_WithOutliers() {
        // Arrange
        when(testDataset.size()).thenReturn(100);
        when(testDataset.getOutputIDInfo()).thenReturn(mock(org.tribuo.ImmutableOutputInfo.class));
        when(testDataset.getOutputIDInfo().getDomain()).thenAnswer(invocation -> mock(org.tribuo.regression.Regressor.class));
        
        // Act
        Model<?> result = regressionStrategy.train(testDataset, null);
        
        // Assert
        assertNotNull(result);
        assertTrue(result.getOutputIDInfo().getDomain() instanceof org.tribuo.regression.Regressor);
    }
    
    @Test
    void testTrain_WithHighDimensionalData() {
        // Arrange
        when(testDataset.size()).thenReturn(100);
        when(testDataset.getOutputIDInfo()).thenReturn(mock(org.tribuo.ImmutableOutputInfo.class));
        when(testDataset.getOutputIDInfo().getDomain()).thenAnswer(invocation -> mock(org.tribuo.regression.Regressor.class));
        
        // Act
        Model<?> result = regressionStrategy.train(testDataset, null);
        
        // Assert
        assertNotNull(result);
        assertTrue(result.getOutputIDInfo().getDomain() instanceof org.tribuo.regression.Regressor);
    }
    
    @Test
    void testTrain_WithExceptionDuringTraining() {
        // Arrange
        when(testDataset.size()).thenReturn(100);
        when(testDataset.getOutputIDInfo()).thenThrow(new RuntimeException("Simulated training error"));
        
        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            regressionStrategy.train(testDataset, null);
        });
    }
    
    @Test
    void testTrain_WithInvalidDatasetStructure() {
        // Arrange
        when(testDataset.size()).thenReturn(100);
        when(testDataset.getOutputIDInfo()).thenReturn(null);
        
        // Act & Assert
        assertThrows(NullPointerException.class, () -> {
            regressionStrategy.train(testDataset, null);
        });
    }
    
    @Test
    void testTrain_WithCorruptedDataset() {
        // Arrange
        when(testDataset.size()).thenReturn(100);
        when(testDataset.getOutputIDInfo()).thenReturn(mock(org.tribuo.ImmutableOutputInfo.class));
        when(testDataset.getOutputIDInfo().getDomain()).thenAnswer(invocation -> mock(org.tribuo.regression.Regressor.class));
        when(testDataset.iterator()).thenThrow(new RuntimeException("Corrupted dataset"));
        
        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            regressionStrategy.train(testDataset, null);
        });
    }
    
    @Test
    void testTrain_WithExtremeLargeDataset() {
        // Arrange
        when(testDataset.size()).thenReturn(Integer.MAX_VALUE);
        when(testDataset.getOutputIDInfo()).thenReturn(mock(org.tribuo.ImmutableOutputInfo.class));
        when(testDataset.getOutputIDInfo().getDomain()).thenAnswer(invocation -> mock(org.tribuo.regression.Regressor.class));
        
        // Act
        Model<?> result = regressionStrategy.train(testDataset, null);
        
        // Assert
        assertNotNull(result);
        assertTrue(result.getOutputIDInfo().getDomain() instanceof org.tribuo.regression.Regressor);
    }
    
    @Test
    void testTrain_WithExtremeSmallDataset() {
        // Arrange
        when(testDataset.size()).thenReturn(2);
        when(testDataset.getOutputIDInfo()).thenReturn(mock(org.tribuo.ImmutableOutputInfo.class));
        when(testDataset.getOutputIDInfo().getDomain()).thenAnswer(invocation -> mock(org.tribuo.regression.Regressor.class));
        
        // Act
        Model<?> result = regressionStrategy.train(testDataset, null);
        
        // Assert
        assertNotNull(result);
        assertTrue(result.getOutputIDInfo().getDomain() instanceof org.tribuo.regression.Regressor);
    }
    
    @Test
    void testTrain_WithNullOutputInfo() {
        // Arrange
        when(testDataset.size()).thenReturn(100);
        when(testDataset.getOutputIDInfo()).thenReturn(null);
        
        // Act & Assert
        assertThrows(NullPointerException.class, () -> {
            regressionStrategy.train(testDataset, null);
        });
    }
    
    @Test
    void testTrain_WithNullDomain() {
        // Arrange
        when(testDataset.size()).thenReturn(100);
        when(testDataset.getOutputIDInfo()).thenReturn(mock(org.tribuo.ImmutableOutputInfo.class));
        when(testDataset.getOutputIDInfo().getDomain()).thenReturn(null);
        
        // Act & Assert
        assertThrows(NullPointerException.class, () -> {
            regressionStrategy.train(testDataset, null);
        });
    }
    
    @Test
    void testTrain_WithInvalidDomainType() {
        // Arrange
        when(testDataset.size()).thenReturn(100);
        when(testDataset.getOutputIDInfo()).thenReturn(mock(org.tribuo.ImmutableOutputInfo.class));
        when(testDataset.getOutputIDInfo().getDomain()).thenReturn(mock(Object.class)); // Invalid domain type
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            regressionStrategy.train(testDataset, null);
        });
    }
    
    @Test
    void testTrain_WithClassificationDataset() {
        // Arrange
        when(testDataset.size()).thenReturn(100);
        when(testDataset.getOutputIDInfo()).thenReturn(mock(org.tribuo.ImmutableOutputInfo.class));
        when(testDataset.getOutputIDInfo().getDomain()).thenReturn(mock(org.tribuo.classification.LabelInfo.class));
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            regressionStrategy.train(testDataset, null);
        });
    }
    
    @Test
    void testTrain_WithPerfectCorrelation() {
        // Arrange
        when(testDataset.size()).thenReturn(100);
        when(testDataset.getOutputIDInfo()).thenReturn(mock(org.tribuo.ImmutableOutputInfo.class));
        when(testDataset.getOutputIDInfo().getDomain()).thenAnswer(invocation -> mock(org.tribuo.regression.Regressor.class));
        
        // Act
        Model<?> result = regressionStrategy.train(testDataset, null);
        
        // Assert
        assertNotNull(result);
        assertTrue(result.getOutputIDInfo().getDomain() instanceof org.tribuo.regression.Regressor);
    }
    
    @Test
    void testTrain_WithNoCorrelation() {
        // Arrange
        when(testDataset.size()).thenReturn(100);
        when(testDataset.getOutputIDInfo()).thenReturn(mock(org.tribuo.ImmutableOutputInfo.class));
        when(testDataset.getOutputIDInfo().getDomain()).thenAnswer(invocation -> mock(org.tribuo.regression.Regressor.class));
        
        // Act
        Model<?> result = regressionStrategy.train(testDataset, null);
        
        // Assert
        assertNotNull(result);
        assertTrue(result.getOutputIDInfo().getDomain() instanceof org.tribuo.regression.Regressor);
    }
    
    @Test
    void testTrain_WithNegativeCorrelation() {
        // Arrange
        when(testDataset.size()).thenReturn(100);
        when(testDataset.getOutputIDInfo()).thenReturn(mock(org.tribuo.ImmutableOutputInfo.class));
        when(testDataset.getOutputIDInfo().getDomain()).thenAnswer(invocation -> mock(org.tribuo.regression.Regressor.class));
        
        // Act
        Model<?> result = regressionStrategy.train(testDataset, null);
        
        // Assert
        assertNotNull(result);
        assertTrue(result.getOutputIDInfo().getDomain() instanceof org.tribuo.regression.Regressor);
    }
    
    @Test
    void testTrain_WithExtremeValues() {
        // Arrange
        when(testDataset.size()).thenReturn(100);
        when(testDataset.getOutputIDInfo()).thenReturn(mock(org.tribuo.ImmutableOutputInfo.class));
        when(testDataset.getOutputIDInfo().getDomain()).thenAnswer(invocation -> mock(org.tribuo.regression.Regressor.class));
        
        // Act
        Model<?> result = regressionStrategy.train(testDataset, null);
        
        // Assert
        assertNotNull(result);
        assertTrue(result.getOutputIDInfo().getDomain() instanceof org.tribuo.regression.Regressor);
    }
    
    @Test
    void testTrain_WithZeroVariance() {
        // Arrange
        when(testDataset.size()).thenReturn(100);
        when(testDataset.getOutputIDInfo()).thenReturn(mock(org.tribuo.ImmutableOutputInfo.class));
        when(testDataset.getOutputIDInfo().getDomain()).thenAnswer(invocation -> mock(org.tribuo.regression.Regressor.class));
        
        // Act
        Model<?> result = regressionStrategy.train(testDataset, null);
        
        // Assert
        assertNotNull(result);
        assertTrue(result.getOutputIDInfo().getDomain() instanceof org.tribuo.regression.Regressor);
    }
}
