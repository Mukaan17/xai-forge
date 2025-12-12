/**
 * @Author: Mukhil Sundararaj
 * @Date:   2025-09-04 16:10:46
 * @Last Modified by:   Mukhil Sundararaj
 * @Last Modified time: 2025-10-24 18:36:23
 */
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
  CircularProgress,
  Grid,
  Card,
  CardContent,
} from '@mui/material';
import {
  Psychology,
} from '@mui/icons-material';
import { modelsAPI } from '../../api/models';
import { predictionsAPI } from '../../api/predictions';
import { useQuery, useMutation } from '@tanstack/react-query';
import { toast } from 'sonner';
import XaiDisplay from './XaiDisplay';

const Predictor = () => {
  const [selectedModel, setSelectedModel] = useState('');
  const [inputData, setInputData] = useState({});
  const [prediction, setPrediction] = useState(null);
  const [explanation, setExplanation] = useState(null);

  // Fetch ready models
  const { data: models = [] } = useQuery({
    queryKey: ['models', 'ready'],
    queryFn: () => modelsAPI.getReadyModels(),
    select: (response) => {
      if (Array.isArray(response)) return response;
      if (response?.content) return response.content;
      if (response?.data) return Array.isArray(response.data) ? response.data : [];
      return [];
    },
  });

  // Fetch model details when selected
  const { data: modelDetails } = useQuery({
    queryKey: ['model', selectedModel],
    queryFn: () => modelsAPI.getById(selectedModel),
    enabled: !!selectedModel,
    select: (response) => response?.data || response,
  });

  // Initialize input data when model details change
  useEffect(() => {
    if (modelDetails) {
      const features = modelDetails.featureColumns || modelDetails.featureNames || [];
      const initialInputData = {};
      features.forEach(feature => {
        initialInputData[feature] = '';
      });
      setInputData(initialInputData);
      setPrediction(null);
      setExplanation(null);
    }
  }, [modelDetails]);

  const handleInputChange = (feature, value) => {
    setInputData(prev => ({
      ...prev,
      [feature]: value,
    }));
  };

  // Prediction mutation
  const predictMutation = useMutation({
    mutationFn: (inputData) => predictionsAPI.predict(selectedModel, inputData),
    onSuccess: (response) => {
      const data = response?.data || response;
      setPrediction(data);
      // Extract explanation from prediction response if available
      if (data.explanation) {
        setExplanation(data.explanation);
      }
      toast.success('Prediction completed successfully!');
    },
    onError: (error) => {
      toast.error(error.response?.data?.message || 'Prediction failed');
    },
  });

  const handlePredict = () => {
    // Validate input data
    const missingFields = Object.entries(inputData).filter(([key, value]) => !value);
    if (missingFields.length > 0) {
      toast.error('Please fill in all required fields');
      return;
    }

    predictMutation.mutate(inputData);
  };

  const formatDate = (dateString) => {
    return new Date(dateString).toLocaleDateString();
  };

  return (
    <Box>
      <Typography variant="h5" gutterBottom>
        Make Predictions & Get Explanations
      </Typography>


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
                    {model.name || model.modelName} ({model.modelType || model.type})
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
                      <strong>Name:</strong> {modelDetails.name || modelDetails.modelName}
                    </Typography>
                    <Typography variant="body2">
                      <strong>Type:</strong> {modelDetails.modelType || modelDetails.type}
                    </Typography>
                    <Typography variant="body2">
                      <strong>Target Variable:</strong> {modelDetails.targetColumn || modelDetails.targetVariable}
                    </Typography>
                    <Typography variant="body2">
                      <strong>Accuracy:</strong> {modelDetails.accuracy ? (modelDetails.accuracy * 100).toFixed(2) + '%' : 'N/A'}
                    </Typography>
                    <Typography variant="body2">
                      <strong>Trained:</strong> {formatDate(modelDetails.trainedAt || modelDetails.trainingDate)}
                    </Typography>
                  </CardContent>
                </Card>

                <Typography variant="h6" gutterBottom>
                  Input Data
                </Typography>

                {(modelDetails.featureColumns || modelDetails.featureNames || []).map((feature) => (
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
                  disabled={predictMutation.isPending}
                  startIcon={predictMutation.isPending ? <CircularProgress size={20} /> : <Psychology />}
                  fullWidth
                >
                  {predictMutation.isPending ? 'Predicting...' : 'Make Prediction & Get Explanation'}
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
              modelType={modelDetails?.modelType || modelDetails?.type}
            />
          )}
        </Grid>
      </Grid>
    </Box>
  );
};

export default Predictor;
