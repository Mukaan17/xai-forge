# Deployment Guide

This document provides instructions for deploying the XAI-Forge application in different environments.

## Prerequisites

### System Requirements
- Java 17 or higher
- Node.js 18 or higher
- PostgreSQL 14 or higher
- Maven 3.8 or higher
- Minimum 4GB RAM
- Minimum 10GB disk space

### Environment Setup
- Database server configured
- File system permissions set
- Network access configured
- SSL certificates (for production)

## Development Deployment

### 1. Database Setup
```bash
# Create database and user
psql -U postgres -f setup-database.sql

# Verify database connection
psql -U xai_user -d xai_db -c "SELECT version();"
```

### 2. Backend Deployment
```bash
cd backend

# Install dependencies
mvn clean install

# Run application
mvn spring-boot:run
```

### 3. Frontend Deployment
```bash
cd frontend

# Install dependencies
npm install

# Start development server
npm start
```

## Production Deployment

### 1. Environment Configuration
Create production configuration files:
- `config/application-prod.properties`
- Environment variables for sensitive data
- SSL certificates for HTTPS

### 2. Database Configuration
```sql
-- Create production database
CREATE DATABASE xai_prod;
CREATE USER xai_prod_user WITH PASSWORD 'secure_password';
GRANT ALL PRIVILEGES ON DATABASE xai_prod TO xai_prod_user;
```

### 3. Backend Production Build
```bash
cd backend

# Build production JAR
mvn clean package -Pprod

# Run with production profile
java -jar -Dspring.profiles.active=prod target/backend-1.0.0.jar
```

### 4. Frontend Production Build
```bash
cd frontend

# Build production bundle
npm run build

# Serve static files (use nginx or similar)
```

## Docker Deployment

### 1. Create Dockerfile
```dockerfile
# Backend Dockerfile
FROM openjdk:17-jdk-slim
COPY target/backend-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

### 2. Docker Compose
```yaml
version: '3.8'
services:
  database:
    image: postgres:14
    environment:
      POSTGRES_DB: xai_db
      POSTGRES_USER: xai_user
      POSTGRES_PASSWORD: xai_password
    volumes:
      - postgres_data:/var/lib/postgresql/data

  backend:
    build: ./backend
    ports:
      - "8080:8080"
    depends_on:
      - database
    environment:
      SPRING_PROFILES_ACTIVE: prod

  frontend:
    build: ./frontend
    ports:
      - "3000:80"
    depends_on:
      - backend
```

## Monitoring and Logging

### Application Monitoring
- Health check endpoint: `/actuator/health`
- Metrics endpoint: `/actuator/metrics`
- Logs: Configure logback for structured logging

### Database Monitoring
- Connection pool monitoring
- Query performance monitoring
- Disk space monitoring

### File System Monitoring
- Upload directory size monitoring
- Disk space alerts
- File cleanup automation

## Security Considerations

### Production Security
- Use HTTPS in production
- Configure CORS properly
- Set up rate limiting
- Implement audit logging
- Regular security updates

### Database Security
- Use strong passwords
- Limit database access
- Enable SSL connections
- Regular backups

### File Security
- Secure file upload validation
- Virus scanning for uploads
- Access control for files
- Regular cleanup of temporary files

## Backup and Recovery

### Database Backup
```bash
# Create backup
pg_dump -U xai_user -d xai_db > backup_$(date +%Y%m%d_%H%M%S).sql

# Restore backup
psql -U xai_user -d xai_db < backup_file.sql
```

### File Backup
```bash
# Backup uploads directory
tar -czf uploads_backup_$(date +%Y%m%d_%H%M%S).tar.gz uploads/
```

## Troubleshooting

### Common Issues
1. **Database Connection Issues**
   - Check database server status
   - Verify connection parameters
   - Check network connectivity

2. **File Upload Issues**
   - Check disk space
   - Verify file permissions
   - Check file size limits

3. **Performance Issues**
   - Monitor database connections
   - Check memory usage
   - Review query performance

### Log Analysis
- Application logs: `logs/application.log`
- Database logs: Check PostgreSQL logs
- System logs: Check system resource usage

## Scaling Considerations

### Horizontal Scaling
- Load balancer configuration
- Session management
- Database connection pooling
- File storage considerations

### Vertical Scaling
- Memory optimization
- CPU optimization
- Database tuning
- Caching strategies
