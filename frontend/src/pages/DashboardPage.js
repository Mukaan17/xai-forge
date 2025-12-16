/**
 * @Date:   2025-09-04 16:09:53
 * @Last Modified by:   Mukhil Sundararaj
 * @Last Modified time: 2025-10-24 18:36:13
 */
import React, { useState, useEffect } from 'react';
import {
  Container,
  Typography,
  Box,
  Tabs,
  Tab,
  Alert,
} from '@mui/material';
import DatasetUpload from '../components/dashboard/DatasetUpload';
import ModelTrainer from '../components/dashboard/ModelTrainer';
import Predictor from '../components/dashboard/Predictor';
import { datasetAPI, modelAPI } from '../api/api';

const DashboardPage = () => {
  const [activeTab, setActiveTab] = useState(0);
  const [datasets, setDatasets] = useState([]);
  const [models, setModels] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const fetchData = async () => {
    setLoading(true);
    setError('');
    try {
      const [datasetsResponse, modelsResponse] = await Promise.all([
        datasetAPI.getAll(),
        modelAPI.getAll(),
      ]);
      // Ensure we always set arrays, even if response.data is null or undefined
      setDatasets(Array.isArray(datasetsResponse?.data) ? datasetsResponse.data : []);
      setModels(Array.isArray(modelsResponse?.data) ? modelsResponse.data : []);
    } catch (err) {
      setError('Failed to load data: ' + (err.response?.data?.message || err.message));
      // Ensure models and datasets are always arrays even on error
      setModels([]);
      setDatasets([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, []);

  const handleTabChange = (event, newValue) => {
    setActiveTab(newValue);
  };

  const handleDatasetUploaded = () => {
    fetchData(); // Refresh datasets
  };

  const handleModelTrained = () => {
    fetchData(); // Refresh models
  };

  const TabPanel = ({ children, value, index, ...other }) => (
    <div
      role="tabpanel"
      hidden={value !== index}
      id={`dashboard-tabpanel-${index}`}
      aria-labelledby={`dashboard-tab-${index}`}
      {...other}
    >
      {value === index && <Box sx={{ p: 3 }}>{children}</Box>}
    </div>
  );

  return (
    <Container maxWidth="lg" sx={{ mt: 4, mb: 4 }}>
      <Typography variant="h4" component="h1" gutterBottom>
        Dashboard
      </Typography>
      
      {error && (
        <Alert severity="error" sx={{ mb: 2 }}>
          {error}
        </Alert>
      )}

      <Box sx={{ borderBottom: 1, borderColor: 'divider' }}>
        <Tabs value={activeTab} onChange={handleTabChange}>
          <Tab label="Datasets" />
          <Tab label="Train Model" />
          <Tab label="Make Predictions" />
        </Tabs>
      </Box>

      <TabPanel value={activeTab} index={0}>
        <DatasetUpload
          datasets={datasets}
          onDatasetUploaded={handleDatasetUploaded}
          loading={loading}
        />
      </TabPanel>

      <TabPanel value={activeTab} index={1}>
        <ModelTrainer
          datasets={datasets}
          models={models}
          onModelTrained={handleModelTrained}
          loading={loading}
        />
      </TabPanel>

      <TabPanel value={activeTab} index={2}>
        <Predictor
          models={models}
          loading={loading}
        />
      </TabPanel>
    </Container>
  );
};

export default DashboardPage;
