# Contributing to XAI-Forge

> 📘 **Source**: This wiki page contains complete information from [CONTRIBUTING.md](https://github.com/Mukaan17/xai-forge/blob/main/CONTRIBUTING.md)

**Navigation**: [[Home]] > [[Contributing & Development]] > Contributing

## Table of Contents

1. [Code of Conduct](#code-of-conduct)
2. [How to Contribute](#how-to-contribute)
3. [Reporting Issues](#reporting-issues)
4. [Submitting Changes](#submitting-changes)
5. [Development Setup](#development-setup)
6. [Coding Standards](#coding-standards)
7. [Testing Requirements](#testing-requirements)
8. [Pull Request Process](#pull-request-process)
9. [Community Guidelines](#community-guidelines)

---

## Code of Conduct

### Our Pledge

We are committed to providing a welcoming and inspiring community for all. Please read and follow our Code of Conduct:

- **Be respectful and inclusive**
- **Be collaborative and constructive**
- **Be patient and understanding**
- **Be professional and courteous**

### Reporting Issues

If you experience or witness unacceptable behavior, please report it by contacting the project maintainers at [mukaan17@github.com](mailto:mukaan17@github.com).

---

## How to Contribute

We welcome contributions from the community! There are many ways to contribute to XAI-Forge:

### Types of Contributions

1. **Bug Reports**: Help us identify and fix issues
2. **Feature Requests**: Suggest new features or improvements
3. **Code Contributions**: Submit bug fixes or new features
4. **Documentation**: Improve or add documentation
5. **Testing**: Help improve test coverage and quality
6. **Examples**: Create example projects or tutorials

### Getting Started

1. **Fork the repository** on GitHub
2. **Clone your fork** locally
3. **Set up the development environment** (see [[Development Setup|Development-Setup]])
4. **Create a new branch** for your changes
5. **Make your changes** following our coding standards
6. **Test your changes** thoroughly
7. **Submit a pull request**

---

## Reporting Issues

### Before Reporting

Before reporting an issue, please:

1. **Search existing issues** to avoid duplicates
2. **Check the documentation** for solutions
3. **Verify the issue** with the latest version
4. **Gather relevant information** about your environment

### Issue Template

When reporting an issue, please include:

```markdown
## Bug Report

### Description
A clear and concise description of the bug.

### Steps to Reproduce
1. Go to '...'
2. Click on '....'
3. Scroll down to '....'
4. See error

### Expected Behavior
What you expected to happen.

### Actual Behavior
What actually happened.

### Environment
- OS: [e.g., Windows 10, macOS 12.0, Ubuntu 20.04]
- Java Version: [e.g., 17.0.2]
- Node.js Version: [e.g., 18.17.0]
- Browser: [e.g., Chrome 91, Firefox 89]

### Additional Context
Any other context about the problem.
```

### Issue Labels

We use the following labels to categorize issues:

- **bug**: Something isn't working
- **enhancement**: New feature or request
- **documentation**: Improvements or additions to documentation
- **good first issue**: Good for newcomers
- **help wanted**: Extra attention is needed
- **question**: Further information is requested

---

## Submitting Changes

### Pull Request Process

1. **Fork the repository** and create your branch from `main`
2. **Make your changes** following our coding standards
3. **Add tests** for any new functionality
4. **Update documentation** as needed
5. **Ensure all tests pass** locally
6. **Submit a pull request** with a clear description

### Pull Request Template

```markdown
## Pull Request

### Description
A clear and concise description of what this PR does.

### Type of Change
- [ ] Bug fix (non-breaking change which fixes an issue)
- [ ] New feature (non-breaking change which adds functionality)
- [ ] Breaking change (fix or feature that would cause existing functionality to not work as expected)
- [ ] Documentation update

### Testing
- [ ] Unit tests pass
- [ ] Integration tests pass
- [ ] Manual testing completed

### Checklist
- [ ] My code follows the project's coding standards
- [ ] I have performed a self-review of my code
- [ ] I have commented my code, particularly in hard-to-understand areas
- [ ] I have made corresponding changes to the documentation
- [ ] My changes generate no new warnings
- [ ] I have added tests that prove my fix is effective or that my feature works
- [ ] New and existing unit tests pass locally with my changes
```

---

## Development Setup

### Prerequisites

- **Java 17+**: Required for backend development
- **Node.js 18+**: Required for frontend development
- **PostgreSQL 14+**: Required for database
- **Maven 3.8+**: Required for backend build
- **Git**: Required for version control

### Environment Setup

#### 1. Clone the Repository
```bash
git clone https://github.com/Mukaan17/xai-forge.git
cd xai-forge
```

#### 2. Set Up Backend
```bash
cd backend
mvn clean install
```

#### 3. Set Up Frontend
```bash
cd frontend
npm install
```

#### 4. Configure Database
```bash
psql -U postgres -f setup-database.sql
```

#### 5. Set Environment Variables
```bash
export JWT_SECRET=$(openssl rand -base64 64)
export DB_PASSWORD=your-secure-password
```

### IDE Configuration

#### IntelliJ IDEA
- Install Lombok plugin
- Configure code style settings
- Set up run configurations

#### Eclipse
- Install Lombok plugin
- Configure code formatting
- Set up project preferences

#### VS Code
- Install Java Extension Pack
- Configure settings.json
- Set up debugging configuration

---

## Coding Standards

### Java Coding Standards

#### 1. Code Style
- Use **4 spaces** for indentation
- Use **camelCase** for variables and methods
- Use **PascalCase** for classes and interfaces
- Use **UPPER_CASE** for constants

#### 2. Naming Conventions
```java
// Classes and interfaces
public class UserService { }
public interface TrainingStrategy { }

// Methods and variables
public void trainModel() { }
private String modelName;

// Constants
public static final String DEFAULT_MODEL_TYPE = "CLASSIFICATION";
```

#### 3. Documentation
- **Class-level Javadoc** for all public classes
- **Method-level Javadoc** for all public methods
- **Parameter documentation** for complex methods
- **Return value documentation** when not obvious

```java
/**
 * Service for managing machine learning models.
 * 
 * @author Your Name
 * @since 1.0.0
 */
@Service
public class ModelService {
    
    /**
     * Trains a new machine learning model.
     * 
     * @param request the training request containing model parameters
     * @param userId the ID of the user requesting training
     * @return the trained model
     * @throws ModelTrainingException if training fails
     */
    public MLModel trainModel(TrainRequestDto request, Long userId) {
        // Implementation
    }
}
```

#### 4. Error Handling
- Use **specific exception types** for different error conditions
- **Log errors** with appropriate levels
- **Provide meaningful error messages**
- **Handle exceptions gracefully**

```java
try {
    Model<?> model = modelService.trainModel(request, userId);
    return ResponseEntity.ok(model);
} catch (ModelTrainingException e) {
    log.error("Model training failed for user {}: {}", userId, e.getMessage());
    return ResponseEntity.badRequest()
        .body(ApiResponse.error("Model training failed: " + e.getMessage()));
}
```

### JavaScript/React Coding Standards

#### 1. Code Style
- Use **2 spaces** for indentation
- Use **camelCase** for variables and functions
- Use **PascalCase** for components
- Use **UPPER_CASE** for constants

#### 2. Component Structure
```javascript
import React, { useState, useEffect } from 'react';
import { Button, TextField } from '@mui/material';

/**
 * Component for uploading datasets.
 * 
 * @param {Object} props - Component props
 * @param {Function} props.onUpload - Callback for upload completion
 * @returns {JSX.Element} The component
 */
const DatasetUpload = ({ onUpload }) => {
    const [file, setFile] = useState(null);
    
    const handleUpload = async () => {
        // Implementation
    };
    
    return (
        <div>
            <TextField
                type="file"
                onChange={(e) => setFile(e.target.files[0])}
            />
            <Button onClick={handleUpload}>Upload</Button>
        </div>
    );
};

export default DatasetUpload;
```

#### 3. Error Handling
```javascript
const handleUpload = async () => {
    try {
        setLoading(true);
        const response = await api.uploadDataset(file);
        onUpload(response.data);
    } catch (error) {
        console.error('Upload failed:', error);
        setError('Upload failed. Please try again.');
    } finally {
        setLoading(false);
    }
};
```

---

## Testing Requirements

### Test Coverage

- **Minimum 80% line coverage** for all new code
- **Unit tests** for all service methods
- **Integration tests** for all API endpoints
- **E2E tests** for critical user workflows

### Test Structure

#### Unit Tests
```java
@ExtendWith(MockitoExtension.class)
class ModelServiceTest {
    
    @Mock
    private MLModelRepository modelRepository;
    
    @InjectMocks
    private ModelService modelService;
    
    @Test
    @DisplayName("Should train model successfully")
    void testTrainModel_Success() {
        // Given
        TrainRequestDto request = createTestRequest();
        Long userId = 1L;
        
        // When
        MLModel result = modelService.trainModel(request, userId);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getModelName()).isEqualTo(request.getModelName());
    }
}
```

#### Integration Tests
```java
@SpringBootTest
@AutoConfigureTestDatabase
class ModelControllerIntegrationTest {
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Test
    @DisplayName("Should train model via API")
    void testTrainModel_Integration() {
        // Given
        TrainRequestDto request = createTestRequest();
        
        // When
        ResponseEntity<MLModel> response = restTemplate.postForEntity(
            "/api/models/train", request, MLModel.class);
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
    }
}
```

### Test Data

- Use **test fixtures** for consistent test data
- **Clean up** test data after each test
- Use **meaningful test data** that reflects real usage
- **Isolate tests** to avoid dependencies

---

## Pull Request Process

### Before Submitting

1. **Run all tests** locally
2. **Check code coverage** meets requirements
3. **Update documentation** as needed
4. **Self-review** your code
5. **Squash commits** if necessary

### Review Process

1. **Automated checks** must pass
2. **Code review** by maintainers
3. **Testing** by maintainers
4. **Approval** from at least one maintainer
5. **Merge** to main branch

### Review Criteria

- **Code quality** meets project standards
- **Tests** are comprehensive and pass
- **Documentation** is updated appropriately
- **Performance** is acceptable
- **Security** considerations are addressed

---

## Community Guidelines

### Communication

- **Be respectful** in all interactions
- **Use clear language** and provide context
- **Ask questions** when you need help
- **Share knowledge** with others

### Getting Help

- **Check documentation** first
- **Search existing issues** for solutions
- **Ask questions** in GitHub Discussions
- **Join community** conversations

### Recognition

- **Contributors** are recognized in CONTRIBUTORS.md
- **Significant contributions** are highlighted in release notes
- **Community members** are celebrated for their efforts

---

## Development Workflow

### Branch Strategy

- **main**: Production-ready code
- **develop**: Integration branch for features
- **feature/**: Feature development branches
- **bugfix/**: Bug fix branches
- **hotfix/**: Critical bug fixes

### Commit Messages

Use conventional commit format:

```
feat: add new model training algorithm
fix: resolve dataset upload validation issue
docs: update API documentation
test: add unit tests for model service
refactor: improve error handling in controllers
```

### Release Process

1. **Feature freeze** on develop branch
2. **Testing** and bug fixes
3. **Release candidate** creation
4. **Final testing** and validation
5. **Release** to main branch
6. **Tagging** and documentation

---

## Resources

### Documentation
- [[Development Setup|Development-Setup]] - Detailed setup instructions
- [[Coding Standards|Coding-Standards]] - Comprehensive coding guidelines
- [[Testing Guide|Testing-Guide]] - Testing requirements and examples
- [[API Reference|API-Reference]] - API documentation

### Tools
- [GitHub Issues](https://github.com/Mukaan17/xai-forge/issues) - Issue tracking
- [GitHub Discussions](https://github.com/Mukaan17/xai-forge/discussions) - Community discussions
- [GitHub Actions](https://github.com/Mukaan17/xai-forge/actions) - CI/CD pipeline

### Community
- [GitHub Repository](https://github.com/Mukaan17/xai-forge) - Main repository
- [Contributors](https://github.com/Mukaan17/xai-forge/graphs/contributors) - Project contributors
- [Releases](https://github.com/Mukaan17/xai-forge/releases) - Project releases

---

Thank you for contributing to XAI-Forge! Your contributions help make this project better for everyone.

---

**Next**: [[Development Setup|Development-Setup]] | **Previous**: [[Test Coverage|Test-Coverage]]  
**Related**: [[Development Setup|Development-Setup]], [[Coding Standards|Coding-Standards]], [[Testing Guide|Testing-Guide]]
