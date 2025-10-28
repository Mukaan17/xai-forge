# FAQ - Frequently Asked Questions

> 📘 **Source**: This wiki page compiles common questions and answers from across all project documentation

**Navigation**: [[Home]] > [[User Guide]] > FAQ

## Table of Contents

1. [General Questions](#general-questions)
2. [Installation & Setup](#installation--setup)
3. [Data & Datasets](#data--datasets)
4. [Model Training](#model-training)
5. [Predictions & Explanations](#predictions--explanations)
6. [Technical Issues](#technical-issues)
7. [Performance & Limitations](#performance--limitations)

---

## General Questions

### What is XAI-Forge?

XAI-Forge is a comprehensive full-stack platform that demystifies machine learning by providing transparent, human-understandable explanations for every prediction. It allows users to upload datasets, train ML models, make predictions, and receive detailed explanations using Explainable AI techniques.

### What does XAI stand for?

XAI stands for **Explainable AI** (also known as **eXplainable AI**). It refers to artificial intelligence systems that can provide human-understandable explanations for their decisions and predictions.

### What makes XAI-Forge different?

XAI-Forge focuses on **transparency and explainability**:
- Every prediction comes with detailed explanations
- Uses LIME (Local Interpretable Model-agnostic Explanations) for feature-level insights
- Provides interactive visualizations of model reasoning
- Designed for users who need to understand and trust AI decisions

### Who is XAI-Forge designed for?

- **Data Scientists** who need explainable models
- **Business Analysts** who want to understand AI decisions
- **Students** learning about machine learning and XAI
- **Researchers** working on interpretable AI
- **Anyone** who wants transparent AI predictions

---

## Installation & Setup

### What are the system requirements?

**Required Software:**
- Java JDK 17 or higher
- Node.js 18 or higher
- PostgreSQL 14 or higher
- Maven 3.8 or higher
- Git (for cloning)

**System Requirements:**
- Minimum 4GB RAM
- 2GB free disk space
- Internet connection for dependencies

### How do I install PostgreSQL?

**Ubuntu/Debian:**
```bash
sudo apt update
sudo apt install postgresql postgresql-contrib
```

**macOS (with Homebrew):**
```bash
brew install postgresql
brew services start postgresql
```

**Windows:**
Download from [postgresql.org](https://www.postgresql.org/download/windows/)

### How do I generate a secure JWT secret?

```bash
# Generate a 256-bit secret
export JWT_SECRET=$(openssl rand -base64 64)

# Verify it's set
echo $JWT_SECRET
```

### What if I get "JWT_SECRET must be at least 32 characters"?

This error means your JWT secret is too short. Generate a new one:
```bash
export JWT_SECRET=$(openssl rand -base64 64)
```

### How do I check if all services are running?

```bash
# Check Java
java -version

# Check PostgreSQL
sudo systemctl status postgresql

# Check if backend is running
curl http://localhost:8080/api/health

# Check if frontend is running
curl http://localhost:3000
```

---

## Data & Datasets

### What file formats are supported?

Currently, XAI-Forge supports **CSV files only**. The CSV file must:
- Have a header row with column names
- Be properly formatted (comma-separated)
- Be under 10MB in size
- Use UTF-8 encoding

### What's the maximum file size?

The maximum file size is **10MB**. This limit ensures:
- Fast processing and upload times
- Reasonable memory usage
- Good user experience

### How should I prepare my CSV data?

**Best Practices:**
- Include a header row with descriptive column names
- Remove or handle missing values
- Ensure consistent data types in each column
- Use meaningful column names (avoid spaces, special characters)
- Save with UTF-8 encoding

**Example CSV format:**
```csv
age,tenure,monthly_bill,churn
25,12,45.50,False
30,24,67.25,True
45,6,89.00,False
```

### Can I upload multiple datasets?

Yes! You can upload multiple datasets and use them to train different models. Each dataset is stored separately and can be managed independently.

### How do I delete a dataset?

From your dashboard:
1. Go to "My Datasets" section
2. Find the dataset you want to delete
3. Click the "Delete" button
4. Confirm the deletion

**Note**: Deleting a dataset will also delete any models trained on that dataset.

---

## Model Training

### What types of models are supported?

XAI-Forge currently supports:
- **Classification**: Logistic Regression (predicting categories)
- **Regression**: Linear SGD (predicting continuous values)

### How do I choose between classification and regression?

**Use Classification when:**
- Your target variable has discrete categories (e.g., Yes/No, High/Medium/Low)
- You want to predict which category something belongs to
- Examples: churn prediction, spam detection, customer segmentation

**Use Regression when:**
- Your target variable is continuous (e.g., prices, scores, quantities)
- You want to predict a numerical value
- Examples: sales forecasting, price prediction, performance scores

### How much data do I need for training?

**Minimum Requirements:**
- At least 100 rows for basic training
- More data generally leads to better models
- For classification: balanced classes work best

**Recommended:**
- 1000+ rows for reliable models
- 10+ rows per feature (avoid overfitting)
- Balanced datasets for classification problems

### What features should I select?

**Choose features that:**
- Are logically related to your target variable
- Don't have too many missing values
- Are not highly correlated with each other
- Make business sense for your problem

**Avoid:**
- Features with too many missing values
- Highly correlated features
- Features that don't relate to your target
- Too many features relative to your data size

### How long does training take?

Training time depends on:
- **Data size**: More rows = longer training
- **Number of features**: More features = longer training
- **Model complexity**: Some algorithms are faster than others

**Typical times:**
- Small datasets (< 1000 rows): 5-30 seconds
- Medium datasets (1000-10000 rows): 30 seconds - 2 minutes
- Large datasets (> 10000 rows): 2-10 minutes

### What if training fails?

**Common causes and solutions:**
- **Insufficient data**: Add more rows or reduce features
- **Missing values**: Clean your data first
- **Invalid target variable**: Check target variable values
- **Memory issues**: Reduce dataset size or features

---

## Predictions & Explanations

### How do I make a prediction?

1. Go to "My Models" section
2. Click "Predict" next to your trained model
3. Fill in the input fields with your test data
4. Click "Explain Prediction"
5. View the prediction and explanation chart

### What does the confidence score mean?

The confidence score indicates how certain the model is about its prediction:
- **High confidence (0.8-1.0)**: Model is very sure
- **Medium confidence (0.5-0.8)**: Model is moderately sure
- **Low confidence (0.0-0.5)**: Model is uncertain

### How do I interpret the explanation chart?

**Bar Chart Elements:**
- **X-axis**: Feature names
- **Y-axis**: Impact score (positive or negative)
- **Green bars**: Features that increase the prediction
- **Red bars**: Features that decrease the prediction
- **Bar length**: Strength of the feature's influence

**Example:**
```
Feature: "tenure"
Impact: +0.65 (Green bar)
Meaning: Higher tenure increases the likelihood of the predicted outcome
```

### What is LIME?

**LIME (Local Interpretable Model-agnostic Explanations)** is the technique XAI-Forge uses to generate explanations. It:
- Works with any machine learning model
- Provides explanations for individual predictions
- Shows which features were most important
- Indicates the direction of each feature's impact

### Why are explanations important?

**Explanations help you:**
- **Trust the model**: Understand why it made a decision
- **Validate results**: Check if the reasoning makes sense
- **Identify bias**: Spot unfair or incorrect patterns
- **Improve models**: Understand what the model learned
- **Comply with regulations**: Meet explainability requirements

### Can I get explanations for multiple predictions?

Yes! You can make multiple predictions and compare their explanations to understand:
- How different inputs affect the model
- Which features are consistently important
- Whether the model's reasoning is consistent

---

## Technical Issues

### The backend won't start. What should I check?

**Check these in order:**
1. **Environment variables**: `echo $JWT_SECRET`
2. **Database connection**: `psql -U xai_user -d xai_db -c "SELECT 1;"`
3. **Port availability**: `lsof -i :8080`
4. **Java version**: `java -version`
5. **Maven build**: `mvn clean install`

### The frontend won't connect to the backend. What's wrong?

**Common issues:**
- Backend not running on port 8080
- CORS configuration problems
- Wrong API URL in environment variables
- Network/firewall blocking the connection

**Solutions:**
```bash
# Check backend is running
curl http://localhost:8080/api/health

# Check environment variable
echo $REACT_APP_API_URL

# Restart both services
```

### I'm getting CORS errors. How do I fix this?

CORS (Cross-Origin Resource Sharing) errors occur when the frontend can't communicate with the backend. Solutions:

1. **Check CORS configuration** in backend
2. **Verify API URL** in frontend environment
3. **Ensure both services are running**
4. **Check browser console** for specific error messages

### Database connection failed. What should I do?

**Troubleshooting steps:**
1. **Check PostgreSQL is running**: `sudo systemctl status postgresql`
2. **Verify database exists**: `psql -U postgres -l | grep xai_db`
3. **Test connection**: `psql -U xai_user -d xai_db -c "SELECT 1;"`
4. **Check credentials**: Verify DB_USERNAME and DB_PASSWORD
5. **Check URL**: Verify DB_URL format

### Upload directory errors. How do I fix this?

**Error**: "Upload directory is not writable"

**Solution**:
```bash
# Create directories
mkdir -p uploads/datasets uploads/models

# Set permissions
chmod 755 uploads/
chmod 755 uploads/datasets/
chmod 755 uploads/models/

# Or set custom directory
export UPLOAD_DIR=/path/to/your/uploads
```

---

## Performance & Limitations

### What are the current limitations?

**File Size**: Maximum 10MB CSV files
**Model Types**: Only Logistic Regression and Linear SGD
**Concurrent Users**: Limited by server resources
**File Storage**: Local filesystem only
**Explanations**: LIME-based only (no SHAP or other methods)

### How many users can use the system simultaneously?

This depends on your server resources:
- **Development**: 5-10 concurrent users
- **Production**: 50-100+ users with proper server setup
- **Limiting factors**: CPU, memory, database connections

### Can I use the system in production?

**Current state**: The system is designed for development and testing
**For production use**, consider:
- Setting up proper server infrastructure
- Configuring SSL/TLS
- Setting up monitoring and logging
- Implementing backup strategies
- Using cloud storage for files

### How do I improve performance?

**For better performance:**
- Use smaller datasets when possible
- Select only relevant features
- Ensure adequate server resources
- Use SSD storage for better I/O
- Configure database connection pooling

### What's the roadmap for new features?

**Planned enhancements:**
- Support for more ML algorithms (Random Forest, Neural Networks)
- Advanced XAI techniques (SHAP, Integrated Gradients)
- Model comparison and evaluation metrics
- Data preprocessing and feature engineering tools
- Model deployment and API endpoints
- Real-time prediction services

---

## Getting Help

### Where can I get more help?

- **Documentation**: Check other wiki pages for detailed guides
- **GitHub Issues**: Report bugs or request features
- **GitHub Discussions**: Ask questions and share ideas
- **Code Review**: Submit pull requests for improvements

### How do I report a bug?

1. Go to [GitHub Issues](https://github.com/Mukaan17/xai-forge/issues)
2. Click "New Issue"
3. Provide:
   - Clear description of the problem
   - Steps to reproduce
   - Expected vs actual behavior
   - Your environment details

### How do I request a new feature?

1. Go to [GitHub Discussions](https://github.com/Mukaan17/xai-forge/discussions)
2. Start a new discussion
3. Describe:
   - What you want to achieve
   - Why it would be useful
   - How it might work
   - Any examples or references

---

**Next**: [[Architecture]] | **Previous**: [[User Guide|User-Guide]]  
**Related**: [[Troubleshooting]], [[Installation]], [[Configuration]]
