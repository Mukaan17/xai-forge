# Configuration Guide

> 📘 **Source**: This wiki page contains complete information from [docs/ENVIRONMENT_VARIABLES.md](https://github.com/Mukaan17/xai-forge/blob/main/docs/ENVIRONMENT_VARIABLES.md)

**Navigation**: [[Home]] > [[Getting Started]] > Configuration

## Table of Contents

1. [Required Environment Variables](#required-environment-variables)
2. [Optional Environment Variables](#optional-environment-variables)
3. [Environment-Specific Examples](#environment-specific-examples)
4. [Security Best Practices](#security-best-practices)
5. [Configuration Validation](#configuration-validation)
6. [Troubleshooting](#troubleshooting)

---

## Required Environment Variables

### Security Configuration

#### JWT_SECRET
- **Description**: Secret key for JWT token signing and validation
- **Required**: Yes (critical for security)
- **Minimum Length**: 32 characters (256 bits)
- **Generate**: `openssl rand -base64 64`
- **Example**: `JWT_SECRET=your-super-secure-secret-key-here-at-least-32-characters-long`

```bash
# Generate a secure JWT secret
export JWT_SECRET=$(openssl rand -base64 64)
```

### Database Configuration

#### DB_URL
- **Description**: Database connection URL
- **Required**: Yes (for production)
- **Default**: `jdbc:postgresql://localhost:5432/xai_db`
- **Example**: `DB_URL=jdbc:postgresql://localhost:5432/xai_prod`

#### DB_USERNAME
- **Description**: Database username
- **Required**: Yes (for production)
- **Default**: `xai_user`
- **Example**: `DB_USERNAME=xai_prod_user`

#### DB_PASSWORD
- **Description**: Database password
- **Required**: Yes (for production)
- **Default**: `changeme` (must be changed!)
- **Example**: `DB_PASSWORD=your-secure-database-password`

```bash
# Set database configuration
export DB_URL=jdbc:postgresql://localhost:5432/xai_db
export DB_USERNAME=xai_user
export DB_PASSWORD=your-secure-password
```

---

## Optional Environment Variables

### Frontend Configuration

#### REACT_APP_API_URL
- **Description**: Backend API URL for frontend
- **Required**: No
- **Default**: `http://localhost:8080/api`
- **Example**: `REACT_APP_API_URL=https://api.xai-forge.com/api`

#### REACT_APP_DEBUG
- **Description**: Enable debug logging in frontend
- **Required**: No
- **Default**: `false`
- **Example**: `REACT_APP_DEBUG=true`

### File Storage Configuration

#### UPLOAD_DIR
- **Description**: Directory for file uploads
- **Required**: No
- **Default**: `./uploads`
- **Example**: `UPLOAD_DIR=/var/xai/uploads`

### Server Configuration

#### SERVER_PORT
- **Description**: Server port
- **Required**: No
- **Default**: `8080`
- **Example**: `SERVER_PORT=8080`

#### CORS_ORIGINS
- **Description**: Allowed CORS origins (comma-separated)
- **Required**: No
- **Default**: `http://localhost:3000`
- **Example**: `CORS_ORIGINS=https://xai-forge.com,https://www.xai-forge.com`

### Email Configuration (Optional)

#### MAIL_HOST
- **Description**: SMTP server host
- **Required**: No
- **Default**: `smtp.gmail.com`
- **Example**: `MAIL_HOST=smtp.gmail.com`

#### MAIL_PORT
- **Description**: SMTP server port
- **Required**: No
- **Default**: `587`
- **Example**: `MAIL_PORT=587`

#### MAIL_USERNAME
- **Description**: SMTP username
- **Required**: No
- **Example**: `MAIL_USERNAME=your-email@gmail.com`

#### MAIL_PASSWORD
- **Description**: SMTP password
- **Required**: No
- **Example**: `MAIL_PASSWORD=your-app-password`

---

## Environment-Specific Examples

### Development Environment

```bash
# .env.development
JWT_SECRET=dev-secret-key-change-in-production
DB_URL=jdbc:postgresql://localhost:5432/xai_dev
DB_USERNAME=xai_user
DB_PASSWORD=dev_password
REACT_APP_API_URL=http://localhost:8080/api
REACT_APP_DEBUG=true
```

### Production Environment

```bash
# .env.production
JWT_SECRET=your-production-secret-key-generated-with-openssl-rand-base64-64
DB_URL=jdbc:postgresql://prod-db-host:5432/xai_prod
DB_USERNAME=xai_prod_user
DB_PASSWORD=your-secure-production-password
REACT_APP_API_URL=https://api.xai-forge.com/api
CORS_ORIGINS=https://xai-forge.com,https://www.xai-forge.com
UPLOAD_DIR=/var/xai/uploads
```

### Testing Environment

```bash
# .env.test
JWT_SECRET=test-secret-key-for-testing-only
DB_URL=jdbc:postgresql://localhost:5432/xai_test
DB_USERNAME=xai_test_user
DB_PASSWORD=test_password
REACT_APP_API_URL=http://localhost:8080/api
```

---

## Security Best Practices

### 1. Secret Generation
- Use `openssl rand -base64 64` to generate secure secrets
- Never use default or weak secrets in production
- Rotate secrets regularly

### 2. Database Security
- Use strong, unique passwords
- Limit database user permissions
- Use SSL connections in production

### 3. Environment Variable Management
- Never commit `.env` files to version control
- Use environment variable injection in production
- Consider using secret management services (AWS Secrets Manager, HashiCorp Vault)

### 4. CORS Configuration
- Only allow necessary origins in production
- Avoid using wildcards (`*`) in production
- Use HTTPS in production

---

## Configuration Validation

The application validates critical configuration on startup:

- JWT secret length and presence
- Database connectivity
- File upload directory accessibility
- ML training parameter ranges

If validation fails, the application will not start and will provide clear error messages.

### Validation Process

The ConfigurationValidator component performs these checks:

```java
@PostConstruct
public void validateConfiguration() {
    validateJwtSecret();
    validateUploadDirectory();
    validateDatabaseConnection();
    log.info("Configuration validation completed successfully");
}
```

---

## Troubleshooting

### Common Issues

#### 1. JWT_SECRET not set
- **Error**: "JWT_SECRET environment variable is required"
- **Solution**: Set JWT_SECRET environment variable
```bash
export JWT_SECRET=$(openssl rand -base64 64)
```

#### 2. Database connection failed
- **Error**: Database connection errors
- **Solution**: Verify DB_URL, DB_USERNAME, DB_PASSWORD are correct
```bash
psql $DB_URL -U $DB_USERNAME -c "SELECT 1;"
```

#### 3. Upload directory not writable
- **Error**: "Upload directory is not writable"
- **Solution**: Check directory permissions or set UPLOAD_DIR
```bash
mkdir -p uploads/datasets uploads/models
chmod 755 uploads/
```

#### 4. CORS errors in frontend
- **Error**: CORS policy errors
- **Solution**: Verify CORS_ORIGINS includes your frontend URL
```bash
export CORS_ORIGINS=http://localhost:3000,https://your-domain.com
```

### Validation Commands

```bash
# Check if JWT secret is set
echo $JWT_SECRET

# Test database connection
psql $DB_URL -U $DB_USERNAME -c "SELECT 1;"

# Check upload directory
ls -la $UPLOAD_DIR

# Verify environment variables
env | grep -E "(JWT_SECRET|DB_|REACT_APP_)"
```

### Configuration Testing

```bash
# Test backend configuration
curl http://localhost:8080/api/health

# Test frontend configuration
curl http://localhost:3000

# Test database connectivity
psql -h localhost -U xai_user -d xai_db -c "SELECT version();"
```

---

## Production Configuration Checklist

- [ ] JWT_SECRET generated with `openssl rand -base64 64`
- [ ] Database password changed from default
- [ ] CORS_ORIGINS configured for production domains
- [ ] UPLOAD_DIR set to secure location
- [ ] SSL/TLS configured for HTTPS
- [ ] Environment variables injected securely
- [ ] Database SSL enabled
- [ ] File permissions set correctly
- [ ] Logging configured appropriately
- [ ] Monitoring and alerting configured

---

**Next**: [[User Guide|User-Guide]] | **Previous**: [[Installation]]  
**Related**: [[Troubleshooting]], [[Deployment]], [[Security]]
