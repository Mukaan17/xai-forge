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
import { modelsAPI } from '../../api/models';
import { datasetsAPI } from '../../api/datasets';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { toast } from 'sonner';

const ModelTrainer = ({ onModelTrained }) => {
  const queryClient = useQueryClient();
  const [selectedDataset, setSelectedDataset] = useState('');
  const [modelName, setModelName] = useState('');
  const [modelType, setModelType] = useState('CLASSIFICATION');
  const [targetVariable, setTargetVariable] = useState('');
  const [selectedFeatures, setSelectedFeatures] = useState([]);

  // Fetch datasets
  const { data: datasets = [] } = useQuery({
    queryKey: ['datasets'],
    queryFn: () => datasetsAPI.getAll(),
    select: (response) => {
      if (Array.isArray(response)) return response;
      if (response?.content) return response.content;
      if (response?.data) return Array.isArray(response.data) ? response.data : [];
      return [];
    },
  });

  // Fetch models
  const { data: models = [] } = useQuery({
    queryKey: ['models'],
    queryFn: () => modelsAPI.getAll(),
    select: (response) => {
      if (Array.isArray(response)) return response;
      if (response?.content) return response.content;
      if (response?.data) return Array.isArray(response.data) ? response.data : [];
      return [];
    },
  });

  // Fetch dataset details when selected
  const { data: datasetDetails } = useQuery({
    queryKey: ['dataset', selectedDataset],
    queryFn: () => datasetsAPI.getById(selectedDataset),
    enabled: !!selectedDataset,
    select: (response) => response?.data || response,
  });

  // Reset form when dataset changes
  useEffect(() => {
    if (!selectedDataset) {
      setTargetVariable('');
      setSelectedFeatures([]);
    }
  }, [selectedDataset]);

  const handleFeatureToggle = (feature) => {
    setSelectedFeatures(prev => {
      if (prev.includes(feature)) {
        return prev.filter(f => f !== feature);
      } else {
        return [...prev, feature];
      }
    });
  };

  // Train mutation
  const trainMutation = useMutation({
    mutationFn: (trainData) => modelsAPI.train(trainData),
    onSuccess: () => {
      toast.success('Model training started!');
      setModelName('');
      setTargetVariable('');
      setSelectedFeatures([]);
      queryClient.invalidateQueries({ queryKey: ['models'] });
      if (onModelTrained) onModelTrained();
    },
    onError: (error) => {
      const errorMessage = error.response?.data?.message || 'Training failed';
      if (errorMessage.includes('already exists')) {
        toast.error(errorMessage + ' Please delete the existing model first.');
      } else {
        toast.error(errorMessage);
      }
    },
  });

  // Delete mutation
  const deleteMutation = useMutation({
    mutationFn: (id) => modelsAPI.delete(id),
    onSuccess: () => {
      toast.success('Model deleted successfully!');
      queryClient.invalidateQueries({ queryKey: ['models'] });
      if (onModelTrained) onModelTrained();
    },
    onError: (error) => {
      toast.error(error.response?.data?.message || 'Failed to delete model');
    },
  });

  const handleTrain = () => {
    if (!selectedDataset || !modelName || !targetVariable || selectedFeatures.length === 0) {
      toast.error('Please fill in all required fields');
      return;
    }

    const trainData = {
      datasetId: parseInt(selectedDataset),
      modelName,
      modelType,
      targetVariable,
      featureNames: selectedFeatures,
    };

    trainMutation.mutate(trainData);
  };

  const handleDeleteModel = (modelId) => {
    if (!window.confirm('Are you sure you want to delete this model? This action cannot be undone.')) {
      return;
    }
    deleteMutation.mutate(modelId);
  };

  const availableFeatures = (datasetDetails?.columnNames || datasetDetails?.headers || []).filter(
    header => header !== targetVariable
  );

  return (
    <Box>
      <Typography variant="h5" gutterBottom>
        Train Machine Learning Model
      </Typography>


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
                    {dataset.fileName || dataset.originalFilename || dataset.name} ({dataset.rowCount || 0} rows)
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
                    {(datasetDetails?.columnNames || datasetDetails?.headers || []).map((column) => (
                      <MenuItem key={column} value={column}>
                        {column}
                      </MenuItem>
                    ))}
                  </Select>
                </FormControl>

                <Button
                  variant="contained"
                  onClick={handleTrain}
                  disabled={trainMutation.isPending || !modelName || !targetVariable || selectedFeatures.length === 0}
                  startIcon={trainMutation.isPending ? <CircularProgress size={20} /> : <Psychology />}
                  fullWidth
                >
                  {trainMutation.isPending ? 'Training...' : 'Train Model'}
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
      <Paper sx={{ p: 3, mt: 3 }}>
        <Typography variant="h6" gutterBottom>
          Trained Models
          {selectedDataset && models && Array.isArray(models) && models.length > 0 && (
            <Typography variant="caption" color="text.secondary" sx={{ ml: 1 }}>
              ({models.length} total)
            </Typography>
          )}
        </Typography>
        {models.length > 0 ? (
          <>
            <List>
              {models.map((model) => (
                <ListItem 
                  key={model.id}
                  sx={{
                    backgroundColor: selectedDataset && (model.dataset?.id === parseInt(selectedDataset) || model.datasetId === parseInt(selectedDataset))
                      ? 'action.selected' 
                      : 'transparent',
                    borderRadius: 1,
                    mb: 1,
                    border: selectedDataset && (model.dataset?.id === parseInt(selectedDataset) || model.datasetId === parseInt(selectedDataset))
                      ? '2px solid' 
                      : 'none',
                    borderColor: 'primary.main'
                  }}
                >
                  <ListItemText
                    primary={
                      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                        <span>{model.name || model.modelName}</span>
                        {selectedDataset && (model.dataset?.id === parseInt(selectedDataset) || model.datasetId === parseInt(selectedDataset)) && (
                          <Chip label="For this dataset" size="small" color="primary" />
                        )}
                      </Box>
                    }
                    secondary={`Type: ${model.modelType || model.type} | Target: ${model.targetColumn || model.targetVariable} | Accuracy: ${model.accuracy ? (model.accuracy * 100).toFixed(2) + '%' : 'N/A'}`}
                  />
                  <ListItemSecondaryAction>
                    <IconButton
                      edge="end"
                      aria-label="delete"
                      onClick={() => handleDeleteModel(model.id)}
                      color="error"
                      title="Delete this model"
                    >
                      <Delete />
                    </IconButton>
                  </ListItemSecondaryAction>
                </ListItem>
              ))}
            </List>
            {selectedDataset && models.some(m => m.dataset?.id === parseInt(selectedDataset) || m.datasetId === parseInt(selectedDataset)) && (
              <Alert severity="warning" sx={{ mt: 2 }}>
                A model already exists for the selected dataset. Delete it above to train a new one.
              </Alert>
            )}
          </>
        ) : (
          <Typography variant="body2" color="text.secondary" sx={{ py: 2 }}>
            No trained models yet. Train a model using the form above.
          </Typography>
        )}
      </Paper>
    </Box>
  );
};

export default ModelTrainer;
