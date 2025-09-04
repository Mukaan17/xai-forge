import React, { useState, useEffect } from 'react';
import {
  Box,
  Typography,
  Paper,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  TextField,
  Button,
  Alert,
  CircularProgress,
  Grid,
  Card,
  CardContent,
} from '@mui/material';
import {
  Psychology,
  Visibility,
} from '@mui/icons-material';
import { modelAPI } from '../../api/api';
import XaiDisplay from './XaiDisplay';

const Predictor = ({ models, loading }) => {
  const [selectedModel, setSelectedModel] = useState('');
  const [modelDetails, setModelDetails] = useState(null);
  const [inputData, setInputData] = useState({});
  const [prediction, setPrediction] = useState(null);
  const [explanation, setExplanation] = useState(null);
  const [predicting, setPredicting] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  useEffect(() => {
    if (selectedModel) {
      fetchModelDetails();
    } else {
      setModelDetails(null);
      setInputData({});
      setPrediction(null);
      setExplanation(null);
    }
  }, [selectedModel]);

  const fetchModelDetails = async () => {
    try {
      const response = await modelAPI.getById(selectedModel);
      setModelDetails(response.data);
      
      // Initialize input data with empty values
      const initialInputData = {};
      response.data.featureNames.forEach(feature => {
        initialInputData[feature] = '';
      });
      setInputData(initialInputData);
    } catch (err) {
      setError('Failed to load model details');
    }
  };

  const handleInputChange = (feature, value) => {
    setInputData(prev => ({
      ...prev,
      [feature]: value,
    }));
  };

  const handlePredict = async () => {
    // Validate input data
    const missingFields = Object.entries(inputData).filter(([key, value]) => !value);
    if (missingFields.length > 0) {
      setError('Please fill in all required fields');
      return;
    }

    setPredicting(true);
    setError('');
    setSuccess('');

    try {
      // Make prediction and get explanation
      const [predictionResponse, explanationResponse] = await Promise.all([
        modelAPI.predict(selectedModel, inputData),
        modelAPI.explain(selectedModel, inputData),
      ]);

      setPrediction(predictionResponse.data);
      setExplanation(explanationResponse.data);
      setSuccess('Prediction completed successfully!');
    } catch (err) {
      setError(err.response?.data?.message || 'Prediction failed');
    } finally {
      setPredicting(false);
    }
  };

  const formatDate = (dateString) => {
    return new Date(dateString).toLocaleDateString();
  };

  return (
    <Box>
      <Typography variant="h5" gutterBottom>
        Make Predictions & Get Explanations
      </Typography>

      {error && (
        <Alert severity="error" sx={{ mb: 2 }}>
          {error}
        </Alert>
      )}

      {success && (
        <Alert severity="success" sx={{ mb: 2 }}>
          {success}
        </Alert>
      )}

      <Grid container spacing={3}>
        {/* Model Selection and Input */}
        <Grid item xs={12} md={6}>
          <Paper sx={{ p: 3 }}>
            <Typography variant="h6" gutterBottom>
              Model Selection & Input
            </Typography>

            <FormControl fullWidth sx={{ mb: 2 }}>
              <InputLabel>Select Model</InputLabel>
              <Select
                value={selectedModel}
                onChange={(e) => setSelectedModel(e.target.value)}
                label="Select Model"
              >
                {models.map((model) => (
                  <MenuItem key={model.id} value={model.id}>
                    {model.modelName} ({model.modelType})
                  </MenuItem>
                ))}
              </Select>
            </FormControl>

            {selectedModel && modelDetails && (
              <>
                <Card sx={{ mb: 2 }}>
                  <CardContent>
                    <Typography variant="h6" gutterBottom>
                      Model Information
                    </Typography>
                    <Typography variant="body2">
                      <strong>Name:</strong> {modelDetails.modelName}
                    </Typography>
                    <Typography variant="body2">
                      <strong>Type:</strong> {modelDetails.modelType}
                    </Typography>
                    <Typography variant="body2">
                      <strong>Target Variable:</strong> {modelDetails.targetVariable}
                    </Typography>
                    <Typography variant="body2">
                      <strong>Accuracy:</strong> {modelDetails.accuracy ? (modelDetails.accuracy * 100).toFixed(2) + '%' : 'N/A'}
                    </Typography>
                    <Typography variant="body2">
                      <strong>Trained:</strong> {formatDate(modelDetails.trainingDate)}
                    </Typography>
                  </CardContent>
                </Card>

                <Typography variant="h6" gutterBottom>
                  Input Data
                </Typography>

                {modelDetails.featureNames.map((feature) => (
                  <TextField
                    key={feature}
                    fullWidth
                    label={feature}
                    value={inputData[feature] || ''}
                    onChange={(e) => handleInputChange(feature, e.target.value)}
                    sx={{ mb: 2 }}
                    type="number"
                    inputProps={{ step: "any" }}
                  />
                ))}

                <Button
                  variant="contained"
                  onClick={handlePredict}
                  disabled={predicting}
                  startIcon={predicting ? <CircularProgress size={20} /> : <Psychology />}
                  fullWidth
                >
                  {predicting ? 'Predicting...' : 'Make Prediction & Get Explanation'}
                </Button>
              </>
            )}
          </Paper>
        </Grid>

        {/* Results */}
        <Grid item xs={12} md={6}>
          {prediction && explanation && (
            <XaiDisplay
              prediction={prediction}
              explanation={explanation}
              modelType={modelDetails?.modelType}
            />
          )}
        </Grid>
      </Grid>
    </Box>
  );
};

export default Predictor;
