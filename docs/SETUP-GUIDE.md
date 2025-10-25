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
    *   Or use the provided setup script:
        ```bash
        psql -U postgres -f setup-database.sql
        ```

3.  **Set Environment Variables:**
    *   **Required for security:**
        ```bash
        # Generate a secure JWT secret (minimum 32 characters)
        export JWT_SECRET=$(openssl rand -base64 64)
        
        # Set database password (change from default)
        export DB_PASSWORD=your-secure-password
        ```
    *   **Optional:**
        ```bash
        # Database configuration (defaults provided)
        export DB_URL=jdbc:postgresql://localhost:5432/xai_db
        export DB_USERNAME=xai_user
        
        # Frontend API URL
        export REACT_APP_API_URL=http://localhost:8080/api
        ```
    
    **Note**: The application includes ConfigurationValidator that will validate these parameters on startup and provide clear error messages if they are missing or invalid.

4.  **Create Upload Directories:**
    *   From the `project_root/` directory, create the folders for uploads:
        ```bash
        mkdir -p uploads/datasets
        mkdir -p uploads/models
        ```

5.  **Build and Run the Backend:**
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

### 3. Security Configuration

**Important**: The application now requires environment variables for security. See [ENVIRONMENT_VARIABLES.md](ENVIRONMENT_VARIABLES.md) for complete documentation.

#### Required Security Steps:

1. **Generate JWT Secret:**
   ```bash
   # Generate a secure 256-bit secret
   export JWT_SECRET=$(openssl rand -base64 64)
   ```

2. **Set Database Password:**
   ```bash
   # Change from default password
   export DB_PASSWORD=your-secure-password
   ```

3. **Verify Configuration:**
   The application will validate configuration on startup and provide clear error messages if required variables are missing.

## Configuration Validation

The application includes a ConfigurationValidator that performs the following checks on startup:

### Startup Validation Checks
- **JWT Secret**: Validates JWT_SECRET is set and at least 32 characters
- **Database Connection**: Tests database connectivity with provided credentials
- **Upload Directory**: Verifies upload directory exists and is writable
- **ML Parameters**: Validates ML training parameters are within acceptable ranges

### Common Configuration Errors

#### JWT Secret Issues
```
Error: JWT_SECRET must be at least 32 characters
Solution: Generate a new secret with: openssl rand -base64 64
```

#### Database Connection Issues
```
Error: Database connection failed
Solution: Verify DB_URL, DB_USERNAME, DB_PASSWORD are correct
```

#### Upload Directory Issues
```
Error: Upload directory is not writable
Solution: Check directory permissions or set UPLOAD_DIR environment variable
```

### Troubleshooting

If the application fails to start due to configuration issues:

1. **Check Environment Variables**:
   ```bash
   echo $JWT_SECRET
   echo $DB_PASSWORD
   ```

2. **Test Database Connection**:
   ```bash
   psql $DB_URL -U $DB_USERNAME -c "SELECT 1;"
   ```

3. **Verify Upload Directory**:
   ```bash
   ls -la uploads/
   chmod 755 uploads/
   ```

You are now ready to use the XAI-Forge application!
