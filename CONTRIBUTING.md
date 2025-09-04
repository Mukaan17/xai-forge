# Contributing to XAI-Forge

Thank you for your interest in contributing to XAI-Forge! This document provides guidelines and information for contributors.

## Code of Conduct

This project and everyone participating in it is governed by our commitment to creating a welcoming and inclusive environment. By participating, you agree to uphold these values.

## How to Contribute

### Reporting Issues

- Use the GitHub issue tracker to report bugs or request features
- Provide clear, detailed descriptions of the issue
- Include steps to reproduce bugs
- Specify your environment (OS, Java version, Node.js version, etc.)

### Submitting Changes

1. **Fork the repository** on GitHub
2. **Clone your fork** locally:
   ```bash
   git clone https://github.com/your-username/xai-forge.git
   cd xai-forge
   ```
3. **Create a feature branch**:
   ```bash
   git checkout -b feature/your-feature-name
   ```
4. **Make your changes** following our coding standards
5. **Test your changes** thoroughly
6. **Update documentation** if necessary
7. **Commit your changes** with clear, descriptive messages
8. **Push to your fork**:
   ```bash
   git push origin feature/your-feature-name
   ```
9. **Submit a pull request** with a clear description of your changes

## Development Setup

### Prerequisites

- Java JDK 17+
- Apache Maven 3.8+
- PostgreSQL 14+
- Node.js 18+ & npm
- Git

### Setup Instructions

1. Follow the setup guide in [docs/SETUP-GUIDE.md](docs/SETUP-GUIDE.md)
2. Ensure all tests pass before submitting changes
3. Update the project status in [PROJECT_STATUS.md](PROJECT_STATUS.md) if needed

## Coding Standards

### Java (Backend)

- Follow Java naming conventions
- Use meaningful variable and method names
- Add Javadoc comments for public methods
- Maintain consistent indentation (4 spaces)
- Use Lombok annotations where appropriate
- Follow Spring Boot best practices

### JavaScript/React (Frontend)

- Use ES6+ features
- Follow React best practices
- Use meaningful component and variable names
- Maintain consistent indentation (2 spaces)
- Use Material-UI components consistently
- Add PropTypes or TypeScript types where applicable

### General

- Write clear, descriptive commit messages
- Keep functions and methods focused and small
- Add appropriate error handling
- Include unit tests for new functionality
- Update documentation for new features

## Testing

### Backend Testing

- Write unit tests for service layer methods
- Add integration tests for API endpoints
- Test security configurations
- Verify database operations

### Frontend Testing

- Test component rendering
- Test user interactions
- Test API integration
- Verify responsive design

## Documentation

- Update README.md for significant changes
- Add API documentation for new endpoints
- Update user guide for new features
- Keep architecture documentation current

## Pull Request Process

1. Ensure your branch is up to date with the main branch
2. Run all tests and ensure they pass
3. Update documentation as needed
4. Provide a clear description of your changes
5. Reference any related issues
6. Request review from maintainers

## Release Process

- Version numbers follow semantic versioning (MAJOR.MINOR.PATCH)
- Create release notes for each version
- Tag releases appropriately
- Update changelog

## Questions?

If you have questions about contributing, please:

- Open an issue with the "question" label
- Check existing documentation
- Review closed issues for similar questions

Thank you for contributing to XAI-Forge!
