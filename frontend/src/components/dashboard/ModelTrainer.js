/**
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
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
} from '@mui/material';
import {
  Psychology,
  Delete,
} from '@mui/icons-material';
import { modelAPI, datasetAPI } from '../../api/api';

const ModelTrainer = ({ datasets, models = [], onModelTrained, loading }) => {
  const [selectedDataset, setSelectedDataset] = useState('');
  const [datasetDetails, setDatasetDetails] = useState(null);
  const [modelName, setModelName] = useState('');
  const [modelType, setModelType] = useState('CLASSIFICATION');
  const [targetVariable, setTargetVariable] = useState('');
  const [selectedFeatures, setSelectedFeatures] = useState([]);

  // Hyperparameters
  const [epochs, setEpochs] = useState(10);
  const [learningRate, setLearningRate] = useState(0.1);
  const [batchSize, setBatchSize] = useState(32);

  const [training, setTraining] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [deleteDialog, setDeleteDialog] = useState({ open: false, model: null });

  useEffect(() => {
    if (selectedDataset) {
      fetchDatasetDetails();
    } else {
      setDatasetDetails(null);
      setTargetVariable('');
      setSelectedFeatures([]);
    }
  }, [selectedDataset]);

  // Remove target variable from selected features when it changes
  useEffect(() => {
    if (targetVariable && selectedFeatures.includes(targetVariable)) {
      setSelectedFeatures(prev => prev.filter(f => f !== targetVariable));
    }
  }, [targetVariable]);

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
      // Filter out target variable from features (in case it was accidentally included)
      const filteredFeatures = selectedFeatures.filter(feature => feature !== targetVariable);

      if (filteredFeatures.length === 0) {
        setError('Please select at least one feature (excluding the target variable)');
        setTraining(false);
        return;
      }

      const trainData = {
        datasetId: parseInt(selectedDataset),
        modelName,
        modelType,
        targetVariable,
        featureNames: filteredFeatures,
        epochs: parseInt(epochs),
        learningRate: parseFloat(learningRate),
        batchSize: parseInt(batchSize)
      };

      // Add timeout (5 minutes for training)
      const timeoutPromise = new Promise((_, reject) =>
        setTimeout(() => reject(new Error('Training request timed out after 5 minutes. Please try again.')), 300000)
      );

      const response = await Promise.race([modelAPI.train(trainData), timeoutPromise]);
      setSuccess('Model trained successfully!');
      setModelName('');
      setTargetVariable('');
      setSelectedFeatures([]);
      onModelTrained(); // This refreshes the models list
    } catch (err) {
      console.error('Training error:', err);
      console.error('Error response:', err.response);

      // Extract error message from various possible locations
      let errorMessage = 'Training failed';
      if (err.response?.data) {
        // Try different paths for error message
        errorMessage = err.response.data.message ||
          err.response.data.data?.userMessage ||
          err.response.data.data?.message ||
          err.response.data.error ||
          errorMessage;
      } else if (err.message) {
        errorMessage = err.message;
      }

      setError(errorMessage);
    } finally {
      setTraining(false);
    }
  };

  const handleDelete = async (model) => {
    try {
      await modelAPI.delete(model.id);
      setSuccess('Model deleted successfully!');
      onModelTrained(); // Refresh the models list
      setDeleteDialog({ open: false, model: null });
    } catch (err) {
      console.error('Delete error:', err);
      const errorMessage = err.response?.data?.message ||
        err.response?.data?.data?.userMessage ||
        err.message ||
        'Failed to delete model';
      setError(errorMessage);
      setDeleteDialog({ open: false, model: null });
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

                {/* Hyperparameters Section */}
                <Typography variant="subtitle1" gutterBottom sx={{ mt: 2 }}>
                  Hyperparameters
                </Typography>

                <Grid container spacing={2}>
                  <Grid item xs={4}>
                    <TextField
                      fullWidth
                      label="Epochs"
                      type="number"
                      value={epochs}
                      onChange={(e) => setEpochs(e.target.value)}
                      inputProps={{ min: 1, max: 1000 }}
                      sx={{ mb: 2 }}
                    />
                  </Grid>
                  <Grid item xs={4}>
                    <TextField
                      fullWidth
                      label="Learning Rate"
                      type="number"
                      value={learningRate}
                      onChange={(e) => setLearningRate(e.target.value)}
                      inputProps={{ min: 0.0001, max: 1.0, step: 0.001 }}
                      sx={{ mb: 2 }}
                    />
                  </Grid>
                  <Grid item xs={4}>
                    <TextField
                      fullWidth
                      label="Batch Size"
                      type="number"
                      value={batchSize}
                      onChange={(e) => setBatchSize(e.target.value)}
                      inputProps={{ min: 1, step: 1 }}
                      sx={{ mb: 2 }}
                    />
                  </Grid>
                </Grid>

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

      {/* Trained Models - Always show for debugging */}
      <Paper sx={{ p: 3, mt: 3 }}>
        <Typography variant="h6" gutterBottom>
          Trained Models
        </Typography>
        {(() => {
          console.log('Models in ModelTrainer:', models);
          console.log('Models type:', typeof models);
          console.log('Is array?', Array.isArray(models));
          console.log('Models length:', models?.length);
          return null;
        })()}
        {models && Array.isArray(models) ? (
          models.length > 0 ? (
            <List>
              {models.map((model) => (
                <ListItem key={model.id}>
                  <ListItemText
                    primary={model.modelName}
                    secondary={`Type: ${model.modelType} | Target: ${model.targetVariable} | Accuracy: ${model.accuracy !== null && model.accuracy !== undefined ? (model.accuracy * 100).toFixed(2) + '%' : 'N/A'}`}
                  />
                  <ListItemSecondaryAction>
                    <IconButton
                      edge="end"
                      color="error"
                      onClick={() => setDeleteDialog({ open: true, model })}
                      aria-label="delete"
                    >
                      <Delete />
                    </IconButton>
                  </ListItemSecondaryAction>
                </ListItem>
              ))}
            </List>
          ) : (
            <Typography variant="body2" color="text.secondary">
              No models trained yet. Train a model to see it here.
            </Typography>
          )
        ) : (
          <Typography variant="body2" color="error">
            Error: Models data is not an array. Type: {typeof models}, Value: {JSON.stringify(models)}
          </Typography>
        )}
      </Paper>

      {/* Delete Confirmation Dialog */}
      <Dialog
        open={deleteDialog.open}
        onClose={() => setDeleteDialog({ open: false, model: null })}
      >
        <DialogTitle>Delete Model</DialogTitle>
        <DialogContent>
          <Typography>
            Are you sure you want to delete "{deleteDialog.model?.modelName}"?
            This action cannot be undone.
          </Typography>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDeleteDialog({ open: false, model: null })}>
            Cancel
          </Button>
          <Button
            onClick={() => handleDelete(deleteDialog.model)}
            color="error"
            variant="contained"
          >
            Delete
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default ModelTrainer;
