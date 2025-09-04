# XAI-Forge: Setup and Installation Guide

This guide provides step-by-step instructions to set up and run the XAI-Forge project on a local development machine.

### Prerequisites

*   **Java JDK 17+**: Required for the backend.
*   **Apache Maven 3.8+**: For building the backend project.
*   **PostgreSQL 14+**: The primary database.
*   **Node.js 18+ & npm**: For the frontend React application.
*   **Git**: For version control.

### 1. Backend Setup (Spring Boot)

1.  **Clone the Repository:**
    ```bash
    git clone <your-repository-url>
    cd xai-forge
    ```

2.  **Configure the Database:**
    *   Log in to PostgreSQL and create a new database and user.
        ```sql
        CREATE DATABASE xai_db;
        CREATE USER your_username WITH ENCRYPTED PASSWORD 'your_password';
        GRANT ALL PRIVILEGES ON DATABASE xai_db TO your_username;
        ```
    *   Navigate to the `backend/` module.
    *   Open `src/main/resources/application.properties`.
    *   Update the datasource and JWT properties with your local configuration:
        ```properties
        # Database
        spring.datasource.url=jdbc:postgresql://localhost:5432/xai_db
        spring.datasource.username=your_username
        spring.datasource.password=your_password
        spring.jpa.hibernate.ddl-auto=update

        # File Upload Paths (create these directories)
        app.file.upload-dir=./uploads/datasets
        app.model.storage-dir=./uploads/models

        # JWT Secret
        app.jwt.secret=your_super_secret_key_with_at_least_256_bits_for_testing
        app.jwt.expiration-ms=86400000
        ```

3.  **Create Upload Directories:**
    *   From the `project_root/` directory, create the folders for uploads:
        ```bash
        mkdir -p uploads/datasets
        mkdir -p uploads/models
        ```

4.  **Build and Run the Backend:**
    *   Navigate to the `backend/` directory.
    *   Use Maven to build and run the application:
        ```bash
        cd backend
        mvn spring-boot:run
        ```
    *   The backend server should now be running on `http://localhost:8080`.

### 2. Frontend Setup (React)

1.  **Navigate to the Frontend Module:**
    *   Open a new terminal window and navigate to the `frontend/` directory.
        ```bash
        cd frontend
        ```

2.  **Install Dependencies:**
    *   Use `npm` to install all the required packages defined in `package.json`.
        ```bash
        npm install
        ```

3.  **Run the Development Server:**
    *   Start the React development server.
        ```bash
        npm start
        ```
    *   The frontend application should now be running and accessible at `http://localhost:3000`. It will automatically proxy API requests to the backend server.

You are now ready to use the XAI-Forge application!
