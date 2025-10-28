# Changelog

> 📘 **Source**: This wiki page contains complete information from [CHANGELOG.md](https://github.com/Mukaan17/xai-forge/blob/main/CHANGELOG.md)

**Navigation**: [[Home]] > [[Project Management]] > Changelog

## Table of Contents

1. [Unreleased](#unreleased)
2. [Version 1.1.0](#version-110---2025-10-24)
3. [Version 1.0.0](#version-100---2025-01-04)

---

## Unreleased

### Added
- Builder Pattern implementation (PredictionResponseBuilder, TrainRequestBuilder)
- Configuration validation system with startup checks
- Enhanced async processing with three executor beans
- Comprehensive exception hierarchy (11 custom exceptions)
- Factory and Strategy patterns for ML algorithms
- Enhanced error handling and validation
- Advanced design patterns documentation

### Changed
- Updated project structure to include new packages (builder/, factory/, strategy/, exception/)
- Enhanced API responses with builder pattern usage
- Improved error handling with specific exception types

### Deprecated
- N/A

### Removed
- N/A

### Fixed
- Resolved package structure issues
- Updated documentation to reflect current implementation

### Security
- JWT-based authentication system
- Password encryption with BCrypt
- CORS configuration for secure frontend-backend communication
- User-specific data isolation
- Configuration validation for security parameters

---

## Version 1.1.0 - 2025-10-24

### Added
- **Builder Pattern**: PredictionResponseBuilder and TrainRequestBuilder for fluent object creation
- **Configuration Validation**: Startup validation for critical parameters (JWT_SECRET, database, file permissions)
- **Enhanced Async Processing**: Three dedicated executor beans (ML training, prediction, file processing)
- **Exception Hierarchy**: 11 custom exceptions with specific error handling
- **Factory Pattern**: AlgorithmFactory and ModelFactory for ML algorithm selection
- **Strategy Pattern**: ClassificationStrategy and RegressionStrategy for training algorithms
- **Advanced Error Handling**: Global exception handler with proper HTTP status codes
- **Enhanced Documentation**: Updated all documentation to reflect current implementation

### Technical Improvements
- **47 Java source files** with advanced design patterns
- **15 React components** with Material-UI
- **Comprehensive package structure** with builder/, factory/, strategy/, exception/ packages
- **Enhanced API responses** with builder pattern usage
- **Improved error handling** with specific exception types and error codes
- **Configuration validation** for production readiness

### Security Enhancements
- Configuration validation for security parameters
- Enhanced error handling with proper HTTP status codes
- Improved exception hierarchy for better error tracking

---

## Version 1.0.0 - 2025-01-04

### Added
- Complete full-stack XAI application
- Backend API with Spring Boot 3
- React frontend with Material-UI
- PostgreSQL database integration
- Tribuo ML library integration
- LIME explainability implementation
- File upload and management
- Model serialization and storage
- Comprehensive documentation suite
- Project tracking and status management

### Features
- **User Management**: Registration, login, and authentication
- **Dataset Management**: CSV upload, parsing, and metadata storage
- **Model Training**: Classification and regression model training
- **Predictions**: Real-time prediction generation
- **Explainability**: LIME-based feature importance explanations
- **Modern UI**: Responsive React interface with Material-UI
- **Security**: JWT authentication and user data isolation
- **Documentation**: Complete setup, user, API, and architecture guides

### Technical Stack
- **Backend**: Java 17, Spring Boot 3, Spring Security, Spring Data JPA
- **Database**: PostgreSQL 14+
- **ML Library**: Tribuo 4.3.2
- **Frontend**: React 18, Material-UI, Chart.js, Axios
- **Build Tools**: Maven, npm
- **Authentication**: JWT tokens
- **File Storage**: Local filesystem with organized directory structure

---

**Next**: [[Roadmap]] | **Previous**: [[Project Status|Project-Status]]  
**Related**: [[Project Status|Project-Status]], [[Roadmap]], [[Architecture]]
