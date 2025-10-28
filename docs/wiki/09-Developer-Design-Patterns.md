# Design Patterns

> 📘 **Source**: This wiki page extracts design pattern details from [ADVANCED_CONCEPTS.md](https://github.com/Mukaan17/xai-forge/blob/main/ADVANCED_CONCEPTS.md)

**Navigation**: [[Home]] > [[Developer Documentation]] > Design Patterns

## Table of Contents

1. [Overview](#overview)
2. [Builder Pattern](#builder-pattern)
3. [Factory Pattern](#factory-pattern)
4. [Strategy Pattern](#strategy-pattern)
5. [Template Method Pattern](#template-method-pattern)
6. [Pattern Benefits](#pattern-benefits)
7. [Usage Examples](#usage-examples)

---

## Overview

XAI-Forge implements several key design patterns to create maintainable, extensible, and robust code. These patterns demonstrate advanced software engineering practices and provide clear separation of concerns.

### Implemented Patterns

1. **Builder Pattern** - Fluent object creation with validation
2. **Factory Pattern** - Algorithm selection and model creation
3. **Strategy Pattern** - Training algorithm implementations
4. **Template Method Pattern** - Training workflow structure

---

## Builder Pattern

### Purpose
Provides a fluent interface for creating complex objects with built-in validation and error handling.

### Implementation

#### PredictionResponseBuilder
```java
public class PredictionResponseBuilder {
    private String prediction;
    private Double confidence;
    private Map<String, Object> probabilities;
    private Map<String, String> inputData;
    
    public static PredictionResponseBuilder builder() {
        return new PredictionResponseBuilder();
    }
    
    public PredictionResponseBuilder setPrediction(String prediction) {
        this.prediction = prediction;
        return this;
    }
    
    public PredictionResponseBuilder setConfidence(Double confidence) {
        this.confidence = confidence;
        return this;
    }
    
    public PredictionResponseBuilder setProbabilities(Map<String, Object> probabilities) {
        this.probabilities = probabilities;
        return this;
    }
    
    public PredictionResponseBuilder setInputData(Map<String, String> inputData) {
        this.inputData = inputData;
        return this;
    }
    
    public PredictionResponse build() {
        validate();
        return new PredictionResponse(prediction, confidence, probabilities, inputData);
    }
    
    private void validate() {
        if (prediction == null || prediction.trim().isEmpty()) {
            throw new IllegalArgumentException("Prediction value is required");
        }
        if (confidence == null || confidence < 0.0 || confidence > 1.0) {
            throw new IllegalArgumentException("Confidence score must be between 0.0 and 1.0");
        }
        if (probabilities == null || probabilities.isEmpty()) {
            throw new IllegalArgumentException("Probabilities map is required");
        }
    }
}
```

#### TrainRequestBuilder
```java
public class TrainRequestBuilder {
    private String modelName;
    private String modelType;
    private Long datasetId;
    private String targetVariable;
    private List<String> featureNames;
    
    public static TrainRequestBuilder builder() {
        return new TrainRequestBuilder();
    }
    
    public TrainRequestBuilder setModelName(String modelName) {
        this.modelName = modelName;
        return this;
    }
    
    public TrainRequestBuilder setModelType(String modelType) {
        this.modelType = modelType;
        return this;
    }
    
    public TrainRequestBuilder setDatasetId(Long datasetId) {
        this.datasetId = datasetId;
        return this;
    }
    
    public TrainRequestBuilder setTargetVariable(String targetVariable) {
        this.targetVariable = targetVariable;
        return this;
    }
    
    public TrainRequestBuilder setFeatureNames(List<String> featureNames) {
        this.featureNames = featureNames;
        return this;
    }
    
    public TrainRequestDto build() {
        validate();
        return new TrainRequestDto(modelName, modelType, datasetId, targetVariable, featureNames);
    }
    
    private void validate() {
        if (modelName == null || modelName.trim().isEmpty()) {
            throw new IllegalArgumentException("Model name is required");
        }
        if (modelType == null || (!modelType.equals("CLASSIFICATION") && !modelType.equals("REGRESSION"))) {
            throw new IllegalArgumentException("Model type must be CLASSIFICATION or REGRESSION");
        }
        if (datasetId == null) {
            throw new IllegalArgumentException("Dataset ID is required");
        }
        if (targetVariable == null || targetVariable.trim().isEmpty()) {
            throw new IllegalArgumentException("Target variable is required");
        }
        if (featureNames == null || featureNames.isEmpty()) {
            throw new IllegalArgumentException("At least one feature must be selected");
        }
    }
}
```

### Benefits
- **Fluent API**: Method chaining for readable code
- **Built-in Validation**: Automatic validation during object creation
- **Immutable Objects**: Final objects with validated state
- **Type Safety**: Compile-time type checking

---

## Factory Pattern

### Purpose
Centralizes object creation logic and provides a consistent interface for creating different types of ML models and algorithms.

### Implementation

#### ModelFactory
```java
@Component
public class ModelFactory {
    
    private final Map<MLModel.ModelType, TrainingStrategy> strategies;
    
    public ModelFactory(List<TrainingStrategy> strategyList) {
        this.strategies = strategyList.stream()
            .collect(Collectors.toMap(
                TrainingStrategy::getModelType,
                Function.identity()
            ));
    }
    
    public Model<?> createModel(MLModel.ModelType modelType, MutableDataset<?> dataset) {
        TrainingStrategy strategy = strategies.get(modelType);
        if (strategy == null) {
            throw new IllegalArgumentException("Unsupported model type: " + modelType);
        }
        return strategy.train(dataset, new HashMap<>());
    }
    
    public List<MLModel.ModelType> getSupportedModelTypes() {
        return new ArrayList<>(strategies.keySet());
    }
}
```

#### AlgorithmFactory
```java
@Component
public class AlgorithmFactory {
    
    public TrainingStrategy getStrategy(MLModel.ModelType modelType) {
        return switch (modelType) {
            case CLASSIFICATION -> new ClassificationStrategy();
            case REGRESSION -> new RegressionStrategy();
            default -> throw new IllegalArgumentException("Unsupported model type: " + modelType);
        };
    }
    
    public List<String> getAvailableAlgorithms(MLModel.ModelType modelType) {
        return switch (modelType) {
            case CLASSIFICATION -> List.of("Logistic Regression", "Random Forest", "SVM");
            case REGRESSION -> List.of("Linear Regression", "Ridge Regression", "Lasso");
            default -> Collections.emptyList();
        };
    }
}
```

### Benefits
- **Centralized Creation**: Single point for object creation logic
- **Extensibility**: Easy to add new model types
- **Consistency**: Uniform creation process
- **Decoupling**: Client code doesn't need to know creation details

---

## Strategy Pattern

### Purpose
Encapsulates different training algorithms and allows runtime selection of the appropriate strategy.

### Implementation

#### TrainingStrategy Interface
```java
public interface TrainingStrategy {
    Model<?> train(MutableDataset<?> dataset, Map<String, Object> parameters);
    String getAlgorithmName();
    MLModel.ModelType getModelType();
    Map<String, Object> getDefaultParameters();
}
```

#### ClassificationStrategy
```java
@Component
public class ClassificationStrategy implements TrainingStrategy {
    
    @Override
    public Model<?> train(MutableDataset<?> dataset, Map<String, Object> parameters) {
        LogisticRegressionTrainer trainer = new LogisticRegressionTrainer();
        
        // Apply parameters if provided
        if (parameters.containsKey("regularization")) {
            trainer.setRegularization((Double) parameters.get("regularization"));
        }
        
        return trainer.train((MutableDataset<Label>) dataset);
    }
    
    @Override
    public String getAlgorithmName() {
        return "Logistic Regression";
    }
    
    @Override
    public MLModel.ModelType getModelType() {
        return MLModel.ModelType.CLASSIFICATION;
    }
    
    @Override
    public Map<String, Object> getDefaultParameters() {
        Map<String, Object> params = new HashMap<>();
        params.put("regularization", 1.0);
        params.put("maxIterations", 1000);
        return params;
    }
}
```

#### RegressionStrategy
```java
@Component
public class RegressionStrategy implements TrainingStrategy {
    
    @Override
    public Model<?> train(MutableDataset<?> dataset, Map<String, Object> parameters) {
        LinearSGDTrainer trainer = new LinearSGDTrainer();
        
        // Apply parameters if provided
        if (parameters.containsKey("learningRate")) {
            trainer.setLearningRate((Double) parameters.get("learningRate"));
        }
        
        return trainer.train((MutableDataset<Regressor>) dataset);
    }
    
    @Override
    public String getAlgorithmName() {
        return "Linear SGD";
    }
    
    @Override
    public MLModel.ModelType getModelType() {
        return MLModel.ModelType.REGRESSION;
    }
    
    @Override
    public Map<String, Object> getDefaultParameters() {
        Map<String, Object> params = new HashMap<>();
        params.put("learningRate", 0.01);
        params.put("maxIterations", 1000);
        return params;
    }
}
```

### Benefits
- **Algorithm Flexibility**: Easy to switch between algorithms
- **Extensibility**: Simple to add new training strategies
- **Parameter Management**: Each strategy manages its own parameters
- **Runtime Selection**: Algorithm choice at runtime

---

## Template Method Pattern

### Purpose
Defines the skeleton of the training algorithm while allowing subclasses to override specific steps.

### Implementation

#### AbstractModelService
```java
public abstract class AbstractModelService {
    
    public final MLModel trainModel(TrainRequestDto request, Long userId) throws Exception {
        // Template method with fixed steps
        Dataset dataset = validateDataset(request.getDatasetId(), userId);
        MutableDataset<?> tribuoDataset = loadDataset(dataset, request);
        Model<?> trainedModel = performTraining(tribuoDataset, request);
        return saveModel(trainedModel, request, dataset);
    }
    
    // Common implementation for all subclasses
    protected Dataset validateDataset(Long datasetId, Long userId) {
        return datasetRepository.findByIdAndOwnerId(datasetId, userId)
            .orElseThrow(() -> new DatasetNotFoundException(datasetId));
    }
    
    protected MutableDataset<?> loadDataset(Dataset dataset, TrainRequestDto request) throws IOException {
        // Common dataset loading logic
        return DatasetLoader.loadFromCSV(dataset.getFilePath(), request.getTargetVariable());
    }
    
    protected MLModel saveModel(Model<?> trainedModel, TrainRequestDto request, Dataset dataset) {
        // Common model saving logic
        MLModel mlModel = new MLModel();
        mlModel.setModelName(request.getModelName());
        mlModel.setModelType(MLModel.ModelType.valueOf(request.getModelType()));
        mlModel.setDataset(dataset);
        mlModel.setFeatureNames(request.getFeatureNames());
        mlModel.setTargetVariable(request.getTargetVariable());
        
        return modelRepository.save(mlModel);
    }
    
    // Abstract method to be implemented by subclasses
    protected abstract Model<?> performTraining(MutableDataset<?> dataset, TrainRequestDto request);
}
```

#### Concrete Implementations
```java
@Service
public class ClassificationModelService extends AbstractModelService {
    
    @Override
    protected Model<?> performTraining(MutableDataset<?> dataset, TrainRequestDto request) {
        ClassificationStrategy strategy = new ClassificationStrategy();
        return strategy.train(dataset, new HashMap<>());
    }
}

@Service
public class RegressionModelService extends AbstractModelService {
    
    @Override
    protected Model<?> performTraining(MutableDataset<?> dataset, TrainRequestDto request) {
        RegressionStrategy strategy = new RegressionStrategy();
        return strategy.train(dataset, new HashMap<>());
    }
}
```

### Benefits
- **Code Reuse**: Common logic shared across implementations
- **Consistency**: Uniform training workflow
- **Flexibility**: Subclasses can override specific steps
- **Maintainability**: Changes to common logic affect all implementations

---

## Pattern Benefits

### Overall Benefits

1. **Maintainability**: Clear separation of concerns makes code easier to maintain
2. **Extensibility**: Easy to add new features without modifying existing code
3. **Testability**: Each pattern can be tested independently
4. **Readability**: Code is more self-documenting and easier to understand
5. **Flexibility**: Runtime decisions and parameterization possible

### Specific Benefits by Pattern

#### Builder Pattern
- **Validation**: Built-in validation prevents invalid objects
- **Fluent API**: More readable object creation
- **Immutability**: Objects are created in valid state

#### Factory Pattern
- **Centralization**: Single point for object creation
- **Consistency**: Uniform creation process
- **Extensibility**: Easy to add new types

#### Strategy Pattern
- **Algorithm Selection**: Runtime algorithm choice
- **Parameter Management**: Each strategy manages its parameters
- **Extensibility**: Simple to add new algorithms

#### Template Method Pattern
- **Code Reuse**: Common workflow shared
- **Consistency**: Uniform process across implementations
- **Flexibility**: Specific steps can be customized

---

## Usage Examples

### Builder Pattern Usage
```java
// Creating a prediction response
PredictionResponse response = PredictionResponseBuilder.builder()
    .setPrediction("True")
    .setConfidence(0.85)
    .setProbabilities(Map.of("True", 0.85, "False", 0.15))
    .setInputData(Map.of("age", "45", "tenure", "3"))
    .build();

// Creating a training request
TrainRequestDto request = TrainRequestBuilder.builder()
    .setModelName("Churn Predictor")
    .setModelType("CLASSIFICATION")
    .setDatasetId(1L)
    .setTargetVariable("churn")
    .setFeatureNames(List.of("age", "tenure", "monthly_bill"))
    .build();
```

### Factory Pattern Usage
```java
// Creating a model using factory
Model<?> model = modelFactory.createModel(MLModel.ModelType.CLASSIFICATION, dataset);

// Getting available algorithms
List<String> algorithms = algorithmFactory.getAvailableAlgorithms(MLModel.ModelType.CLASSIFICATION);
```

### Strategy Pattern Usage
```java
// Getting strategy for model type
TrainingStrategy strategy = algorithmFactory.getStrategy(MLModel.ModelType.CLASSIFICATION);

// Training with strategy
Model<?> model = strategy.train(dataset, parameters);
```

### Template Method Usage
```java
// Using template method for training
MLModel model = classificationModelService.trainModel(request, userId);
```

---

## Best Practices

### Builder Pattern
- Always validate in the `build()` method
- Use method chaining for fluent API
- Make the builder immutable after building
- Provide meaningful error messages

### Factory Pattern
- Use dependency injection for strategy registration
- Provide clear error messages for unsupported types
- Consider using enums for type safety
- Document supported types

### Strategy Pattern
- Keep strategies focused on single responsibility
- Use interfaces for strategy contracts
- Provide default parameters
- Document algorithm-specific parameters

### Template Method Pattern
- Keep template methods focused
- Use abstract methods sparingly
- Provide meaningful default implementations
- Document the algorithm flow

---

**Next**: [[Architecture Decisions|Architecture-Decisions]] | **Previous**: [[Advanced Concepts|Advanced-Concepts]]  
**Related**: [[Architecture]], [[Advanced Concepts|Advanced-Concepts]], [[Contributing]]
