# Technology Stack

> 📘 **Source**: This wiki page provides a comprehensive overview of the XAI-Forge technology stack

**Navigation**: [[Home]] > [[Reference & Resources]] > Technology Stack

## Table of Contents

1. [Backend Technologies](#backend-technologies)
2. [Frontend Technologies](#frontend-technologies)
3. [Database Technologies](#database-technologies)
4. [Build Tools](#build-tools)
5. [Development Tools](#development-tools)
6. [Deployment Technologies](#deployment-technologies)
7. [Version Information](#version-information)

---

## Backend Technologies

### Core Framework
- **Java 17+**: Modern Java with latest features
- **Spring Boot 3.2+**: Rapid application development framework
- **Spring Security**: Authentication and authorization
- **Spring Data JPA**: Data persistence layer
- **Spring Web**: REST API development

### Machine Learning
- **Tribuo 4.3.2**: Oracle's ML library for Java
- **Apache Commons CSV**: CSV file parsing
- **Apache POI 5.2.5**: Excel file processing

### Database & Persistence
- **PostgreSQL 14+**: Primary database
- **Hibernate**: ORM implementation
- **HikariCP**: Connection pooling

### Security
- **JWT (JSON Web Tokens)**: Stateless authentication
- **BCrypt**: Password hashing
- **Spring Security**: Security framework

### Testing
- **JUnit 5**: Unit testing framework
- **Mockito**: Mocking framework
- **Testcontainers**: Integration testing
- **JaCoCo**: Code coverage analysis

---

## Frontend Technologies

### Core Framework
- **React 18**: Modern JavaScript library
- **JavaScript ES6+**: Modern JavaScript features
- **HTML5**: Markup language
- **CSS3**: Styling language

### UI Framework
- **Material-UI (MUI)**: React component library
- **Material Design**: Design system
- **Responsive Design**: Mobile-first approach

### Data Visualization
- **Chart.js**: Charting library
- **React Chart.js 2**: React wrapper for Chart.js
- **D3.js**: Data visualization (via Chart.js)

### HTTP Client
- **Axios**: Promise-based HTTP client
- **REST API**: Communication with backend

### Routing
- **React Router**: Client-side routing
- **Protected Routes**: Authentication-based routing

### Build Tools
- **npm**: Package manager
- **Webpack**: Module bundler (via Create React App)
- **Babel**: JavaScript compiler

---

## Database Technologies

### Primary Database
- **PostgreSQL 14+**: Relational database
- **ACID Compliance**: Data integrity
- **JSON Support**: Flexible data types
- **Full-text Search**: Advanced search capabilities

### Database Features
- **Connection Pooling**: HikariCP for performance
- **Transaction Management**: ACID transactions
- **Indexing**: Performance optimization
- **Backup & Recovery**: Data protection

### Schema Management
- **Hibernate DDL**: Automatic schema creation
- **Migration Scripts**: Database updates
- **Entity Relationships**: JPA mappings

---

## Build Tools

### Backend Build
- **Maven 3.8+**: Dependency management and build
- **Spring Boot Maven Plugin**: Application packaging
- **Maven Surefire**: Test execution
- **Maven Compiler**: Java compilation

### Frontend Build
- **npm**: Package management
- **Create React App**: Development environment
- **Webpack**: Module bundling
- **Babel**: JavaScript transpilation

### CI/CD
- **GitHub Actions**: Continuous integration
- **Maven**: Automated testing and building
- **npm**: Frontend testing and building

---

## Development Tools

### IDE Support
- **IntelliJ IDEA**: Recommended IDE
- **Eclipse**: Alternative IDE
- **VS Code**: Lightweight editor
- **Spring Tools Suite**: Spring-specific IDE

### Version Control
- **Git**: Version control system
- **GitHub**: Repository hosting
- **GitHub Actions**: CI/CD pipeline

### Code Quality
- **Lombok**: Code generation
- **Checkstyle**: Code style checking
- **SpotBugs**: Static analysis
- **JaCoCo**: Code coverage

---

## Deployment Technologies

### Containerization
- **Docker**: Container platform
- **Docker Compose**: Multi-container orchestration
- **OpenJDK 17**: Java runtime container

### Web Server
- **Spring Boot Embedded Tomcat**: Development server
- **Nginx**: Production web server (recommended)
- **Apache HTTP Server**: Alternative web server

### Process Management
- **systemd**: Linux service management
- **PM2**: Node.js process manager
- **Supervisor**: Process monitoring

---

## Version Information

### Backend Dependencies
```xml
<properties>
    <java.version>17</java.version>
    <spring-boot.version>3.2.0</spring-boot.version>
    <tribuo.version>4.3.2</tribuo.version>
    <postgresql.version>42.7.1</postgresql.version>
    <junit.version>5.10.1</junit.version>
    <mockito.version>5.8.0</mockito.version>
</properties>
```

### Frontend Dependencies
```json
{
  "dependencies": {
    "react": "^18.2.0",
    "@mui/material": "^5.15.0",
    "chart.js": "^4.4.0",
    "axios": "^1.6.0",
    "react-router-dom": "^6.20.0"
  }
}
```

### System Requirements
- **Java**: 17 or higher
- **Node.js**: 18 or higher
- **PostgreSQL**: 14 or higher
- **Maven**: 3.8 or higher
- **Memory**: 4GB minimum, 8GB recommended
- **Disk Space**: 10GB minimum

---

## Technology Decisions

### Why Java 17?
- **Modern Features**: Records, pattern matching, text blocks
- **Performance**: Improved garbage collection
- **Security**: Enhanced security features
- **Long-term Support**: LTS version

### Why Spring Boot?
- **Rapid Development**: Auto-configuration
- **Production Ready**: Built-in monitoring and metrics
- **Ecosystem**: Large community and ecosystem
- **Enterprise Support**: Professional support available

### Why React?
- **Component-based**: Reusable UI components
- **Virtual DOM**: Performance optimization
- **Ecosystem**: Large package ecosystem
- **Industry Standard**: Widely adopted

### Why PostgreSQL?
- **ACID Compliance**: Data integrity
- **Performance**: Excellent query performance
- **Features**: Advanced features (JSON, full-text search)
- **Open Source**: No licensing costs

### Why Tribuo?
- **Java Native**: No Python dependencies
- **Oracle Support**: Enterprise-grade library
- **Performance**: Optimized for Java
- **XAI Support**: Built-in explainability features

---

## Performance Characteristics

### Backend Performance
- **Startup Time**: < 30 seconds
- **Memory Usage**: 512MB - 2GB
- **Response Time**: < 2 seconds for predictions
- **Throughput**: 100+ requests/second

### Frontend Performance
- **Bundle Size**: < 2MB gzipped
- **Load Time**: < 3 seconds
- **Runtime Performance**: 60 FPS
- **Memory Usage**: < 100MB

### Database Performance
- **Query Time**: < 100ms for simple queries
- **Connection Pool**: 20 max connections
- **Storage**: Efficient data compression
- **Backup Time**: < 5 minutes for 1GB database

---

## Security Considerations

### Authentication
- **JWT Tokens**: Stateless authentication
- **Password Hashing**: BCrypt with salt
- **Session Management**: Token-based sessions
- **CORS**: Configurable cross-origin policies

### Data Protection
- **Input Validation**: Comprehensive validation
- **SQL Injection**: Parameterized queries
- **XSS Protection**: Input sanitization
- **File Upload**: Secure file handling

### Infrastructure
- **HTTPS**: SSL/TLS encryption
- **Environment Variables**: Secure configuration
- **Database Security**: Connection encryption
- **File Permissions**: Proper access controls

---

**Next**: [[Edge Cases]] | **Previous**: [[27-Project-Roadmap]]  
**Related**: [[06-Developer-Architecture]], [[Advanced Concepts]], [[02-Getting-Started-Installation]]
