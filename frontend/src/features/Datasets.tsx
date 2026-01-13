import { useState } from 'react';
import { FileSpreadsheet, Upload, Search, MoreVertical, Eye, Play, Trash2, Grid3x3, List, CloudUpload, CheckCircle2, X } from 'lucide-react';
import { Button } from './ui/button';
import { Input } from './ui/input';
import { Card } from './ui/card';
import { Badge } from './ui/badge';
import { Dialog, DialogContent, DialogHeader, DialogTitle } from './ui/dialog';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from './ui/select';
import { Checkbox } from './ui/checkbox';
import { Label } from './ui/label';
import { Textarea } from './ui/textarea';

interface DatasetsProps {
  onNavigate: (page: string) => void;
}

export function Datasets({ onNavigate }: DatasetsProps) {
  const [viewMode, setViewMode] = useState<'grid' | 'list'>('grid');
  const [uploadModalOpen, setUploadModalOpen] = useState(false);
  const [uploadStep, setUploadStep] = useState(1);
  const [uploadedFile, setUploadedFile] = useState<File | null>(null);

  const datasets = [
    { 
      name: 'sales_data_2024.csv', 
      rows: 15420, 
      features: 12, 
      target: 'churn_status', 
      uploaded: 'Dec 5, 2024',
      size: '2.3 MB'
    },
    { 
      name: 'customer_profiles.csv', 
      rows: 8932, 
      features: 18, 
      target: 'segment', 
      uploaded: 'Dec 4, 2024',
      size: '1.8 MB'
    },
    { 
      name: 'leads_q4.csv', 
      rows: 5234, 
      features: 9, 
      target: 'converted', 
      uploaded: 'Dec 3, 2024',
      size: '892 KB'
    },
    { 
      name: 'pricing_data.csv', 
      rows: 23456, 
      features: 15, 
      target: 'optimal_price', 
      uploaded: 'Dec 2, 2024',
      size: '3.1 MB'
    },
    { 
      name: 'sales_history.csv', 
      rows: 45678, 
      features: 21, 
      target: 'revenue', 
      uploaded: 'Nov 30, 2024',
      size: '5.6 MB'
    },
    { 
      name: 'marketing_campaigns.csv', 
      rows: 3421, 
      features: 14, 
      target: 'roi', 
      uploaded: 'Nov 28, 2024',
      size: '645 KB'
    },
  ];

  const previewData = [
    { customer_id: 'C001', age: 35, income: 75000, region: 'Northeast', churn_status: 'No' },
    { customer_id: 'C002', age: 42, income: 92000, region: 'Southeast', churn_status: 'Yes' },
    { customer_id: 'C003', age: 28, income: 58000, region: 'West', churn_status: 'No' },
    { customer_id: 'C004', age: 51, income: 105000, region: 'Midwest', churn_status: 'No' },
    { customer_id: 'C005', age: 39, income: 81000, region: 'Northeast', churn_status: 'Yes' },
  ];

  const columnConfig = [
    { name: 'customer_id', type: 'ID', include: false },
    { name: 'age', type: 'Numeric', include: true },
    { name: 'income', type: 'Numeric', include: true },
    { name: 'region', type: 'Categorical', include: true },
    { name: 'churn_status', type: 'Categorical', include: true },
  ];

  const handleFileUpload = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files && e.target.files[0]) {
      setUploadedFile(e.target.files[0]);
      setUploadStep(2);
    }
  };

  const handleDrop = (e: React.DragEvent) => {
    e.preventDefault();
    if (e.dataTransfer.files && e.dataTransfer.files[0]) {
      setUploadedFile(e.dataTransfer.files[0]);
      setUploadStep(2);
    }
  };

  return (
    <div className="p-8 space-y-6">
      {/* Page Header */}
      <div className="flex items-start justify-between">
        <div>
          <h1>Datasets</h1>
          <p className="text-muted-foreground mt-1">Manage your uploaded datasets for model training</p>
        </div>
        <Button onClick={() => setUploadModalOpen(true)} className="bg-primary text-primary-foreground hover:bg-primary/90">
          <Upload className="w-4 h-4 mr-2" />
          Upload Dataset
        </Button>
      </div>

      {/* Filter/Search Bar */}
      <Card className="p-4">
        <div className="flex items-center gap-4">
          <div className="flex-1 relative">
            <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-muted-foreground" />
            <Input
              placeholder="Search datasets..."
              className="pl-10 bg-background border-border/50"
            />
          </div>
          <Select defaultValue="all">
            <SelectTrigger className="w-40">
              <SelectValue placeholder="All Types" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="all">All Types</SelectItem>
              <SelectItem value="csv">CSV</SelectItem>
              <SelectItem value="json">JSON</SelectItem>
            </SelectContent>
          </Select>
          <Select defaultValue="recent">
            <SelectTrigger className="w-48">
              <SelectValue placeholder="Sort by" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="recent">Sort by: Recent</SelectItem>
              <SelectItem value="name">Sort by: Name</SelectItem>
              <SelectItem value="size">Sort by: Size</SelectItem>
            </SelectContent>
          </Select>
          <div className="flex gap-1 border border-border rounded-lg p-1">
            <Button
              size="sm"
              variant={viewMode === 'grid' ? 'secondary' : 'ghost'}
              onClick={() => setViewMode('grid')}
              className="px-2"
            >
              <Grid3x3 className="w-4 h-4" />
            </Button>
            <Button
              size="sm"
              variant={viewMode === 'list' ? 'secondary' : 'ghost'}
              onClick={() => setViewMode('list')}
              className="px-2"
            >
              <List className="w-4 h-4" />
            </Button>
          </div>
        </div>
      </Card>

      {/* Dataset Cards Grid */}
      {viewMode === 'grid' && (
        <div className="grid grid-cols-3 gap-6">
          {datasets.map((dataset, index) => (
            <Card key={index} className="p-6 hover:border-primary/30 transition-all group">
              <div className="flex items-start justify-between mb-4">
                <div className="w-12 h-12 rounded-lg bg-primary/10 flex items-center justify-center">
                  <FileSpreadsheet className="w-6 h-6 text-primary" />
                </div>
                <Button variant="ghost" size="icon" className="opacity-0 group-hover:opacity-100 transition-opacity">
                  <MoreVertical className="w-4 h-4" />
                </Button>
              </div>
              <h4 className="mb-2 truncate">{dataset.name}</h4>
              <p className="text-sm text-muted-foreground mb-4">
                {dataset.rows.toLocaleString()} rows • {dataset.features} features
              </p>
              {dataset.target && (
                <Badge variant="outline" className="mb-4 border-primary/30 text-primary">
                  Target: {dataset.target}
                </Badge>
              )}
              <p className="text-sm text-tertiary mb-4">Uploaded {dataset.uploaded}</p>
              <div className="flex gap-2">
                <Button variant="outline" size="sm" className="flex-1">
                  <Eye className="w-4 h-4 mr-1" />
                  Preview
                </Button>
                <Button size="sm" className="flex-1 bg-primary text-primary-foreground hover:bg-primary/90" onClick={() => onNavigate('models-train')}>
                  <Play className="w-4 h-4 mr-1" />
                  Train Model
                </Button>
              </div>
            </Card>
          ))}
        </div>
      )}

      {/* Upload Modal */}
      <Dialog open={uploadModalOpen} onOpenChange={setUploadModalOpen}>
        <DialogContent className="max-w-3xl max-h-[90vh] overflow-y-auto">
          <DialogHeader>
            <DialogTitle>Upload Dataset</DialogTitle>
          </DialogHeader>

          {/* Progress Indicator */}
          <div className="flex items-center justify-center gap-2 mb-6">
            {[1, 2, 3].map((step) => (
              <div key={step} className="flex items-center">
                <div className={`w-8 h-8 rounded-full flex items-center justify-center ${
                  uploadStep >= step ? 'bg-primary text-primary-foreground' : 'bg-muted text-muted-foreground'
                }`}>
                  {uploadStep > step ? <CheckCircle2 className="w-5 h-5" /> : step}
                </div>
                {step < 3 && (
                  <div className={`w-16 h-0.5 mx-2 ${uploadStep > step ? 'bg-primary' : 'bg-muted'}`}></div>
                )}
              </div>
            ))}
          </div>

          {/* Step 1: Upload File */}
          {uploadStep === 1 && (
            <div className="space-y-4">
              <div
                onDrop={handleDrop}
                onDragOver={(e) => e.preventDefault()}
                className="border-2 border-dashed border-border rounded-lg p-12 text-center hover:border-primary/50 transition-colors cursor-pointer"
              >
                <input
                  type="file"
                  accept=".csv"
                  onChange={handleFileUpload}
                  className="hidden"
                  id="file-upload"
                />
                <label htmlFor="file-upload" className="cursor-pointer">
                  <CloudUpload className="w-16 h-16 mx-auto mb-4 text-muted-foreground" />
                  <p className="mb-2">Drag and drop your CSV file here, or click to browse</p>
                  <p className="text-sm text-muted-foreground">Supported: .csv (max 50MB)</p>
                </label>
              </div>
            </div>
          )}

          {/* Step 2: Preview & Configure */}
          {uploadStep === 2 && (
            <div className="space-y-6">
              {uploadedFile && (
                <div className="flex items-center gap-3 p-4 bg-success/10 border border-success/20 rounded-lg">
                  <CheckCircle2 className="w-5 h-5 text-success" />
                  <div className="flex-1">
                    <p>{uploadedFile.name}</p>
                    <p className="text-sm text-muted-foreground">{(uploadedFile.size / 1024).toFixed(2)} KB</p>
                  </div>
                </div>
              )}

              <div>
                <h4 className="mb-3">Data Preview</h4>
                <div className="border border-border rounded-lg overflow-hidden">
                  <div className="overflow-x-auto">
                    <table className="w-full">
                      <thead>
                        <tr className="bg-muted">
                          {Object.keys(previewData[0]).map((key) => (
                            <th key={key} className="text-left py-2 px-4 text-sm font-medium">{key}</th>
                          ))}
                        </tr>
                      </thead>
                      <tbody>
                        {previewData.map((row, i) => (
                          <tr key={i} className="border-t border-border">
                            {Object.values(row).map((value, j) => (
                              <td key={j} className="py-2 px-4 text-sm">{value}</td>
                            ))}
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                </div>
              </div>

              <div>
                <h4 className="mb-3">Column Configuration</h4>
                <div className="border border-border rounded-lg overflow-hidden">
                  <table className="w-full">
                    <thead>
                      <tr className="bg-muted">
                        <th className="text-left py-2 px-4 text-sm font-medium">Column Name</th>
                        <th className="text-left py-2 px-4 text-sm font-medium">Data Type</th>
                        <th className="text-left py-2 px-4 text-sm font-medium">Include</th>
                      </tr>
                    </thead>
                    <tbody>
                      {columnConfig.map((col, i) => (
                        <tr key={i} className="border-t border-border">
                          <td className="py-3 px-4">{col.name}</td>
                          <td className="py-3 px-4">
                            <Badge variant="outline" className={col.type === 'ID' ? 'border-tertiary text-tertiary' : ''}>
                              {col.type} {col.type === 'ID' && '(excluded)'}
                            </Badge>
                          </td>
                          <td className="py-3 px-4">
                            <Checkbox checked={col.include} disabled={col.type === 'ID'} />
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              </div>

              <div className="flex gap-3">
                <Button variant="outline" onClick={() => setUploadStep(1)}>Back</Button>
                <Button className="flex-1 bg-primary text-primary-foreground hover:bg-primary/90" onClick={() => setUploadStep(3)}>
                  Continue
                </Button>
              </div>
            </div>
          )}

          {/* Step 3: Confirm */}
          {uploadStep === 3 && (
            <div className="space-y-6">
              <Card className="p-4 bg-muted/30">
                <h4 className="mb-3">Dataset Summary</h4>
                <div className="grid grid-cols-2 gap-4 text-sm">
                  <div>
                    <p className="text-muted-foreground">File name</p>
                    <p>{uploadedFile?.name}</p>
                  </div>
                  <div>
                    <p className="text-muted-foreground">Row count</p>
                    <p>15,420 rows</p>
                  </div>
                  <div>
                    <p className="text-muted-foreground">Column count</p>
                    <p>12 columns</p>
                  </div>
                  <div>
                    <p className="text-muted-foreground">Estimated size</p>
                    <p>2.3 MB</p>
                  </div>
                </div>
              </Card>

              <div className="space-y-2">
                <Label>Dataset Name</Label>
                <Input defaultValue={uploadedFile?.name.replace('.csv', '')} />
              </div>

              <div className="space-y-2">
                <Label>Description (Optional)</Label>
                <Textarea placeholder="Add a description for this dataset..." rows={3} />
              </div>

              <div className="flex gap-3">
                <Button variant="outline" onClick={() => setUploadStep(2)}>Back</Button>
                <Button 
                  className="flex-1 bg-primary text-primary-foreground hover:bg-primary/90"
                  onClick={() => {
                    setUploadModalOpen(false);
                    setUploadStep(1);
                    setUploadedFile(null);
                  }}
                >
                  Upload Dataset
                </Button>
              </div>
            </div>
          )}
        </DialogContent>
      </Dialog>
    </div>
  );
}
