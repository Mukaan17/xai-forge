/**
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
  const getContributionColor = (contribution) => {
    return contribution.direction === 'positive' ? '#4caf50' : '#f44336';
  };

  const getContributionIcon = (contribution) => {
    return contribution.direction === 'positive' ? <TrendingUp /> : <TrendingDown />;
  };

  const chartData = {
    labels: explanation.featureContributions.map(fc => fc.featureName),
    datasets: [
      {
        label: 'Feature Contribution',
        data: explanation.featureContributions.map(fc => fc.contribution),
        backgroundColor: explanation.featureContributions.map(fc => getContributionColor(fc)),
        borderColor: explanation.featureContributions.map(fc => getContributionColor(fc)),
        borderWidth: 1,
      },
    ],
  };

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
              {prediction.prediction}
            </Typography>
            {prediction.confidence && (
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
                  color={className === prediction.prediction ? 'primary' : 'default'}
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
          {explanation.explanationText}
        </Typography>

        {/* Feature Contributions Chart */}
        <Box sx={{ mb: 3 }}>
          <Typography variant="h6" gutterBottom>
            Feature Contributions
          </Typography>
          <Bar data={chartData} options={chartOptions} />
        </Box>

        {/* Feature Contributions List */}
        <Typography variant="h6" gutterBottom>
          Detailed Feature Analysis
        </Typography>
        <List>
          {explanation.featureContributions.map((contribution, index) => (
            <ListItem key={index}>
              <ListItemIcon>
                {getContributionIcon(contribution)}
              </ListItemIcon>
              <ListItemText
                primary={contribution.featureName}
                secondary={`${contribution.direction} impact: ${contribution.contribution.toFixed(4)}`}
              />
              <Chip
                label={contribution.direction}
                color={contribution.direction === 'positive' ? 'success' : 'error'}
                size="small"
              />
            </ListItem>
          ))}
        </List>
      </Paper>

      {/* Input Data Summary */}
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
    </Box>
  );
};

export default XaiDisplay;
