/**
 * @Author: Mukhil Sundararaj
 * @Date:   2025-09-04 16:10:30
 * @Last Modified by:   Mukhil Sundararaj
 * @Last Modified time: 2025-10-24 18:36:24
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
  Chip,
  Alert,
  CircularProgress,
  Grid,
  FormControlLabel,
  Checkbox,
  List,
  ListItem,
  ListItemText,
  ListItemSecondaryAction,
  IconButton,
} from '@mui/material';
import {
  Psychology,
  Delete,
} from '@mui/icons-material';
import { modelAPI, datasetAPI } from '../../api/api';

const ModelTrainer = ({ datasets, models, onModelTrained, loading }) => {
  const [selectedDataset, setSelectedDataset] = useState('');
  const [datasetDetails, setDatasetDetails] = useState(null);
  const [modelName, setModelName] = useState('');
  const [modelType, setModelType] = useState('CLASSIFICATION');
  const [targetVariable, setTargetVariable] = useState('');
  const [selectedFeatures, setSelectedFeatures] = useState([]);
  const [training, setTraining] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  useEffect(() => {
    if (selectedDataset) {
      fetchDatasetDetails();
    } else {
      setDatasetDetails(null);
      setTargetVariable('');
      setSelectedFeatures([]);
    }
  }, [selectedDataset]);

  const fetchDatasetDetails = async () => {
    try {
      const response = await datasetAPI.getById(selectedDataset);
      setDatasetDetails(response.data);
    } catch (err) {
      setError('Failed to load dataset details');
    }
  };

  const handleFeatureToggle = (feature) => {
    setSelectedFeatures(prev => {
      if (prev.includes(feature)) {
        return prev.filter(f => f !== feature);
      } else {
        return [...prev, feature];
      }
    });
  };

  const handleTrain = async () => {
    if (!selectedDataset || !modelName || !targetVariable || selectedFeatures.length === 0) {
      setError('Please fill in all required fields');
      return;
    }

    setTraining(true);
    setError('');
    setSuccess('');

    try {
      const trainData = {
        datasetId: parseInt(selectedDataset),
        modelName,
        modelType,
        targetVariable,
        featureNames: selectedFeatures,
      };

      await modelAPI.train(trainData);
      setSuccess('Model trained successfully!');
      setModelName('');
      setTargetVariable('');
      setSelectedFeatures([]);
      onModelTrained();
    } catch (err) {
      setError(err.response?.data?.message || 'Training failed');
    } finally {
      setTraining(false);
    }
  };

  const availableFeatures = datasetDetails?.headers?.filter(
    header => header !== targetVariable
  ) || [];

  return (
    <Box>
      <Typography variant="h5" gutterBottom>
        Train Machine Learning Model
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
        {/* Configuration Panel */}
        <Grid item xs={12} md={6}>
          <Paper sx={{ p: 3 }}>
            <Typography variant="h6" gutterBottom>
              Model Configuration
            </Typography>

            <FormControl fullWidth sx={{ mb: 2 }}>
              <InputLabel>Select Dataset</InputLabel>
              <Select
                value={selectedDataset}
                onChange={(e) => setSelectedDataset(e.target.value)}
                label="Select Dataset"
              >
                {datasets.map((dataset) => (
                  <MenuItem key={dataset.id} value={dataset.id}>
                    {dataset.fileName} ({dataset.rowCount} rows)
                  </MenuItem>
                ))}
              </Select>
            </FormControl>

            {selectedDataset && (
              <>
                <TextField
                  fullWidth
                  label="Model Name"
                  value={modelName}
                  onChange={(e) => setModelName(e.target.value)}
                  sx={{ mb: 2 }}
                />

                <FormControl fullWidth sx={{ mb: 2 }}>
                  <InputLabel>Model Type</InputLabel>
                  <Select
                    value={modelType}
                    onChange={(e) => setModelType(e.target.value)}
                    label="Model Type"
                  >
                    <MenuItem value="CLASSIFICATION">Classification</MenuItem>
                    <MenuItem value="REGRESSION">Regression</MenuItem>
                  </Select>
                </FormControl>

                <FormControl fullWidth sx={{ mb: 2 }}>
                  <InputLabel>Target Variable</InputLabel>
                  <Select
                    value={targetVariable}
                    onChange={(e) => setTargetVariable(e.target.value)}
                    label="Target Variable"
                  >
                    {datasetDetails?.headers?.map((header) => (
                      <MenuItem key={header} value={header}>
                        {header}
                      </MenuItem>
                    ))}
                  </Select>
                </FormControl>

                <Button
                  variant="contained"
                  onClick={handleTrain}
                  disabled={training || !modelName || !targetVariable || selectedFeatures.length === 0}
                  startIcon={training ? <CircularProgress size={20} /> : <Psychology />}
                  fullWidth
                >
                  {training ? 'Training...' : 'Train Model'}
                </Button>
              </>
            )}
          </Paper>
        </Grid>

        {/* Feature Selection Panel */}
        <Grid item xs={12} md={6}>
          <Paper sx={{ p: 3 }}>
            <Typography variant="h6" gutterBottom>
              Feature Selection
            </Typography>

            {selectedDataset && targetVariable ? (
              <>
                <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
                  Select features to use for training (excluding target variable)
                </Typography>

                <List dense>
                  {availableFeatures.map((feature) => (
                    <ListItem key={feature}>
                      <ListItemText primary={feature} />
                      <ListItemSecondaryAction>
                        <Checkbox
                          checked={selectedFeatures.includes(feature)}
                          onChange={() => handleFeatureToggle(feature)}
                        />
                      </ListItemSecondaryAction>
                    </ListItem>
                  ))}
                </List>

                {selectedFeatures.length > 0 && (
                  <Box sx={{ mt: 2 }}>
                    <Typography variant="body2" gutterBottom>
                      Selected Features ({selectedFeatures.length}):
                    </Typography>
                    <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1 }}>
                      {selectedFeatures.map((feature) => (
                        <Chip
                          key={feature}
                          label={feature}
                          onDelete={() => handleFeatureToggle(feature)}
                          size="small"
                        />
                      ))}
                    </Box>
                  </Box>
                )}
              </>
            ) : (
              <Typography variant="body2" color="text.secondary">
                Please select a dataset and target variable to see available features
              </Typography>
            )}
          </Paper>
        </Grid>
      </Grid>

      {/* Trained Models */}
      {models.length > 0 && (
        <Paper sx={{ p: 3, mt: 3 }}>
          <Typography variant="h6" gutterBottom>
            Trained Models
          </Typography>
          <List>
            {models.map((model) => (
              <ListItem key={model.id}>
                <ListItemText
                  primary={model.modelName}
                  secondary={`Type: ${model.modelType} | Target: ${model.targetVariable} | Accuracy: ${model.accuracy ? (model.accuracy * 100).toFixed(2) + '%' : 'N/A'}`}
                />
              </ListItem>
            ))}
          </List>
        </Paper>
      )}
    </Box>
  );
};

export default ModelTrainer;
