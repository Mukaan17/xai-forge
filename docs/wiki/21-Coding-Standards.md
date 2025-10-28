# Coding Standards

> 📘 **Source**: This wiki page contains coding standards from [CONTRIBUTING.md](https://github.com/Mukaan17/xai-forge/blob/main/CONTRIBUTING.md)

**Navigation**: [[Home]] > [[Contributing & Development]] > Coding Standards

## Table of Contents

1. [Java Coding Standards](#java-coding-standards)
2. [JavaScript/React Standards](#javascriptreact-standards)
3. [Documentation Standards](#documentation-standards)
4. [Error Handling](#error-handling)
5. [Testing Standards](#testing-standards)

---

## Java Coding Standards

### Code Style
- Use **4 spaces** for indentation
- Use **camelCase** for variables and methods
- Use **PascalCase** for classes and interfaces
- Use **UPPER_CASE** for constants
- Maximum line length: **120 characters**

### Naming Conventions
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

### Documentation
- **Class-level Javadoc** for all public classes
- **Method-level Javadoc** for all public methods
- Include parameter and return value documentation

```java
/**
 * Service for managing machine learning models.
 * 
 * @param request the training request containing model parameters
 * @param userId the ID of the user requesting training
 * @return the trained model
 * @throws ModelTrainingException if training fails
 */
public MLModel trainModel(TrainRequestDto request, Long userId) {
    // Implementation
}
```

---

## JavaScript/React Standards

### Code Style
- Use **2 spaces** for indentation
- Use **camelCase** for variables and functions
- Use **PascalCase** for components
- Use **single quotes** for strings
- Maximum line length: **100 characters**

### Component Structure
```javascript
/**
 * Component for uploading datasets.
 * 
 * @param {Object} props - Component props
 * @param {Function} props.onUpload - Upload completion callback
 * @returns {JSX.Element} The component
 */
const DatasetUpload = ({ onUpload }) => {
    const [file, setFile] = useState(null);
    
    const handleUpload = async () => {
        try {
            // Implementation
        } catch (error) {
            console.error('Upload failed:', error);
        }
    };
    
    return (
        <div>
            {/* Component JSX */}
        </div>
    );
};
```

---

## Documentation Standards

### Java Documentation
- Use Javadoc for all public classes and methods
- Include parameter descriptions
- Document exceptions thrown
- Provide usage examples for complex methods

### JavaScript Documentation
- Use JSDoc for functions and components
- Document props and parameters
- Include return value descriptions
- Provide usage examples

---

## Error Handling

### Java Error Handling
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

### JavaScript Error Handling
```javascript
const handleUpload = async () => {
    try {
        setLoading(true);
        const response = await api.uploadDataset(file);
        onUpload(response.data);
    } catch (error) {
        setError('Upload failed. Please try again.');
        console.error('Upload error:', error);
    } finally {
        setLoading(false);
    }
};
```

---

## Testing Standards

### Java Testing
- Use **JUnit 5** for unit tests
- Use **Mockito** for mocking
- Follow **AAA pattern** (Arrange, Act, Assert)
- Use descriptive test method names

```java
@Test
@DisplayName("Should train model successfully with valid request")
void testTrainModel_Success() {
    // Given
    TrainRequestDto request = createValidRequest();
    
    // When
    MLModel result = modelService.trainModel(request, userId);
    
    // Then
    assertThat(result).isNotNull();
    assertThat(result.getModelName()).isEqualTo(request.getModelName());
}
```

### JavaScript Testing
- Use **Jest** for unit tests
- Use **React Testing Library** for component tests
- Test user interactions and behavior
- Use descriptive test descriptions

```javascript
test('should handle file selection', async () => {
    render(<DatasetUpload onUpload={mockOnUpload} />);
    
    const file = new File(['test content'], 'test.csv', { type: 'text/csv' });
    const input = screen.getByLabelText(/file/i);
    
    fireEvent.change(input, { target: { files: [file] } });
    
    await waitFor(() => {
        expect(screen.getByText('Upload')).not.toBeDisabled();
    });
});
```

---

**Next**: [[22-Operations-Deployment]] ]]  
**Related**: [[19-Contributing]], [[Development Setup]], [[Testing Guide]]
