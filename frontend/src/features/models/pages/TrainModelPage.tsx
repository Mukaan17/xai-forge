import { useState, useEffect, useRef } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import { motion, AnimatePresence } from 'framer-motion';
import { Card, CardContent, CardHeader, CardTitle } from '@/shared/components/ui/card';
import { Button } from '@/shared/components/ui/button';
import { Input } from '@/shared/components/ui/input';
import { Label } from '@/shared/components/ui/label';
import { Checkbox } from '@/shared/components/ui/checkbox';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/shared/components/ui/select';
import { Progress } from '@/shared/components/ui/progress';
import { 
  BrainCircuit, 
  Database, 
  Loader2, 
  CheckCircle2, 
  X,
  ChevronRight,
  Info
} from 'lucide-react';
import { datasetsApi } from '@/features/datasets/api/datasetsApi';
import { modelsApi, TrainRequest, TrainingProgressDto } from '../api/modelsApi';
import { DatasetDto } from '@/shared/types/dataset.types';
import { toast } from '@/shared/lib/toast';

const revealVariants = {
  hidden: {
    opacity: 0,
    filter: 'blur(12px)',
    y: 12,
  },
  visible: {
    opacity: 1,
    filter: 'blur(0px)',
    y: 0,
    transition: {
      type: 'spring',
      bounce: 0.3,
      duration: 1.5,
    },
  },
};

const staggerContainer = {
  visible: {
    transition: {
      staggerChildren: 0.05,
      delayChildren: 0.2,
    },
  },
};

export function TrainModelPage() {
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  
  const [selectedDatasetId, setSelectedDatasetId] = useState<number | null>(null);
  const [modelName, setModelName] = useState('');
  const [algorithm, setAlgorithm] = useState<'CLASSIFICATION' | 'REGRESSION'>('CLASSIFICATION');
  const [selectedAlgorithm, setSelectedAlgorithm] = useState<string>('logistic');
  const [targetColumn, setTargetColumn] = useState('');
  const [selectedFeatures, setSelectedFeatures] = useState<string[]>([]);
  const [trainTestSplit, setTrainTestSplit] = useState(80);
  const [crossValidation, setCrossValidation] = useState(false);
  const [hyperparameters, setHyperparameters] = useState<Record<string, unknown>>({});
  const [trainingStatus, setTrainingStatus] = useState<'config' | 'training' | 'complete'>('config');
  const [trainingProgress, setTrainingProgress] = useState(0);
  const [trainedModelId, setTrainedModelId] = useState<number | null>(null);
  const [trainingJobId, setTrainingJobId] = useState<number | null>(null);
  const [currentStep, setCurrentStep] = useState<string>('');

  // Fetch datasets
  const { data: datasets = [], isLoading: datasetsLoading } = useQuery<DatasetDto[]>({
    queryKey: ['datasets'],
    queryFn: async () => {
      return datasetsApi.getAll();
    },
  });

  // Fetch selected dataset details
  const { data: selectedDataset } = useQuery<DatasetDto>({
    queryKey: ['dataset', selectedDatasetId],
    queryFn: async () => {
      if (!selectedDatasetId) throw new Error('No dataset selected');
      return datasetsApi.getById(selectedDatasetId);
    },
    enabled: !!selectedDatasetId,
  });

  // Reset form when dataset changes
  useEffect(() => {
    if (selectedDataset) {
      setTargetColumn('');
      setSelectedFeatures([]);
    }
  }, [selectedDatasetId]);

  // Train mutation
  const trainMutation = useMutation({
    mutationFn: async (request: TrainRequest) => {
      const response = await modelsApi.train(request);
      return response;
    },
    onSuccess: (response) => {
      setTrainedModelId(response.id);
      setTrainingJobId(response.jobId);
      setTrainingStatus('training');
      queryClient.invalidateQueries({ queryKey: ['models'] });
    },
    onError: (error: Error) => {
      toast.error(`Training failed: ${error.message}`);
      setTrainingStatus('config');
    },
  });

  // Poll for training progress
  const { data: progressData } = useQuery<TrainingProgressDto>({
    queryKey: ['training-progress', trainedModelId],
    queryFn: async () => {
      if (!trainedModelId) throw new Error('No model ID');
      return modelsApi.getTrainingProgress(trainedModelId);
    },
    enabled: trainingStatus === 'training' && trainedModelId !== null,
    refetchInterval: (query) => {
      const data = query.state.data;
      if (data?.status === 'COMPLETED' || data?.status === 'FAILED' || data?.status === 'CANCELLED') {
        return false; // Stop polling when done
      }
      return 2000; // Poll every 2 seconds
    },
  });

  // Update progress state when progress data changes
  useEffect(() => {
    if (progressData) {
      setTrainingProgress(progressData.progress);
      setCurrentStep(progressData.currentStep || '');
      
      if (progressData.status === 'COMPLETED') {
        setTrainingStatus('complete');
        toast.success('Model training completed successfully!');
        queryClient.invalidateQueries({ queryKey: ['models'] });
      } else if (progressData.status === 'FAILED') {
        setTrainingStatus('config');
        toast.error(`Training failed: ${progressData.errorMessage || 'Unknown error'}`);
      } else if (progressData.status === 'CANCELLED') {
        setTrainingStatus('config');
        toast.info('Training was cancelled');
      }
    }
  }, [progressData, queryClient]);

  const handleFeatureToggle = (feature: string) => {
    setSelectedFeatures(prev => {
      if (prev.includes(feature)) {
        return prev.filter(f => f !== feature);
      } else {
        return [...prev, feature];
      }
    });
  };

  const handleSelectAllFeatures = () => {
    if (!selectedDataset) return;
    const availableFeatures = (selectedDataset.headers || []).filter(h => h !== targetColumn);
    if (selectedFeatures.length === availableFeatures.length) {
      setSelectedFeatures([]);
    } else {
      setSelectedFeatures([...availableFeatures]);
    }
  };

  const handleTrain = () => {
    if (!selectedDatasetId || !modelName || !targetColumn || selectedFeatures.length === 0) {
      toast.error('Please fill in all required fields');
      return;
    }

    const request: TrainRequest = {
      datasetId: selectedDatasetId,
      modelName,
      algorithm: selectedAlgorithm || (algorithm === 'CLASSIFICATION' ? 'logistic' : 'linear'),
      targetColumn,
      featureNames: selectedFeatures,
      trainTestSplit,
      crossValidation,
      hyperparameters: Object.keys(hyperparameters).length > 0 ? hyperparameters : undefined,
    };

    trainMutation.mutate(request);
  };

  const availableFeatures = (selectedDataset?.headers || []).filter(h => h !== targetColumn);
  const canTrain = selectedDatasetId && modelName && targetColumn && selectedFeatures.length > 0;

  if (trainingStatus === 'training') {
    return (
      <motion.div
        initial={{ opacity: 0 }}
        animate={{ opacity: 1 }}
        className="p-4 sm:p-6 lg:p-8 flex items-center justify-center min-h-[80vh]"
      >
        <motion.div
          initial={{ opacity: 0, filter: 'blur(12px)', y: 12 }}
          animate={{ opacity: 1, filter: 'blur(0px)', y: 0 }}
          transition={{ type: 'spring', bounce: 0.3, duration: 1.5 }}
        >
          <Card className="p-8 sm:p-12 max-w-2xl w-full text-center border-primary/20">
            <div className="w-32 h-32 mx-auto mb-8 relative">
              <div className="absolute inset-0 rounded-full border-4 border-primary/20"></div>
              <motion.div
                className="absolute inset-0 rounded-full border-4 border-primary border-t-transparent"
                animate={{ rotate: 360 }}
                transition={{ duration: 1, repeat: Infinity, ease: 'linear' }}
              />
              <div className="absolute inset-0 flex items-center justify-center">
                <Loader2 className="w-16 h-16 text-primary animate-spin" />
              </div>
            </div>
            
            <h2 className="text-2xl font-bold mb-4">Training Model</h2>
            
            {currentStep && (
              <p className="text-muted-foreground mb-6">{currentStep}</p>
            )}
            
            <div className="space-y-2 mb-6">
              <div className="flex justify-between text-sm text-muted-foreground">
                <span>Progress</span>
                <span>{trainingProgress}%</span>
              </div>
              <Progress value={trainingProgress} className="h-2" />
            </div>
            
            {progressData?.estimatedCompletionSeconds && progressData.estimatedCompletionSeconds > 0 && (
              <p className="text-sm text-muted-foreground">
                Estimated time remaining: {Math.ceil(progressData.estimatedCompletionSeconds / 60)} minutes
              </p>
            )}
            
            {trainingJobId && (
              <Button
                variant="outline"
                className="mt-6"
                onClick={async () => {
                  try {
                    await modelsApi.cancelTraining(trainingJobId);
                    setTrainingStatus('config');
                    toast.info('Training cancelled');
                  } catch (error) {
                    toast.error('Failed to cancel training');
                  }
                }}
              >
                Cancel Training
              </Button>
            )}
          </Card>
        </motion.div>
      </motion.div>
    );
  }

  if (trainingStatus === 'complete') {
    return (
      <motion.div
        initial={{ opacity: 0 }}
        animate={{ opacity: 1 }}
        className="p-4 sm:p-6 lg:p-8 flex items-center justify-center min-h-[80vh]"
      >
        <motion.div
          initial={{ opacity: 0, filter: 'blur(12px)', y: 12 }}
          animate={{ opacity: 1, filter: 'blur(0px)', y: 0 }}
          transition={{ type: 'spring', bounce: 0.3, duration: 1.5 }}
        >
          <Card className="p-8 sm:p-12 max-w-2xl w-full text-center border-primary/20">
            <motion.div
              initial={{ scale: 0 }}
              animate={{ scale: 1 }}
              transition={{ type: 'spring', bounce: 0.5, duration: 0.8 }}
              className="w-32 h-32 mx-auto mb-8 rounded-full bg-success/20 flex items-center justify-center"
            >
              <CheckCircle2 className="w-16 h-16 text-success" />
            </motion.div>
            
            <h2 className="text-2xl font-bold mb-4">Training Complete!</h2>
            <p className="text-muted-foreground mb-8">
              Your model has been trained successfully.
            </p>
            
            <div className="flex gap-4 justify-center">
              <Button
                onClick={() => {
                  setTrainingStatus('config');
                  setTrainedModelId(null);
                  setTrainingJobId(null);
                  setTrainingProgress(0);
                  setCurrentStep('');
                }}
                variant="outline"
              >
                Train Another Model
              </Button>
              <Button
                onClick={() => navigate('/models')}
              >
                View Models
              </Button>
            </div>
          </Card>
        </motion.div>
      </motion.div>
    );
  }

  // Config view
  return (
    <div className="p-4 sm:p-6 lg:p-8 space-y-6">
      <motion.div
        initial={{ opacity: 0, filter: 'blur(12px)', y: 12 }}
        animate={{ opacity: 1, filter: 'blur(0px)', y: 0 }}
        transition={{ type: 'spring', bounce: 0.3, duration: 1.5 }}
      >
        <h1 className="text-3xl font-bold mb-2 bg-gradient-to-r from-primary via-secondary to-primary bg-clip-text text-transparent">
          Train Model
        </h1>
        <p className="text-muted-foreground">
          Select a dataset and configure your model training parameters
        </p>
      </motion.div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Left Column - Dataset Selection & Configuration */}
        <motion.div
          initial={{ opacity: 0, filter: 'blur(12px)', x: -12 }}
          animate={{ opacity: 1, filter: 'blur(0px)', x: 0 }}
          transition={{ type: 'spring', bounce: 0.3, duration: 1.5, delay: 0.1 }}
        >
          {/* Dataset Selection */}
          <Card className="border-primary/20 mb-6">
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <Database className="w-5 h-5 text-primary" />
                Select Dataset
              </CardTitle>
            </CardHeader>
            <CardContent>
              {datasetsLoading ? (
                <div className="flex items-center justify-center py-8">
                  <Loader2 className="w-6 h-6 animate-spin text-muted-foreground" />
                </div>
              ) : datasets.length === 0 ? (
                <div className="text-center py-8">
                  <p className="text-muted-foreground mb-4">No datasets available</p>
                  <Button onClick={() => navigate('/datasets')} variant="outline">
                    Upload Dataset
                  </Button>
                </div>
              ) : (
                <Select
                  value={selectedDatasetId?.toString() || ''}
                  onValueChange={(value) => setSelectedDatasetId(parseInt(value))}
                >
                  <SelectTrigger className="bg-input border-border">
                    <SelectValue placeholder="Select a dataset" />
                  </SelectTrigger>
                  <SelectContent>
                    {datasets.map((dataset) => (
                      <SelectItem key={dataset.id} value={dataset.id.toString()}>
                        {dataset.fileName} ({dataset.rowCount} rows)
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              )}
            </CardContent>
          </Card>

          {selectedDataset && (
            <motion.div
              initial={{ opacity: 0, filter: 'blur(12px)', y: 12 }}
              animate={{ opacity: 1, filter: 'blur(0px)', y: 0 }}
              transition={{ type: 'spring', bounce: 0.3, duration: 1.5 }}
            >
              {/* Model Configuration */}
              <Card className="border-primary/20">
                <CardHeader>
                  <CardTitle className="flex items-center gap-2">
                    <BrainCircuit className="w-5 h-5 text-primary" />
                    Model Configuration
                  </CardTitle>
                </CardHeader>
                <CardContent className="space-y-4">
                  <div className="space-y-2">
                    <Label htmlFor="modelName">Model Name</Label>
                    <Input
                      id="modelName"
                      value={modelName}
                      onChange={(e) => setModelName(e.target.value)}
                      placeholder="e.g., Churn Predictor"
                      className="bg-input border-border"
                    />
                  </div>

                  <div className="space-y-2">
                    <Label htmlFor="algorithm">Algorithm Type</Label>
                    <Select value={algorithm} onValueChange={(value: 'CLASSIFICATION' | 'REGRESSION') => {
                      setAlgorithm(value);
                      setHyperparameters({});
                    }}>
                      <SelectTrigger className="bg-input border-border">
                        <SelectValue />
                      </SelectTrigger>
                      <SelectContent>
                        <SelectItem value="CLASSIFICATION">Classification</SelectItem>
                        <SelectItem value="REGRESSION">Regression</SelectItem>
                      </SelectContent>
                    </Select>
                    {algorithm === 'CLASSIFICATION' && (
                      <Select 
                        value={selectedAlgorithm || 'logistic'} 
                        onValueChange={(value) => {
                          setSelectedAlgorithm(value);
                          setHyperparameters({});
                        }}
                      >
                        <SelectTrigger className="bg-input border-border mt-2">
                          <SelectValue placeholder="Select algorithm" />
                        </SelectTrigger>
                        <SelectContent>
                          <SelectItem value="logistic">Logistic Regression</SelectItem>
                          <SelectItem value="random_forest">Random Forest</SelectItem>
                          <SelectItem value="neural_network">Neural Network (MLP)</SelectItem>
                          <SelectItem value="svm">Support Vector Machine (SVM)</SelectItem>
                        </SelectContent>
                      </Select>
                    )}
                    {algorithm === 'REGRESSION' && (
                      <Select 
                        value={selectedAlgorithm || 'linear'} 
                        onValueChange={(value) => {
                          setSelectedAlgorithm(value);
                          setHyperparameters({});
                        }}
                      >
                        <SelectTrigger className="bg-input border-border mt-2">
                          <SelectValue placeholder="Select algorithm" />
                        </SelectTrigger>
                        <SelectContent>
                          <SelectItem value="linear">Linear Regression (SGD)</SelectItem>
                          <SelectItem value="random_forest">Random Forest</SelectItem>
                        </SelectContent>
                      </Select>
                    )}
                  </div>

                  <div className="space-y-2">
                    <Label htmlFor="targetColumn">Target Variable</Label>
                    <Select value={targetColumn} onValueChange={setTargetColumn}>
                      <SelectTrigger className="bg-input border-border">
                        <SelectValue placeholder="Select target column" />
                      </SelectTrigger>
                      <SelectContent>
                        {(selectedDataset.headers || []).map((header) => (
                          <SelectItem key={header} value={header}>
                            {header}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                  </div>

                  {/* Training Configuration */}
                  <div className="pt-4 border-t border-border space-y-4">
                    <div className="flex items-center gap-2 text-sm text-muted-foreground">
                      <Info className="w-4 h-4" />
                      <span>Advanced Configuration</span>
                    </div>
                    <div className="space-y-2">
                      <Label htmlFor="trainTestSplit">Train/Test Split: {trainTestSplit}%</Label>
                      <input
                        id="trainTestSplit"
                        type="range"
                        min="50"
                        max="90"
                        value={trainTestSplit}
                        onChange={(e) => setTrainTestSplit(parseInt(e.target.value))}
                        className="w-full"
                      />
                      <div className="flex justify-between text-xs text-muted-foreground">
                        <span>50%</span>
                        <span>90%</span>
                      </div>
                    </div>
                    <div className="flex items-center space-x-2">
                      <Checkbox
                        id="crossValidation"
                        checked={crossValidation}
                        onCheckedChange={(checked) => setCrossValidation(checked === true)}
                      />
                      <Label htmlFor="crossValidation" className="cursor-pointer">
                        Enable Cross-Validation (5-fold)
                      </Label>
                    </div>
                    
                    {/* Hyperparameters Section */}
                    {(selectedAlgorithm === 'random_forest' || selectedAlgorithm === 'neural_network' || selectedAlgorithm === 'svm') && (
                      <div className="pt-4 border-t border-border space-y-3">
                        <Label className="text-sm font-medium">Hyperparameters</Label>
                        {selectedAlgorithm === 'random_forest' && (
                          <div className="space-y-2 text-sm">
                            <div>
                              <Label htmlFor="numTrees" className="text-xs">Number of Trees</Label>
                              <Input
                                id="numTrees"
                                type="number"
                                min="10"
                                max="500"
                                defaultValue="100"
                                onChange={(e) => setHyperparameters({
                                  ...hyperparameters,
                                  numTrees: parseInt(e.target.value) || 100
                                })}
                                className="h-8"
                              />
                            </div>
                            <div>
                              <Label htmlFor="maxDepth" className="text-xs">Max Depth</Label>
                              <Input
                                id="maxDepth"
                                type="number"
                                min="1"
                                max="50"
                                defaultValue="10"
                                onChange={(e) => setHyperparameters({
                                  ...hyperparameters,
                                  maxDepth: parseInt(e.target.value) || 10
                                })}
                                className="h-8"
                              />
                            </div>
                          </div>
                        )}
                        {selectedAlgorithm === 'neural_network' && (
                          <div className="space-y-2 text-sm">
                            <div>
                              <Label htmlFor="epochs" className="text-xs">Epochs</Label>
                              <Input
                                id="epochs"
                                type="number"
                                min="1"
                                max="1000"
                                defaultValue="100"
                                onChange={(e) => setHyperparameters({
                                  ...hyperparameters,
                                  epochs: parseInt(e.target.value) || 100
                                })}
                                className="h-8"
                              />
                            </div>
                            <div>
                              <Label htmlFor="learningRate" className="text-xs">Learning Rate</Label>
                              <Input
                                id="learningRate"
                                type="number"
                                step="0.001"
                                min="0.0001"
                                max="1"
                                defaultValue="0.01"
                                onChange={(e) => setHyperparameters({
                                  ...hyperparameters,
                                  learningRate: parseFloat(e.target.value) || 0.01
                                })}
                                className="h-8"
                              />
                            </div>
                            <div>
                              <Label htmlFor="batchSize" className="text-xs">Batch Size</Label>
                              <Input
                                id="batchSize"
                                type="number"
                                min="1"
                                max="256"
                                defaultValue="32"
                                onChange={(e) => setHyperparameters({
                                  ...hyperparameters,
                                  batchSize: parseInt(e.target.value) || 32
                                })}
                                className="h-8"
                              />
                            </div>
                          </div>
                        )}
                        {selectedAlgorithm === 'svm' && (
                          <div className="space-y-2 text-sm">
                            <div>
                              <Label htmlFor="svmEpochs" className="text-xs">Epochs</Label>
                              <Input
                                id="svmEpochs"
                                type="number"
                                min="1"
                                max="1000"
                                defaultValue="100"
                                onChange={(e) => setHyperparameters({
                                  ...hyperparameters,
                                  epochs: parseInt(e.target.value) || 100
                                })}
                                className="h-8"
                              />
                            </div>
                            <div>
                              <Label htmlFor="svmLearningRate" className="text-xs">Learning Rate</Label>
                              <Input
                                id="svmLearningRate"
                                type="number"
                                step="0.001"
                                min="0.0001"
                                max="1"
                                defaultValue="0.01"
                                onChange={(e) => setHyperparameters({
                                  ...hyperparameters,
                                  learningRate: parseFloat(e.target.value) || 0.01
                                })}
                                className="h-8"
                              />
                            </div>
                          </div>
                        )}
                      </div>
                    )}
                  </div>
                </CardContent>
              </Card>
            </motion.div>
          )}
        </motion.div>

        {/* Right Column - Feature Selection */}
        {selectedDataset && (
          <motion.div
            initial={{ opacity: 0, filter: 'blur(12px)', x: 12 }}
            animate={{ opacity: 1, filter: 'blur(0px)', x: 0 }}
            transition={{ type: 'spring', bounce: 0.3, duration: 1.5, delay: 0.2 }}
          >
            <Card className="border-primary/20">
              <CardHeader>
                <CardTitle className="flex items-center justify-between">
                  <span className="flex items-center gap-2">
                    <Database className="w-5 h-5 text-primary" />
                    Select Features
                  </span>
                  <Button
                    variant="ghost"
                    size="sm"
                    onClick={handleSelectAllFeatures}
                  >
                    {selectedFeatures.length === availableFeatures.length ? 'Deselect All' : 'Select All'}
                  </Button>
                </CardTitle>
              </CardHeader>
              <CardContent>
                {availableFeatures.length === 0 ? (
                  <p className="text-muted-foreground text-center py-8">
                    No features available (target column excluded)
                  </p>
                ) : (
                  <div className="space-y-2 max-h-[600px] overflow-y-auto">
                    {availableFeatures.map((feature) => (
                      <motion.div
                        key={feature}
                        initial={{ opacity: 0, x: -12 }}
                        animate={{ opacity: 1, x: 0 }}
                        transition={{ duration: 0.2 }}
                        className="flex items-center space-x-2 p-2 rounded-lg hover:bg-muted/50 transition-colors"
                      >
                        <Checkbox
                          id={`feature-${feature}`}
                          checked={selectedFeatures.includes(feature)}
                          onCheckedChange={() => handleFeatureToggle(feature)}
                        />
                        <Label
                          htmlFor={`feature-${feature}`}
                          className="flex-1 cursor-pointer text-sm"
                        >
                          {feature}
                        </Label>
                      </motion.div>
                    ))}
                  </div>
                )}
              </CardContent>
            </Card>
          </motion.div>
        )}
      </div>

      {/* Train Button */}
      {selectedDataset && (
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.4, type: 'spring', bounce: 0.3, duration: 1.5 }}
          className="flex justify-end"
        >
          <Button
            onClick={handleTrain}
            disabled={!canTrain || trainMutation.isPending}
            size="lg"
            className="bg-primary text-primary-foreground hover:bg-primary/90 px-8"
          >
            {trainMutation.isPending ? (
              <>
                <Loader2 className="w-4 h-4 mr-2 animate-spin" />
                Starting Training...
              </>
            ) : (
              <>
                <BrainCircuit className="w-4 h-4 mr-2" />
                Train Model
                <ChevronRight className="w-4 h-4 ml-2" />
              </>
            )}
          </Button>
        </motion.div>
      )}
    </div>
  );
}
