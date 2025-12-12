/**
 * @Author: Mukhil Sundararaj
 * @Date:   2025-09-04 16:09:53
 * @Last Modified by:   Mukhil Sundararaj
 * @Last Modified time: 2025-10-24 18:36:13
 */
import React, { useState } from 'react';
import {
  Container,
  Typography,
  Box,
  Tabs,
  Tab,
} from '@mui/material';
import DatasetUpload from '../components/dashboard/DatasetUpload';
import ModelTrainer from '../components/dashboard/ModelTrainer';
import Predictor from '../components/dashboard/Predictor';

const DashboardPage = () => {
  const [activeTab, setActiveTab] = useState(0);

  const handleTabChange = (event, newValue) => {
    setActiveTab(newValue);
  };

  const handleDatasetUploaded = () => {
    // React Query will automatically refetch
  };

  const handleModelTrained = () => {
    // React Query will automatically refetch
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
      

      <Box sx={{ borderBottom: 1, borderColor: 'divider' }}>
        <Tabs value={activeTab} onChange={handleTabChange}>
          <Tab label="Datasets" />
          <Tab label="Train Model" />
          <Tab label="Make Predictions" />
        </Tabs>
      </Box>

      <TabPanel value={activeTab} index={0}>
        <DatasetUpload
          onDatasetUploaded={handleDatasetUploaded}
        />
      </TabPanel>

      <TabPanel value={activeTab} index={1}>
        <ModelTrainer
          onModelTrained={handleModelTrained}
        />
      </TabPanel>

      <TabPanel value={activeTab} index={2}>
        <Predictor />
      </TabPanel>
    </Container>
  );
};

export default DashboardPage;
