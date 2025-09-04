# Changelog

All notable changes to XAI-Forge will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- Initial project structure with Spring Boot backend and React frontend
- User authentication with JWT tokens
- Dataset upload and management functionality
- Machine learning model training with Tribuo
- Prediction and explanation generation using LIME
- Comprehensive documentation
- Project status tracking system

### Changed
- N/A

### Deprecated
- N/A

### Removed
- N/A

### Fixed
- N/A

### Security
- JWT-based authentication system
- Password encryption with BCrypt
- CORS configuration for secure frontend-backend communication
- User-specific data isolation

## [1.0.0] - 2025-01-04

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
- **ML Library**: Tribuo 4.3.1
- **Frontend**: React 18, Material-UI, Chart.js, Axios
- **Build Tools**: Maven, npm
- **Authentication**: JWT tokens
- **File Storage**: Local filesystem with organized directory structure
