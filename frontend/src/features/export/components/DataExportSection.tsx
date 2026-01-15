import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/shared/components/ui/card';
import { Button } from '@/shared/components/ui/button';
import { Checkbox } from '@/shared/components/ui/checkbox';
import { Badge } from '@/shared/components/ui/badge';
import { Progress } from '@/shared/components/ui/progress';
import { Download, FileDown, Loader2, AlertCircle, CheckCircle2, Clock } from 'lucide-react';
import { exportApi, ExportJobDto, ExportRequest } from '../api/exportApi';
import { toast } from '@/shared/lib/toast';

const EXPORT_ITEMS = [
  { id: 'datasets', label: 'Datasets', description: 'All uploaded datasets and metadata' },
  { id: 'models', label: 'Models', description: 'All trained models and metrics' },
  { id: 'predictions', label: 'Predictions', description: 'All prediction history' },
  { id: 'activity', label: 'Activity Logs', description: 'All activity and audit logs' },
  { id: 'profile', label: 'Profile', description: 'User profile information' },
  { id: 'preferences', label: 'Preferences', description: 'User preferences and settings' },
];

export function DataExportSection() {
  const queryClient = useQueryClient();
  const [selectedItems, setSelectedItems] = useState<Set<string>>(
    new Set(EXPORT_ITEMS.map(item => item.id))
  );

  const { data: exportJobs = [], isLoading: isLoadingJobs } = useQuery({
    queryKey: ['export-jobs'],
    queryFn: () => exportApi.getExportJobs(),
    refetchInterval: (data) => {
      // Poll if there are any pending or processing jobs
      const hasActiveJobs = data?.some(
        job => job.status === 'PENDING' || job.status === 'PROCESSING'
      );
      return hasActiveJobs ? 3000 : false;
    },
  });

  const requestExportMutation = useMutation({
    mutationFn: (request: ExportRequest) => exportApi.requestExport(request),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['export-jobs'] });
      toast.success('Export requested', {
        description: 'Your data export has been started. You will be notified when it\'s ready.',
      });
    },
    onError: (error: Error) => {
      toast.error('Failed to request export', {
        description: error.message,
      });
    },
  });

  const downloadExportMutation = useMutation({
    mutationFn: (jobId: number) => exportApi.downloadExport(jobId),
    onSuccess: (blob, jobId) => {
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `xai-export-${jobId}.zip`;
      document.body.appendChild(a);
      a.click();
      window.URL.revokeObjectURL(url);
      document.body.removeChild(a);
      toast.success('Export downloaded', {
        description: 'Your data export has been downloaded.',
      });
    },
    onError: (error: Error) => {
      toast.error('Failed to download export', {
        description: error.message,
      });
    },
  });

  const handleRequestExport = () => {
    if (selectedItems.size === 0) {
      toast.error('Please select at least one item to export');
      return;
    }
    requestExportMutation.mutate({ includeItems: Array.from(selectedItems) });
  };

  const toggleItem = (itemId: string) => {
    setSelectedItems(prev => {
      const next = new Set(prev);
      if (next.has(itemId)) {
        next.delete(itemId);
      } else {
        next.add(itemId);
      }
      return next;
    });
  };

  const getStatusIcon = (status: string) => {
    switch (status) {
      case 'COMPLETED':
        return <CheckCircle2 className="w-4 h-4 text-success" />;
      case 'PROCESSING':
      case 'PENDING':
        return <Loader2 className="w-4 h-4 text-primary animate-spin" />;
      case 'FAILED':
        return <AlertCircle className="w-4 h-4 text-destructive" />;
      case 'EXPIRED':
        return <Clock className="w-4 h-4 text-muted-foreground" />;
      default:
        return null;
    }
  };

  const formatFileSize = (bytes: number | null) => {
    if (!bytes) return 'N/A';
    if (bytes < 1024) return `${bytes} B`;
    if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(2)} KB`;
    return `${(bytes / (1024 * 1024)).toFixed(2)} MB`;
  };

  const formatDate = (dateString: string | null) => {
    if (!dateString) return 'N/A';
    return new Date(dateString).toLocaleString();
  };

  return (
    <div className="space-y-6">
      <Card>
        <CardHeader>
          <CardTitle>Export Your Data</CardTitle>
          <CardDescription>
            Request a complete export of your data in JSON format. This export includes all your datasets, models, predictions, and activity logs. 
            Exports are available for download for 7 days.
          </CardDescription>
        </CardHeader>
        <CardContent className="space-y-6">
          {/* Export Options */}
          <div>
            <h3 className="text-sm font-medium mb-4">Select data to export:</h3>
            <div className="space-y-3">
              {EXPORT_ITEMS.map((item) => (
                <div
                  key={item.id}
                  className="flex items-start gap-3 p-3 rounded-lg border border-border hover:bg-muted/50 transition-colors cursor-pointer"
                  onClick={() => toggleItem(item.id)}
                >
                  <Checkbox
                    checked={selectedItems.has(item.id)}
                    onCheckedChange={() => toggleItem(item.id)}
                    className="mt-1"
                  />
                  <div className="flex-1">
                    <p className="font-medium text-sm">{item.label}</p>
                    <p className="text-xs text-muted-foreground">{item.description}</p>
                  </div>
                </div>
              ))}
            </div>
          </div>

          {/* Request Export Button */}
          <Button
            onClick={handleRequestExport}
            disabled={requestExportMutation.isPending || selectedItems.size === 0}
            className="w-full"
          >
            {requestExportMutation.isPending ? (
              <>
                <Loader2 className="w-4 h-4 mr-2 animate-spin" />
                Requesting Export...
              </>
            ) : (
              <>
                <FileDown className="w-4 h-4 mr-2" />
                Request Data Export
              </>
            )}
          </Button>
        </CardContent>
      </Card>

      {/* Export Jobs List */}
      <Card>
        <CardHeader>
          <CardTitle>Export History</CardTitle>
          <CardDescription>
            View and download your previous data exports
          </CardDescription>
        </CardHeader>
        <CardContent>
          {isLoadingJobs ? (
            <div className="text-center py-8 text-muted-foreground">Loading export jobs...</div>
          ) : exportJobs.length === 0 ? (
            <div className="text-center py-8 text-muted-foreground">
              No export jobs yet. Request an export to get started.
            </div>
          ) : (
            <div className="space-y-4">
              {exportJobs.map((job) => (
                <div
                  key={job.id}
                  className="p-4 rounded-lg border border-border bg-card"
                >
                  <div className="flex items-start justify-between gap-4">
                    <div className="flex-1">
                      <div className="flex items-center gap-2 mb-2">
                        {getStatusIcon(job.status)}
                        <Badge variant="outline" className="capitalize">
                          {job.status.toLowerCase()}
                        </Badge>
                        <span className="text-sm text-muted-foreground">
                          Created: {formatDate(job.createdAt)}
                        </span>
                      </div>

                      {job.status === 'PROCESSING' && (
                        <div className="mt-3">
                          <div className="flex items-center justify-between text-sm mb-1">
                            <span className="text-muted-foreground">{job.currentStep || 'Processing...'}</span>
                            <span className="text-muted-foreground">{job.progress}%</span>
                          </div>
                          <Progress value={job.progress} className="h-2" />
                        </div>
                      )}

                      {job.status === 'COMPLETED' && (
                        <div className="mt-2 space-y-1">
                          <p className="text-sm text-muted-foreground">
                            Completed: {formatDate(job.completedAt)}
                          </p>
                          <p className="text-sm text-muted-foreground">
                            File size: {formatFileSize(job.fileSize)}
                          </p>
                          {job.expiresAt && (
                            <p className="text-sm text-muted-foreground">
                              Expires: {formatDate(job.expiresAt)}
                            </p>
                          )}
                        </div>
                      )}

                      {job.status === 'FAILED' && job.errorMessage && (
                        <p className="text-sm text-destructive mt-2">{job.errorMessage}</p>
                      )}
                    </div>

                    {job.status === 'COMPLETED' && (
                      <Button
                        variant="outline"
                        size="sm"
                        onClick={() => downloadExportMutation.mutate(job.id)}
                        disabled={downloadExportMutation.isPending}
                      >
                        {downloadExportMutation.isPending ? (
                          <Loader2 className="w-4 h-4 animate-spin" />
                        ) : (
                          <>
                            <Download className="w-4 h-4 mr-2" />
                            Download
                          </>
                        )}
                      </Button>
                    )}
                  </div>
                </div>
              ))}
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  );
}
