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
        "explanation": [
            { "feature": "tenure", "score": 0.65 },
            { "feature": "age", "score": -0.21 }
        ]
    }
    ```
