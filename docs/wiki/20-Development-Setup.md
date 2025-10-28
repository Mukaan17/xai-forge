# Development Setup

> 📘 **Source**: This wiki page contains detailed development setup instructions from [CONTRIBUTING.md](https://github.com/Mukaan17/xai-forge/blob/main/CONTRIBUTING.md)

**Navigation**: [[Home]] > [[Contributing & Development]] > Development Setup

## Table of Contents

1. [Prerequisites](#prerequisites)
2. [Environment Setup](#environment-setup)
3. [IDE Configuration](#ide-configuration)
4. [Database Setup](#database-setup)
5. [Testing Setup](#testing-setup)
6. [Development Workflow](#development-workflow)
7. [Troubleshooting](#troubleshooting)

---

## Prerequisites

### Required Software

- **Java JDK 17+**: Required for backend development
- **Node.js 18+**: Required for frontend development
- **PostgreSQL 14+**: Required for database
- **Maven 3.8+**: Required for backend build
- **Git**: Required for version control

### Installation Verification

```bash
# Check Java version
java -version
# Should show: openjdk version "17.x.x"

# Check Maven version
mvn -version
# Should show: Apache Maven 3.8.x

# Check Node.js version
node -v
# Should show: v18.x.x

# Check npm version
npm -v
# Should show: 9.x.x

# Check PostgreSQL version
psql --version
# Should show: psql (PostgreSQL) 14.x
```

---

## Environment Setup

### 1. Clone the Repository

```bash
# Clone the main repository
git clone https://github.com/Mukaan17/xai-forge.git
cd xai-forge

# Add upstream remote
git remote add upstream https://github.com/Mukaan17/xai-forge.git
```

### 2. Backend Setup

```bash
# Navigate to backend directory
cd backend

# Install dependencies
mvn clean install

# Verify backend setup
mvn spring-boot:run
# Should start on http://localhost:8080
```

### 3. Frontend Setup

```bash
# Navigate to frontend directory
cd frontend

# Install dependencies
npm install

# Start development server
npm start
# Should start on http://localhost:3000
```

### 4. Environment Variables

Create a `.env` file in the project root:

```bash
# JWT Configuration
JWT_SECRET=your-super-secure-secret-key-here-at-least-32-characters-long

# Database Configuration
DB_URL=jdbc:postgresql://localhost:5432/xai_db
DB_USERNAME=xai_user
DB_PASSWORD=your-secure-password

# Frontend Configuration
REACT_APP_API_URL=http://localhost:8080/api
REACT_APP_DEBUG=true

# File Upload Configuration
UPLOAD_DIR=./uploads
```

### 5. Generate Secure JWT Secret

```bash
# Generate a secure JWT secret
export JWT_SECRET=$(openssl rand -base64 64)

# Verify length (should be 88 characters)
echo $JWT_SECRET | wc -c
```

---

## IDE Configuration

### IntelliJ IDEA

#### 1. Project Setup
- Open the project root directory
- Import as Maven project
- Configure Java SDK to version 17+
- Enable annotation processing

#### 2. Plugins
Install the following plugins:
- **Lombok**: For annotation processing
- **Spring Boot**: For Spring Boot support
- **Database Navigator**: For database management
- **Git Integration**: For version control

#### 3. Code Style
- Go to `File > Settings > Editor > Code Style`
- Import the project's code style settings
- Set line length to 120 characters
- Use 4 spaces for indentation

#### 4. Run Configurations
Create run configurations for:
- **Backend**: Spring Boot application
- **Frontend**: npm start
- **Tests**: Maven test execution

### Eclipse

#### 1. Project Setup
- Import as existing Maven project
- Configure Java build path
- Enable annotation processing
- Set project facets

#### 2. Plugins
Install the following plugins:
- **Lombok**: For annotation processing
- **Spring Tools**: For Spring Boot support
- **Maven Integration**: For Maven support

#### 3. Code Formatting
- Go to `Window > Preferences > Java > Code Style > Formatter`
- Import the project's formatting rules
- Set line length to 120 characters
- Use 4 spaces for indentation

### VS Code

#### 1. Extensions
Install the following extensions:
- **Extension Pack for Java**: Java development support
- **Spring Boot Extension Pack**: Spring Boot support
- **Lombok Annotations Support**: Lombok support
- **Database Client**: Database management

#### 2. Settings
Create `.vscode/settings.json`:

```json
{
    "java.configuration.updateBuildConfiguration": "automatic",
    "java.compile.nullAnalysis.mode": "automatic",
    "java.format.settings.url": ".vscode/java-formatter.xml",
    "editor.formatOnSave": true,
    "editor.codeActionsOnSave": {
        "source.organizeImports": true
    }
}
```

#### 3. Launch Configuration
Create `.vscode/launch.json`:

```json
{
    "version": "0.2.0",
    "configurations": [
        {
            "type": "java",
            "name": "Launch XaiApplication",
            "request": "launch",
            "mainClass": "com.example.xaiapp.XaiApplication",
            "projectName": "xai-app"
        }
    ]
}
```

---

## Database Setup

### 1. PostgreSQL Installation

#### Ubuntu/Debian
```bash
sudo apt update
sudo apt install postgresql postgresql-contrib
sudo systemctl start postgresql
sudo systemctl enable postgresql
```

#### macOS (with Homebrew)
```bash
brew install postgresql
brew services start postgresql
```

#### Windows
Download from [postgresql.org](https://www.postgresql.org/download/windows/)

### 2. Database Configuration

```bash
# Connect to PostgreSQL
sudo -u postgres psql

# Create database and user
CREATE DATABASE xai_db;
CREATE USER xai_user WITH ENCRYPTED PASSWORD 'your-secure-password';
GRANT ALL PRIVILEGES ON DATABASE xai_db TO xai_user;

# Exit psql
\q
```

### 3. Test Database Connection

```bash
# Test connection
psql -h localhost -U xai_user -d xai_db -c "SELECT version();"

# Should return PostgreSQL version information
```

### 4. Database Schema

The application will automatically create the database schema on first startup. You can also run the setup script:

```bash
# Run setup script
psql -U postgres -f setup-database.sql
```

---

## Testing Setup

### 1. Backend Testing

#### Unit Tests
```bash
# Run all unit tests
mvn test

# Run specific test class
mvn test -Dtest=ModelServiceTest

# Run tests with coverage
mvn test jacoco:report
```

#### Integration Tests
```bash
# Run integration tests
mvn verify -Pintegration-test

# Run with testcontainers
mvn test -Dtest=*IntegrationTest
```

### 2. Frontend Testing

#### Unit Tests
```bash
# Run all tests
npm test

# Run with coverage
npm test -- --coverage

# Run specific test file
npm test -- --testPathPattern=DatasetUpload.test.js
```

#### Integration Tests
```bash
# Run integration tests
npm run test:integration

# Run with coverage
npm run test:integration -- --coverage
```

### 3. End-to-End Testing

```bash
# Start backend
cd backend
mvn spring-boot:run &

# Start frontend
cd frontend
npm start &

# Run E2E tests
npm run test:e2e
```

---

## Development Workflow

### 1. Branch Strategy

```bash
# Create feature branch
git checkout -b feature/new-feature

# Create bugfix branch
git checkout -b bugfix/fix-issue

# Create hotfix branch
git checkout -b hotfix/critical-fix
```

### 2. Development Process

```bash
# Make changes
# ... edit files ...

# Stage changes
git add .

# Commit changes
git commit -m "feat: add new feature"

# Push to remote
git push origin feature/new-feature
```

### 3. Testing Process

```bash
# Run tests before committing
mvn test
npm test

# Check code coverage
mvn test jacoco:report

# Run integration tests
mvn verify -Pintegration-test
```

### 4. Code Quality

```bash
# Check code style
mvn checkstyle:check

# Run static analysis
mvn spotbugs:check

# Format code
mvn spring-javaformat:apply
```

---

## Troubleshooting

### Common Issues

#### 1. Java Version Issues
```bash
# Check Java version
java -version

# Set JAVA_HOME if needed
export JAVA_HOME=/path/to/java17
```

#### 2. Maven Issues
```bash
# Clean Maven cache
mvn clean

# Update dependencies
mvn dependency:resolve

# Check Maven configuration
mvn help:effective-settings
```

#### 3. Node.js Issues
```bash
# Clear npm cache
npm cache clean --force

# Delete node_modules and reinstall
rm -rf node_modules package-lock.json
npm install
```

#### 4. Database Issues
```bash
# Check PostgreSQL status
sudo systemctl status postgresql

# Start PostgreSQL if stopped
sudo systemctl start postgresql

# Check database connection
psql -h localhost -U xai_user -d xai_db -c "SELECT 1;"
```

#### 5. Port Conflicts
```bash
# Check what's using port 8080
lsof -i :8080

# Check what's using port 3000
lsof -i :3000

# Kill process if needed
kill -9 <PID>
```

### Debugging

#### 1. Backend Debugging
```bash
# Run with debug logging
mvn spring-boot:run -Dlogging.level.com.example.xaiapp=DEBUG

# Run with JVM debug options
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005"
```

#### 2. Frontend Debugging
```bash
# Run with debug logging
REACT_APP_DEBUG=true npm start

# Run with verbose output
npm start -- --verbose
```

#### 3. Database Debugging
```bash
# Enable SQL logging
# Add to application.properties:
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE
```

---

## Performance Optimization

### 1. JVM Tuning

```bash
# Set JVM options for development
export MAVEN_OPTS="-Xmx2g -Xms1g -XX:+UseG1GC"

# Run with optimized JVM settings
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Xmx2g -Xms1g -XX:+UseG1GC"
```

### 2. Database Optimization

```sql
-- Check database performance
EXPLAIN ANALYZE SELECT * FROM users WHERE username = 'testuser';

-- Check index usage
SELECT * FROM pg_stat_user_indexes;
```

### 3. Frontend Optimization

```bash
# Build optimized frontend
npm run build

# Analyze bundle size
npm run analyze

# Run with production optimizations
NODE_ENV=production npm start
```

---

## Additional Resources

### Documentation
- [[Coding Standards|Coding-Standards]] - Coding guidelines
- [[Testing Guide|Testing-Guide]] - Testing requirements
- [[API Reference|API-Reference]] - API documentation
- [[Architecture]] - System architecture

### Tools
- [Maven Documentation](https://maven.apache.org/guides/)
- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [React Documentation](https://reactjs.org/docs/)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)

### Community
- [GitHub Issues](https://github.com/Mukaan17/xai-forge/issues)
- [GitHub Discussions](https://github.com/Mukaan17/xai-forge/discussions)
- [Stack Overflow](https://stackoverflow.com/questions/tagged/xai-forge)

---

**Next**: [[Coding Standards|Coding-Standards]] | **Previous**: [[Contributing]]  
**Related**: [[Contributing]], [[Coding Standards|Coding-Standards]], [[Testing Guide|Testing-Guide]]
