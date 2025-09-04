# XAI-Forge: User Guide

This guide will walk you through the process of using the XAI-Forge platform, from registering an account to getting your first model explanation.

### Step 1: Registration and Login

1.  **Navigate** to the application homepage.
2.  Click the **"Register"** button.
3.  Fill in your desired username, email, and a secure password.
4.  Once registered, you will be redirected to the **"Login"** page.
5.  Enter your credentials to log in. You will be taken to your personal dashboard.

### Step 2: Uploading a Dataset

Your dashboard is where you manage your datasets and models. Before you can train a model, you need to provide data.

1.  On the dashboard, locate the **"Upload Dataset"** section.
2.  Click the "Choose File" button and select a **CSV file** from your computer.
    *   **Note:** Your CSV file should have a header row with column names.
3.  Click **"Upload"**. The dataset will appear in your "My Datasets" list, showing its name and column headers.

### Step 3: Training a Model

Once a dataset is uploaded, you can use it to train a machine learning model.

1.  In the "My Datasets" list, find the dataset you wish to use and click the **"Train Model"** button next to it.
2.  You will be taken to the model training page, which displays all columns from your dataset.
3.  **Give your model a name** (e.g., "Customer Churn Predictor").
4.  **Select the Target Variable:** This is the column you want the model to learn to predict (e.g., "churn").
5.  **Select the Feature Variables:** These are the columns the model should use as input to make its predictions (e.g., "age", "tenure", "monthly_bill").
6.  Click the **"Start Training"** button. The training process will begin and may take a few moments. Once complete, your new model will appear in the "My Models" list on your dashboard.

### Step 4: Making and Explaining a Prediction

Now for the exciting part! Let's use your trained model to make a prediction and understand its reasoning.

1.  In the "My Models" list, find your newly trained model and click the **"Predict"** button.
2.  This will open the **Prediction & Explanation Interface**.
3.  You will see input fields for each feature you used during training (e.g., an input box for "age", another for "tenure").
4.  **Enter a new data point** you want to test. For example, `age: 50`, `tenure: 2`.
5.  Click the **"Explain Prediction"** button.
6.  The interface will update instantly to show you two things:
    *   **The Prediction:** The final outcome from the model (e.g., "Prediction: True").
    *   **The Explanation Chart:** A bar chart showing which features were most influential for *this specific prediction*. Green bars might indicate features that pushed the prediction towards "True", while red bars show features that pushed it towards "False".

You have now successfully trained a model and received a fully transparent explanation for its decision!
