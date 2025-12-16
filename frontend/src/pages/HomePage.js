/**
 * @Date:   2025-09-04 16:09:36
 * @Last Modified by:   Mukhil Sundararaj
 * @Last Modified time: 2025-10-24 18:36:12
 */
import React from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Container,
  Typography,
  Button,
  Box,
  Grid,
  Card,
  CardContent,
  CardActions,
} from '@mui/material';
import {
  CloudUpload,
  Psychology,
  Visibility,
  Security,
} from '@mui/icons-material';
import { useAuth } from '../contexts/AuthContext';

const HomePage = () => {
  const navigate = useNavigate();
  const { isAuthenticated } = useAuth();

  const features = [
    {
      icon: <CloudUpload sx={{ fontSize: 40 }} />,
      title: 'Dataset Management',
      description: 'Upload and manage your CSV datasets with ease. View headers, row counts, and organize your data.',
    },
    {
      icon: <Psychology sx={{ fontSize: 40 }} />,
      title: 'ML Model Training',
      description: 'Train classification and regression models using state-of-the-art algorithms with Tribuo.',
    },
    {
      icon: <Visibility sx={{ fontSize: 40 }} />,
      title: 'Explainable AI',
      description: 'Get human-understandable explanations for model predictions using LIME (Local Interpretable Model-agnostic Explanations).',
    },
    {
      icon: <Security sx={{ fontSize: 40 }} />,
      title: 'Secure & Private',
      description: 'Your data and models are secure with JWT authentication and user-specific access controls.',
    },
  ];

  return (
    <Container maxWidth="lg">
      <Box sx={{ py: 8 }}>
        {/* Hero Section */}
        <Box textAlign="center" sx={{ mb: 8 }}>
          <Typography variant="h2" component="h1" gutterBottom>
            Explainable AI Platform
          </Typography>
          <Typography variant="h5" color="text.secondary" paragraph>
            Train machine learning models and understand their decisions with transparent explanations
          </Typography>
          <Box sx={{ mt: 4 }}>
            {isAuthenticated ? (
              <Button
                variant="contained"
                size="large"
                onClick={() => navigate('/dashboard')}
                sx={{ mr: 2 }}
              >
                Go to Dashboard
              </Button>
            ) : (
              <>
                <Button
                  variant="contained"
                  size="large"
                  onClick={() => navigate('/register')}
                  sx={{ mr: 2 }}
                >
                  Get Started
                </Button>
                <Button
                  variant="outlined"
                  size="large"
                  onClick={() => navigate('/login')}
                >
                  Sign In
                </Button>
              </>
            )}
          </Box>
        </Box>

        {/* Features Section */}
        <Typography variant="h3" component="h2" textAlign="center" gutterBottom>
          Features
        </Typography>
        <Grid container spacing={4} sx={{ mt: 2 }}>
          {features.map((feature, index) => (
            <Grid item xs={12} md={6} key={index}>
              <Card sx={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
                <CardContent sx={{ flexGrow: 1 }}>
                  <Box textAlign="center" sx={{ mb: 2 }}>
                    {feature.icon}
                  </Box>
                  <Typography variant="h5" component="h3" gutterBottom>
                    {feature.title}
                  </Typography>
                  <Typography variant="body1" color="text.secondary">
                    {feature.description}
                  </Typography>
                </CardContent>
              </Card>
            </Grid>
          ))}
        </Grid>

        {/* Technology Stack */}
        <Box sx={{ mt: 8, textAlign: 'center' }}>
          <Typography variant="h3" component="h2" gutterBottom>
            Built With Modern Technology
          </Typography>
          <Typography variant="h6" color="text.secondary" paragraph>
            Backend: Java 17, Spring Boot 3, Spring Security, PostgreSQL, Tribuo ML
          </Typography>
          <Typography variant="h6" color="text.secondary">
            Frontend: React 18, Material-UI, Chart.js, Axios
          </Typography>
        </Box>
      </Box>
    </Container>
  );
};

export default HomePage;
