package com.example.xaiapp.unit.service;

import com.example.xaiapp.dto.TrainRequestDto;
import com.example.xaiapp.entity.Dataset;
import com.example.xaiapp.entity.MLModel;
import com.example.xaiapp.entity.User;
import com.example.xaiapp.exception.DatasetNotFoundException;
import com.example.xaiapp.exception.ModelTrainingException;
import com.example.xaiapp.factory.AlgorithmFactory;
import com.example.xaiapp.repository.DatasetRepository;
import com.example.xaiapp.repository.MLModelRepository;
import com.example.xaiapp.service.ModelService;
import com.example.xaiapp.strategy.ClassificationStrategy;
import com.example.xaiapp.strategy.RegressionStrategy;
import com.example.xaiapp.util.TestConstants;
import com.example.xaiapp.util.TestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.tribuo.MutableDataset;
import org.tribuo.Model;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ModelService
 * Tests model training, retrieval, deletion, and accuracy calculation
 */
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class ModelServiceTest {
    
    @Mock
    private MLModelRepository modelRepository;
    
    @Mock
    private DatasetRepository datasetRepository;
    
    @Mock
    private AlgorithmFactory algorithmFactory;
    
    @Mock
    private ClassificationStrategy classificationStrategy;
    
    @Mock
    private RegressionStrategy regressionStrategy;
    
    @InjectMocks
    private ModelService modelService;
    
    private User testUser;
    private Dataset testDataset;
    private TrainRequestDto testRequest;
    private Model<?> testModel;
    
    @BeforeEach
    void setUp() {
        // Set upload directory for testing
        ReflectionTestUtils.setField(modelService, "uploadDir", TestConstants.TEST_UPLOAD_DIR);
        
        // Create test data
        testUser = TestDataBuilder.createTestUser();
        testDataset = TestDataBuilder.createTestDataset(testUser);
        testRequest = TestDataBuilder.createTrainRequest(testDataset.getId());
        testModel = mock(Model.class);
    }
    
    @Test
    void testTrainModel_Success_Classification() throws Exception {
        // Arrange
        when(datasetRepository.findByIdAndOwnerId(testRequest.getDatasetId(), testUser.getId()))
            .thenReturn(Optional.of(testDataset));
        when(modelRepository.findByDataset(testDataset))
            .thenReturn(Optional.empty());
        when(classificationStrategy.train(any(MutableDataset.class), isNull()))
            .thenAnswer(invocation -> testModel);
        when(modelRepository.save(any(MLModel.class)))
            .thenAnswer(invocation -> {
                MLModel model = invocation.getArgument(0);
                model.setId(1L);
                return model;
            });
        
        // Mock accuracy calculation
        when(testModel.getOutputIDInfo()).thenReturn(mock(org.tribuo.ImmutableOutputInfo.class));
        when(testModel.getOutputIDInfo().getDomain()).thenAnswer(invocation -> mock(org.tribuo.classification.LabelInfo.class));
        
        // Act
        MLModel result = modelService.trainModel(testRequest, testUser.getId());
        
        // Assert
        assertNotNull(result);
        assertEquals(testRequest.getModelName(), result.getModelName());
        assertEquals(MLModel.ModelType.CLASSIFICATION, result.getModelType());
        assertEquals(testRequest.getTargetVariable(), result.getTargetVariable());
        assertEquals(Arrays.asList(testRequest.getFeatureNames()), result.getFeatureNames());
        assertEquals(testDataset, result.getDataset());
        
        verify(datasetRepository).findByIdAndOwnerId(testRequest.getDatasetId(), testUser.getId());
        verify(modelRepository).findByDataset(testDataset);
        verify(classificationStrategy).train(any(MutableDataset.class), isNull());
        verify(modelRepository).save(any(MLModel.class));
    }
    
    @Test
    void testTrainModel_Success_Regression() throws Exception {
        // Arrange
        TrainRequestDto regressionRequest = TestDataBuilder.createRegressionTrainRequest(testDataset.getId());
        
        when(datasetRepository.findByIdAndOwnerId(regressionRequest.getDatasetId(), testUser.getId()))
            .thenReturn(Optional.of(testDataset));
        when(modelRepository.findByDataset(testDataset))
            .thenReturn(Optional.empty());
        when(regressionStrategy.train(any(MutableDataset.class), isNull()))
            .thenAnswer(invocation -> testModel);
        when(modelRepository.save(any(MLModel.class)))
            .thenAnswer(invocation -> {
                MLModel model = invocation.getArgument(0);
                model.setId(1L);
                return model;
            });
        
        // Mock accuracy calculation for regression
        when(testModel.getOutputIDInfo()).thenReturn(mock(org.tribuo.ImmutableOutputInfo.class));
        when(testModel.getOutputIDInfo().getDomain()).thenAnswer(invocation -> mock(org.tribuo.regression.Regressor.class));
        
        // Act
        MLModel result = modelService.trainModel(regressionRequest, testUser.getId());
        
        // Assert
        assertNotNull(result);
        assertEquals(regressionRequest.getModelName(), result.getModelName());
        assertEquals(MLModel.ModelType.REGRESSION, result.getModelType());
        
        verify(regressionStrategy).train(any(MutableDataset.class), isNull());
    }
    
    @Test
    void testTrainModel_DatasetNotFound() {
        // Arrange
        when(datasetRepository.findByIdAndOwnerId(testRequest.getDatasetId(), testUser.getId()))
            .thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(DatasetNotFoundException.class, () -> {
            modelService.trainModel(testRequest, testUser.getId());
        });
        
        verify(datasetRepository).findByIdAndOwnerId(testRequest.getDatasetId(), testUser.getId());
        verify(modelRepository, never()).save(any(MLModel.class));
    }
    
    @Test
    void testTrainModel_ModelAlreadyExists() {
        // Arrange
        MLModel existingModel = TestDataBuilder.createTestModel(testDataset);
        when(datasetRepository.findByIdAndOwnerId(testRequest.getDatasetId(), testUser.getId()))
            .thenReturn(Optional.of(testDataset));
        when(modelRepository.findByDataset(testDataset))
            .thenReturn(Optional.of(existingModel));
        
        // Act & Assert
        assertThrows(ModelTrainingException.class, () -> {
            modelService.trainModel(testRequest, testUser.getId());
        });
        
        verify(datasetRepository).findByIdAndOwnerId(testRequest.getDatasetId(), testUser.getId());
        verify(modelRepository).findByDataset(testDataset);
        verify(modelRepository, never()).save(any(MLModel.class));
    }
    
    @Test
    void testGetModel_Success() {
        // Arrange
        MLModel testModel = TestDataBuilder.createTestModel(testDataset);
        when(modelRepository.findByIdAndDatasetOwnerId(1L, testUser.getId()))
            .thenReturn(Optional.of(testModel));
        
        // Act
        MLModel result = modelService.getModel(1L, testUser.getId());
        
        // Assert
        assertNotNull(result);
        assertEquals(testModel.getId(), result.getId());
        assertEquals(testModel.getModelName(), result.getModelName());
        
        verify(modelRepository).findByIdAndDatasetOwnerId(1L, testUser.getId());
    }
    
    @Test
    void testGetModel_NotFound() {
        // Arrange
        when(modelRepository.findByIdAndDatasetOwnerId(1L, testUser.getId()))
            .thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            modelService.getModel(1L, testUser.getId());
        });
        
        verify(modelRepository).findByIdAndDatasetOwnerId(1L, testUser.getId());
    }
    
    @Test
    void testGetUserModels_Success() {
        // Arrange
        List<MLModel> userModels = Arrays.asList(
            TestDataBuilder.createTestModel(testDataset),
            TestDataBuilder.createTestModel(testDataset)
        );
        when(modelRepository.findByDatasetOwnerId(testUser.getId()))
            .thenReturn(userModels);
        
        // Act
        List<MLModel> result = modelService.getUserModels(testUser.getId());
        
        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        
        verify(modelRepository).findByDatasetOwnerId(testUser.getId());
    }
    
    @Test
    void testDeleteModel_Success() throws Exception {
        // Arrange
        MLModel testModel = TestDataBuilder.createTestModel(testDataset);
        when(modelRepository.findByIdAndDatasetOwnerId(1L, testUser.getId()))
            .thenReturn(Optional.of(testModel));
        
        // Act
        modelService.deleteModel(1L, testUser.getId());
        
        // Assert
        verify(modelRepository).findByIdAndDatasetOwnerId(1L, testUser.getId());
        verify(modelRepository).delete(testModel);
    }
    
    @Test
    void testDeleteModel_NotFound() {
        // Arrange
        when(modelRepository.findByIdAndDatasetOwnerId(1L, testUser.getId()))
            .thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            modelService.deleteModel(1L, testUser.getId());
        });
        
        verify(modelRepository).findByIdAndDatasetOwnerId(1L, testUser.getId());
        verify(modelRepository, never()).delete(any(MLModel.class));
    }
    
    @Test
    void testTrainModel_InvalidModelType() {
        // Arrange
        testRequest.setModelType("INVALID_TYPE");
        when(datasetRepository.findByIdAndOwnerId(testRequest.getDatasetId(), testUser.getId()))
            .thenReturn(Optional.of(testDataset));
        when(modelRepository.findByDataset(testDataset))
            .thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            modelService.trainModel(testRequest, testUser.getId());
        });
    }
    
    @Test
    void testTrainModel_NullRequest() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> {
            modelService.trainModel(null, testUser.getId());
        });
    }
    
    @Test
    void testTrainModel_NullUserId() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> {
            modelService.trainModel(testRequest, null);
        });
    }
    
    @Test
    void testGetUserModels_EmptyList() {
        // Arrange
        when(modelRepository.findByDatasetOwnerId(testUser.getId()))
            .thenReturn(Arrays.asList());
        
        // Act
        List<MLModel> result = modelService.getUserModels(testUser.getId());
        
        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        
        verify(modelRepository).findByDatasetOwnerId(testUser.getId());
    }
    
    @Test
    void testTrainModel_ConcurrentTrainingAttempt() {
        // Arrange
        MLModel existingModel = TestDataBuilder.createTestModel(testDataset);
        when(datasetRepository.findByIdAndOwnerId(testRequest.getDatasetId(), testUser.getId()))
            .thenReturn(Optional.of(testDataset));
        when(modelRepository.findByDataset(testDataset))
            .thenReturn(Optional.of(existingModel));
        
        // Act & Assert
        ModelTrainingException exception = assertThrows(ModelTrainingException.class, () -> {
            modelService.trainModel(testRequest, testUser.getId());
        });
        
        assertTrue(exception.getMessage().contains("Model already exists"));
        assertTrue(exception.getMessage().contains("Please delete the existing model first"));
    }
}
