import { useState, useRef } from 'react';
import { Upload, Database, Calendar, Activity, Trash2, Eye, Download, Plus, Search, Filter, X } from 'lucide-react';
import { Button } from './ui/button';
import { Card } from './ui/card';
import { Input } from './ui/input';
import { Label } from './ui/label';
import { Badge } from './ui/badge';
import { Progress } from './ui/progress';
import { Dialog, DialogContent, DialogHeader, DialogTitle } from './ui/dialog';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from './ui/select';
import { useStore } from '../lib/store';
import { toast } from 'sonner@2.0.3';

interface DatasetsEnhancedProps {
  onNavigate: (page: string) => void;
}

export function DatasetsEnhanced({ onNavigate }: DatasetsEnhancedProps) {
  const { datasets, addDataset, deleteDataset, isUploading, uploadProgress, setIsUploading, setUploadProgress } = useStore();
  const [uploadModalOpen, setUploadModalOpen] = useState(false);
  const [dragActive, setDragActive] = useState(false);
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [previewData, setPreviewData] = useState<any>(null);
  const [uploadStep, setUploadStep] = useState<'select' | 'preview' | 'configure' | 'uploading'>('select');
  const [targetColumn, setTargetColumn] = useState('');
  const [problemType, setProblemType] = useState<'classification' | 'regression'>('classification');
  const [searchQuery, setSearchQuery] = useState('');
  const fileInputRef = useRef<HTMLInputElement>(null);

  const handleDrag = (e: React.DragEvent) => {
    e.preventDefault();
    e.stopPropagation();
    if (e.type === 'dragenter' || e.type === 'dragover') {
      setDragActive(true);
    } else if (e.type === 'dragleave') {
      setDragActive(false);
    }
  };

  const handleDrop = (e: React.DragEvent) => {
    e.preventDefault();
    e.stopPropagation();
    setDragActive(false);

    if (e.dataTransfer.files && e.dataTransfer.files[0]) {
      handleFileSelect(e.dataTransfer.files[0]);
    }
  };

  const handleFileSelect = (file: File) => {
    setSelectedFile(file);
    
    // Mock preview data
    const mockPreview = {
      columns: ['age', 'income', 'account_tenure', 'monthly_charges', 'contract_type', 'tech_support', 'will_churn'],
      rows: [
        [25, 50000, 6, 95, 'Month-to-month', 'No', 'Yes'],
        [45, 75000, 24, 75, 'Two year', 'Yes', 'No'],
        [32, 45000, 12, 89, 'One year', 'No', 'No'],
        [52, 85000, 36, 65, 'Two year', 'Yes', 'No'],
        [28, 42000, 3, 99, 'Month-to-month', 'No', 'Yes'],
      ],
      totalRows: 15420,
    };
    
    setPreviewData(mockPreview);
    setUploadStep('preview');
  };

  const handleUpload = async () => {
    if (!selectedFile || !targetColumn) return;

    setUploadStep('uploading');
    setIsUploading(true);
    
    // Simulate upload progress
    for (let i = 0; i <= 100; i += 10) {
      await new Promise(resolve => setTimeout(resolve, 200));
      setUploadProgress(i);
    }

    const newDataset = {
      id: Date.now().toString(),
      name: selectedFile.name,
      size: `${(selectedFile.size / (1024 * 1024)).toFixed(1)} MB`,
      rows: previewData?.totalRows || 0,
      columns: previewData?.columns.length || 0,
      uploadDate: new Date().toISOString().split('T')[0],
      status: 'ready' as const,
      type: problemType === 'classification' ? 'Classification' : 'Regression',
    };

    addDataset(newDataset);
    setIsUploading(false);
    setUploadProgress(0);
    setUploadModalOpen(false);
    setSelectedFile(null);
    setPreviewData(null);
    setUploadStep('select');
    
    toast.success('Dataset uploaded successfully', {
      description: `${newDataset.name} is ready for training`,
    });
  };

  const handleDeleteDataset = (id: string, name: string) => {
    deleteDataset(id);
    toast.success('Dataset deleted', {
      description: `${name} has been removed`,
    });
  };

  const filteredDatasets = datasets.filter(d => 
    d.name.toLowerCase().includes(searchQuery.toLowerCase())
  );

  return (
    <div className="p-8 space-y-6">
      {/* Header */}
      <div className="flex items-start justify-between">
        <div>
          <h1>Datasets</h1>
          <p className="text-muted-foreground mt-1">Manage your training data</p>
        </div>
        <Button
          onClick={() => setUploadModalOpen(true)}
          className="bg-primary text-primary-foreground hover:bg-primary/90"
        >
          <Upload className="w-4 h-4 mr-2" />
          Upload Dataset
        </Button>
      </div>

      {/* Search & Filters */}
      <div className="flex gap-4">
        <div className="flex-1 relative">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-muted-foreground" />
          <Input
            placeholder="Search datasets..."
            className="pl-10"
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
          />
        </div>
        <Button variant="outline">
          <Filter className="w-4 h-4 mr-2" />
          Filter
        </Button>
      </div>

      {/* Datasets Grid */}
      <div className="grid grid-cols-3 gap-6">
        {filteredDatasets.map((dataset) => (
          <Card key={dataset.id} className="p-6 hover:border-primary/30 transition-colors group">
            <div className="flex items-start justify-between mb-4">
              <div className="w-12 h-12 rounded-lg bg-gradient-to-br from-primary/20 to-secondary/20 flex items-center justify-center">
                <Database className="w-6 h-6 text-primary" />
              </div>
              <Badge variant={dataset.status === 'ready' ? 'default' : 'outline'}>
                {dataset.status}
              </Badge>
            </div>

            <h3 className="mb-2 truncate">{dataset.name}</h3>
            
            <div className="space-y-2 text-sm text-muted-foreground mb-4">
              <div className="flex items-center gap-2">
                <Activity className="w-4 h-4" />
                <span>{dataset.rows.toLocaleString()} rows • {dataset.columns} columns</span>
              </div>
              <div className="flex items-center gap-2">
                <Calendar className="w-4 h-4" />
                <span>Uploaded {dataset.uploadDate}</span>
              </div>
              <div className="flex items-center gap-2">
                <Database className="w-4 h-4" />
                <span>{dataset.size}</span>
              </div>
            </div>

            <div className="flex gap-2 opacity-0 group-hover:opacity-100 transition-opacity">
              <Button variant="outline" size="sm" className="flex-1" onClick={() => onNavigate('models-train')}>
                <Plus className="w-4 h-4 mr-1" />
                Train Model
              </Button>
              <Button variant="outline" size="sm">
                <Eye className="w-4 h-4" />
              </Button>
              <Button
                variant="outline"
                size="sm"
                className="text-error hover:bg-error hover:text-white"
                onClick={() => handleDeleteDataset(dataset.id, dataset.name)}
              >
                <Trash2 className="w-4 h-4" />
              </Button>
            </div>
          </Card>
        ))}

        {/* Upload Card */}
        <Card
          className="p-6 border-dashed border-2 hover:border-primary/50 transition-colors cursor-pointer group"
          onClick={() => setUploadModalOpen(true)}
        >
          <div className="h-full flex flex-col items-center justify-center text-center">
            <div className="w-16 h-16 rounded-full bg-primary/10 flex items-center justify-center mb-4 group-hover:bg-primary/20 transition-colors">
              <Upload className="w-8 h-8 text-primary" />
            </div>
            <p className="font-medium mb-2">Upload New Dataset</p>
            <p className="text-sm text-muted-foreground">
              Drag & drop or click to browse
            </p>
          </div>
        </Card>
      </div>

      {/* Upload Modal */}
      <Dialog open={uploadModalOpen} onOpenChange={setUploadModalOpen}>
        <DialogContent className="max-w-3xl">
          <DialogHeader>
            <DialogTitle>Upload Dataset</DialogTitle>
          </DialogHeader>

          {uploadStep === 'select' && (
            <div>
              <div
                className={`border-2 border-dashed rounded-lg p-12 text-center transition-colors ${
                  dragActive ? 'border-primary bg-primary/5' : 'border-border hover:border-primary/50'
                }`}
                onDragEnter={handleDrag}
                onDragLeave={handleDrag}
                onDragOver={handleDrag}
                onDrop={handleDrop}
                onClick={() => fileInputRef.current?.click()}
              >
                <Upload className="w-16 h-16 mx-auto mb-4 text-muted-foreground" />
                <p className="text-lg mb-2">Drop your file here or click to browse</p>
                <p className="text-sm text-muted-foreground">
                  Supports CSV, Excel, and JSON files (max 100MB)
                </p>
                <input
                  ref={fileInputRef}
                  type="file"
                  accept=".csv,.xlsx,.xls,.json"
                  className="hidden"
                  onChange={(e) => e.target.files?.[0] && handleFileSelect(e.target.files[0])}
                />
              </div>
            </div>
          )}

          {uploadStep === 'preview' && previewData && (
            <div className="space-y-4">
              <div>
                <h3 className="mb-2">Data Preview</h3>
                <p className="text-sm text-muted-foreground mb-4">
                  Showing first 5 rows of {previewData.totalRows.toLocaleString()} total rows
                </p>
                <div className="border border-border rounded-lg overflow-hidden">
                  <div className="overflow-x-auto">
                    <table className="w-full text-sm">
                      <thead className="bg-muted/30">
                        <tr>
                          {previewData.columns.map((col: string) => (
                            <th key={col} className="px-4 py-2 text-left font-medium">{col}</th>
                          ))}
                        </tr>
                      </thead>
                      <tbody>
                        {previewData.rows.map((row: any[], i: number) => (
                          <tr key={i} className="border-t border-border">
                            {row.map((cell, j) => (
                              <td key={j} className="px-4 py-2">{cell}</td>
                            ))}
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                </div>
              </div>

              <div>
                <Label>Select Target Column (what you want to predict)</Label>
                <Select value={targetColumn} onValueChange={setTargetColumn}>
                  <SelectTrigger className="mt-2">
                    <SelectValue placeholder="Choose a column..." />
                  </SelectTrigger>
                  <SelectContent>
                    {previewData.columns.map((col: string) => (
                      <SelectItem key={col} value={col}>{col}</SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>

              <div>
                <Label>Problem Type</Label>
                <Select value={problemType} onValueChange={(v: any) => setProblemType(v)}>
                  <SelectTrigger className="mt-2">
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="classification">Classification</SelectItem>
                    <SelectItem value="regression">Regression</SelectItem>
                  </SelectContent>
                </Select>
              </div>

              <div className="flex gap-3 pt-4">
                <Button variant="outline" onClick={() => setUploadStep('select')} className="flex-1">
                  Back
                </Button>
                <Button
                  onClick={handleUpload}
                  disabled={!targetColumn}
                  className="flex-1 bg-primary text-primary-foreground hover:bg-primary/90"
                >
                  Upload Dataset
                </Button>
              </div>
            </div>
          )}

          {uploadStep === 'uploading' && (
            <div className="py-8">
              <div className="text-center mb-6">
                <div className="w-16 h-16 rounded-full bg-primary/10 flex items-center justify-center mx-auto mb-4">
                  <Upload className="w-8 h-8 text-primary animate-pulse" />
                </div>
                <h3 className="mb-2">Uploading Dataset...</h3>
                <p className="text-muted-foreground">
                  Processing {selectedFile?.name}
                </p>
              </div>
              <Progress value={uploadProgress} className="mb-4" />
              <p className="text-center text-sm text-muted-foreground">{uploadProgress}%</p>
            </div>
          )}
        </DialogContent>
      </Dialog>
    </div>
  );
}
