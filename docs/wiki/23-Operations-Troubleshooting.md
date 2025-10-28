# Troubleshooting Guide

> 📘 **Source**: This wiki page contains complete information from [docs/operations/TROUBLESHOOTING.md](https://github.com/Mukaan17/xai-forge/blob/main/docs/operations/TROUBLESHOOTING.md)

**Navigation**: [[Home]] > [[Operations & Deployment]] > Troubleshooting

## Table of Contents

1. [Common Issues and Solutions](#common-issues-and-solutions)
2. [Performance Issues](#performance-issues)
3. [Debugging Techniques](#debugging-techniques)
4. [Monitoring and Alerting](#monitoring-and-alerting)
5. [Recovery Procedures](#recovery-procedures)
6. [Prevention Strategies](#prevention-strategies)

---

## Common Issues and Solutions

### 1. Application Startup Issues

#### Issue: Application fails to start
**Symptoms:**
- Spring Boot application doesn't start
- Port already in use errors
- Database connection errors

**Solutions:**
```bash
# Check if port is in use
netstat -tulpn | grep :8080

# Kill process using port
sudo lsof -ti:8080 | xargs kill -9

# Check database connection
psql -U xai_user -d xai_db -c "SELECT 1;"

# Verify Java version
java -version
```

#### Issue: Package import errors
**Symptoms:**
- Compilation errors with imports
- "Cannot find symbol" errors

**Solutions:**
- Check package declarations in Java files
- Ensure imports use correct package names
- Clean and rebuild project: `mvn clean compile`

### 2. Database Issues

#### Issue: Database connection failed
**Symptoms:**
- "Connection refused" errors
- "Authentication failed" errors

**Solutions:**
```bash
# Check PostgreSQL status
sudo systemctl status postgresql

# Start PostgreSQL
sudo systemctl start postgresql

# Verify database exists
psql -U postgres -l | grep xai_db

# Check user permissions
psql -U postgres -c "\du xai_user"
```

#### Issue: Database schema not created
**Symptoms:**
- Tables not found errors
- JPA entity mapping errors

**Solutions:**
- Check `spring.jpa.hibernate.ddl-auto` setting
- Verify database connection in `application.properties`
- Check entity annotations and relationships

### 3. File Upload Issues

#### Issue: File upload fails
**Symptoms:**
- "File too large" errors
- "Invalid file type" errors
- Upload directory not found

**Solutions:**
```bash
# Check file size limits in application.properties
spring.servlet.multipart.max-file-size=10MB

# Verify upload directory exists
ls -la uploads/

# Create upload directories
mkdir -p uploads/datasets uploads/models

# Check file permissions
chmod 755 uploads/
```

#### Issue: CSV parsing errors
**Symptoms:**
- "Malformed CSV" errors
- "Invalid header" errors

**Solutions:**
- Check CSV file format and encoding
- Verify headers match expected format
- Check for special characters in data
- Ensure proper line endings (LF vs CRLF)

### 4. ML Model Issues

#### Issue: Model training fails
**Symptoms:**
- "Insufficient data" errors
- "Training timeout" errors
- Tribuo library errors

**Solutions:**
- Check dataset size and quality
- Verify feature names match dataset headers
- Check target variable has multiple classes (classification)
- Ensure sufficient memory for training

#### Issue: Prediction errors
**Symptoms:**
- "Model not found" errors
- "Feature mismatch" errors
- Serialization errors

**Solutions:**
- Verify model file exists and is readable
- Check feature names in prediction input
- Ensure model type matches prediction request
- Check model file permissions

### 5. Authentication Issues

#### Issue: JWT token errors
**Symptoms:**
- "Invalid token" errors
- "Token expired" errors
- Authentication failures

**Solutions:**
- Check JWT secret configuration
- Verify token expiration settings
- Check token format and signature
- Ensure proper token transmission

#### Issue: User registration fails
**Symptoms:**
- "Username already exists" errors
- "Email already in use" errors
- Password validation errors

**Solutions:**
- Check database constraints
- Verify email format validation
- Check password complexity requirements
- Ensure proper transaction handling

### 6. Frontend Issues

#### Issue: API calls fail
**Symptoms:**
- CORS errors
- 404 Not Found errors
- Network timeout errors

**Solutions:**
- Check CORS configuration in backend
- Verify API endpoint URLs
- Check network connectivity
- Ensure backend is running

#### Issue: Component rendering errors
**Symptoms:**
- Blank pages
- JavaScript errors
- Styling issues

**Solutions:**
- Check browser console for errors
- Verify React component imports
- Check Material-UI theme configuration
- Ensure proper state management

---

## Performance Issues

### 1. Slow Response Times

#### Database Performance
```sql
-- Check slow queries
SELECT query, mean_time, calls 
FROM pg_stat_statements 
ORDER BY mean_time DESC;

-- Analyze table statistics
ANALYZE;

-- Check index usage
SELECT schemaname, tablename, indexname, idx_scan 
FROM pg_stat_user_indexes;
```

#### Application Performance
- Check JVM memory settings
- Monitor garbage collection
- Analyze thread dumps
- Check database connection pool

### 2. Memory Issues

#### Out of Memory Errors
**Solutions:**
```bash
# Increase JVM heap size
java -Xmx2g -Xms1g -jar app.jar

# Check memory usage
jstat -gc <pid>

# Analyze heap dump
jmap -dump:format=b,file=heap.hprof <pid>
```

#### Memory Leaks
- Check for unclosed resources
- Monitor object retention
- Analyze heap dumps
- Review caching strategies

### 3. File System Issues

#### Disk Space
```bash
# Check disk usage
df -h

# Find large files
find uploads/ -type f -size +100M

# Clean up old files
find uploads/ -type f -mtime +30 -delete
```

#### File Permissions
```bash
# Check permissions
ls -la uploads/

# Fix permissions
chmod -R 755 uploads/
chown -R app:app uploads/
```

---

## Debugging Techniques

### 1. Log Analysis
```bash
# View application logs
tail -f logs/application.log

# Search for errors
grep -i error logs/application.log

# Filter by time
grep "2025-01-04" logs/application.log
```

### 2. Database Debugging
```sql
-- Enable query logging
SET log_statement = 'all';
SET log_duration = on;

-- Check active connections
SELECT * FROM pg_stat_activity;

-- Monitor locks
SELECT * FROM pg_locks;
```

### 3. Network Debugging
```bash
# Check port connectivity
telnet localhost 8080

# Monitor network traffic
netstat -tulpn | grep :8080

# Check firewall rules
sudo ufw status
```

---

## Monitoring and Alerting

### 1. Health Checks
- Application health: `GET /actuator/health`
- Database health: Check connection pool
- File system health: Check disk space

### 2. Metrics to Monitor
- Response times
- Error rates
- Memory usage
- Database connections
- File system usage

### 3. Alert Thresholds
- Response time > 5 seconds
- Error rate > 5%
- Memory usage > 80%
- Disk space > 90%

---

## Recovery Procedures

### 1. Database Recovery
```bash
# Restore from backup
psql -U xai_user -d xai_db < backup.sql

# Recreate database
dropdb -U postgres xai_db
createdb -U postgres xai_db
psql -U postgres -f setup-database.sql
```

### 2. File System Recovery
```bash
# Restore uploads from backup
tar -xzf uploads_backup.tar.gz

# Recreate directories
mkdir -p uploads/datasets uploads/models
```

### 3. Application Recovery
```bash
# Restart application
sudo systemctl restart xai-app

# Check logs
journalctl -u xai-app -f

# Verify health
curl http://localhost:8080/actuator/health
```

---

## Prevention Strategies

### 1. Regular Maintenance
- Database backups
- Log rotation
- File cleanup
- Security updates

### 2. Monitoring Setup
- Health check monitoring
- Performance metrics
- Error rate tracking
- Resource utilization

### 3. Documentation
- Keep troubleshooting guide updated
- Document known issues
- Maintain runbooks
- Share knowledge with team

---

**Next**: [[Monitoring]] | **Previous**: [[Deployment]]  
**Related**: [[Deployment]], [[Monitoring]], [[FAQ]]
