/**
 * @Date:   2025-09-04 16:10:10
 * @Last Modified by:   Mukhil Sundararaj
 * @Last Modified time: 2025-10-24 18:36:25
 */
import React, { useState } from 'react';
import {
  Box,
  Typography,
  Button,
  Paper,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  IconButton,
  Alert,
  CircularProgress,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
} from '@mui/material';
import {
  CloudUpload,
  Delete,
  Visibility,
} from '@mui/icons-material';
import { datasetAPI } from '../../api/api';

const DatasetUpload = ({ datasets, onDatasetUploaded, loading }) => {
  const [file, setFile] = useState(null);
  const [uploading, setUploading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [deleteDialog, setDeleteDialog] = useState({ open: false, dataset: null });

  const handleFileChange = (event) => {
    const selectedFile = event.target.files[0];
    if (selectedFile) {
      if (selectedFile.type !== 'text/csv' && !selectedFile.name.endsWith('.csv')) {
        setError('Please select a CSV file');
        return;
      }
      setFile(selectedFile);
      setError('');
    }
  };

  const handleUpload = async () => {
    if (!file) {
      setError('Please select a file');
      return;
    }

    setUploading(true);
    setError('');
    setSuccess('');

    try {
      await datasetAPI.upload(file);
      setSuccess('Dataset uploaded successfully!');
      setFile(null);
      onDatasetUploaded();
    } catch (err) {
      setError(err.response?.data?.message || 'Upload failed');
    } finally {
      setUploading(false);
    }
  };

  const handleDelete = async (dataset) => {
    try {
      await datasetAPI.delete(dataset.id);
      setSuccess('Dataset deleted successfully!');
      onDatasetUploaded();
    } catch (err) {
      setError(err.response?.data?.message || 'Delete failed');
    }
    setDeleteDialog({ open: false, dataset: null });
  };

  const formatDate = (dateString) => {
    return new Date(dateString).toLocaleDateString();
  };

  return (
    <Box>
      <Typography variant="h5" gutterBottom>
        Dataset Management
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

      {/* Upload Section */}
      <Paper sx={{ p: 3, mb: 3 }}>
        <Typography variant="h6" gutterBottom>
          Upload New Dataset
        </Typography>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
          <input
            accept=".csv"
            style={{ display: 'none' }}
            id="file-upload"
            type="file"
            onChange={handleFileChange}
          />
          <label htmlFor="file-upload">
            <Button
              variant="outlined"
              component="span"
              startIcon={<CloudUpload />}
              disabled={uploading}
            >
              Choose CSV File
            </Button>
          </label>
          {file && (
            <Typography variant="body2" color="text.secondary">
              Selected: {file.name}
            </Typography>
          )}
          <Button
            variant="contained"
            onClick={handleUpload}
            disabled={!file || uploading}
            startIcon={uploading ? <CircularProgress size={20} /> : <CloudUpload />}
          >
            {uploading ? 'Uploading...' : 'Upload'}
          </Button>
        </Box>
      </Paper>

      {/* Datasets Table */}
      <Paper>
        <TableContainer>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell>File Name</TableCell>
                <TableCell>Upload Date</TableCell>
                <TableCell>Rows</TableCell>
                <TableCell>Columns</TableCell>
                <TableCell>Actions</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {loading ? (
                <TableRow>
                  <TableCell colSpan={5} align="center">
                    <CircularProgress />
                  </TableCell>
                </TableRow>
              ) : datasets.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={5} align="center">
                    No datasets uploaded yet
                  </TableCell>
                </TableRow>
              ) : (
                datasets.map((dataset) => (
                  <TableRow key={dataset.id}>
                    <TableCell>{dataset.fileName}</TableCell>
                    <TableCell>{formatDate(dataset.uploadDate)}</TableCell>
                    <TableCell>{dataset.rowCount}</TableCell>
                    <TableCell>{dataset.headers?.length || 0}</TableCell>
                    <TableCell>
                      <IconButton
                        color="error"
                        onClick={() => setDeleteDialog({ open: true, dataset })}
                      >
                        <Delete />
                      </IconButton>
                    </TableCell>
                  </TableRow>
                ))
              )}
            </TableBody>
          </Table>
        </TableContainer>
      </Paper>

      {/* Delete Confirmation Dialog */}
      <Dialog
        open={deleteDialog.open}
        onClose={() => setDeleteDialog({ open: false, dataset: null })}
      >
        <DialogTitle>Delete Dataset</DialogTitle>
        <DialogContent>
          <Typography>
            Are you sure you want to delete "{deleteDialog.dataset?.fileName}"? 
            This action cannot be undone.
          </Typography>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDeleteDialog({ open: false, dataset: null })}>
            Cancel
          </Button>
          <Button
            onClick={() => handleDelete(deleteDialog.dataset)}
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

export default DatasetUpload;
