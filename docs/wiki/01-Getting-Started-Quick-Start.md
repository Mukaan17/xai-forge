# Quick Start Guide

> 📘 **Source**: This wiki page contains complete information from [README.md](https://github.com/Mukaan17/xai-forge/blob/main/README.md)

**Navigation**: [[Home]] > Getting Started > Quick Start

## Table of Contents

1. [Prerequisites](#prerequisites)
2. [Installation Steps](#installation-steps)
3. [First-Time Configuration](#first-time-configuration)
4. [Verification](#verification)
5. [Common Startup Issues](#common-startup-issues)
6. [Next Steps](#next-steps)

---

## Prerequisites

Before starting, ensure you have the following installed:

- **Java 17+** - Backend runtime
- **Node.js 18+** - Frontend development
- **PostgreSQL 14+** - Database
- **Maven 3.8+** - Backend build tool

### Quick Check

```bash
# Verify installations
java -version    # Should show Java 17+
node -v         # Should show Node.js 18+
psql --version  # Should show PostgreSQL 14+
mvn -version    # Should show Maven 3.8+
```

---

## Installation Steps

### 1. Clone the Repository

```bash
git clone https://github.com/Mukaan17/xai-forge.git
cd xai-forge
```

### 2. Set Up the Database

```bash
# Create database and user
psql -U postgres -f setup-database.sql
```

### 3. Configure the Backend

```bash
# Set required environment variables
export JWT_SECRET=$(openssl rand -base64 64)
export DB_PASSWORD=your-secure-password

# Create upload directories
mkdir -p uploads/datasets uploads/models
```

### 4. Start the Backend

```bash
cd backend
mvn spring-boot:run
```

The backend will be available at `http://localhost:8080`

### 5. Start the Frontend

```bash
cd frontend
npm install
# Optional: Set API URL for different environments
export REACT_APP_API_URL=http://localhost:8080/api
npm start
```

The frontend will be available at `http://localhost:3000`

### 6. Access the Application

- **Frontend**: http://localhost:3000
- **Backend API**: http://localhost:8080

---

## First-Time Configuration

### Required Environment Variables

- `JWT_SECRET` - JWT signing secret (generate with `openssl rand -base64 64`)
- `DB_PASSWORD` - Database password (change from default)

### Optional Environment Variables

- `REACT_APP_API_URL` - Frontend API URL (default: `http://localhost:8080/api`)
- `DB_URL` - Database URL (default: `jdbc:postgresql://localhost:5432/xai_db`)
- `DB_USERNAME` - Database username (default: `xai_user`)

### Configuration Validation

The application includes a ConfigurationValidator that performs startup checks:
- JWT secret validation (minimum 32 characters)
- Database connectivity test
- Upload directory verification
- ML parameter validation

---

## Verification

### Backend Health Check

```bash
curl http://localhost:8080/api/health
# Should return: {"status":"UP"}
```

### Frontend Access

1. Open http://localhost:3000
2. You should see the XAI-Forge homepage
3. Click "Register" to create an account

### Database Connection

```bash
psql -U xai_user -d xai_db -c "SELECT 1;"
# Should return: 1
```

---

## Common Startup Issues

### Issue: "JWT_SECRET must be at least 32 characters"

**Solution**:
```bash
export JWT_SECRET=$(openssl rand -base64 64)
```

### Issue: "Database connection failed"

**Solution**:
1. Verify PostgreSQL is running: `sudo systemctl status postgresql`
2. Check database exists: `psql -U postgres -l | grep xai_db`
3. Verify credentials in environment variables

### Issue: "Upload directory is not writable"

**Solution**:
```bash
mkdir -p uploads/datasets uploads/models
chmod 755 uploads/
```

### Issue: Frontend can't connect to backend

**Solution**:
1. Verify backend is running on port 8080
2. Check CORS configuration
3. Verify `REACT_APP_API_URL` environment variable

### Issue: Maven build fails

**Solution**:
```bash
# Clean and rebuild
mvn clean install
# Or skip tests if needed
mvn clean install -DskipTests
```

---

## Next Steps

Once you have the application running:

1. **[[User Guide]]** - Complete walkthrough of features
2. **[[02-Getting-Started-Installation]]** - Detailed setup instructions
3. **[[03-Getting-Started-Configuration]]** - Environment and security configuration
4. **[[API Reference]]** - REST API documentation

---

**Next**: [[02-Getting-Started-Installation]] ]]  
**Related**: [[03-Getting-Started-Configuration]], [[23-Operations-Troubleshooting]], [[05-User-FAQ]]
