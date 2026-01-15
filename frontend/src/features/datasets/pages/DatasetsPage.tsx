import { useQuery } from '@tanstack/react-query';
import { apiClient } from '@/shared/lib/api/client';
import { Card, CardContent, CardHeader, CardTitle } from '@/shared/components/ui/card';
import { Button } from '@/shared/components/ui/button';
import { Database, Trash2, Eye, ChevronLeft, ChevronRight, Search, X, Download } from 'lucide-react';
import { DatasetDto } from '@/shared/types/dataset.types';
import { UploadModal } from '../components/UploadModal';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { toast } from '@/shared/lib/toast';
import { useState } from 'react';
import { Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle } from '@/shared/components/ui/dialog';
import { datasetsApi, DatasetPreviewDto } from '../api/datasetsApi';
import { PaginatedResponse } from '@/shared/types/api.types';
import { Input } from '@/shared/components/ui/input';
import { motion, AnimatePresence } from 'framer-motion';

export function DatasetsPage() {
  const [previewDataset, setPreviewDataset] = useState<DatasetDto | null>(null);
  const [previewData, setPreviewData] = useState<DatasetPreviewDto | null>(null);
  const [previewOffset, setPreviewOffset] = useState(0);
  const [isLoadingPreview, setIsLoadingPreview] = useState(false);
  const [searchQuery, setSearchQuery] = useState('');
  const [currentPage, setCurrentPage] = useState(0);
  const queryClient = useQueryClient();

  const { data: datasetsResponse, isLoading } = useQuery<PaginatedResponse<DatasetDto>>({
    queryKey: ['datasets', searchQuery, currentPage],
    queryFn: async () => {
      return datasetsApi.getAll({
        search: searchQuery || undefined,
        page: currentPage,
        size: 20,
      });
    },
  });

  const datasets = datasetsResponse?.content || [];

  const loadPreview = async (dataset: DatasetDto, offset: number = 0) => {
    setIsLoadingPreview(true);
    try {
      const data = await datasetsApi.getPreview(dataset.id, 10, offset);
      setPreviewData(data);
      setPreviewOffset(offset);
    } catch (error) {
      toast.error(`Failed to load preview: ${error instanceof Error ? error.message : 'Unknown error'}`);
    } finally {
      setIsLoadingPreview(false);
    }
  };

  const handlePreviewClick = (dataset: DatasetDto) => {
    setPreviewDataset(dataset);
    setPreviewOffset(0);
    loadPreview(dataset, 0);
  };

  const deleteMutation = useMutation({
    mutationFn: async (id: number) => {
      await apiClient.delete(`/v1/datasets/${id}`);
    },
    onSuccess: () => {
      toast.success('Dataset deleted successfully');
      queryClient.invalidateQueries({ queryKey: ['datasets', searchQuery, currentPage] });
    },
    onError: (error: Error) => {
      toast.error(`Failed to delete dataset: ${error.message}`);
    },
  });

  const handleDelete = (id: number) => {
    if (confirm('Are you sure you want to delete this dataset?')) {
      deleteMutation.mutate(id);
    }
  };

  if (isLoading) {
    return (
      <div className="p-8">
        <div className="text-muted-foreground">Loading datasets...</div>
      </div>
    );
  }

  const handleSearchChange = (value: string) => {
    setSearchQuery(value);
    setCurrentPage(0); // Reset to first page on search
  };

  return (
    <div className="p-4 sm:p-6 lg:p-8 space-y-4 sm:space-y-6">
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
        <h1 className="text-xl sm:text-2xl font-semibold">Datasets</h1>
        <UploadModal />
      </div>

      {/* Search Bar */}
      <div className="flex items-center gap-2">
        <div className="flex-1 relative">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-muted-foreground" />
          <Input
            placeholder="Search datasets by name..."
            value={searchQuery}
            onChange={(e) => handleSearchChange(e.target.value)}
            className="pl-10"
          />
          {searchQuery && (
            <Button
              variant="ghost"
              size="sm"
              className="absolute right-1 top-1/2 -translate-y-1/2 h-7 w-7 p-0"
              onClick={() => handleSearchChange('')}
            >
              <X className="h-4 w-4" />
            </Button>
          )}
        </div>
      </div>

      {!datasets || datasets.length === 0 ? (
        <Card>
          <CardContent className="py-12 text-center">
            <Database className="w-12 h-12 mx-auto mb-4 text-muted-foreground" />
            <p className="text-muted-foreground mb-4">
              {searchQuery ? 'No datasets found matching your search' : 'No datasets uploaded yet'}
            </p>
            {!searchQuery && <UploadModal />}
          </CardContent>
        </Card>
      ) : (
        <>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {datasets.map((dataset) => (
            <Card key={dataset.id} className="hover:border-primary/30 transition-colors">
              <CardHeader>
                <CardTitle className="flex items-center gap-2">
                  <Database className="w-5 h-5" />
                  {dataset.fileName}
                </CardTitle>
              </CardHeader>
              <CardContent>
                <div className="space-y-2">
                  <p className="text-sm text-muted-foreground">
                    {dataset.rowCount?.toLocaleString()} rows • {dataset.headers?.length || 0} columns
                  </p>
                  <p className="text-xs text-tertiary">
                    Uploaded {new Date(dataset.uploadDate).toLocaleDateString()}
                  </p>
                  <div className="flex gap-2 mt-4">
                    <Button
                      variant="outline"
                      size="sm"
                      onClick={() => handlePreviewClick(dataset)}
                    >
                      <Eye className="w-4 h-4 mr-2" />
                      Preview
                    </Button>
                    <Button
                      variant="outline"
                      size="sm"
                      onClick={async () => {
                        try {
                          await datasetsApi.export(dataset.id, dataset.fileName);
                          toast.success('Dataset exported successfully');
                        } catch (error) {
                          toast.error(`Failed to export dataset: ${error instanceof Error ? error.message : 'Unknown error'}`);
                        }
                      }}
                    >
                      <Download className="w-4 h-4 mr-2" />
                      Export
                    </Button>
                    <Button
                      variant="outline"
                      size="sm"
                      onClick={() => handleDelete(dataset.id)}
                      disabled={deleteMutation.isPending}
                    >
                      <Trash2 className="w-4 h-4 mr-2" />
                      Delete
                    </Button>
                  </div>
                </div>
              </CardContent>
            </Card>
            ))}
          </div>

          {/* Pagination */}
          {datasetsResponse && datasetsResponse.totalPages > 1 && (
            <motion.div
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ delay: 0.2, type: 'spring', bounce: 0.3, duration: 1.5 }}
              className="flex items-center justify-between pt-4 border-t border-border"
            >
              <div className="text-sm text-muted-foreground">
                Showing {currentPage * 20 + 1} - {Math.min((currentPage + 1) * 20, datasetsResponse.totalItems)} of {datasetsResponse.totalItems} datasets
              </div>
              <div className="flex gap-2">
                <Button
                  variant="outline"
                  size="sm"
                  onClick={() => setCurrentPage(p => Math.max(0, p - 1))}
                  disabled={!datasetsResponse.hasPrevious}
                  className="hover:bg-primary/10 hover:border-primary/30 transition-colors"
                >
                  <ChevronLeft className="w-4 h-4 mr-1" />
                  Previous
                </Button>
                <Button
                  variant="outline"
                  size="sm"
                  onClick={() => setCurrentPage(p => p + 1)}
                  disabled={!datasetsResponse.hasNext}
                  className="hover:bg-primary/10 hover:border-primary/30 transition-colors"
                >
                  Next
                  <ChevronRight className="w-4 h-4 ml-1" />
                </Button>
              </div>
            </motion.div>
          )}
        </>
      )}

      {previewDataset && (
        <Dialog open={!!previewDataset} onOpenChange={() => {
          setPreviewDataset(null);
          setPreviewData(null);
          setPreviewOffset(0);
        }}>
          <DialogContent className="max-w-4xl max-h-[85vh] overflow-y-auto">
            <DialogHeader>
              <DialogTitle className="bg-gradient-to-r from-primary via-secondary to-primary bg-clip-text text-transparent">
                Dataset Preview: {previewDataset.fileName}
              </DialogTitle>
              <DialogDescription>
                {previewDataset.rowCount?.toLocaleString()} rows • {previewDataset.headers?.length || 0} columns
              </DialogDescription>
            </DialogHeader>
            
            <div className="space-y-4">
              {/* Columns */}
              <div>
                <h4 className="font-medium mb-2 text-foreground">Columns:</h4>
                <div className="flex flex-wrap gap-2">
                  {previewDataset.headers?.map((header, idx) => (
                    <motion.span
                      key={idx}
                      initial={{ opacity: 0, y: 10 }}
                      animate={{ opacity: 1, y: 0 }}
                      transition={{ delay: idx * 0.05, type: 'spring', bounce: 0.3, duration: 0.5 }}
                      className="px-2 py-1 bg-muted border border-border rounded-md text-sm text-foreground"
                    >
                      {header}
                    </motion.span>
                  ))}
                </div>
              </div>

              {/* Data Table */}
              {previewData && (
                <motion.div
                  initial={{ opacity: 0, filter: 'blur(12px)', y: 12 }}
                  animate={{ opacity: 1, filter: 'blur(0px)', y: 0 }}
                  transition={{ type: 'spring', bounce: 0.3, duration: 1.5 }}
                  className="border border-border rounded-xl overflow-hidden bg-card"
                >
                  <div className="overflow-x-auto">
                    <table className="w-full text-sm">
                      <thead className="bg-muted border-b border-border">
                        <tr>
                          {previewDataset.headers?.map((header, idx) => (
                            <th
                              key={idx}
                              className="px-4 py-3 text-left font-semibold text-foreground"
                            >
                              {header}
                            </th>
                          ))}
                        </tr>
                      </thead>
                      <tbody>
                        {isLoadingPreview ? (
                          <tr>
                            <td
                              colSpan={previewDataset.headers?.length || 1}
                              className="px-4 py-8 text-center text-muted-foreground"
                            >
                              Loading...
                            </td>
                          </tr>
                        ) : previewData.rows.length === 0 ? (
                          <tr>
                            <td
                              colSpan={previewDataset.headers?.length || 1}
                              className="px-4 py-8 text-center text-muted-foreground"
                            >
                              No data available
                            </td>
                          </tr>
                        ) : (
                          previewData.rows.map((row, rowIdx) => (
                            <motion.tr
                              key={rowIdx}
                              initial={{ opacity: 0 }}
                              animate={{ opacity: 1 }}
                              transition={{ delay: rowIdx * 0.02 }}
                              className="border-b border-border hover:bg-muted/50 transition-colors"
                            >
                              {previewDataset.headers?.map((header, colIdx) => (
                                <td
                                  key={colIdx}
                                  className="px-4 py-3 text-foreground"
                                >
                                  {row[header] || '-'}
                                </td>
                              ))}
                            </motion.tr>
                          ))
                        )}
                      </tbody>
                    </table>
                  </div>
                </motion.div>
              )}

              {/* Pagination */}
              {previewData && (
                <div className="flex items-center justify-between pt-4 border-t border-border">
                  <div className="text-sm text-muted-foreground">
                    Showing {previewOffset + 1} - {Math.min(previewOffset + previewData.limit, previewData.totalRows)} of {previewData.totalRows.toLocaleString()} rows
                  </div>
                  <div className="flex gap-2">
                    <Button
                      variant="outline"
                      size="sm"
                      onClick={() => {
                        const newOffset = Math.max(0, previewOffset - previewData.limit);
                        loadPreview(previewDataset, newOffset);
                      }}
                      disabled={previewOffset === 0 || isLoadingPreview}
                      className="hover:bg-primary/10 hover:border-primary/30 transition-colors"
                    >
                      <ChevronLeft className="w-4 h-4 mr-1" />
                      Previous
                    </Button>
                    <Button
                      variant="outline"
                      size="sm"
                      onClick={() => {
                        const newOffset = previewOffset + previewData.limit;
                        loadPreview(previewDataset, newOffset);
                      }}
                      disabled={!previewData.hasMore || isLoadingPreview}
                      className="hover:bg-primary/10 hover:border-primary/30 transition-colors"
                    >
                      Next
                      <ChevronRight className="w-4 h-4 ml-1" />
                    </Button>
                  </div>
                </div>
              )}
            </div>
          </DialogContent>
        </Dialog>
      )}
    </div>
  );
}
