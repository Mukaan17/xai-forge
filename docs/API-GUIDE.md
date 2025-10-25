# XAI-Forge: REST API Guide

This document provides a detailed reference for the REST API endpoints exposed by the XAI-Forge backend.

**Base URL:** `http://localhost:8080`
**Authentication:** All endpoints, except for `/api/auth/**`, require a `Bearer` token in the `Authorization` header.
`Authorization: Bearer <your-jwt-token>`

---

### 1. Authentication (`/api/auth`)

#### `POST /api/auth/register`
*   **Description:** Registers a new user.
*   **Request Body:**
    ```json
    {
      "username": "testuser",
      "email": "test@example.com",
      "password": "password123"
    }
    ```
*   **Success Response (201 CREATED):**
    ```json
    { "message": "User registered successfully!" }
    ```

#### `POST /api/auth/login`
*   **Description:** Authenticates a user and returns a JWT.
*   **Request Body:**
    ```json
    {
      "username": "testuser",
      "password": "password123"
    }
    ```
*   **Success Response (200 OK):**
    ```json
    {
      "accessToken": "ey...",
      "tokenType": "Bearer"
    }
    ```

---

### 2. Datasets (`/api/datasets`)

#### `POST /api/datasets/upload`
*   **Description:** Uploads a CSV dataset for the authenticated user.
*   **Request Type:** `multipart/form-data`
*   **Form Data:** `file`: The CSV file.
*   **Success Response (201 CREATED):** Returns the created `Dataset` entity metadata.
    ```json
    {
        "id": 1,
        "fileName": "customer-churn.csv",
        "headers": ["gender", "age", "tenure", "churn"],
        "uploadDate": "2025-09-05T10:00:00.000+00:00"
    }
    ```

#### `GET /api/datasets`
*   **Description:** Retrieves a list of all datasets uploaded by the authenticated user.
*   **Success Response (200 OK):** An array of `Dataset` metadata objects.

#### `GET /api/datasets/{id}`
*   **Description:** Retrieves detailed metadata for a single dataset.
*   **Success Response (200 OK):** A single `Dataset` metadata object.

---

### 3. Models (`/api/models`)

#### `POST /api/models/train`
*   **Description:** Trains a new ML model using a specified dataset.
*   **Request Body:**
    ```json
    {
      "datasetId": 1,
      "modelName": "Churn Predictor",
      "targetVariable": "churn",
      "featureNames": ["age", "tenure"]
    }
    ```
*   **Success Response (201 CREATED):** Returns the created `MLModel` entity metadata.
    ```json
    {
        "id": 1,
        "modelName": "Churn Predictor",
        "modelType": "CLASSIFICATION",
        "trainingDate": "2025-09-05T11:00:00.000+00:00"
    }
    ```

#### `POST /api/models/{id}/predict`
*   **Description:** Gets a prediction from a trained model for a single data point.
*   **Request Body:** A map of feature names to values.
    ```json
    {
      "age": "45",
      "tenure": "3"
    }
    ```
*   **Success Response (200 OK):**
    ```json
    {
        "prediction": "True",
        "confidenceScores": {
            "True": 0.85,
            "False": 0.15
        }
    }
    ```

#### `POST /api/models/{id}/explain`
*   **Description:** Gets a prediction and a feature-based explanation for a single data point.
*   **Request Body:** Same as the predict endpoint.
*   **Success Response (200 OK):**
    ```json
    {
        "prediction": "True",
        "confidence": 0.85,
        "probabilities": {
            "True": 0.85,
            "False": 0.15
        },
        "inputData": {
            "age": "45",
            "tenure": "3"
        },
        "explanation": [
            { "feature": "tenure", "score": 0.65 },
            { "feature": "age", "score": -0.21 }
        ]
    }
    ```

---

## Error Responses

### Standard Error Format

All error responses follow a consistent format:

```json
{
    "success": false,
    "message": "Error description",
    "errorCode": "ERROR_CODE",
    "timestamp": "2025-10-24T18:45:00Z"
}
```

### Common Error Responses

#### 400 Bad Request
```json
{
    "success": false,
    "message": "Validation failed",
    "errorCode": "VALIDATION_ERROR",
    "details": {
        "field": "modelName",
        "reason": "Model name is required"
    }
}
```

#### 401 Unauthorized
```json
{
    "success": false,
    "message": "Authentication required",
    "errorCode": "AUTHENTICATION_REQUIRED"
}
```

#### 404 Not Found
```json
{
    "success": false,
    "message": "Dataset not found with ID: 123",
    "errorCode": "DATASET_NOT_FOUND"
}
```

#### 500 Internal Server Error
```json
{
    "success": false,
    "message": "Model training failed: Insufficient data",
    "errorCode": "MODEL_TRAINING_ERROR"
}
```

### Configuration Validation Errors

#### Startup Configuration Errors
```json
{
    "success": false,
    "message": "Configuration validation failed",
    "errorCode": "CONFIGURATION_ERROR",
    "details": {
        "parameter": "JWT_SECRET",
        "reason": "JWT_SECRET must be at least 32 characters"
    }
}
```

### Exception Hierarchy Error Codes

| Exception | Error Code | HTTP Status |
|-----------|------------|-------------|
| `AuthenticationException` | `AUTHENTICATION_ERROR` | 401 |
| `AuthorizationException` | `AUTHORIZATION_ERROR` | 403 |
| `DatasetNotFoundException` | `DATASET_NOT_FOUND` | 404 |
| `ModelNotFoundException` | `MODEL_NOT_FOUND` | 404 |
| `ModelTrainingException` | `MODEL_TRAINING_ERROR` | 500 |
| `DatasetParsingException` | `DATASET_PARSING_ERROR` | 400 |
| `ResourceExhaustedException` | `RESOURCE_EXHAUSTED` | 507 |
| `ConcurrentModificationException` | `CONCURRENT_MODIFICATION` | 409 |
