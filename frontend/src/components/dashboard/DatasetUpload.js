/**
 * @Author: Mukhil Sundararaj
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
} from '@mui/icons-material';
import { datasetsAPI } from '../../api/datasets';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { toast } from 'sonner';

const DatasetUpload = ({ onDatasetUploaded }) => {
  const queryClient = useQueryClient();
  const [file, setFile] = useState(null);
  const [deleteDialog, setDeleteDialog] = useState({ open: false, dataset: null });

  // Fetch datasets using React Query
  const { data: datasets = [], isLoading: loading, error: fetchError } = useQuery({
    queryKey: ['datasets'],
    queryFn: () => datasetsAPI.getAll(),
    select: (response) => {
      // Handle different response structures
      if (Array.isArray(response)) return response;
      if (response?.content) return response.content;
      if (response?.data) return Array.isArray(response.data) ? response.data : [];
      return [];
    },
  });

  // Upload mutation
  const uploadMutation = useMutation({
    mutationFn: (file) => datasetsAPI.upload(file),
    onSuccess: () => {
      toast.success('Dataset uploaded successfully!');
      setFile(null);
      queryClient.invalidateQueries({ queryKey: ['datasets'] });
      if (onDatasetUploaded) onDatasetUploaded();
    },
    onError: (error) => {
      toast.error(error.response?.data?.message || 'Upload failed');
    },
  });

  // Delete mutation
  const deleteMutation = useMutation({
    mutationFn: (id) => datasetsAPI.delete(id),
    onSuccess: () => {
      toast.success('Dataset deleted successfully!');
      queryClient.invalidateQueries({ queryKey: ['datasets'] });
      setDeleteDialog({ open: false, dataset: null });
      if (onDatasetUploaded) onDatasetUploaded();
    },
    onError: (error) => {
      toast.error(error.response?.data?.message || 'Delete failed');
    },
  });

  const handleFileChange = (event) => {
    const selectedFile = event.target.files[0];
    if (selectedFile) {
      if (selectedFile.type !== 'text/csv' && !selectedFile.name.endsWith('.csv')) {
        toast.error('Please select a CSV file');
        return;
      }
      setFile(selectedFile);
    }
  };

  const handleUpload = () => {
    if (!file) {
      toast.error('Please select a file');
      return;
    }
    uploadMutation.mutate(file);
  };

  const handleDelete = (dataset) => {
    if (dataset?.id) {
      deleteMutation.mutate(dataset.id);
    }
  };

  const formatDate = (dateString) => {
    return new Date(dateString).toLocaleDateString();
  };

  return (
    <Box>
      <Typography variant="h5" gutterBottom>
        Dataset Management
      </Typography>

      {fetchError && (
        <Alert severity="error" sx={{ mb: 2 }}>
          {fetchError.response?.data?.message || 'Failed to load datasets'}
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
              disabled={uploadMutation.isPending}
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
            disabled={!file || uploadMutation.isPending}
            startIcon={uploadMutation.isPending ? <CircularProgress size={20} /> : <CloudUpload />}
          >
            {uploadMutation.isPending ? 'Uploading...' : 'Upload'}
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
                datasets && Array.isArray(datasets) && datasets.map((dataset) => (
                  <TableRow key={dataset.id}>
                    <TableCell>{dataset.fileName || dataset.originalFilename || dataset.name || 'Unknown'}</TableCell>
                    <TableCell>{formatDate(dataset.createdAt || dataset.uploadDate)}</TableCell>
                    <TableCell>{dataset.rowCount || 0}</TableCell>
                    <TableCell>{dataset.columnCount || dataset.headers?.length || 0}</TableCell>
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
            Are you sure you want to delete "{deleteDialog.dataset?.fileName || deleteDialog.dataset?.originalFilename || deleteDialog.dataset?.name}"? 
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
