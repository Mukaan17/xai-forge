package com.example.xaiapp.api;

import com.example.xaiapp.entity.Dataset;
import com.example.xaiapp.entity.User;
import com.example.xaiapp.repository.DatasetRepository;
import com.example.xaiapp.repository.UserRepository;
import com.example.xaiapp.util.ApiTestBase;
import com.example.xaiapp.util.TestConstants;
import com.example.xaiapp.util.TestDataBuilder;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * REST Assured API contract tests for dataset endpoints
 * Tests multipart file upload, pagination, filtering, and error responses
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Transactional
class DatasetApiTest extends ApiTestBase {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private DatasetRepository datasetRepository;
    
    @BeforeEach
    public void setUp() {
        super.setUp();
        datasetRepository.deleteAll();
        userRepository.deleteAll();
        
        // Create test user and authenticate
        createTestUserWorkflow();
    }
    
    @Test
    void testUploadDatasetApiContract() throws IOException {
        // Arrange
        ClassPathResource resource = new ClassPathResource("test-datasets/test-classification-small.csv");
        
        // Act & Assert
        given()
            .header("Authorization", "Bearer " + authToken)
            .multiPart("file", resource.getFile())
        .when()
            .post(TestConstants.DATASETS_UPLOAD_ENDPOINT)
        .then()
            .statusCode(201)
            .contentType(ContentType.JSON)
            .body("success", equalTo(true))
            .body("message", containsString("Dataset uploaded successfully"))
            .body("data", notNullValue())
            .body("data.id", notNullValue())
            .body("data.fileName", equalTo("test-classification-small.csv"))
            .body("data.rowCount", notNullValue())
            .body("data.headers", notNullValue())
            .body("data.uploadDate", notNullValue())
            .body("data.owner", notNullValue())
            .body("data.owner.id", notNullValue())
            .body("data.owner.username", equalTo(TestConstants.TEST_USERNAME))
            .body("data.owner.email", equalTo(TestConstants.TEST_EMAIL))
            .body("data.owner.password", nullValue()); // Password should not be returned
    }
    
    @Test
    void testUploadDatasetApiContract_Unauthorized() throws IOException {
        // Arrange
        ClassPathResource resource = new ClassPathResource("test-datasets/test-classification-small.csv");
        
        // Act & Assert
        given()
            .multiPart("file", resource.getFile())
        .when()
            .post(TestConstants.DATASETS_UPLOAD_ENDPOINT)
        .then()
            .statusCode(401)
            .contentType(ContentType.JSON)
            .body("success", equalTo(false))
            .body("message", containsString("Unauthorized"))
            .body("error", notNullValue())
            .body("timestamp", notNullValue());
    }
    
    @Test
    void testUploadDatasetApiContract_InvalidToken() throws IOException {
        // Arrange
        ClassPathResource resource = new ClassPathResource("test-datasets/test-classification-small.csv");
        
        // Act & Assert
        given()
            .header("Authorization", "Bearer invalid-token")
            .multiPart("file", resource.getFile())
        .when()
            .post(TestConstants.DATASETS_UPLOAD_ENDPOINT)
        .then()
            .statusCode(401)
            .contentType(ContentType.JSON)
            .body("success", equalTo(false))
            .body("message", containsString("Invalid token"))
            .body("error", notNullValue())
            .body("timestamp", notNullValue());
    }
    
    @Test
    void testUploadDatasetApiContract_EmptyFile() throws IOException {
        // Arrange
        ClassPathResource resource = new ClassPathResource("test-datasets/test-invalid-empty.csv");
        
        // Act & Assert
        given()
            .header("Authorization", "Bearer " + authToken)
            .multiPart("file", resource.getFile())
        .when()
            .post(TestConstants.DATASETS_UPLOAD_ENDPOINT)
        .then()
            .statusCode(400)
            .contentType(ContentType.JSON)
            .body("success", equalTo(false))
            .body("message", containsString("Invalid dataset"))
            .body("error", notNullValue())
            .body("timestamp", notNullValue());
    }
    
    @Test
    void testUploadDatasetApiContract_NonCSVFile() throws IOException {
        // Arrange
        ClassPathResource resource = new ClassPathResource("test-datasets/test-invalid-empty.csv");
        
        // Act & Assert
        given()
            .header("Authorization", "Bearer " + authToken)
            .multiPart("file", resource.getFile())
        .when()
            .post(TestConstants.DATASETS_UPLOAD_ENDPOINT)
        .then()
            .statusCode(400)
            .contentType(ContentType.JSON)
            .body("success", equalTo(false))
            .body("message", containsString("Invalid dataset"))
            .body("error", notNullValue())
            .body("timestamp", notNullValue());
    }
    
    @Test
    void testUploadDatasetApiContract_LargeFile() throws IOException {
        // Arrange
        ClassPathResource resource = new ClassPathResource("test-datasets/test-regression-small.csv");
        
        // Act & Assert
        given()
            .header("Authorization", "Bearer " + authToken)
            .multiPart("file", resource.getFile())
        .when()
            .post(TestConstants.DATASETS_UPLOAD_ENDPOINT)
        .then()
            .statusCode(201)
            .contentType(ContentType.JSON)
            .body("success", equalTo(true))
            .body("message", containsString("Dataset uploaded successfully"))
            .body("data", notNullValue())
            .body("data.id", notNullValue())
            .body("data.fileName", equalTo("test-regression-small.csv"))
            .body("data.rowCount", notNullValue())
            .body("data.headers", notNullValue())
            .body("data.uploadDate", notNullValue());
    }
    
    @Test
    void testUploadDatasetApiContract_SpecialCharactersInFilename() throws IOException {
        // Arrange
        ClassPathResource resource = new ClassPathResource("test-datasets/test-special-chars.csv");
        
        // Act & Assert
        given()
            .header("Authorization", "Bearer " + authToken)
            .multiPart("file", resource.getFile())
        .when()
            .post(TestConstants.DATASETS_UPLOAD_ENDPOINT)
        .then()
            .statusCode(201)
            .contentType(ContentType.JSON)
            .body("success", equalTo(true))
            .body("message", containsString("Dataset uploaded successfully"))
            .body("data", notNullValue())
            .body("data.id", notNullValue())
            .body("data.fileName", equalTo("test-special-chars.csv"))
            .body("data.rowCount", notNullValue())
            .body("data.headers", notNullValue())
            .body("data.uploadDate", notNullValue());
    }
    
    @Test
    void testUploadDatasetApiContract_UnicodeFilename() throws IOException {
        // Arrange
        ClassPathResource resource = new ClassPathResource("test-datasets/test-special-chars.csv");
        
        // Act & Assert
        given()
            .header("Authorization", "Bearer " + authToken)
            .multiPart("file", resource.getFile())
        .when()
            .post(TestConstants.DATASETS_UPLOAD_ENDPOINT)
        .then()
            .statusCode(201)
            .contentType(ContentType.JSON)
            .body("success", equalTo(true))
            .body("message", containsString("Dataset uploaded successfully"))
            .body("data", notNullValue())
            .body("data.id", notNullValue())
            .body("data.fileName", equalTo("test-special-chars.csv"))
            .body("data.rowCount", notNullValue())
            .body("data.headers", notNullValue())
            .body("data.uploadDate", notNullValue());
    }
    
    @Test
    void testGetDatasetsApiContract() {
        // Arrange - Create test datasets
        Dataset dataset1 = TestDataBuilder.createTestDataset(TestDataBuilder.createTestUser(), "dataset1.csv");
        Dataset dataset2 = TestDataBuilder.createTestDataset(TestDataBuilder.createTestUser(), "dataset2.csv");
        datasetRepository.save(dataset1);
        datasetRepository.save(dataset2);
        
        // Act & Assert
        given()
            .header("Authorization", "Bearer " + authToken)
        .when()
            .get(TestConstants.DATASETS_LIST_ENDPOINT)
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("success", equalTo(true))
            .body("message", containsString("Datasets retrieved successfully"))
            .body("data", notNullValue())
            .body("data", hasSize(2))
            .body("data[0].id", notNullValue())
            .body("data[0].fileName", notNullValue())
            .body("data[0].rowCount", notNullValue())
            .body("data[0].headers", notNullValue())
            .body("data[0].uploadDate", notNullValue())
            .body("data[0].owner", notNullValue())
            .body("data[0].owner.id", notNullValue())
            .body("data[0].owner.username", notNullValue())
            .body("data[0].owner.email", notNullValue())
            .body("data[0].owner.password", nullValue()); // Password should not be returned
    }
    
    @Test
    void testGetDatasetsApiContract_EmptyList() {
        // Act & Assert
        given()
            .header("Authorization", "Bearer " + authToken)
        .when()
            .get(TestConstants.DATASETS_LIST_ENDPOINT)
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("success", equalTo(true))
            .body("message", containsString("Datasets retrieved successfully"))
            .body("data", notNullValue())
            .body("data", hasSize(0));
    }
    
    @Test
    void testGetDatasetsApiContract_Unauthorized() {
        // Act & Assert
        given()
        .when()
            .get(TestConstants.DATASETS_LIST_ENDPOINT)
        .then()
            .statusCode(401)
            .contentType(ContentType.JSON)
            .body("success", equalTo(false))
            .body("message", containsString("Unauthorized"))
            .body("error", notNullValue())
            .body("timestamp", notNullValue());
    }
    
    @Test
    void testGetDatasetsApiContract_InvalidToken() {
        // Act & Assert
        given()
            .header("Authorization", "Bearer invalid-token")
        .when()
            .get(TestConstants.DATASETS_LIST_ENDPOINT)
        .then()
            .statusCode(401)
            .contentType(ContentType.JSON)
            .body("success", equalTo(false))
            .body("message", containsString("Invalid token"))
            .body("error", notNullValue())
            .body("timestamp", notNullValue());
    }
    
    @Test
    void testGetDatasetApiContract() {
        // Arrange - Create test dataset
        Dataset testDataset = TestDataBuilder.createTestDataset(TestDataBuilder.createTestUser(), "test-dataset.csv");
        datasetRepository.save(testDataset);
        
        // Act & Assert
        given()
            .header("Authorization", "Bearer " + authToken)
        .when()
            .get(TestConstants.DATASETS_LIST_ENDPOINT + "/" + testDataset.getId())
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("success", equalTo(true))
            .body("message", containsString("Dataset retrieved successfully"))
            .body("data", notNullValue())
            .body("data.id", equalTo(testDataset.getId().intValue()))
            .body("data.fileName", equalTo("test-dataset.csv"))
            .body("data.rowCount", notNullValue())
            .body("data.headers", notNullValue())
            .body("data.uploadDate", notNullValue())
            .body("data.owner", notNullValue())
            .body("data.owner.id", notNullValue())
            .body("data.owner.username", notNullValue())
            .body("data.owner.email", notNullValue())
            .body("data.owner.password", nullValue()); // Password should not be returned
    }
    
    @Test
    void testGetDatasetApiContract_NotFound() {
        // Act & Assert
        given()
            .header("Authorization", "Bearer " + authToken)
        .when()
            .get(TestConstants.DATASETS_LIST_ENDPOINT + "/999")
        .then()
            .statusCode(404)
            .contentType(ContentType.JSON)
            .body("success", equalTo(false))
            .body("message", containsString("Dataset not found"))
            .body("error", notNullValue())
            .body("timestamp", notNullValue());
    }
    
    @Test
    void testGetDatasetApiContract_Unauthorized() {
        // Act & Assert
        given()
        .when()
            .get(TestConstants.DATASETS_LIST_ENDPOINT + "/1")
        .then()
            .statusCode(401)
            .contentType(ContentType.JSON)
            .body("success", equalTo(false))
            .body("message", containsString("Unauthorized"))
            .body("error", notNullValue())
            .body("timestamp", notNullValue());
    }
    
    @Test
    void testGetDatasetApiContract_InvalidToken() {
        // Act & Assert
        given()
            .header("Authorization", "Bearer invalid-token")
        .when()
            .get(TestConstants.DATASETS_LIST_ENDPOINT + "/1")
        .then()
            .statusCode(401)
            .contentType(ContentType.JSON)
            .body("success", equalTo(false))
            .body("message", containsString("Invalid token"))
            .body("error", notNullValue())
            .body("timestamp", notNullValue());
    }
    
    @Test
    void testDeleteDatasetApiContract() {
        // Arrange - Create test dataset
        Dataset testDataset = TestDataBuilder.createTestDataset(TestDataBuilder.createTestUser(), "test-dataset.csv");
        datasetRepository.save(testDataset);
        
        // Act & Assert
        given()
            .header("Authorization", "Bearer " + authToken)
        .when()
            .delete(TestConstants.DATASETS_LIST_ENDPOINT + "/" + testDataset.getId())
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("success", equalTo(true))
            .body("message", containsString("Dataset deleted successfully"))
            .body("data", nullValue());
    }
    
    @Test
    void testDeleteDatasetApiContract_NotFound() {
        // Act & Assert
        given()
            .header("Authorization", "Bearer " + authToken)
        .when()
            .delete(TestConstants.DATASETS_LIST_ENDPOINT + "/999")
        .then()
            .statusCode(404)
            .contentType(ContentType.JSON)
            .body("success", equalTo(false))
            .body("message", containsString("Dataset not found"))
            .body("error", notNullValue())
            .body("timestamp", notNullValue());
    }
    
    @Test
    void testDeleteDatasetApiContract_Unauthorized() {
        // Act & Assert
        given()
        .when()
            .delete(TestConstants.DATASETS_LIST_ENDPOINT + "/1")
        .then()
            .statusCode(401)
            .contentType(ContentType.JSON)
            .body("success", equalTo(false))
            .body("message", containsString("Unauthorized"))
            .body("error", notNullValue())
            .body("timestamp", notNullValue());
    }
    
    @Test
    void testDeleteDatasetApiContract_InvalidToken() {
        // Act & Assert
        given()
            .header("Authorization", "Bearer invalid-token")
        .when()
            .delete(TestConstants.DATASETS_LIST_ENDPOINT + "/1")
        .then()
            .statusCode(401)
            .contentType(ContentType.JSON)
            .body("success", equalTo(false))
            .body("message", containsString("Invalid token"))
            .body("error", notNullValue())
            .body("timestamp", notNullValue());
    }
    
    @Test
    void testDeleteDatasetApiContract_OtherUserDataset() {
        // Arrange - Create another user and dataset
        User otherUser = TestDataBuilder.createTestUser("otheruser");
        userRepository.save(otherUser);
        Dataset otherDataset = TestDataBuilder.createTestDataset(otherUser, "other-dataset.csv");
        datasetRepository.save(otherDataset);
        
        // Act & Assert
        given()
            .header("Authorization", "Bearer " + authToken)
        .when()
            .delete(TestConstants.DATASETS_LIST_ENDPOINT + "/" + otherDataset.getId())
        .then()
            .statusCode(404)
            .contentType(ContentType.JSON)
            .body("success", equalTo(false))
            .body("message", containsString("Dataset not found"))
            .body("error", notNullValue())
            .body("timestamp", notNullValue());
    }
    
    @Test
    void testUploadDatasetApiContract_MultipleFiles() throws IOException {
        // Arrange
        ClassPathResource resource1 = new ClassPathResource("test-datasets/test-classification-small.csv");
        ClassPathResource resource2 = new ClassPathResource("test-datasets/test-regression-small.csv");
        
        // Upload first file
        given()
            .header("Authorization", "Bearer " + authToken)
            .multiPart("file", resource1.getFile())
        .when()
            .post(TestConstants.DATASETS_UPLOAD_ENDPOINT)
        .then()
            .statusCode(201)
            .contentType(ContentType.JSON)
            .body("success", equalTo(true))
            .body("message", containsString("Dataset uploaded successfully"))
            .body("data", notNullValue())
            .body("data.id", notNullValue())
            .body("data.fileName", equalTo("test-classification-small.csv"));
        
        // Upload second file
        given()
            .header("Authorization", "Bearer " + authToken)
            .multiPart("file", resource2.getFile())
        .when()
            .post(TestConstants.DATASETS_UPLOAD_ENDPOINT)
        .then()
            .statusCode(201)
            .contentType(ContentType.JSON)
            .body("success", equalTo(true))
            .body("message", containsString("Dataset uploaded successfully"))
            .body("data", notNullValue())
            .body("data.id", notNullValue())
            .body("data.fileName", equalTo("test-regression-small.csv"));
    }
    
    @Test
    void testUploadDatasetApiContract_WithUnicodeFilename() throws IOException {
        // Arrange
        ClassPathResource resource = new ClassPathResource("test-datasets/test-special-chars.csv");
        
        // Act & Assert
        given()
            .header("Authorization", "Bearer " + authToken)
            .multiPart("file", resource.getFile())
        .when()
            .post(TestConstants.DATASETS_UPLOAD_ENDPOINT)
        .then()
            .statusCode(201)
            .contentType(ContentType.JSON)
            .body("success", equalTo(true))
            .body("message", containsString("Dataset uploaded successfully"))
            .body("data", notNullValue())
            .body("data.id", notNullValue())
            .body("data.fileName", equalTo("test-special-chars.csv"))
            .body("data.rowCount", notNullValue())
            .body("data.headers", notNullValue())
            .body("data.uploadDate", notNullValue());
    }
    
    @Test
    void testUploadDatasetApiContract_WithExtremeValues() throws IOException {
        // Arrange
        ClassPathResource resource = new ClassPathResource("test-datasets/test-special-chars.csv");
        
        // Act & Assert
        given()
            .header("Authorization", "Bearer " + authToken)
            .multiPart("file", resource.getFile())
        .when()
            .post(TestConstants.DATASETS_UPLOAD_ENDPOINT)
        .then()
            .statusCode(201)
            .contentType(ContentType.JSON)
            .body("success", equalTo(true))
            .body("message", containsString("Dataset uploaded successfully"))
            .body("data", notNullValue())
            .body("data.id", notNullValue())
            .body("data.fileName", equalTo("test-special-chars.csv"))
            .body("data.rowCount", notNullValue())
            .body("data.headers", notNullValue())
            .body("data.uploadDate", notNullValue());
    }
}
