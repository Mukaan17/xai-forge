import { useState, useEffect } from 'react';
import { BrainCircuit, Sparkles, AlertCircle, CheckCircle2, Play, Settings as SettingsIcon } from 'lucide-react';
import { Button } from './ui/button';
import { Card } from './ui/card';
import { Label } from './ui/label';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from './ui/select';
import { Progress } from './ui/progress';
import { Slider } from './ui/slider';
import { Switch } from './ui/switch';
import { useStore } from '../lib/store';
import { toast } from 'sonner';

interface ModelTrainingEnhancedProps {
  onNavigate: (page: string) => void;
}

export function ModelTrainingEnhanced({ onNavigate }: ModelTrainingEnhancedProps) {
  const { datasets, addModel, isTraining, trainingProgress, setIsTraining, setTrainingProgress } = useStore();
  const [selectedDataset, setSelectedDataset] = useState('');
  const [selectedAlgorithm, setSelectedAlgorithm] = useState('');
  const [modelName, setModelName] = useState('');
  const [targetColumn, setTargetColumn] = useState('');
  const [trainTestSplit, setTrainTestSplit] = useState(80);
  const [crossValidation, setCrossValidation] = useState(true);
  const [showAdvanced, setShowAdvanced] = useState(false);
  const [trainingStage, setTrainingStage] = useState<'idle' | 'preparing' | 'training' | 'evaluating' | 'complete'>('idle');

  const algorithms = {
    classification: [
      { value: 'logistic', label: 'Logistic Regression', description: 'Fast, interpretable binary/multiclass classification' },
      { value: 'random-forest', label: 'Random Forest', description: 'Ensemble method with high accuracy' },
      { value: 'gradient-boost', label: 'Gradient Boosting', description: 'Powerful ensemble for complex patterns' },
      { value: 'svm', label: 'Support Vector Machine', description: 'Effective for high-dimensional data' },
    ],
    regression: [
      { value: 'linear', label: 'Linear Regression', description: 'Simple, interpretable predictions' },
      { value: 'ridge', label: 'Ridge Regression', description: 'Linear with L2 regularization' },
      { value: 'random-forest-reg', label: 'Random Forest Regressor', description: 'Non-linear ensemble method' },
      { value: 'xgboost', label: 'XGBoost', description: 'State-of-the-art gradient boosting' },
    ],
  };

  const selectedDatasetObj = datasets.find(d => d.id === selectedDataset);
  const availableAlgorithms = selectedDatasetObj?.type === 'Classification' 
    ? algorithms.classification 
    : algorithms.regression;

  const mockColumns = ['age', 'income', 'account_tenure', 'monthly_charges', 'contract_type', 'tech_support', 'will_churn'];

  const handleStartTraining = async () => {
    if (!selectedDataset || !selectedAlgorithm || !targetColumn) {
      toast.error('Missing required fields', {
        description: 'Please select dataset, algorithm, and target column',
      });
      return;
    }

    setIsTraining(true);
    setTrainingStage('preparing');
    setTrainingProgress(0);

    // Stage 1: Preparing data
    await simulateProgress(0, 20, 1000);
    setTrainingStage('training');

    // Stage 2: Training model
    await simulateProgress(20, 70, 3000);
    setTrainingStage('evaluating');

    // Stage 3: Evaluating
    await simulateProgress(70, 100, 1500);
    setTrainingStage('complete');

    // Generate model
    const newModel = {
      id: Date.now().toString(),
      name: modelName || `${selectedAlgorithm} Model`,
      algorithm: availableAlgorithms.find(a => a.value === selectedAlgorithm)?.label || selectedAlgorithm,
      dataset: selectedDatasetObj?.name || '',
      accuracy: 85 + Math.random() * 10,
      precision: 0.80 + Math.random() * 0.15,
      recall: 0.80 + Math.random() * 0.15,
      f1Score: 0.80 + Math.random() * 0.15,
      trainedDate: new Date().toISOString().split('T')[0],
      status: 'ready' as const,
      trainingTime: `${Math.floor(15 + Math.random() * 20)}s`,
      features: mockColumns.length - 1,
    };

    addModel(newModel);
    
    toast.success('Model trained successfully!', {
      description: `${newModel.name} achieved ${newModel.accuracy.toFixed(1)}% accuracy`,
      action: {
        label: 'View Model',
        onClick: () => onNavigate('models-all'),
      },
    });

    setTimeout(() => {
      setIsTraining(false);
      setTrainingProgress(0);
      setTrainingStage('idle');
      onNavigate('models-all');
    }, 2000);
  };

  const simulateProgress = async (start: number, end: number, duration: number) => {
    const steps = 20;
    const stepSize = (end - start) / steps;
    const stepDuration = duration / steps;

    for (let i = 0; i <= steps; i++) {
      await new Promise(resolve => setTimeout(resolve, stepDuration));
      setTrainingProgress(Math.min(start + stepSize * i, end));
    }
  };

  return (
    <div className="p-8 max-w-4xl mx-auto space-y-6">
      {/* Header */}
      <div>
        <h1>Train New Model</h1>
        <p className="text-muted-foreground mt-1">Configure and train a machine learning model</p>
      </div>

      {!isTraining ? (
        <div className="space-y-6">
          {/* Model Configuration */}
          <Card className="p-6">
            <h3 className="mb-6">Model Configuration</h3>
            
            <div className="space-y-6">
              {/* Dataset Selection */}
              <div>
                <Label>Select Dataset</Label>
                <Select value={selectedDataset} onValueChange={setSelectedDataset}>
                  <SelectTrigger className="mt-2">
                    <SelectValue placeholder="Choose a dataset..." />
                  </SelectTrigger>
                  <SelectContent>
                    {datasets.map((dataset) => (
                      <SelectItem key={dataset.id} value={dataset.id}>
                        {dataset.name} ({dataset.rows.toLocaleString()} rows)
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>

              {selectedDataset && (
                <>
                  {/* Target Column */}
                  <div>
                    <Label>Target Column (what to predict)</Label>
                    <Select value={targetColumn} onValueChange={setTargetColumn}>
                      <SelectTrigger className="mt-2">
                        <SelectValue placeholder="Select target column..." />
                      </SelectTrigger>
                      <SelectContent>
                        {mockColumns.map((col) => (
                          <SelectItem key={col} value={col}>{col}</SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                  </div>

                  {/* Algorithm Selection */}
                  <div>
                    <Label>Algorithm</Label>
                    <div className="grid grid-cols-2 gap-3 mt-2">
                      {availableAlgorithms.map((algo) => (
                        <button
                          key={algo.value}
                          onClick={() => setSelectedAlgorithm(algo.value)}
                          className={`p-4 border rounded-lg text-left transition-colors ${
                            selectedAlgorithm === algo.value
                              ? 'border-primary bg-primary/10'
                              : 'border-border hover:border-primary/50'
                          }`}
                        >
                          <p className="font-medium mb-1">{algo.label}</p>
                          <p className="text-sm text-muted-foreground">{algo.description}</p>
                        </button>
                      ))}
                    </div>
                  </div>
                </>
              )}
            </div>
          </Card>

          {/* Advanced Settings */}
          {selectedDataset && (
            <Card className="p-6">
              <button
                onClick={() => setShowAdvanced(!showAdvanced)}
                className="flex items-center justify-between w-full mb-4"
              >
                <h3>Advanced Settings</h3>
                <SettingsIcon className={`w-5 h-5 transition-transform ${showAdvanced ? 'rotate-90' : ''}`} />
              </button>

              {showAdvanced && (
                <div className="space-y-6 pt-4">
                  {/* Train/Test Split */}
                  <div>
                    <div className="flex items-center justify-between mb-3">
                      <Label>Train/Test Split</Label>
                      <span className="text-sm text-muted-foreground">{trainTestSplit}% / {100 - trainTestSplit}%</span>
                    </div>
                    <Slider
                      value={[trainTestSplit]}
                      onValueChange={([value]) => setTrainTestSplit(value)}
                      min={60}
                      max={90}
                      step={5}
                    />
                  </div>

                  {/* Cross Validation */}
                  <div className="flex items-center justify-between">
                    <div>
                      <Label>Enable Cross-Validation</Label>
                      <p className="text-sm text-muted-foreground mt-1">
                        Use 5-fold cross-validation for more robust evaluation
                      </p>
                    </div>
                    <Switch checked={crossValidation} onCheckedChange={setCrossValidation} />
                  </div>
                </div>
              )}
            </Card>
          )}

          {/* Training Summary */}
          {selectedDataset && selectedAlgorithm && targetColumn && (
            <Card className="p-6 bg-gradient-to-br from-primary/5 to-secondary/5 border-primary/20">
              <h3 className="mb-4">Training Summary</h3>
              <div className="grid grid-cols-2 gap-4 mb-6">
                <div>
                  <p className="text-sm text-muted-foreground mb-1">Dataset</p>
                  <p className="font-medium">{selectedDatasetObj?.name}</p>
                </div>
                <div>
                  <p className="text-sm text-muted-foreground mb-1">Algorithm</p>
                  <p className="font-medium">
                    {availableAlgorithms.find(a => a.value === selectedAlgorithm)?.label}
                  </p>
                </div>
                <div>
                  <p className="text-sm text-muted-foreground mb-1">Target Column</p>
                  <p className="font-medium">{targetColumn}</p>
                </div>
                <div>
                  <p className="text-sm text-muted-foreground mb-1">Training Samples</p>
                  <p className="font-medium">
                    {Math.floor((selectedDatasetObj?.rows || 0) * trainTestSplit / 100).toLocaleString()}
                  </p>
                </div>
              </div>
              <Button
                onClick={handleStartTraining}
                className="w-full bg-primary text-primary-foreground hover:bg-primary/90"
                size="lg"
              >
                <Play className="w-5 h-5 mr-2" />
                Start Training
              </Button>
            </Card>
          )}
        </div>
      ) : (
        /* Training Progress */
        <Card className="p-8">
          <div className="text-center mb-8">
            <div className="w-20 h-20 rounded-full bg-gradient-to-br from-primary/20 to-secondary/20 flex items-center justify-center mx-auto mb-4">
              <BrainCircuit className="w-10 h-10 text-primary animate-pulse" />
            </div>
            <h2 className="mb-2">
              {trainingStage === 'preparing' && 'Preparing Data...'}
              {trainingStage === 'training' && 'Training Model...'}
              {trainingStage === 'evaluating' && 'Evaluating Performance...'}
              {trainingStage === 'complete' && 'Training Complete!'}
            </h2>
            <p className="text-muted-foreground">
              {trainingStage === 'preparing' && 'Preprocessing features and splitting data'}
              {trainingStage === 'training' && 'Optimizing model parameters'}
              {trainingStage === 'evaluating' && 'Calculating performance metrics'}
              {trainingStage === 'complete' && 'Model is ready for predictions'}
            </p>
          </div>

          <Progress value={trainingProgress} className="mb-4 h-3" />
          <p className="text-center text-sm text-muted-foreground mb-8">
            {Math.round(trainingProgress)}%
          </p>

          {/* Training Stages */}
          <div className="space-y-3">
            {[
              { stage: 'preparing', label: 'Data Preparation', range: '0-20%' },
              { stage: 'training', label: 'Model Training', range: '20-70%' },
              { stage: 'evaluating', label: 'Performance Evaluation', range: '70-100%' },
            ].map((item, i) => (
              <div
                key={item.stage}
                className={`flex items-center gap-3 p-3 rounded-lg ${
                  trainingStage === item.stage
                    ? 'bg-primary/10 border border-primary/30'
                    : trainingProgress >= (i === 2 ? 70 : i === 1 ? 20 : 0)
                    ? 'bg-success/10 border border-success/30'
                    : 'bg-muted/20 border border-border'
                }`}
              >
                {trainingProgress >= (i === 2 ? 100 : i === 1 ? 70 : 20) ? (
                  <CheckCircle2 className="w-5 h-5 text-success" />
                ) : trainingStage === item.stage ? (
                  <div className="w-5 h-5 rounded-full border-2 border-primary border-t-transparent animate-spin" />
                ) : (
                  <div className="w-5 h-5 rounded-full border-2 border-muted" />
                )}
                <div className="flex-1">
                  <p className="font-medium">{item.label}</p>
                  <p className="text-sm text-muted-foreground">{item.range}</p>
                </div>
              </div>
            ))}
          </div>
        </Card>
      )}
    </div>
  );
}
