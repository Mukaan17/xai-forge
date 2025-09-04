# XAI-Forge: System Architecture

This document provides a detailed breakdown of the XAI-Forge system architecture, covering high-level components, low-level backend design, and data flows.

**(Note: To view the diagrams below, use a Markdown previewer with Mermaid.js support, such as the one in VS Code or GitHub.)**

### 1. High-Level Architecture

The system follows a classic **three-tier architecture**, which separates the presentation, logic, and data layers. This is a robust and scalable pattern for modern web applications.

#### High-Level Diagram

```mermaid
graph TD
    A[User Browser <br> (React SPA)] <-->|1. HTTP/S REST API| B(Backend Server <br> (Spring Boot App))
    B -->|2. JDBC| C(PostgreSQL DB <br> (Metadata))
    B -->|3. File I/O| D(File System <br> (CSVs & Models))
```

#### Component Breakdown:

1.  **Client (React Single-Page Application):**
    *   **Role:** Provides the entire user experience. It's responsible for all rendering, user interaction, and state management.
    *   **Communication:** Interacts exclusively with the Backend Server via a stateless REST API using JSON. A JWT is sent in the `Authorization` header of each request.

2.  **Backend Server (Spring Boot Application):**
    *   **Role:** The core of the application. It handles all business logic, security, and data processing.
    *   **Responsibilities:** Exposes REST endpoints, manages security, contains all business logic, and coordinates data between the database and file system.

3.  **Data Persistence Layer:**
    *   **PostgreSQL Database:** Stores structured metadata like user accounts, dataset info, and model configurations.
    *   **File System:** Stores large, unstructured binary data: the uploaded CSV files and the serialized machine learning models.

---

### 2. Low-Level Backend Architecture

Internally, the backend is structured using a standard **layered architecture** to promote separation of concerns, testability, and maintainability.

```mermaid
graph TD
    subgraph Backend
        Controller(Controller Layer) --> Service(Service Layer)
        Service --> Repository(Repository Layer)
        Repository --> Entity(Entity Layer)
    end
```

*   **Controller Layer (`@RestController`):** The entry point for all API requests. Handles HTTP protocol, validates DTOs, and calls the appropriate service. Contains no business logic.
*   **Service Layer (`@Service`):** Contains the core application logic. Orchestrates model training, prediction, and explanation generation by coordinating data from repositories and external libraries like Tribuo.
*   **Repository Layer (`@Repository`):** An abstraction over the database provided by Spring Data JPA. Provides CRUD functionality for entities.
*   **Entity Layer (`@Entity`):** Plain Java Objects (POJOs) that map directly to tables in the PostgreSQL database.

---

### 3. Data Flow: Prediction & Explanation

This sequence diagram illustrates the interactions for the system's most critical feature.

```mermaid
sequenceDiagram
    participant Client as React App
    participant ModelController as Controller
    participant XaiService as Service
    participant MLModelRepository as Repository
    participant FileSystem as File System
    participant Tribuo as Tribuo ML Engine

    Client->>+ModelController: POST /api/models/{id}/explain (with input data & JWT)
    ModelController->>+XaiService: explain(modelId, inputData)
    XaiService->>+MLModelRepository: findById(modelId)
    MLModelRepository-->>-XaiService: Return MLModel entity (with modelFilePath)
    XaiService->>+FileSystem: Read serialized model from modelFilePath
    FileSystem-->>-XaiService: Return model byte stream
    XaiService->>+Tribuo: Deserialize model object
    XaiService->>+Tribuo: Create LIME explainer with model
    XaiService->>+Tribuo: generateExplanation(inputData)
    Tribuo-->>-XaiService: Return Explanation object
    XaiService-->>-ModelController: Return ExplanationResponse DTO
    ModelController-->>-Client: 200 OK (with JSON payload)
```
