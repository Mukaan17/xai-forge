/**
 * Enhanced Dashboard Component with real API integration
 * Uses React Query for data fetching and Zustand for state management
 */
import React from 'react';
import { useQuery } from '@tanstack/react-query';
import {
  Container,
  Grid,
  Paper,
  Typography,
  Box,
  Card,
  CardContent,
  CircularProgress,
  Alert,
} from '@mui/material';
import {
  Dataset as DatasetIcon,
  Psychology as ModelIcon,
  TrendingUp as PredictionIcon,
  Assessment as AccuracyIcon,
} from '@mui/icons-material';
import { dashboardAPI } from '../api/dashboard';
import { formatNumber, formatPercentage } from '../utils';

const Dashboard = () => {
  // Fetch dashboard summary
  const { data: summary, isLoading: summaryLoading, error: summaryError } = useQuery({
    queryKey: ['dashboard', 'summary'],
    queryFn: () => dashboardAPI.getSummary(),
  });

  // Fetch recent activity
  const { data: recentActivity = [] } = useQuery({
    queryKey: ['dashboard', 'activity'],
    queryFn: () => dashboardAPI.getRecentActivity(10),
    select: (response) => {
      if (Array.isArray(response)) return response;
      if (response?.data) return Array.isArray(response.data) ? response.data : [];
      return [];
    },
  });

  // Fetch quick stats
  const { data: quickStats } = useQuery({
    queryKey: ['dashboard', 'quickStats'],
    queryFn: () => dashboardAPI.getQuickStats(),
  });

  if (summaryLoading) {
    return (
      <Container maxWidth="lg" sx={{ mt: 4, mb: 4 }}>
        <Box display="flex" justifyContent="center" alignItems="center" minHeight="400px">
          <CircularProgress />
        </Box>
      </Container>
    );
  }

  if (summaryError) {
    return (
      <Container maxWidth="lg" sx={{ mt: 4, mb: 4 }}>
        <Alert severity="error">
          Failed to load dashboard data. Please try again later.
        </Alert>
      </Container>
    );
  }

  const kpiCards = [
    {
      title: 'Total Datasets',
      value: formatNumber(summary?.totalDatasets || 0),
      icon: <DatasetIcon sx={{ fontSize: 40 }} />,
      color: '#1976d2',
    },
    {
      title: 'Total Models',
      value: formatNumber(summary?.totalModels || 0),
      icon: <ModelIcon sx={{ fontSize: 40 }} />,
      color: '#9c27b0',
    },
    {
      title: 'Total Predictions',
      value: formatNumber(summary?.totalPredictions || 0),
      icon: <PredictionIcon sx={{ fontSize: 40 }} />,
      color: '#2e7d32',
    },
    {
      title: 'Avg. Accuracy',
      value: formatPercentage(summary?.averageModelAccuracy || 0),
      icon: <AccuracyIcon sx={{ fontSize: 40 }} />,
      color: '#f57c00',
    },
  ];

  return (
    <Container maxWidth="lg" sx={{ mt: 4, mb: 4 }}>
      <Typography variant="h4" component="h1" gutterBottom>
        Dashboard
      </Typography>

      {/* KPI Cards */}
      <Grid container spacing={3} sx={{ mb: 4 }}>
        {kpiCards.map((kpi, index) => (
          <Grid item xs={12} sm={6} md={3} key={index}>
            <Card>
              <CardContent>
                <Box display="flex" justifyContent="space-between" alignItems="center">
                  <Box>
                    <Typography color="textSecondary" gutterBottom variant="body2">
                      {kpi.title}
                    </Typography>
                    <Typography variant="h4" component="div">
                      {kpi.value}
                    </Typography>
                  </Box>
                  <Box sx={{ color: kpi.color }}>
                    {kpi.icon}
                  </Box>
                </Box>
              </CardContent>
            </Card>
          </Grid>
        ))}
      </Grid>

      {/* Recent Activity */}
      <Grid container spacing={3}>
        <Grid item xs={12} md={8}>
          <Paper sx={{ p: 3 }}>
            <Typography variant="h6" gutterBottom>
              Recent Activity
            </Typography>
            {recentActivity.length > 0 ? (
              <Box>
                {recentActivity.slice(0, 5).map((activity, index) => (
                  <Box key={index} sx={{ py: 1, borderBottom: index < 4 ? '1px solid #eee' : 'none' }}>
                    <Typography variant="body2">
                      <strong>{activity.title}</strong> - {activity.subtitle}
                    </Typography>
                    <Typography variant="caption" color="textSecondary">
                      {activity.timestamp ? new Date(activity.timestamp).toLocaleString() : ''}
                    </Typography>
                  </Box>
                ))}
              </Box>
            ) : (
              <Typography variant="body2" color="textSecondary">
                No recent activity
              </Typography>
            )}
          </Paper>
        </Grid>

        {/* Quick Stats */}
        <Grid item xs={12} md={4}>
          <Paper sx={{ p: 3 }}>
            <Typography variant="h6" gutterBottom>
              Quick Stats
            </Typography>
            {quickStats && (
              <Box>
                <Typography variant="body2" sx={{ mb: 1 }}>
                  Predictions Today: <strong>{formatNumber(quickStats.predictionsToday || 0)}</strong>
                </Typography>
                <Typography variant="body2" sx={{ mb: 1 }}>
                  Models in Training: <strong>{formatNumber(quickStats.modelsInTraining || 0)}</strong>
                </Typography>
                <Typography variant="body2">
                  Storage Used: <strong>{formatFileSize(quickStats.storageUsedBytes || 0)}</strong>
                </Typography>
              </Box>
            )}
          </Paper>
        </Grid>
      </Grid>
    </Container>
  );
};

// Helper function for file size formatting
function formatFileSize(bytes) {
  if (!bytes || bytes === 0) return '0 B';
  const k = 1024;
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB'];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  return `${parseFloat((bytes / Math.pow(k, i)).toFixed(2))} ${sizes[i]}`;
}

export default Dashboard;
