# XAI-Forge 🔬

> **XAI-Forge** is a comprehensive full-stack platform that demystifies machine learning by providing transparent, human-understandable explanations for every prediction. Built with modern Java and React technologies, it empowers users to build, train, and understand ML models with complete transparency.

## 🌟 Quick Navigation

### Getting Started
- [[01-Getting-Started-Quick-Start|Quick Start]] - Get up and running in 5 minutes
- [[02-Getting-Started-Installation|Installation]] - Complete setup guide
- [[03-Getting-Started-Configuration|Configuration]] - Environment and security setup

### User Guide
- [[04-User-Guide|User Guide]] - Complete walkthrough
- [[05-User-FAQ|FAQ]] - Common questions and answers

### Developer Documentation
- [[06-Developer-Architecture|Architecture]] - System design and structure
- [[07-Developer-API-Reference|API Reference]] - Complete REST API docs
- [[08-Developer-Advanced-Concepts|Advanced Concepts]] - 13+ Java concepts
- [[09-Developer-Design-Patterns|Design Patterns]] - Implementation patterns

### Architecture Decisions
- [[10-Architecture-Decisions|Architecture Decisions]] - ADR index and links

### Testing & Quality
- [[17-Testing-Guide|Testing Guide]] - Complete testing documentation
- [[18-Testing-Coverage|Test Coverage]] - Coverage metrics and analysis

### Contributing & Development
- [[19-Contributing|Contributing]] - How to contribute
- [[20-Development-Setup|Development Setup]] - Developer environment
- [[21-Coding-Standards|Coding Standards]] - Code conventions

### Operations
- [[22-Operations-Deployment|Deployment]] - Production deployment
- [[23-Operations-Troubleshooting|Troubleshooting]] - Common issues and solutions
- [[24-Operations-Monitoring|Monitoring]] - Application monitoring

### Project Information
- [[25-Project-Status|Project Status]] - Current development status
- [[26-Project-Changelog|Changelog]] - Version history
- [[27-Project-Roadmap|Roadmap]] - Future enhancements

### Reference
- [[28-Reference-Technology-Stack|Technology Stack]] - Complete tech overview
- [[29-Reference-Edge-Cases|Edge Cases]] - Known limitations
- [[30-Reference-Glossary|Glossary]] - Terminology and definitions

---

## 🚀 Key Features

- **🔐 Secure Authentication** - JWT-based user management with encrypted passwords
- **📊 Dataset Management** - Upload, parse, and manage CSV datasets with metadata
- **🤖 ML Model Training** - Train classification and regression models using Tribuo
- **🔮 Smart Predictions** - Get real-time predictions with confidence scores
- **🧠 Explainable AI** - LIME-based feature importance explanations for every prediction
- **🎨 Modern UI** - Beautiful, responsive React interface with Material-UI
- **📈 Interactive Visualizations** - Chart.js-powered explanation charts
- **🔒 Data Security** - User-specific data isolation and secure file handling
- **⚡ Async Processing** - Non-blocking ML operations with dedicated thread pools
- **🏗️ Advanced Patterns** - Builder, Factory, and Strategy patterns for maintainable code
- **🛡️ Robust Error Handling** - Comprehensive exception hierarchy with 11 custom exceptions
- **⚙️ Configuration Validation** - Startup validation for critical configuration parameters

## 🏗️ Technology Stack

### Backend
- **Java 17+** with Spring Boot 3+
- **Spring Security** with JWT authentication
- **PostgreSQL** database
- **Tribuo 4.3.2** (Oracle Labs) for ML and XAI
- **Apache Commons CSV** for data parsing
- **Apache POI 5.2.5** for Excel processing

### Frontend
- **React 18** with Material-UI
- **Chart.js** for visualizations
- **Axios** for API communication
- **React Router** for navigation

### Build Tools
- **Maven** for backend
- **npm** for frontend

## 📊 Project Status

**Overall Progress**: 99% Complete

| Component | Status | Progress |
|-----------|--------|----------|
| Backend | ✅ Complete | 100% |
| Frontend | ✅ Complete | 100% |
| **Testing** | ✅ **Complete** | **100%** |
| Documentation | ✅ Complete | 100% |

## 🚀 Quick Start

1. **Prerequisites**: Java 17+, Node.js 18+, PostgreSQL 14+, Maven 3.8+
2. **Clone**: `git clone https://github.com/Mukaan17/xai-forge.git`
3. **Database**: Run `psql -U postgres -f setup-database.sql`
4. **Backend**: `cd backend && mvn spring-boot:run`
5. **Frontend**: `cd frontend && npm install && npm start`
6. **Access**: Frontend at http://localhost:3000, Backend at http://localhost:8080

> 📖 **Detailed instructions** available in [[Installation]]

## 📚 Documentation

This wiki contains complete documentation for all aspects of the XAI-Forge project. All information has been transferred from existing documentation with 100% preservation of content.

**Source Documentation**: All original documentation remains in the main repository under `docs/` and root directory.

## 🤝 Contributing

We welcome contributions! See [[Contributing]] for guidelines and [[Development Setup|Development-Setup]] for environment setup.

## 📄 License

This project is licensed under the MIT License.

---

**Navigation**: [[Home]] | **Quick Start**: [[01-Getting-Started-Quick-Start|Quick-Start]] | **Full Setup**: [[02-Getting-Started-Installation|Installation]]
