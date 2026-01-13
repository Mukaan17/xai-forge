import { useQuery } from '@tanstack/react-query';
import { apiClient } from '@/shared/lib/api/client';
import { Card, CardContent, CardHeader, CardTitle } from '@/shared/components/ui/card';
import { Button } from '@/shared/components/ui/button';
import { Database, Trash2, Eye } from 'lucide-react';
import { DatasetDto } from '@/shared/types/dataset.types';
import { UploadModal } from '../components/UploadModal';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { toast } from '@/shared/lib/toast';
import { useState } from 'react';
import { Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle } from '@/shared/components/ui/dialog';

export function DatasetsPage() {
  const [previewDataset, setPreviewDataset] = useState<DatasetDto | null>(null);
  const queryClient = useQueryClient();

  const { data: datasets, isLoading } = useQuery<DatasetDto[]>({
    queryKey: ['datasets'],
    queryFn: async () => {
      const response = await apiClient.get<DatasetDto[]>('/v1/datasets');
      return response;
    },
  });

  const deleteMutation = useMutation({
    mutationFn: async (id: number) => {
      await apiClient.delete(`/v1/datasets/${id}`);
    },
    onSuccess: () => {
      toast.success('Dataset deleted successfully');
      queryClient.invalidateQueries({ queryKey: ['datasets'] });
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

  return (
    <div className="p-4 sm:p-6 lg:p-8 space-y-4 sm:space-y-6">
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
        <h1 className="text-xl sm:text-2xl font-semibold">Datasets</h1>
        <UploadModal />
      </div>

      {!datasets || datasets.length === 0 ? (
        <Card>
          <CardContent className="py-12 text-center">
            <Database className="w-12 h-12 mx-auto mb-4 text-muted-foreground" />
            <p className="text-muted-foreground mb-4">No datasets uploaded yet</p>
            <UploadModal />
          </CardContent>
        </Card>
      ) : (
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
                      onClick={() => setPreviewDataset(dataset)}
                    >
                      <Eye className="w-4 h-4 mr-2" />
                      Preview
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
      )}

      {previewDataset && (
        <Dialog open={!!previewDataset} onOpenChange={() => setPreviewDataset(null)}>
          <DialogContent className="max-w-2xl max-h-[80vh] overflow-y-auto">
            <DialogHeader>
              <DialogTitle>Dataset Preview: {previewDataset.fileName}</DialogTitle>
              <DialogDescription>
                {previewDataset.rowCount} rows, {previewDataset.headers?.length || 0} columns
              </DialogDescription>
            </DialogHeader>
            <div className="space-y-4">
              <div>
                <h4 className="font-medium mb-2">Columns:</h4>
                <div className="flex flex-wrap gap-2">
                  {previewDataset.headers?.map((header, idx) => (
                    <span
                      key={idx}
                      className="px-2 py-1 bg-muted rounded-md text-sm"
                    >
                      {header}
                    </span>
                  ))}
                </div>
              </div>
            </div>
          </DialogContent>
        </Dialog>
      )}
    </div>
  );
}
