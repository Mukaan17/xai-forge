# User Guide

> 📘 **Source**: This wiki page contains complete information from [docs/USER-GUIDE.md](https://github.com/Mukaan17/xai-forge/blob/main/docs/USER-GUIDE.md)

**Navigation**: [[Home]] > [[User Guide]] > User Guide

## Table of Contents

1. [Step 1: Registration and Login](#step-1-registration-and-login)
2. [Step 2: Uploading a Dataset](#step-2-uploading-a-dataset)
3. [Step 3: Training a Model](#step-3-training-a-model)
4. [Step 4: Making and Explaining a Prediction](#step-4-making-and-explaining-a-prediction)
5. [Understanding Explanations](#understanding-explanations)
6. [Best Practices](#best-practices)

---

## Step 1: Registration and Login

### Registration Process

1. **Navigate** to the application homepage at http://localhost:3000
2. Click the **"Register"** button in the top navigation
3. Fill in the registration form:
   - **Username**: Choose a unique username
   - **Email**: Enter your email address
   - **Password**: Create a secure password
4. Click **"Register"** to create your account
5. You will be automatically redirected to the login page

### Login Process

1. On the login page, enter your credentials:
   - **Username**: Your registered username
   - **Password**: Your password
2. Click **"Login"**
3. Upon successful login, you will be taken to your personal dashboard

### Dashboard Overview

Your dashboard is the central hub where you can:
- View your uploaded datasets
- Manage your trained models
- Access all XAI-Forge features
- Navigate between different sections using the tabbed interface

---

## Step 2: Uploading a Dataset

### Before You Start

Your dashboard is where you manage your datasets and models. Before you can train a model, you need to provide data.

### Dataset Requirements

- **File Format**: CSV files only
- **File Size**: Up to 10MB
- **Header Row**: Your CSV file should have a header row with column names
- **Data Quality**: Ensure your data is clean and properly formatted

### Upload Process

1. On the dashboard, locate the **"Upload Dataset"** section
2. Click the **"Choose File"** button
3. Select a **CSV file** from your computer
4. Click **"Upload"**
5. The system will:
   - Parse your CSV file
   - Extract column headers
   - Validate the data format
   - Store the file securely

### After Upload

Once uploaded successfully:
- The dataset will appear in your **"My Datasets"** list
- You'll see the dataset name and column headers
- The system displays metadata like row count and upload date
- You can now use this dataset for model training

### Dataset Management

From the "My Datasets" section, you can:
- View dataset details
- See column information
- Delete datasets you no longer need
- Train models using the dataset

---

## Step 3: Training a Model

### Prerequisites

Once a dataset is uploaded, you can use it to train a machine learning model.

### Training Process

1. In the **"My Datasets"** list, find the dataset you wish to use
2. Click the **"Train Model"** button next to the dataset
3. You will be taken to the model training page
4. The page displays all columns from your dataset

### Model Configuration

#### 1. Give Your Model a Name
- Enter a descriptive name (e.g., "Customer Churn Predictor", "Sales Forecast Model")
- This helps you identify the model later

#### 2. Select the Target Variable
- This is the column you want the model to learn to predict
- Examples: "churn", "sales_amount", "customer_satisfaction"
- The target variable should contain the outcomes you want to predict

#### 3. Select Feature Variables
- These are the columns the model should use as input
- Choose relevant features that might influence the target
- Examples: "age", "tenure", "monthly_bill", "location"
- You can select multiple features

### Starting Training

1. Review your selections:
   - Model name
   - Target variable
   - Feature variables
2. Click the **"Start Training"** button
3. The training process will begin
4. You'll see a progress indicator
5. Training may take a few moments depending on data size

### After Training

Once training is complete:
- Your new model will appear in the **"My Models"** list
- The model shows training date and performance metrics
- You can now use it for predictions

---

## Step 4: Making and Explaining a Prediction

### Accessing Your Model

Now for the exciting part! Let's use your trained model to make a prediction and understand its reasoning.

### Making a Prediction

1. In the **"My Models"** list, find your newly trained model
2. Click the **"Predict"** button
3. This opens the **Prediction & Explanation Interface**

### Input Data Entry

The interface will show:
- Input fields for each feature you used during training
- For example: input boxes for "age", "tenure", "monthly_bill"
- Clear labels and helpful placeholders

### Entering Test Data

1. **Enter a new data point** you want to test
2. Example inputs:
   - `age: 50`
   - `tenure: 2`
   - `monthly_bill: 75.50`
3. Ensure all required fields are filled
4. Click the **"Explain Prediction"** button

### Understanding the Results

The interface will update instantly to show you two key components:

#### 1. The Prediction
- **Final Outcome**: The model's prediction (e.g., "Prediction: True", "Prediction: $1,250")
- **Confidence Score**: How confident the model is in its prediction
- **Probability Distribution**: For classification, shows probabilities for each class

#### 2. The Explanation Chart
- **Feature Importance**: A bar chart showing which features influenced this specific prediction
- **Directional Impact**: 
  - Green bars indicate features that pushed the prediction in one direction
  - Red bars show features that pushed it in the opposite direction
- **Magnitude**: Bar length shows the strength of each feature's influence

### Interpreting Explanations

#### For Classification Models
- **Positive Impact**: Features that increase the likelihood of the predicted class
- **Negative Impact**: Features that decrease the likelihood of the predicted class
- **Feature Scores**: Numerical values showing relative importance

#### For Regression Models
- **Positive Impact**: Features that increase the predicted value
- **Negative Impact**: Features that decrease the predicted value
- **Magnitude**: How much each feature contributes to the final prediction

---

## Understanding Explanations

### What is Explainable AI (XAI)?

XAI-Forge uses **LIME (Local Interpretable Model-agnostic Explanations)** to provide transparent explanations for every prediction.

### Key Concepts

#### Feature Importance
- Shows which input features were most influential
- Helps you understand what the model "looked at" when making its decision
- Provides insight into model behavior

#### Local Explanations
- Explanations are specific to each individual prediction
- Different data points may have different explanations
- Helps identify patterns and anomalies

#### Confidence Scores
- Indicates how certain the model is about its prediction
- Higher confidence = more reliable prediction
- Lower confidence = model is less certain

### Reading Explanation Charts

#### Bar Chart Interpretation
- **X-axis**: Feature names
- **Y-axis**: Impact score (positive or negative)
- **Color coding**: 
  - Green: Positive impact on prediction
  - Red: Negative impact on prediction
- **Bar length**: Strength of impact

#### Example Interpretation
```
Feature: "tenure"
Impact: +0.65 (Green bar)
Meaning: Higher tenure increased the likelihood of the predicted outcome
```

### Best Practices for Understanding Explanations

1. **Look at the biggest bars first** - these are the most influential features
2. **Consider the direction** - positive vs negative impact
3. **Compare with domain knowledge** - do the explanations make sense?
4. **Check multiple predictions** - see if patterns emerge
5. **Use explanations for model validation** - ensure the model is learning the right patterns

---

## Best Practices

### Data Preparation

#### Dataset Quality
- **Clean Data**: Remove or handle missing values
- **Consistent Format**: Ensure data types are consistent
- **Relevant Features**: Choose features that logically relate to your target
- **Sufficient Data**: More data generally leads to better models

#### Feature Selection
- **Domain Knowledge**: Use your expertise to select relevant features
- **Avoid Redundancy**: Don't include highly correlated features
- **Consider Interactions**: Some features might work better together

### Model Training

#### Target Variable Selection
- **Clear Definition**: Ensure your target variable is well-defined
- **Balanced Classes**: For classification, try to have balanced classes
- **Appropriate Type**: Choose classification vs regression based on your problem

#### Feature Engineering
- **Relevant Features**: Select features that make business sense
- **Avoid Overfitting**: Don't use too many features relative to your data size
- **Test Different Combinations**: Experiment with different feature sets

### Making Predictions

#### Input Validation
- **Realistic Values**: Use realistic input values for testing
- **Complete Data**: Ensure all required fields are filled
- **Data Types**: Use appropriate data types (numbers, categories)

#### Interpreting Results
- **Consider Confidence**: Pay attention to confidence scores
- **Validate Explanations**: Check if explanations align with domain knowledge
- **Multiple Tests**: Test with various input scenarios

### Continuous Improvement

#### Model Monitoring
- **Track Performance**: Monitor model accuracy over time
- **Data Drift**: Watch for changes in input data patterns
- **Explanation Consistency**: Ensure explanations remain meaningful

#### Iterative Refinement
- **Collect Feedback**: Gather user feedback on predictions and explanations
- **Retrain Models**: Periodically retrain with new data
- **Feature Updates**: Add or remove features based on performance

---

## Troubleshooting Common Issues

### Upload Issues
- **File Format**: Ensure your file is CSV format
- **File Size**: Check that file is under 10MB limit
- **Headers**: Verify your CSV has a header row
- **Encoding**: Try saving CSV with UTF-8 encoding

### Training Issues
- **Insufficient Data**: Ensure you have enough rows for training
- **Missing Values**: Handle or remove rows with missing data
- **Feature Selection**: Ensure you've selected at least one feature
- **Target Variable**: Verify target variable has appropriate values

### Prediction Issues
- **Input Format**: Ensure input values match training data format
- **Missing Fields**: Fill in all required input fields
- **Data Types**: Use correct data types (numbers vs text)
- **Range Values**: Ensure input values are within reasonable ranges

---

**Next**: [[FAQ]] | **Previous**: [[Configuration]]  
**Related**: [[API Reference|API-Reference]], [[Troubleshooting]], [[Architecture]]
