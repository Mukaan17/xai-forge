import { useState } from 'react';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { apiClient } from '@/shared/lib/api/client';
import { Button } from '@/shared/components/ui/button';
import { Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle, DialogTrigger } from '@/shared/components/ui/dialog';
import { Input } from '@/shared/components/ui/input';
import { Label } from '@/shared/components/ui/label';
import { Upload, X } from 'lucide-react';
import { toast } from '@/shared/lib/toast';
import { DatasetDto } from '@/shared/types/dataset.types';
import { useFocusManagement } from '@/shared/hooks/useFocusManagement';
import { LiveRegion } from '@/shared/components/accessibility/LiveRegion';

interface UploadModalProps {
  onSuccess?: () => void;
}

export function UploadModal({ onSuccess }: UploadModalProps) {
  const [open, setOpen] = useState(false);
  const [file, setFile] = useState<File | null>(null);
  const [liveMessage, setLiveMessage] = useState('');
  const queryClient = useQueryClient();
  const dialogRef = useFocusManagement(open, true);

  const uploadMutation = useMutation({
    mutationFn: async (file: File) => {
      const formData = new FormData();
      formData.append('file', file);
      
      const response = await fetch(`${import.meta.env.VITE_API_URL || 'http://localhost:8080/api'}/v1/datasets/upload`, {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('token') || ''}`,
        },
        body: formData,
      });

      if (!response.ok) {
        const error = await response.json();
        throw new Error(error.detail || 'Upload failed');
      }

      return response.json() as Promise<DatasetDto>;
    },
    onMutate: async (file) => {
      // Cancel outgoing refetches
      await queryClient.cancelQueries({ queryKey: ['datasets'] });

      // Snapshot previous value
      const previousDatasets = queryClient.getQueryData(['datasets']);

      // Optimistically update with temporary dataset
      const optimisticDataset: DatasetDto = {
        id: Date.now(), // Temporary ID
        fileName: file.name,
        filePath: '',
        uploadDate: new Date().toISOString(),
        headers: [],
        rowCount: 0,
        ownerId: 0,
      };

      queryClient.setQueryData(['datasets'], (old: any) => {
        if (!old) return { content: [optimisticDataset], totalElements: 1 };
        return {
          ...old,
          content: [optimisticDataset, ...(old.content || [])],
          totalElements: (old.totalElements || 0) + 1,
        };
      });

      return { previousDatasets };
    },
    onSuccess: (data) => {
      toast.success('Dataset uploaded successfully!');
      setLiveMessage(`Dataset ${data.fileName} uploaded successfully`);
      queryClient.invalidateQueries({ queryKey: ['datasets'] });
      setOpen(false);
      setFile(null);
      onSuccess?.();
    },
    onError: (error: Error, file, context) => {
      // Rollback on error
      if (context?.previousDatasets) {
        queryClient.setQueryData(['datasets'], context.previousDatasets);
      }
      toast.error(`Upload failed: ${error.message}`);
    },
  });

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const selectedFile = e.target.files?.[0];
    if (selectedFile) {
      const lowerName = selectedFile.name.toLowerCase();
      const isValidFile = lowerName.endsWith('.csv') || 
                          lowerName.endsWith('.xlsx') || 
                          lowerName.endsWith('.xls');
      if (!isValidFile) {
        toast.error('Only CSV, XLSX, and XLS files are allowed');
        return;
      }
      setFile(selectedFile);
    }
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!file) {
      toast.error('Please select a file');
      return;
    }
    uploadMutation.mutate(file);
  };

  return (
    <>
      <LiveRegion message={liveMessage} priority="polite" />
      <Dialog open={open} onOpenChange={setOpen}>
        <DialogTrigger asChild>
          <Button aria-label="Upload a new dataset">
            <Upload className="w-4 h-4 mr-2" aria-hidden="true" />
            Upload Dataset
          </Button>
        </DialogTrigger>
        <DialogContent 
          ref={dialogRef as React.RefObject<HTMLDivElement>}
          aria-labelledby="upload-dialog-title"
          aria-describedby="upload-dialog-description"
        >
        <DialogHeader>
          <DialogTitle>Upload Dataset</DialogTitle>
          <DialogDescription>
            Upload a CSV, XLSX, or XLS file to use for training models. The file should have a header row.
          </DialogDescription>
        </DialogHeader>
        <form onSubmit={handleSubmit} className="space-y-4">
          <div className="space-y-2">
            <Label htmlFor="file">Dataset File (CSV, XLSX, XLS)</Label>
            <Input
              id="file"
              type="file"
              accept=".csv,.xlsx,.xls"
              onChange={handleFileChange}
              disabled={uploadMutation.isPending}
            />
            {file && (
              <div className="flex items-center gap-2 p-2 bg-muted rounded-md">
                <span className="text-sm flex-1">{file.name}</span>
                <Button
                  type="button"
                  variant="ghost"
                  size="icon"
                  className="h-6 w-6"
                  onClick={() => setFile(null)}
                >
                  <X className="h-4 w-4" />
                </Button>
              </div>
            )}
          </div>
          <div className="flex justify-end gap-2">
            <Button
              type="button"
              variant="outline"
              onClick={() => setOpen(false)}
              disabled={uploadMutation.isPending}
            >
              Cancel
            </Button>
            <Button type="submit" disabled={!file || uploadMutation.isPending}>
              {uploadMutation.isPending ? 'Uploading...' : 'Upload'}
            </Button>
          </div>
        </form>
        </DialogContent>
      </Dialog>
    </>
  );
}

