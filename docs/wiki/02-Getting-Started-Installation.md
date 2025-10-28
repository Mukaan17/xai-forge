# Installation Guide

> 📘 **Source**: This wiki page contains complete information from [docs/SETUP-GUIDE.md](https://github.com/Mukaan17/xai-forge/blob/main/docs/SETUP-GUIDE.md)

**Navigation**: [[Home]] > Getting Started > Installation

## Table of Contents

1. [Prerequisites](#prerequisites)
2. [Backend Setup](#backend-setup)
3. [Frontend Setup](#frontend-setup)
4. [Security Configuration](#security-configuration)
5. [Configuration Validation](#configuration-validation)
6. [Troubleshooting](#troubleshooting)

---

## Prerequisites

### Required Software

- **Java JDK 17+**: Required for the backend
- **Apache Maven 3.8+**: For building the backend project
- **PostgreSQL 14+**: The primary database
- **Node.js 18+ & npm**: For the frontend React application
- **Git**: For version control

### Installation Verification

```bash
# Check Java version
java -version
# Should show: openjdk version "17.x.x"

# Check Maven version
mvn -version
# Should show: Apache Maven 3.8.x

# Check PostgreSQL version
psql --version
# Should show: psql (PostgreSQL) 14.x

# Check Node.js version
node -v
# Should show: v18.x.x

# Check npm version
npm -v
# Should show: 9.x.x
```

---

## Backend Setup (Spring Boot)

### 1. Clone the Repository

```bash
git clone https://github.com/Mukaan17/xai-forge.git
cd xai-forge
```

### 2. Configure the Database

#### Option A: Using the Setup Script

```bash
# Run the provided setup script
psql -U postgres -f setup-database.sql
```

#### Option B: Manual Database Setup

```sql
-- Connect to PostgreSQL as superuser
psql -U postgres

-- Create database
CREATE DATABASE xai_db;

-- Create user
CREATE USER xai_user WITH ENCRYPTED PASSWORD 'xai_password';

-- Grant privileges
GRANT ALL PRIVILEGES ON DATABASE xai_db TO xai_user;

-- Exit psql
\q
```

### 3. Set Environment Variables

#### Required Variables

```bash
# Generate a secure JWT secret (minimum 32 characters)
export JWT_SECRET=$(openssl rand -base64 64)

# Set database password (change from default)
export DB_PASSWORD=your-secure-password
```

#### Optional Variables

```bash
# Database configuration (defaults provided)
export DB_URL=jdbc:postgresql://localhost:5432/xai_db
export DB_USERNAME=xai_user

# Frontend API URL
export REACT_APP_API_URL=http://localhost:8080/api
```

### 4. Create Upload Directories

```bash
# From the project root directory
mkdir -p uploads/datasets
mkdir -p uploads/models

# Set appropriate permissions
chmod 755 uploads/
chmod 755 uploads/datasets/
chmod 755 uploads/models/
```

### 5. Build and Run the Backend

```bash
# Navigate to backend directory
cd backend

# Build the project
mvn clean install

# Run the application
mvn spring-boot:run
```

The backend server should now be running on `http://localhost:8080`

### 6. Verify Backend Installation

```bash
# Health check
curl http://localhost:8080/api/health

# Expected response
{"status":"UP"}
```

---

## Frontend Setup (React)

### 1. Navigate to Frontend Directory

```bash
# From project root
cd frontend
```

### 2. Install Dependencies

```bash
# Install all required packages
npm install
```

### 3. Configure Environment (Optional)

```bash
# Set API URL for different environments
export REACT_APP_API_URL=http://localhost:8080/api
```

### 4. Start Development Server

```bash
# Start the React development server
npm start
```

The frontend application should now be running and accessible at `http://localhost:3000`

### 5. Verify Frontend Installation

1. Open browser to http://localhost:3000
2. You should see the XAI-Forge homepage
3. The application should automatically proxy API requests to the backend

---

## Security Configuration

### Important Security Steps

#### 1. Generate JWT Secret

```bash
# Generate a secure 256-bit secret
export JWT_SECRET=$(openssl rand -base64 64)

# Verify length (should be 88 characters)
echo $JWT_SECRET | wc -c
```

#### 2. Set Database Password

```bash
# Change from default password
export DB_PASSWORD=your-secure-password

# Update database user password
psql -U postgres -c "ALTER USER xai_user PASSWORD 'your-secure-password';"
```

#### 3. Verify Configuration

The application will validate configuration on startup and provide clear error messages if required variables are missing.

---

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

---

## Troubleshooting

### Backend Issues

#### Application Won't Start

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

#### Maven Build Failures

```bash
# Clean and rebuild
mvn clean install

# Skip tests if needed
mvn clean install -DskipTests

# Check for dependency issues
mvn dependency:tree
```

#### Port Already in Use

```bash
# Check what's using port 8080
lsof -i :8080

# Kill the process if needed
kill -9 <PID>
```

### Frontend Issues

#### npm Install Failures

```bash
# Clear npm cache
npm cache clean --force

# Delete node_modules and reinstall
rm -rf node_modules package-lock.json
npm install
```

#### Frontend Can't Connect to Backend

1. Verify backend is running on port 8080
2. Check CORS configuration in backend
3. Verify `REACT_APP_API_URL` environment variable
4. Check browser console for errors

#### Build Failures

```bash
# Check for TypeScript errors
npm run build

# Run with verbose output
npm start -- --verbose
```

### Database Issues

#### Connection Refused

```bash
# Check PostgreSQL status
sudo systemctl status postgresql

# Start PostgreSQL if stopped
sudo systemctl start postgresql

# Check if database exists
psql -U postgres -l | grep xai_db
```

#### Permission Denied

```bash
# Check user permissions
psql -U postgres -c "\du xai_user"

# Grant necessary permissions
psql -U postgres -c "GRANT ALL PRIVILEGES ON DATABASE xai_db TO xai_user;"
```

---

## Next Steps

After successful installation:

1. **[[03-Getting-Started-Configuration]]** - Detailed configuration options
2. **[[User Guide]]** - Complete user walkthrough
3. **[[API Reference]]** - REST API documentation
4. **[[Development Setup]]** - Developer environment setup

---

**Next**: [[03-Getting-Started-Configuration]] ]]  
**Related**: [[23-Operations-Troubleshooting]], [[Development Setup]], [[05-User-FAQ]]
