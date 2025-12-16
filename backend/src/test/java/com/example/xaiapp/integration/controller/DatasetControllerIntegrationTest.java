package com.example.xaiapp.integration.controller;

import com.example.xaiapp.entity.Dataset;
import com.example.xaiapp.entity.User;
import com.example.xaiapp.repository.DatasetRepository;
import com.example.xaiapp.repository.UserRepository;
import com.example.xaiapp.util.TestConstants;
import com.example.xaiapp.util.TestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for DatasetController
 * Tests dataset management with real database and file system operations
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Transactional
class DatasetControllerIntegrationTest {
    
    @LocalServerPort
    private int port;
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private DatasetRepository datasetRepository;
    
    private String baseUrl;
    private String authToken;
    private User testUser;
    
    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port;
        
        // Clean up before each test
        datasetRepository.deleteAll();
        userRepository.deleteAll();
        
        // Create test user and get auth token
        testUser = TestDataBuilder.createTestUser();
        userRepository.save(testUser);
        authToken = getAuthToken();
    }
    
    private String getAuthToken() {
        // Login to get JWT token
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        
        String loginRequest = String.format(
            "{\"username\":\"%s\",\"password\":\"%s\"}",
            TestConstants.TEST_USERNAME,
            TestConstants.TEST_PASSWORD
        );
        
        HttpEntity<String> request = new HttpEntity<>(loginRequest, headers);
        ResponseEntity<String> response = restTemplate.exchange(
            baseUrl + TestConstants.AUTH_LOGIN_ENDPOINT,
            HttpMethod.POST,
            request,
            String.class
        );
        
        // Extract token from response
        String responseBody = response.getBody();
        if (responseBody != null && responseBody.contains("accessToken")) {
            return responseBody.substring(
                responseBody.indexOf("\"accessToken\":\"") + 16,
                responseBody.indexOf("\"", responseBody.indexOf("\"accessToken\":\"") + 16)
            );
        }
        return null;
    }
    
    @Test
    void testUploadDataset_Success() {
        // Arrange
        ClassPathResource resource = new ClassPathResource("test-datasets/test-classification-small.csv");
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + authToken);
        headers.set("Content-Type", "multipart/form-data");
        
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", resource);
        
        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);
        
        // Act
        ResponseEntity<Dataset> response = restTemplate.exchange(
            baseUrl + TestConstants.DATASETS_UPLOAD_ENDPOINT,
            HttpMethod.POST,
            request,
            Dataset.class
        );
        
        // Assert
        assertEquals(201, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("test-classification-small.csv", response.getBody().getFileName());
        assertNotNull(response.getBody().getHeaders());
        assertTrue(response.getBody().getHeaders().contains("feature1"));
        assertTrue(response.getBody().getHeaders().contains("feature2"));
        assertTrue(response.getBody().getHeaders().contains("feature3"));
        assertTrue(response.getBody().getHeaders().contains("target"));
        
        // Verify dataset was saved in database
        List<Dataset> datasets = datasetRepository.findByOwnerId(testUser.getId());
        assertEquals(1, datasets.size());
        assertEquals("test-classification-small.csv", datasets.get(0).getFileName());
    }
    
    @Test
    void testUploadDataset_Unauthorized() {
        // Arrange
        ClassPathResource resource = new ClassPathResource("test-datasets/test-classification-small.csv");
        
        HttpHeaders headers = new HttpHeaders();
        // No Authorization header
        headers.set("Content-Type", "multipart/form-data");
        
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", resource);
        
        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);
        
        // Act
        ResponseEntity<String> response = restTemplate.exchange(
            baseUrl + TestConstants.DATASETS_UPLOAD_ENDPOINT,
            HttpMethod.POST,
            request,
            String.class
        );
        
        // Assert
        assertEquals(401, response.getStatusCode().value());
        assertTrue(response.getBody().contains("Unauthorized"));
    }
    
    @Test
    void testUploadDataset_InvalidToken() {
        // Arrange
        ClassPathResource resource = new ClassPathResource("test-datasets/test-classification-small.csv");
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer invalid-token");
        headers.set("Content-Type", "multipart/form-data");
        
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", resource);
        
        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);
        
        // Act
        ResponseEntity<String> response = restTemplate.exchange(
            baseUrl + TestConstants.DATASETS_UPLOAD_ENDPOINT,
            HttpMethod.POST,
            request,
            String.class
        );
        
        // Assert
        assertEquals(401, response.getStatusCode().value());
        assertTrue(response.getBody().contains("Invalid token"));
    }
    
    @Test
    void testUploadDataset_EmptyFile() {
        // Arrange
        ClassPathResource resource = new ClassPathResource("test-datasets/test-invalid-empty.csv");
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + authToken);
        headers.set("Content-Type", "multipart/form-data");
        
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", resource);
        
        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);
        
        // Act
        ResponseEntity<String> response = restTemplate.exchange(
            baseUrl + TestConstants.DATASETS_UPLOAD_ENDPOINT,
            HttpMethod.POST,
            request,
            String.class
        );
        
        // Assert
        assertEquals(400, response.getStatusCode().value());
        assertTrue(response.getBody().contains("Invalid dataset"));
    }
    
    @Test
    void testUploadDataset_NonCSVFile() {
        // Arrange
        ClassPathResource resource = new ClassPathResource("test-datasets/test-invalid-empty.csv");
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + authToken);
        headers.set("Content-Type", "multipart/form-data");
        
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", resource);
        
        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);
        
        // Act
        ResponseEntity<String> response = restTemplate.exchange(
            baseUrl + TestConstants.DATASETS_UPLOAD_ENDPOINT,
            HttpMethod.POST,
            request,
            String.class
        );
        
        // Assert
        assertEquals(400, response.getStatusCode().value());
        assertTrue(response.getBody().contains("Invalid dataset"));
    }
    
    @Test
    void testUploadDataset_LargeFile() {
        // Arrange
        ClassPathResource resource = new ClassPathResource("test-datasets/test-regression-small.csv");
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + authToken);
        headers.set("Content-Type", "multipart/form-data");
        
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", resource);
        
        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);
        
        // Act
        ResponseEntity<Dataset> response = restTemplate.exchange(
            baseUrl + TestConstants.DATASETS_UPLOAD_ENDPOINT,
            HttpMethod.POST,
            request,
            Dataset.class
        );
        
        // Assert
        assertEquals(201, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("test-regression-small.csv", response.getBody().getFileName());
    }
    
    @Test
    void testUploadDataset_SpecialCharactersInFilename() {
        // Arrange
        ClassPathResource resource = new ClassPathResource("test-datasets/test-special-chars.csv");
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + authToken);
        headers.set("Content-Type", "multipart/form-data");
        
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", resource);
        
        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);
        
        // Act
        ResponseEntity<Dataset> response = restTemplate.exchange(
            baseUrl + TestConstants.DATASETS_UPLOAD_ENDPOINT,
            HttpMethod.POST,
            request,
            Dataset.class
        );
        
        // Assert
        assertEquals(201, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("test-special-chars.csv", response.getBody().getFileName());
    }
    
    @Test
    void testGetUserDatasets_Success() {
        // Arrange - Create test datasets
        Dataset dataset1 = TestDataBuilder.createTestDataset(testUser, "dataset1.csv");
        Dataset dataset2 = TestDataBuilder.createTestDataset(testUser, "dataset2.csv");
        datasetRepository.save(dataset1);
        datasetRepository.save(dataset2);
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + authToken);
        HttpEntity<String> request = new HttpEntity<>(headers);
        
        // Act
        ResponseEntity<Dataset[]> response = restTemplate.exchange(
            baseUrl + TestConstants.DATASETS_LIST_ENDPOINT,
            HttpMethod.GET,
            request,
            Dataset[].class
        );
        
        // Assert
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().length);
    }
    
    @Test
    void testGetUserDatasets_EmptyList() {
        // Arrange
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + authToken);
        HttpEntity<String> request = new HttpEntity<>(headers);
        
        // Act
        ResponseEntity<Dataset[]> response = restTemplate.exchange(
            baseUrl + TestConstants.DATASETS_LIST_ENDPOINT,
            HttpMethod.GET,
            request,
            Dataset[].class
        );
        
        // Assert
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(0, response.getBody().length);
    }
    
    @Test
    void testGetUserDatasets_Unauthorized() {
        // Arrange
        HttpHeaders headers = new HttpHeaders();
        // No Authorization header
        HttpEntity<String> request = new HttpEntity<>(headers);
        
        // Act
        ResponseEntity<String> response = restTemplate.exchange(
            baseUrl + TestConstants.DATASETS_LIST_ENDPOINT,
            HttpMethod.GET,
            request,
            String.class
        );
        
        // Assert
        assertEquals(401, response.getStatusCode().value());
        assertTrue(response.getBody().contains("Unauthorized"));
    }
    
    @Test
    void testGetDataset_Success() {
        // Arrange - Create test dataset
        Dataset testDataset = TestDataBuilder.createTestDataset(testUser, "test-dataset.csv");
        datasetRepository.save(testDataset);
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + authToken);
        HttpEntity<String> request = new HttpEntity<>(headers);
        
        // Act
        ResponseEntity<Dataset> response = restTemplate.exchange(
            baseUrl + TestConstants.DATASETS_LIST_ENDPOINT + "/" + testDataset.getId(),
            HttpMethod.GET,
            request,
            Dataset.class
        );
        
        // Assert
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(testDataset.getId(), response.getBody().getId());
        assertEquals("test-dataset.csv", response.getBody().getFileName());
    }
    
    @Test
    void testGetDataset_NotFound() {
        // Arrange
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + authToken);
        HttpEntity<String> request = new HttpEntity<>(headers);
        
        // Act
        ResponseEntity<String> response = restTemplate.exchange(
            baseUrl + TestConstants.DATASETS_LIST_ENDPOINT + "/999",
            HttpMethod.GET,
            request,
            String.class
        );
        
        // Assert
        assertEquals(404, response.getStatusCode().value());
        assertTrue(response.getBody().contains("Dataset not found"));
    }
    
    @Test
    void testGetDataset_Unauthorized() {
        // Arrange
        HttpHeaders headers = new HttpHeaders();
        // No Authorization header
        HttpEntity<String> request = new HttpEntity<>(headers);
        
        // Act
        ResponseEntity<String> response = restTemplate.exchange(
            baseUrl + TestConstants.DATASETS_LIST_ENDPOINT + "/1",
            HttpMethod.GET,
            request,
            String.class
        );
        
        // Assert
        assertEquals(401, response.getStatusCode().value());
        assertTrue(response.getBody().contains("Unauthorized"));
    }
    
    @Test
    void testDeleteDataset_Success() {
        // Arrange - Create test dataset
        Dataset testDataset = TestDataBuilder.createTestDataset(testUser, "test-dataset.csv");
        datasetRepository.save(testDataset);
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + authToken);
        HttpEntity<String> request = new HttpEntity<>(headers);
        
        // Act
        ResponseEntity<String> response = restTemplate.exchange(
            baseUrl + TestConstants.DATASETS_LIST_ENDPOINT + "/" + testDataset.getId(),
            HttpMethod.DELETE,
            request,
            String.class
        );
        
        // Assert
        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().contains("Dataset deleted successfully"));
        
        // Verify dataset was deleted from database
        assertFalse(datasetRepository.findById(testDataset.getId()).isPresent());
    }
    
    @Test
    void testDeleteDataset_NotFound() {
        // Arrange
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + authToken);
        HttpEntity<String> request = new HttpEntity<>(headers);
        
        // Act
        ResponseEntity<String> response = restTemplate.exchange(
            baseUrl + TestConstants.DATASETS_LIST_ENDPOINT + "/999",
            HttpMethod.DELETE,
            request,
            String.class
        );
        
        // Assert
        assertEquals(404, response.getStatusCode().value());
        assertTrue(response.getBody().contains("Dataset not found"));
    }
    
    @Test
    void testDeleteDataset_Unauthorized() {
        // Arrange
        HttpHeaders headers = new HttpHeaders();
        // No Authorization header
        HttpEntity<String> request = new HttpEntity<>(headers);
        
        // Act
        ResponseEntity<String> response = restTemplate.exchange(
            baseUrl + TestConstants.DATASETS_LIST_ENDPOINT + "/1",
            HttpMethod.DELETE,
            request,
            String.class
        );
        
        // Assert
        assertEquals(401, response.getStatusCode().value());
        assertTrue(response.getBody().contains("Unauthorized"));
    }
    
    @Test
    void testDeleteDataset_OtherUserDataset() {
        // Arrange - Create another user and dataset
        User otherUser = TestDataBuilder.createTestUser("otheruser");
        userRepository.save(otherUser);
        Dataset otherDataset = TestDataBuilder.createTestDataset(otherUser, "other-dataset.csv");
        datasetRepository.save(otherDataset);
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + authToken);
        HttpEntity<String> request = new HttpEntity<>(headers);
        
        // Act
        ResponseEntity<String> response = restTemplate.exchange(
            baseUrl + TestConstants.DATASETS_LIST_ENDPOINT + "/" + otherDataset.getId(),
            HttpMethod.DELETE,
            request,
            String.class
        );
        
        // Assert
        assertEquals(404, response.getStatusCode().value());
        assertTrue(response.getBody().contains("Dataset not found"));
        
        // Verify other user's dataset still exists
        assertTrue(datasetRepository.findById(otherDataset.getId()).isPresent());
    }
    
    @Test
    void testUploadDataset_MultipleFiles() {
        // Arrange
        ClassPathResource resource1 = new ClassPathResource("test-datasets/test-classification-small.csv");
        ClassPathResource resource2 = new ClassPathResource("test-datasets/test-regression-small.csv");
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + authToken);
        headers.set("Content-Type", "multipart/form-data");
        
        // Upload first file
        MultiValueMap<String, Object> body1 = new LinkedMultiValueMap<>();
        body1.add("file", resource1);
        HttpEntity<MultiValueMap<String, Object>> request1 = new HttpEntity<>(body1, headers);
        
        ResponseEntity<Dataset> response1 = restTemplate.exchange(
            baseUrl + TestConstants.DATASETS_UPLOAD_ENDPOINT,
            HttpMethod.POST,
            request1,
            Dataset.class
        );
        
        // Upload second file
        MultiValueMap<String, Object> body2 = new LinkedMultiValueMap<>();
        body2.add("file", resource2);
        HttpEntity<MultiValueMap<String, Object>> request2 = new HttpEntity<>(body2, headers);
        
        ResponseEntity<Dataset> response2 = restTemplate.exchange(
            baseUrl + TestConstants.DATASETS_UPLOAD_ENDPOINT,
            HttpMethod.POST,
            request2,
            Dataset.class
        );
        
        // Assert
        assertEquals(201, response1.getStatusCode().value());
        assertEquals(201, response2.getStatusCode().value());
        
        // Verify both datasets were saved
        List<Dataset> datasets = datasetRepository.findByOwnerId(testUser.getId());
        assertEquals(2, datasets.size());
    }
    
    @Test
    void testUploadDataset_WithUnicodeFilename() {
        // Arrange
        ClassPathResource resource = new ClassPathResource("test-datasets/test-special-chars.csv");
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + authToken);
        headers.set("Content-Type", "multipart/form-data");
        
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", resource);
        
        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);
        
        // Act
        ResponseEntity<Dataset> response = restTemplate.exchange(
            baseUrl + TestConstants.DATASETS_UPLOAD_ENDPOINT,
            HttpMethod.POST,
            request,
            Dataset.class
        );
        
        // Assert
        assertEquals(201, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("test-special-chars.csv", response.getBody().getFileName());
    }
}
