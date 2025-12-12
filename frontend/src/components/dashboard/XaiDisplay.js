/**
 * @Author: Mukhil Sundararaj
 * @Date:   2025-09-04 16:11:00
 * @Last Modified by:   Mukhil Sundararaj
 * @Last Modified time: 2025-10-24 18:36:22
 */
import React from 'react';
import {
  Paper,
  Typography,
  Box,
  Card,
  CardContent,
  Chip,
  List,
  ListItem,
  ListItemText,
  ListItemIcon,
} from '@mui/material';
import {
  TrendingUp,
  TrendingDown,
  Psychology,
  Visibility,
} from '@mui/icons-material';
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  BarElement,
  Title,
  Tooltip,
  Legend,
} from 'chart.js';
import { Bar } from 'react-chartjs-2';

ChartJS.register(
  CategoryScale,
  LinearScale,
  BarElement,
  Title,
  Tooltip,
  Legend
);

const XaiDisplay = ({ prediction, explanation, modelType }) => {
  // Handle different explanation structures
  const featureImportances = explanation?.featureImportances || explanation?.featureContributions || [];
  const explanationText = explanation?.explanationText || explanation?.summary || explanation?.explanationSummary || 'No explanation available.';

  const getContributionColor = (value) => {
    return value >= 0 ? '#4caf50' : '#f44336';
  };

  const getContributionIcon = (value) => {
    return value >= 0 ? <TrendingUp /> : <TrendingDown />;
  };

  // Prepare chart data from feature importances
  const chartData = featureImportances.length > 0 ? {
    labels: featureImportances.map(fi => {
      if (typeof fi === 'object' && fi !== null) {
        return fi.featureName || fi.feature || fi.name || 'Unknown';
      }
      return 'Unknown';
    }),
    datasets: [
      {
        label: 'Feature Importance',
        data: featureImportances.map(fi => {
          if (typeof fi === 'object' && fi !== null) {
            return fi.importance || fi.contribution || fi.value || 0;
          }
          return typeof fi === 'number' ? fi : 0;
        }),
        backgroundColor: featureImportances.map(fi => {
          const value = typeof fi === 'object' && fi !== null
            ? (fi.importance || fi.contribution || fi.value || 0)
            : (typeof fi === 'number' ? fi : 0);
          return getContributionColor(value);
        }),
        borderColor: featureImportances.map(fi => {
          const value = typeof fi === 'object' && fi !== null
            ? (fi.importance || fi.contribution || fi.value || 0)
            : (typeof fi === 'number' ? fi : 0);
          return getContributionColor(value);
        }),
        borderWidth: 1,
      },
    ],
  } : null;

  const chartOptions = {
    responsive: true,
    plugins: {
      legend: {
        position: 'top',
      },
      title: {
        display: true,
        text: 'Feature Contributions to Prediction',
      },
    },
    scales: {
      y: {
        beginAtZero: true,
      },
    },
  };

  return (
    <Box>
      {/* Prediction Result */}
      <Paper sx={{ p: 3, mb: 3 }}>
        <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
          <Psychology sx={{ mr: 1 }} />
          <Typography variant="h6">
            Prediction Result
          </Typography>
        </Box>
        
        <Card sx={{ mb: 2 }}>
          <CardContent>
            <Typography variant="h4" color="primary" gutterBottom>
              {prediction.predictionResult || prediction.prediction || 'N/A'}
            </Typography>
            {prediction.confidence !== undefined && prediction.confidence !== null && (
              <Typography variant="body1" color="text.secondary">
                Confidence: {(prediction.confidence * 100).toFixed(2)}%
              </Typography>
            )}
          </CardContent>
        </Card>

        {prediction.probabilities && (
          <Box>
            <Typography variant="h6" gutterBottom>
              Class Probabilities
            </Typography>
            <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1 }}>
              {Object.entries(prediction.probabilities).map(([className, probability]) => (
                <Chip
                  key={className}
                  label={`${className}: ${(probability * 100).toFixed(1)}%`}
                  color={className === (prediction.predictionResult || prediction.prediction) ? 'primary' : 'default'}
                />
              ))}
            </Box>
          </Box>
        )}
      </Paper>

      {/* Explanation */}
      <Paper sx={{ p: 3, mb: 3 }}>
        <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
          <Visibility sx={{ mr: 1 }} />
          <Typography variant="h6">
            Model Explanation
          </Typography>
        </Box>

        <Typography variant="body1" paragraph>
          {explanationText}
        </Typography>

        {/* Feature Contributions Chart */}
        {chartData && (
          <Box sx={{ mb: 3 }}>
            <Typography variant="h6" gutterBottom>
              Feature Contributions
            </Typography>
            <Bar data={chartData} options={chartOptions} />
          </Box>
        )}

        {/* Feature Contributions List */}
        {featureImportances.length > 0 && (
          <>
            <Typography variant="h6" gutterBottom>
              Detailed Feature Analysis
            </Typography>
            <List>
              {featureImportances.map((fi, index) => {
                const featureName = typeof fi === 'object' && fi !== null
                  ? (fi.featureName || fi.feature || fi.name || 'Unknown')
                  : 'Unknown';
                const value = typeof fi === 'object' && fi !== null
                  ? (fi.importance || fi.contribution || fi.value || 0)
                  : (typeof fi === 'number' ? fi : 0);
                const direction = value >= 0 ? 'positive' : 'negative';
                
                return (
                  <ListItem key={index}>
                    <ListItemIcon>
                      {getContributionIcon(value)}
                    </ListItemIcon>
                    <ListItemText
                      primary={featureName}
                      secondary={`${direction} impact: ${value.toFixed(4)}`}
                    />
                    <Chip
                      label={direction}
                      color={direction === 'positive' ? 'success' : 'error'}
                      size="small"
                    />
                  </ListItem>
                );
              })}
            </List>
          </>
        )}
      </Paper>

      {/* Input Data Summary */}
      {explanation?.inputData && (
        <Paper sx={{ p: 3 }}>
          <Typography variant="h6" gutterBottom>
            Input Data Summary
          </Typography>
          <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1 }}>
            {Object.entries(explanation.inputData).map(([feature, value]) => (
              <Chip
                key={feature}
                label={`${feature}: ${value}`}
                variant="outlined"
              />
            ))}
          </Box>
        </Paper>
      )}
    </Box>
  );
};

export default XaiDisplay;
