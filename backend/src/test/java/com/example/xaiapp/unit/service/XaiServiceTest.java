package com.example.xaiapp.unit.service;

import com.example.xaiapp.dto.ExplanationResponse;
import com.example.xaiapp.dto.PredictionResponse;
import com.example.xaiapp.entity.Dataset;
import com.example.xaiapp.entity.MLModel;
import com.example.xaiapp.entity.User;
import com.example.xaiapp.repository.MLModelRepository;
import com.example.xaiapp.service.XaiService;
import com.example.xaiapp.util.TestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.doReturn;
import static org.mockito.ArgumentMatchers.any;
import org.tribuo.Model;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for XaiService
 * Tests prediction and explanation generation with LIME
 */
@ExtendWith(MockitoExtension.class)
@SuppressWarnings({"unchecked", "rawtypes"})
class XaiServiceTest {
    
    @Mock
    private MLModelRepository modelRepository;
    
    @InjectMocks
    private XaiService xaiService;
    
    private User testUser;
    private Dataset testDataset;
    private MLModel testModel;
    private Model<?> mockTribuoModel;
    private Map<String, String> testInputData;
    
    @BeforeEach
    void setUp() {
        testUser = TestDataBuilder.createTestUser();
        testDataset = TestDataBuilder.createTestDataset(testUser);
        testModel = TestDataBuilder.createTestModel(testDataset);
        testInputData = new HashMap<>();
        testInputData.put("feature1", "1.5");
        testInputData.put("feature2", "2.3");
        testInputData.put("feature3", "0.8");
        
        // Mock Tribuo model
        mockTribuoModel = mock(Model.class);
    }
    
    @Test
    void testPredict_Success_Classification() throws Exception {
        // Arrange
        when(modelRepository.findByIdAndDatasetOwnerId(1L, testUser.getId()))
            .thenReturn(Optional.of(testModel));
        
        org.tribuo.Prediction<?> mockPrediction = mock(org.tribuo.Prediction.class);
        doReturn(mockPrediction).when((org.tribuo.Model) mockTribuoModel).predict(ArgumentMatchers.<org.tribuo.Example>any());
        when(mockTribuoModel.getOutputIDInfo()).thenReturn(mock(org.tribuo.ImmutableOutputInfo.class));
        
        // Mock file reading and deserialization
        byte[] serializedModel = serializeModel(mockTribuoModel);
        when(java.nio.file.Files.readAllBytes(any(java.nio.file.Path.class)))
            .thenReturn(serializedModel);
        
        // Act
        PredictionResponse result = xaiService.predict(1L, testInputData, testUser.getId());
        
        // Assert
        assertNotNull(result);
        assertNotNull(result.getPrediction());
        assertNotNull(result.getProbabilities());
        
        verify(modelRepository).findByIdAndDatasetOwnerId(1L, testUser.getId());
    }
    
    @Test
    void testPredict_Success_Regression() throws Exception {
        // Arrange
        testModel.setModelType(MLModel.ModelType.REGRESSION);
        when(modelRepository.findByIdAndDatasetOwnerId(1L, testUser.getId()))
            .thenReturn(Optional.of(testModel));
        
        // Mock regression model
        org.tribuo.Prediction<?> mockPrediction = mock(org.tribuo.Prediction.class);
        doReturn(mockPrediction).when((org.tribuo.Model) mockTribuoModel).predict(ArgumentMatchers.<org.tribuo.Example>any());
        when(mockTribuoModel.getOutputIDInfo()).thenReturn(mock(org.tribuo.ImmutableOutputInfo.class));
        
        // Mock file reading and deserialization
        byte[] serializedModel = serializeModel(mockTribuoModel);
        when(java.nio.file.Files.readAllBytes(any(java.nio.file.Path.class)))
            .thenReturn(serializedModel);
        
        // Act
        PredictionResponse result = xaiService.predict(1L, testInputData, testUser.getId());
        
        // Assert
        assertNotNull(result);
        assertNotNull(result.getPrediction());
        
        verify(modelRepository).findByIdAndDatasetOwnerId(1L, testUser.getId());
    }
    
    @Test
    void testPredict_ModelNotFound() {
        // Arrange
        when(modelRepository.findByIdAndDatasetOwnerId(1L, testUser.getId()))
            .thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            xaiService.predict(1L, testInputData, testUser.getId());
        });
        
        verify(modelRepository).findByIdAndDatasetOwnerId(1L, testUser.getId());
    }
    
    @Test
    void testPredict_InvalidInputData() {
        // Arrange
        Map<String, String> invalidInput = new HashMap<>();
        invalidInput.put("invalid_feature", "invalid_value");
        
        when(modelRepository.findByIdAndDatasetOwnerId(1L, testUser.getId()))
            .thenReturn(Optional.of(testModel));
        
        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            xaiService.predict(1L, invalidInput, testUser.getId());
        });
    }
    
    @Test
    void testExplain_Success_Classification() throws Exception {
        // Arrange
        when(modelRepository.findByIdAndDatasetOwnerId(1L, testUser.getId()))
            .thenReturn(Optional.of(testModel));
        
        org.tribuo.Prediction<?> mockPrediction = mock(org.tribuo.Prediction.class);
        doReturn(mockPrediction).when((org.tribuo.Model) mockTribuoModel).predict(ArgumentMatchers.<org.tribuo.Example>any());
        when(mockTribuoModel.getOutputIDInfo()).thenReturn(mock(org.tribuo.ImmutableOutputInfo.class));
        
        // Mock file reading and deserialization
        byte[] serializedModel = serializeModel(mockTribuoModel);
        when(java.nio.file.Files.readAllBytes(any(java.nio.file.Path.class)))
            .thenReturn(serializedModel);
        
        // Act
        ExplanationResponse result = xaiService.explain(1L, testInputData, testUser.getId());
        
        // Assert
        assertNotNull(result);
        assertNotNull(result.getPrediction());
        assertNotNull(result.getExplanationText());
        assertNotNull(result.getInputData());
        
        verify(modelRepository).findByIdAndDatasetOwnerId(1L, testUser.getId());
    }
    
    @Test
    void testExplain_Success_Regression() throws Exception {
        // Arrange
        testModel.setModelType(MLModel.ModelType.REGRESSION);
        when(modelRepository.findByIdAndDatasetOwnerId(1L, testUser.getId()))
            .thenReturn(Optional.of(testModel));
        
        // Mock regression model
        org.tribuo.Prediction<?> mockPrediction = mock(org.tribuo.Prediction.class);
        doReturn(mockPrediction).when((org.tribuo.Model) mockTribuoModel).predict(ArgumentMatchers.<org.tribuo.Example>any());
        when(mockTribuoModel.getOutputIDInfo()).thenReturn(mock(org.tribuo.ImmutableOutputInfo.class));
        
        // Mock file reading and deserialization
        byte[] serializedModel = serializeModel(mockTribuoModel);
        when(java.nio.file.Files.readAllBytes(any(java.nio.file.Path.class)))
            .thenReturn(serializedModel);
        
        // Act
        ExplanationResponse result = xaiService.explain(1L, testInputData, testUser.getId());
        
        // Assert
        assertNotNull(result);
        assertNotNull(result.getPrediction());
        assertNotNull(result.getExplanationText());
        
        verify(modelRepository).findByIdAndDatasetOwnerId(1L, testUser.getId());
    }
    
    @Test
    void testExplain_ModelNotFound() {
        // Arrange
        when(modelRepository.findByIdAndDatasetOwnerId(1L, testUser.getId()))
            .thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            xaiService.explain(1L, testInputData, testUser.getId());
        });
        
        verify(modelRepository).findByIdAndDatasetOwnerId(1L, testUser.getId());
    }
    
    @Test
    void testExplain_InvalidInputData() {
        // Arrange
        Map<String, String> invalidInput = new HashMap<>();
        invalidInput.put("invalid_feature", "invalid_value");
        
        when(modelRepository.findByIdAndDatasetOwnerId(1L, testUser.getId()))
            .thenReturn(Optional.of(testModel));
        
        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            xaiService.explain(1L, invalidInput, testUser.getId());
        });
    }
    
    @Test
    void testPredict_EdgeValues() throws Exception {
        // Arrange
        Map<String, String> edgeInput = new HashMap<>();
        edgeInput.put("feature1", "0.0");
        edgeInput.put("feature2", "999999.99");
        edgeInput.put("feature3", "-1.0");
        
        when(modelRepository.findByIdAndDatasetOwnerId(1L, testUser.getId()))
            .thenReturn(Optional.of(testModel));
        
        org.tribuo.Prediction<?> mockPrediction = mock(org.tribuo.Prediction.class);
        doReturn(mockPrediction).when((org.tribuo.Model) mockTribuoModel).predict(ArgumentMatchers.<org.tribuo.Example>any());
        when(mockTribuoModel.getOutputIDInfo()).thenReturn(mock(org.tribuo.ImmutableOutputInfo.class));
        
        // Mock file reading and deserialization
        byte[] serializedModel = serializeModel(mockTribuoModel);
        when(java.nio.file.Files.readAllBytes(any(java.nio.file.Path.class)))
            .thenReturn(serializedModel);
        
        // Act
        PredictionResponse result = xaiService.predict(1L, edgeInput, testUser.getId());
        
        // Assert
        assertNotNull(result);
        assertNotNull(result.getPrediction());
        
        verify(modelRepository).findByIdAndDatasetOwnerId(1L, testUser.getId());
    }
    
    @Test
    void testPredict_MissingFeatures() {
        // Arrange
        Map<String, String> incompleteInput = new HashMap<>();
        incompleteInput.put("feature1", "1.5");
        // Missing feature2 and feature3
        
        when(modelRepository.findByIdAndDatasetOwnerId(1L, testUser.getId()))
            .thenReturn(Optional.of(testModel));
        
        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            xaiService.predict(1L, incompleteInput, testUser.getId());
        });
    }
    
    @Test
    void testPredict_UnseenCategories() throws Exception {
        // Arrange
        Map<String, String> unseenInput = new HashMap<>();
        unseenInput.put("feature1", "999.0"); // Unseen value
        unseenInput.put("feature2", "999.0");
        unseenInput.put("feature3", "999.0");
        
        when(modelRepository.findByIdAndDatasetOwnerId(1L, testUser.getId()))
            .thenReturn(Optional.of(testModel));
        
        org.tribuo.Prediction<?> mockPrediction = mock(org.tribuo.Prediction.class);
        doReturn(mockPrediction).when((org.tribuo.Model) mockTribuoModel).predict(ArgumentMatchers.<org.tribuo.Example>any());
        when(mockTribuoModel.getOutputIDInfo()).thenReturn(mock(org.tribuo.ImmutableOutputInfo.class));
        
        // Mock file reading and deserialization
        byte[] serializedModel = serializeModel(mockTribuoModel);
        when(java.nio.file.Files.readAllBytes(any(java.nio.file.Path.class)))
            .thenReturn(serializedModel);
        
        // Act
        PredictionResponse result = xaiService.predict(1L, unseenInput, testUser.getId());
        
        // Assert
        assertNotNull(result);
        assertNotNull(result.getPrediction());
        
        verify(modelRepository).findByIdAndDatasetOwnerId(1L, testUser.getId());
    }
    
    @Test
    void testPredict_NullInputData() {
        // Arrange
        when(modelRepository.findByIdAndDatasetOwnerId(1L, testUser.getId()))
            .thenReturn(Optional.of(testModel));
        
        // Act & Assert
        assertThrows(NullPointerException.class, () -> {
            xaiService.predict(1L, null, testUser.getId());
        });
    }
    
    @Test
    void testPredict_NullModelId() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> {
            xaiService.predict(null, testInputData, testUser.getId());
        });
    }
    
    @Test
    void testPredict_NullUserId() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> {
            xaiService.predict(1L, testInputData, null);
        });
    }
    
    @Test
    void testExplain_ModelFileNotFound() throws IOException {
        // Arrange
        testModel.setSerializedModelPath("/nonexistent/path/model.model");
        when(modelRepository.findByIdAndDatasetOwnerId(1L, testUser.getId()))
            .thenReturn(Optional.of(testModel));
        
        // Mock file not found
        when(java.nio.file.Files.readAllBytes(any(java.nio.file.Path.class)))
            .thenThrow(new java.io.IOException("File not found"));
        
        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            xaiService.explain(1L, testInputData, testUser.getId());
        });
    }
    
    @Test
    void testExplain_InvalidModelFile() throws IOException {
        // Arrange
        when(modelRepository.findByIdAndDatasetOwnerId(1L, testUser.getId()))
            .thenReturn(Optional.of(testModel));
        
        // Mock invalid serialized data
        byte[] invalidData = "invalid serialized data".getBytes();
        when(java.nio.file.Files.readAllBytes(any(java.nio.file.Path.class)))
            .thenReturn(invalidData);
        
        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            xaiService.explain(1L, testInputData, testUser.getId());
        });
    }
    
    /**
     * Helper method to serialize a mock model for testing
     */
    private byte[] serializeModel(Model<?> model) throws Exception {
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(baos);
        oos.writeObject(model);
        oos.close();
        return baos.toByteArray();
    }
}
