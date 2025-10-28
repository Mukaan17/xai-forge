# API Reference

> 📘 **Source**: This wiki page contains complete information from [docs/API-GUIDE.md](https://github.com/Mukaan17/xai-forge/blob/main/docs/API-GUIDE.md)

**Navigation**: [[Home]] > [[Developer Documentation]] > API Reference

## Table of Contents

1. [Base Information](#base-information)
2. [Authentication Endpoints](#authentication-endpoints)
3. [Dataset Endpoints](#dataset-endpoints)
4. [Model Endpoints](#model-endpoints)
5. [Error Responses](#error-responses)
6. [Testing Examples](#testing-examples)

---

## Base Information

**Base URL:** `http://localhost:8080`  
**Authentication:** All endpoints, except for `/api/auth/**`, require a `Bearer` token in the `Authorization` header.

```
Authorization: Bearer <your-jwt-token>
```

### Content Type
All requests and responses use `application/json` except for file uploads which use `multipart/form-data`.

---

## Authentication Endpoints (`/api/auth`)

### POST /api/auth/register

**Description:** Registers a new user.

**Request Body:**
```json
{
  "username": "testuser",
  "email": "test@example.com",
  "password": "password123"
}
```

**Success Response (201 CREATED):**
```json
{
  "success": true,
  "message": "User registered successfully!",
  "data": null
}
```

**Error Response (400 BAD REQUEST):**
```json
{
  "success": false,
  "message": "Username already exists",
  "errorCode": "USERNAME_EXISTS"
}
```

### POST /api/auth/login

**Description:** Authenticates a user and returns a JWT.

**Request Body:**
```json
{
  "username": "testuser",
  "password": "password123"
}
```

**Success Response (200 OK):**
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
    "tokenType": "Bearer"
  }
}
```

**Error Response (401 UNAUTHORIZED):**
```json
{
  "success": false,
  "message": "Invalid credentials",
  "errorCode": "INVALID_CREDENTIALS"
}
```

---

## Dataset Endpoints (`/api/datasets`)

### POST /api/datasets/upload

**Description:** Uploads a CSV dataset for the authenticated user.

**Request Type:** `multipart/form-data`

**Form Data:**
- `file`: The CSV file (required)

**Headers:**
```
Authorization: Bearer <jwt-token>
Content-Type: multipart/form-data
```

**Success Response (201 CREATED):**
```json
{
  "success": true,
  "message": "Dataset uploaded successfully",
  "data": {
    "id": 1,
    "fileName": "customer-churn.csv",
    "headers": ["gender", "age", "tenure", "churn"],
    "rowCount": 1000,
    "uploadDate": "2025-01-04T10:00:00.000+00:00"
  }
}
```

**Error Response (400 BAD REQUEST):**
```json
{
  "success": false,
  "message": "Invalid file format. Only CSV files are allowed",
  "errorCode": "INVALID_FILE_FORMAT"
}
```

### GET /api/datasets

**Description:** Retrieves a list of all datasets uploaded by the authenticated user.

**Headers:**
```
Authorization: Bearer <jwt-token>
```

**Success Response (200 OK):**
```json
{
  "success": true,
  "message": "Datasets retrieved successfully",
  "data": [
    {
      "id": 1,
      "fileName": "customer-churn.csv",
      "headers": ["gender", "age", "tenure", "churn"],
      "rowCount": 1000,
      "uploadDate": "2025-01-04T10:00:00.000+00:00"
    },
    {
      "id": 2,
      "fileName": "sales-data.csv",
      "headers": ["product", "price", "quantity", "revenue"],
      "rowCount": 500,
      "uploadDate": "2025-01-04T11:00:00.000+00:00"
    }
  ]
}
```

### GET /api/datasets/{id}

**Description:** Retrieves detailed metadata for a single dataset.

**Path Parameters:**
- `id`: Dataset ID (required)

**Headers:**
```
Authorization: Bearer <jwt-token>
```

**Success Response (200 OK):**
```json
{
  "success": true,
  "message": "Dataset retrieved successfully",
  "data": {
    "id": 1,
    "fileName": "customer-churn.csv",
    "headers": ["gender", "age", "tenure", "churn"],
    "rowCount": 1000,
    "uploadDate": "2025-01-04T10:00:00.000+00:00",
    "filePath": "/uploads/datasets/customer-churn.csv"
  }
}
```

**Error Response (404 NOT FOUND):**
```json
{
  "success": false,
  "message": "Dataset not found with ID: 999",
  "errorCode": "DATASET_NOT_FOUND"
}
```

### DELETE /api/datasets/{id}

**Description:** Deletes a dataset and all associated models.

**Path Parameters:**
- `id`: Dataset ID (required)

**Headers:**
```
Authorization: Bearer <jwt-token>
```

**Success Response (200 OK):**
```json
{
  "success": true,
  "message": "Dataset deleted successfully",
  "data": null
}
```

---

## Model Endpoints (`/api/models`)

### POST /api/models/train

**Description:** Trains a new ML model using a specified dataset.

**Request Body:**
```json
{
  "modelName": "Churn Predictor",
  "modelType": "CLASSIFICATION",
  "datasetId": 1,
  "targetVariable": "churn",
  "featureNames": ["age", "tenure", "monthly_bill"]
}
```

**Request Body Fields:**
- `modelName`: Name for the model (required, max 100 characters)
- `modelType`: Either "CLASSIFICATION" or "REGRESSION" (required)
- `datasetId`: ID of the dataset to train on (required)
- `targetVariable`: Column name to predict (required)
- `featureNames`: Array of feature column names (required, min 1)

**Headers:**
```
Authorization: Bearer <jwt-token>
Content-Type: application/json
```

**Success Response (201 CREATED):**
```json
{
  "success": true,
  "message": "Model trained successfully",
  "data": {
    "id": 1,
    "modelName": "Churn Predictor",
    "modelType": "CLASSIFICATION",
    "targetVariable": "churn",
    "featureNames": ["age", "tenure", "monthly_bill"],
    "trainingDate": "2025-01-04T11:00:00.000+00:00",
    "accuracy": 0.85,
    "datasetId": 1
  }
}
```

**Error Response (400 BAD REQUEST):**
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

### GET /api/models

**Description:** Retrieves a list of all models trained by the authenticated user.

**Headers:**
```
Authorization: Bearer <jwt-token>
```

**Success Response (200 OK):**
```json
{
  "success": true,
  "message": "Models retrieved successfully",
  "data": [
    {
      "id": 1,
      "modelName": "Churn Predictor",
      "modelType": "CLASSIFICATION",
      "targetVariable": "churn",
      "featureNames": ["age", "tenure", "monthly_bill"],
      "trainingDate": "2025-01-04T11:00:00.000+00:00",
      "accuracy": 0.85
    }
  ]
}
```

### GET /api/models/{id}

**Description:** Retrieves detailed information for a specific model.

**Path Parameters:**
- `id`: Model ID (required)

**Headers:**
```
Authorization: Bearer <jwt-token>
```

**Success Response (200 OK):**
```json
{
  "success": true,
  "message": "Model retrieved successfully",
  "data": {
    "id": 1,
    "modelName": "Churn Predictor",
    "modelType": "CLASSIFICATION",
    "targetVariable": "churn",
    "featureNames": ["age", "tenure", "monthly_bill"],
    "trainingDate": "2025-01-04T11:00:00.000+00:00",
    "accuracy": 0.85,
    "modelFilePath": "/uploads/models/model_1.ser"
  }
}
```

### POST /api/models/{id}/predict

**Description:** Gets a prediction from a trained model for a single data point.

**Path Parameters:**
- `id`: Model ID (required)

**Request Body:** A map of feature names to values.
```json
{
  "age": "45",
  "tenure": "3",
  "monthly_bill": "75.50"
}
```

**Headers:**
```
Authorization: Bearer <jwt-token>
Content-Type: application/json
```

**Success Response (200 OK):**
```json
{
  "success": true,
  "message": "Prediction generated successfully",
  "data": {
    "prediction": "True",
    "confidence": 0.85,
    "probabilities": {
      "True": 0.85,
      "False": 0.15
    },
    "inputData": {
      "age": "45",
      "tenure": "3",
      "monthly_bill": "75.50"
    }
  }
}
```

### POST /api/models/{id}/explain

**Description:** Gets a prediction and a feature-based explanation for a single data point.

**Path Parameters:**
- `id`: Model ID (required)

**Request Body:** Same as the predict endpoint.
```json
{
  "age": "45",
  "tenure": "3",
  "monthly_bill": "75.50"
}
```

**Headers:**
```
Authorization: Bearer <jwt-token>
Content-Type: application/json
```

**Success Response (200 OK):**
```json
{
  "success": true,
  "message": "Explanation generated successfully",
  "data": {
    "prediction": "True",
    "confidence": 0.85,
    "probabilities": {
      "True": 0.85,
      "False": 0.15
    },
    "inputData": {
      "age": "45",
      "tenure": "3",
      "monthly_bill": "75.50"
    },
    "explanation": [
      {
        "feature": "tenure",
        "score": 0.65,
        "impact": "positive"
      },
      {
        "feature": "age",
        "score": -0.21,
        "impact": "negative"
      },
      {
        "feature": "monthly_bill",
        "score": 0.12,
        "impact": "positive"
      }
    ]
  }
}
```

### DELETE /api/models/{id}

**Description:** Deletes a trained model.

**Path Parameters:**
- `id`: Model ID (required)

**Headers:**
```
Authorization: Bearer <jwt-token>
```

**Success Response (200 OK):**
```json
{
  "success": true,
  "message": "Model deleted successfully",
  "data": null
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
  "timestamp": "2025-01-04T18:45:00Z",
  "details": {
    "field": "fieldName",
    "reason": "Specific error reason"
  }
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

#### 403 Forbidden
```json
{
  "success": false,
  "message": "Access denied",
  "errorCode": "AUTHORIZATION_ERROR"
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

---

## Testing Examples

### Using curl

#### Register a new user
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "password123"
  }'
```

#### Login and get JWT token
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "password123"
  }'
```

#### Upload a dataset
```bash
curl -X POST http://localhost:8080/api/datasets/upload \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -F "file=@/path/to/your/dataset.csv"
```

#### Train a model
```bash
curl -X POST http://localhost:8080/api/models/train \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "modelName": "Test Model",
    "modelType": "CLASSIFICATION",
    "datasetId": 1,
    "targetVariable": "churn",
    "featureNames": ["age", "tenure"]
  }'
```

#### Make a prediction
```bash
curl -X POST http://localhost:8080/api/models/1/predict \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "age": "45",
    "tenure": "3"
  }'
```

#### Get explanation
```bash
curl -X POST http://localhost:8080/api/models/1/explain \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "age": "45",
    "tenure": "3"
  }'
```

### Using JavaScript/Fetch

#### Login and store token
```javascript
const login = async (username, password) => {
  const response = await fetch('http://localhost:8080/api/auth/login', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({ username, password }),
  });
  
  const data = await response.json();
  if (data.success) {
    localStorage.setItem('token', data.data.accessToken);
    return data.data.accessToken;
  }
  throw new Error(data.message);
};
```

#### Make authenticated request
```javascript
const makeRequest = async (url, options = {}) => {
  const token = localStorage.getItem('token');
  
  const response = await fetch(url, {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`,
      ...options.headers,
    },
  });
  
  return response.json();
};
```

#### Get user's datasets
```javascript
const getDatasets = async () => {
  return makeRequest('http://localhost:8080/api/datasets');
};
```

#### Train a model
```javascript
const trainModel = async (modelData) => {
  return makeRequest('http://localhost:8080/api/models/train', {
    method: 'POST',
    body: JSON.stringify(modelData),
  });
};
```

---

**Next**: [[Advanced Concepts|Advanced-Concepts]] | **Previous**: [[Architecture]]  
**Related**: [[User Guide|User-Guide]], [[Testing Guide|Testing-Guide]], [[Troubleshooting]]
