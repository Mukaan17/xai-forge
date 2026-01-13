import { useState } from 'react';
import { useQuery, useMutation } from '@tanstack/react-query';
import { apiClient } from '@/shared/lib/api/client';
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/shared/components/ui/card';
import { Button } from '@/shared/components/ui/button';
import { Input } from '@/shared/components/ui/input';
import { Label } from '@/shared/components/ui/label';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/shared/components/ui/select';
import { Target, Sparkles, Loader2 } from 'lucide-react';
import { toast } from '@/shared/lib/toast';

interface Model {
  id: number;
  modelName: string;
  modelType: string;
  featureNames: string[];
  targetVariable: string;
}

interface PredictionResponse {
  prediction: string;
  confidence: number | null;
  featureImportance: Record<string, number>;
}

interface ExplanationResponse {
  summary: string;
  featureImpacts: Array<{
    feature: string;
    impact: number;
    direction: string;
    contribution: number;
  }>;
  metadata: Record<string, unknown>;
}

export function PredictionsPage() {
  const [selectedModel, setSelectedModel] = useState<number | null>(null);
  const [inputData, setInputData] = useState<Record<string, string>>({});
  const [prediction, setPrediction] = useState<PredictionResponse | null>(null);
  const [explanation, setExplanation] = useState<ExplanationResponse | null>(null);

  const { data: models, isLoading: modelsLoading } = useQuery<Model[]>({
    queryKey: ['models'],
    queryFn: async () => {
      const response = await apiClient.get<Model[]>('/v1/models');
      return response;
    },
  });

  const selectedModelData = models?.find(m => m.id === selectedModel);

  const predictMutation = useMutation({
    mutationFn: async (data: Record<string, string>) => {
      if (!selectedModel) throw new Error('Please select a model');
      return apiClient.post<PredictionResponse>(`/v1/models/${selectedModel}/predict`, data);
    },
    onSuccess: (data) => {
      setPrediction(data);
      toast.success('Prediction made successfully!');
    },
    onError: (error: Error) => {
      toast.error(`Prediction failed: ${error.message}`);
    },
  });

  const explainMutation = useMutation({
    mutationFn: async (data: Record<string, string>) => {
      if (!selectedModel) throw new Error('Please select a model');
      return apiClient.post<ExplanationResponse>(`/v1/models/${selectedModel}/explain`, data);
    },
    onSuccess: (data) => {
      setExplanation(data);
      toast.success('Explanation generated!');
    },
    onError: (error: Error) => {
      toast.error(`Explanation failed: ${error.message}`);
    },
  });

  const handleModelChange = (modelId: string) => {
    const id = parseInt(modelId);
    setSelectedModel(id);
    setInputData({});
    setPrediction(null);
    setExplanation(null);
  };

  const handleInputChange = (feature: string, value: string) => {
    setInputData(prev => ({ ...prev, [feature]: value }));
  };

  const handlePredict = () => {
    if (!selectedModelData) {
      toast.error('Please select a model');
      return;
    }

    // Validate all required features are provided
    const requiredFeatures = selectedModelData.featureNames.filter(
      f => f !== selectedModelData.targetVariable
    );
    const missingFeatures = requiredFeatures.filter(f => !inputData[f] || inputData[f].trim() === '');

    if (missingFeatures.length > 0) {
      toast.error(`Please provide values for: ${missingFeatures.join(', ')}`);
      return;
    }

    predictMutation.mutate(inputData);
  };

  const handleExplain = () => {
    if (!prediction) {
      toast.error('Please make a prediction first');
      return;
    }
    explainMutation.mutate(inputData);
  };

  return (
    <div className="p-4 sm:p-6 lg:p-8 space-y-4 sm:space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-xl sm:text-2xl font-semibold">Make Predictions</h1>
          <p className="text-muted-foreground text-sm sm:text-base">Use your trained models to make predictions</p>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-4 sm:gap-6">
        {/* Model Selection and Input Form */}
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <Target className="w-5 h-5" />
              Prediction Form
            </CardTitle>
            <CardDescription>Select a model and provide input values</CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="model">Select Model</Label>
              <Select
                value={selectedModel?.toString() || ''}
                onValueChange={handleModelChange}
                disabled={modelsLoading}
              >
                <SelectTrigger>
                  <SelectValue placeholder="Choose a model..." />
                </SelectTrigger>
                <SelectContent>
                  {models?.map((model) => (
                    <SelectItem key={model.id} value={model.id.toString()}>
                      {model.modelName} ({model.modelType})
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>

            {selectedModelData && (
              <div className="space-y-4 pt-4 border-t">
                <Label>Input Features</Label>
                {selectedModelData.featureNames
                  .filter(f => f !== selectedModelData.targetVariable)
                  .map((feature) => (
                    <div key={feature} className="space-y-2">
                      <Label htmlFor={feature}>{feature}</Label>
                      <Input
                        id={feature}
                        type="number"
                        step="any"
                        value={inputData[feature] || ''}
                        onChange={(e) => handleInputChange(feature, e.target.value)}
                        placeholder={`Enter ${feature}`}
                      />
                    </div>
                  ))}
                <Button
                  onClick={handlePredict}
                  disabled={predictMutation.isPending}
                  className="w-full"
                >
                  {predictMutation.isPending ? (
                    <>
                      <Loader2 className="w-4 h-4 mr-2 animate-spin" />
                      Predicting...
                    </>
                  ) : (
                    <>
                      <Target className="w-4 h-4 mr-2" />
                      Make Prediction
                    </>
                  )}
                </Button>
              </div>
            )}
          </CardContent>
        </Card>

        {/* Results */}
        <div className="space-y-6">
          {prediction && (
            <Card>
              <CardHeader>
                <CardTitle>Prediction Result</CardTitle>
              </CardHeader>
              <CardContent className="space-y-4">
                <div>
                  <Label className="text-muted-foreground">Prediction</Label>
                  <p className="text-2xl font-bold">{prediction.prediction}</p>
                </div>
                {prediction.confidence !== null && (
                  <div>
                    <Label className="text-muted-foreground">Confidence</Label>
                    <p className="text-lg">{(prediction.confidence * 100).toFixed(2)}%</p>
                  </div>
                )}
                <Button
                  onClick={handleExplain}
                  disabled={explainMutation.isPending}
                  variant="outline"
                  className="w-full"
                >
                  {explainMutation.isPending ? (
                    <>
                      <Loader2 className="w-4 h-4 mr-2 animate-spin" />
                      Generating...
                    </>
                  ) : (
                    <>
                      <Sparkles className="w-4 h-4 mr-2" />
                      Get Explanation
                    </>
                  )}
                </Button>
              </CardContent>
            </Card>
          )}

          {explanation && (
            <Card>
              <CardHeader>
                <CardTitle className="flex items-center gap-2">
                  <Sparkles className="w-5 h-5" />
                  Explanation
                </CardTitle>
              </CardHeader>
              <CardContent className="space-y-4">
                <div>
                  <Label className="text-muted-foreground">Summary</Label>
                  <p className="text-sm">{explanation.summary}</p>
                </div>
                <div>
                  <Label className="text-muted-foreground">Feature Impacts</Label>
                  <div className="space-y-2 mt-2">
                    {explanation.featureImpacts.map((impact, idx) => (
                      <div key={idx} className="flex items-center justify-between p-2 bg-muted rounded-md">
                        <span className="text-sm">{impact.feature}</span>
                        <span className="text-sm font-medium">
                          {impact.direction === 'positive' ? '+' : '-'}
                          {(Math.abs(impact.impact) * 100).toFixed(1)}%
                        </span>
                      </div>
                    ))}
                  </div>
                </div>
              </CardContent>
            </Card>
          )}
        </div>
      </div>
    </div>
  );
}
