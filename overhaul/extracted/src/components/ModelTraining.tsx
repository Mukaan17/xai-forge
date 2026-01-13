import { useState } from 'react';
import { ChevronDown, Info, CheckCircle2, Loader2 } from 'lucide-react';
import { Button } from './ui/button';
import { Input } from './ui/input';
import { Label } from './ui/label';
import { Card } from './ui/card';
import { Badge } from './ui/badge';
import { Checkbox } from './ui/checkbox';
import { RadioGroup, RadioGroupItem } from './ui/radio-group';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from './ui/select';
import { Progress } from './ui/progress';
import { BarChart, Bar, XAxis, YAxis, ResponsiveContainer, Tooltip } from 'recharts';

interface ModelTrainingProps {
  onNavigate: (page: string) => void;
}

export function ModelTraining({ onNavigate }: ModelTrainingProps) {
  const [trainingStatus, setTrainingStatus] = useState<'config' | 'training' | 'complete'>('config');
  const [trainingProgress, setTrainingProgress] = useState(0);
  const [selectedDataset, setSelectedDataset] = useState('');
  const [targetColumn, setTargetColumn] = useState('churn_status');
  const [modelType, setModelType] = useState<'classification' | 'regression'>('classification');
  const [selectedAlgorithm, setSelectedAlgorithm] = useState('logistic');

  const datasets = [
    { id: 'sales_2024', name: 'sales_data_2024.csv', rows: 15420 },
    { id: 'customers', name: 'customer_profiles.csv', rows: 8932 },
    { id: 'leads', name: 'leads_q4.csv', rows: 5234 },
  ];

  const features = [
    { name: 'age', type: 'Numeric', correlation: 0.32, selected: true },
    { name: 'income', type: 'Numeric', correlation: 0.28, selected: true },
    { name: 'region', type: 'Categorical', correlation: 0.15, selected: true },
    { name: 'account_tenure', type: 'Numeric', correlation: 0.41, selected: true },
    { name: 'monthly_charges', type: 'Numeric', correlation: 0.38, selected: true },
    { name: 'contract_type', type: 'Categorical', correlation: 0.25, selected: true },
    { name: 'tech_support', type: 'Categorical', correlation: 0.19, selected: false },
    { name: 'internet_service', type: 'Categorical', correlation: 0.22, selected: false },
  ];

  const featureDistribution = [
    { feature: 'account_tenure', avg: 32.5 },
    { feature: 'monthly_charges', avg: 64.8 },
    { feature: 'age', avg: 38.2 },
    { feature: 'income', avg: 72000 },
  ];

  const algorithms = [
    {
      id: 'logistic',
      name: 'Logistic Regression',
      type: 'classification',
      description: 'Best for: Binary outcomes',
      icon: '📊',
    },
    {
      id: 'random-forest',
      name: 'Random Forest',
      type: 'classification',
      description: 'Best for: Complex patterns',
      icon: '🌲',
    },
    {
      id: 'linear',
      name: 'Linear Regression',
      type: 'regression',
      description: 'Best for: Continuous values',
      icon: '📈',
    },
    {
      id: 'gradient-boost',
      name: 'Gradient Boosting',
      type: 'regression',
      description: 'Best for: High accuracy',
      icon: '🚀',
    },
  ];

  const handleStartTraining = () => {
    setTrainingStatus('training');
    let progress = 0;
    const interval = setInterval(() => {
      progress += 10;
      setTrainingProgress(progress);
      if (progress >= 100) {
        clearInterval(interval);
        setTimeout(() => setTrainingStatus('complete'), 500);
      }
    }, 800);
  };

  if (trainingStatus === 'training') {
    return (
      <div className="p-8 flex items-center justify-center min-h-[80vh]">
        <Card className="p-12 max-w-2xl w-full text-center">
          <div className="w-32 h-32 mx-auto mb-8 relative">
            <div className="absolute inset-0 rounded-full border-4 border-primary/20"></div>
            <div 
              className="absolute inset-0 rounded-full border-4 border-primary border-t-transparent animate-spin"
            ></div>
            <div className="absolute inset-0 flex items-center justify-center">
              <span className="text-2xl font-semibold">{trainingProgress}%</span>
            </div>
          </div>
          <h2 className="mb-2">Training model...</h2>
          <p className="text-muted-foreground mb-6">This may take a few minutes</p>
          <Progress value={trainingProgress} className="mb-4" />
          <div className="bg-muted/30 rounded-lg p-4 text-left text-sm font-mono max-h-48 overflow-y-auto">
            <p className="text-success">✓ Loading dataset... Complete</p>
            <p className="text-success">✓ Preprocessing data... Complete</p>
            <p className="text-success">✓ Feature engineering... Complete</p>
            <p className="text-primary animate-pulse">▶ Training model... Epoch {Math.floor(trainingProgress / 10)}/10</p>
            <p className="text-muted-foreground">  Accuracy: 0.{82 + Math.floor(trainingProgress / 15)}</p>
          </div>
          <Button variant="outline" className="mt-6" onClick={() => setTrainingStatus('config')}>
            Cancel
          </Button>
        </Card>
      </div>
    );
  }

  if (trainingStatus === 'complete') {
    return (
      <div className="p-8 flex items-center justify-center min-h-[80vh]">
        <Card className="p-12 max-w-2xl w-full text-center">
          <div className="w-24 h-24 mx-auto mb-6 rounded-full bg-success/10 flex items-center justify-center">
            <CheckCircle2 className="w-12 h-12 text-success" />
          </div>
          <h1 className="mb-2">Training Complete! 🎉</h1>
          <p className="text-muted-foreground mb-8">Your model has been successfully trained</p>
          
          <div className="grid grid-cols-2 gap-6 mb-8">
            <Card className="p-6">
              <p className="text-muted-foreground mb-1">Model Accuracy</p>
              <p className="text-4xl font-semibold text-success">87.3%</p>
            </Card>
            <Card className="p-6">
              <p className="text-muted-foreground mb-1">Training Time</p>
              <p className="text-4xl font-semibold">2m 14s</p>
            </Card>
          </div>

          <div className="flex gap-3">
            <Button onClick={() => onNavigate('predictions-new')} className="flex-1 bg-primary text-primary-foreground hover:bg-primary/90">
              Make Prediction
            </Button>
            <Button onClick={() => onNavigate('models-all')} variant="outline" className="flex-1">
              View Model Details
            </Button>
          </div>
          <Button variant="ghost" className="mt-4 text-primary" onClick={() => setTrainingStatus('config')}>
            Train Another Model
          </Button>
        </Card>
      </div>
    );
  }

  return (
    <div className="p-8">
      <div className="mb-6">
        <h1>Train New Model</h1>
        <p className="text-muted-foreground mt-1">Configure and train a new machine learning model</p>
      </div>

      <div className="grid grid-cols-12 gap-6">
        {/* Left Column - Configuration Form */}
        <div className="col-span-7 space-y-6">
          {/* Section 1: Select Dataset */}
          <Card className="p-6">
            <h3 className="mb-4">Select Dataset</h3>
            <Select value={selectedDataset} onValueChange={setSelectedDataset}>
              <SelectTrigger>
                <SelectValue placeholder="Choose a dataset" />
              </SelectTrigger>
              <SelectContent>
                {datasets.map((dataset) => (
                  <SelectItem key={dataset.id} value={dataset.id}>
                    <div className="flex items-center justify-between w-full">
                      <span>{dataset.name}</span>
                      <span className="text-xs text-muted-foreground ml-4">{dataset.rows.toLocaleString()} rows</span>
                    </div>
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </Card>

          {/* Section 2: Define Target Variable */}
          <Card className="p-6">
            <h3 className="mb-4">Define Target Variable</h3>
            <div className="space-y-4">
              <div>
                <Label>Select target column</Label>
                <Select value={targetColumn} onValueChange={setTargetColumn}>
                  <SelectTrigger className="mt-2">
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="churn_status">churn_status</SelectItem>
                    <SelectItem value="revenue">revenue</SelectItem>
                    <SelectItem value="conversion">conversion</SelectItem>
                  </SelectContent>
                </Select>
              </div>
              <div className="flex items-center gap-2 p-3 bg-primary/10 border border-primary/20 rounded-lg">
                <Info className="w-4 h-4 text-primary flex-shrink-0" />
                <p className="text-sm text-primary">Recommended: churn_status (Classification)</p>
              </div>
              <RadioGroup value={modelType} onValueChange={(value: 'classification' | 'regression') => setModelType(value)}>
                <div className="flex items-center space-x-2">
                  <RadioGroupItem value="classification" id="classification" />
                  <Label htmlFor="classification">Classification</Label>
                </div>
                <div className="flex items-center space-x-2">
                  <RadioGroupItem value="regression" id="regression" />
                  <Label htmlFor="regression">Regression</Label>
                </div>
              </RadioGroup>
            </div>
          </Card>

          {/* Section 3: Select Features */}
          <Card className="p-6">
            <div className="flex items-center justify-between mb-4">
              <h3>Select Features</h3>
              <div className="flex gap-2">
                <Button variant="ghost" size="sm" className="text-primary">Select All</Button>
                <Button variant="ghost" size="sm" className="text-primary">Deselect All</Button>
              </div>
            </div>
            <div className="space-y-3">
              {features.map((feature, index) => (
                <div
                  key={index}
                  className="flex items-center justify-between p-3 rounded-lg border border-border hover:border-primary/30 transition-colors group"
                >
                  <div className="flex items-center gap-3">
                    <Checkbox checked={feature.selected} />
                    <div>
                      <p>{feature.name}</p>
                      <div className="flex items-center gap-2 mt-1">
                        <Badge variant="outline" className="text-xs">{feature.type}</Badge>
                        <span className="text-xs text-muted-foreground">
                          {Math.abs(feature.correlation * 100).toFixed(0)}% correlation
                        </span>
                      </div>
                    </div>
                  </div>
                  <div className="w-24 h-2 bg-muted rounded-full overflow-hidden">
                    <div
                      className="h-full bg-primary"
                      style={{ width: `${Math.abs(feature.correlation) * 100}%` }}
                    ></div>
                  </div>
                </div>
              ))}
            </div>
          </Card>

          {/* Section 4: Algorithm Selection */}
          <Card className="p-6">
            <h3 className="mb-4">Select Algorithm</h3>
            <div className="grid grid-cols-2 gap-4">
              {algorithms
                .filter((algo) => algo.type === modelType)
                .map((algo) => (
                  <button
                    key={algo.id}
                    onClick={() => setSelectedAlgorithm(algo.id)}
                    className={`p-4 rounded-lg border-2 transition-all text-left ${
                      selectedAlgorithm === algo.id
                        ? 'border-primary bg-primary/5'
                        : 'border-border hover:border-primary/30'
                    }`}
                  >
                    <div className="text-2xl mb-2">{algo.icon}</div>
                    <p className="font-medium mb-1">{algo.name}</p>
                    <p className="text-sm text-muted-foreground">{algo.description}</p>
                    {selectedAlgorithm === algo.id && (
                      <div className="mt-2">
                        <Badge className="bg-primary text-primary-foreground">Selected ✓</Badge>
                      </div>
                    )}
                  </button>
                ))}
            </div>
          </Card>

          {/* Section 5: Model Name */}
          <Card className="p-6">
            <Label>Model Name</Label>
            <Input
              defaultValue="churn_predictor_v1"
              className="mt-2"
              placeholder="Enter model name"
            />
          </Card>
        </div>

        {/* Right Column - Live Preview Panel */}
        <div className="col-span-5 space-y-6">
          <Card className="p-6 sticky top-24">
            <h3 className="mb-6">Configuration Summary</h3>
            
            <div className="space-y-6">
              {/* Dataset Stats */}
              <div>
                <p className="text-sm text-muted-foreground mb-2">Dataset</p>
                <div className="p-4 bg-muted/30 rounded-lg">
                  <p className="font-medium mb-1">sales_data_2024.csv</p>
                  <p className="text-sm text-muted-foreground">15,420 rows</p>
                </div>
              </div>

              {/* Features Selected */}
              <div>
                <p className="text-sm text-muted-foreground mb-2">Features Selected</p>
                <div className="flex flex-wrap gap-2">
                  {features.filter(f => f.selected).map((f, i) => (
                    <Badge key={i} variant="outline" className="border-primary/30 text-primary">
                      {f.name}
                    </Badge>
                  ))}
                </div>
              </div>

              {/* Feature Distribution */}
              <div>
                <p className="text-sm text-muted-foreground mb-3">Feature Distribution</p>
                <div className="h-40">
                  <ResponsiveContainer width="100%" height="100%">
                    <BarChart data={featureDistribution}>
                      <XAxis dataKey="feature" tick={{ fontSize: 10 }} />
                      <YAxis hide />
                      <Tooltip />
                      <Bar dataKey="avg" fill="#00d9ff" radius={[4, 4, 0, 0]} />
                    </BarChart>
                  </ResponsiveContainer>
                </div>
              </div>

              {/* Estimated Training Time */}
              <div>
                <p className="text-sm text-muted-foreground mb-2">Estimated Training Time</p>
                <div className="flex items-center gap-2">
                  <Loader2 className="w-4 h-4 text-primary" />
                  <p className="font-medium">~2-3 minutes</p>
                </div>
              </div>

              {/* Start Training Button */}
              <Button
                onClick={handleStartTraining}
                className="w-full bg-primary text-primary-foreground hover:bg-primary/90"
                disabled={!selectedDataset}
              >
                Start Training
              </Button>
            </div>
          </Card>
        </div>
      </div>
    </div>
  );
}
