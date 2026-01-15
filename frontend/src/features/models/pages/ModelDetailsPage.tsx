import { useQuery } from '@tanstack/react-query';
import { useParams, Link } from 'react-router-dom';
import { apiClient } from '@/shared/lib/api/client';
import { Card, CardContent, CardHeader, CardTitle } from '@/shared/components/ui/card';
import { Button } from '@/shared/components/ui/button';
import { Skeleton } from '@/shared/components/ui/skeleton';
import { ArrowLeft, Target, Zap, TrendingUp, BarChart3 } from 'lucide-react';
import { ConfusionMatrix } from '../components/ConfusionMatrix';
import { RocCurve } from '../components/RocCurve';
import { FeatureImportanceChart } from '../components/FeatureImportanceChart';

interface ModelDto {
  id: number;
  modelName: string;
  modelType: string;
  trainingDate: string;
  targetVariable: string;
  featureNames: string[];
  accuracy: number | null;
  precision: number | null;
  recall: number | null;
  f1Score: number | null;
  trainingTime: number | null;
  status: string;
  datasetId: number;
}

interface ExtendedMetricsDto {
  accuracy: number | null;
  precision: number | null;
  recall: number | null;
  f1Score: number | null;
  mse: number | null;
  rmse: number | null;
  mae: number | null;
  r2Score: number | null;
  confusionMatrix: number[][] | null;
  classLabels: string[] | null;
  rocCurve: Array<{
    falsePositiveRate: number;
    truePositiveRate: number;
    threshold: number;
  }> | null;
  featureImportance: Record<string, number> | null;
  trainingHistory: Array<Record<string, unknown>> | null;
}

export function ModelDetailsPage() {
  const { id } = useParams<{ id: string }>();

  const { data: model, isLoading: modelLoading } = useQuery<ModelDto>({
    queryKey: ['model', id],
    queryFn: async () => {
      const response = await apiClient.get<ModelDto>(`/v1/models/${id}`);
      return response;
    },
    enabled: !!id,
  });

  const { data: metrics, isLoading: metricsLoading } = useQuery<ExtendedMetricsDto>({
    queryKey: ['model', id, 'metrics'],
    queryFn: async () => {
      const response = await apiClient.get<ExtendedMetricsDto>(`/v1/models/${id}/metrics`);
      return response;
    },
    enabled: !!id,
  });

  if (modelLoading || metricsLoading) {
    return (
      <div className="p-8 space-y-8">
        <Skeleton className="h-32 w-full" />
        <div className="grid grid-cols-4 gap-6">
          {[1, 2, 3, 4].map((i) => (
            <Skeleton key={i} className="h-32" />
          ))}
        </div>
      </div>
    );
  }

  if (!model) {
    return (
      <div className="p-8">
        <p>Model not found</p>
        <Button asChild className="mt-4">
          <Link to="/models">Back to Models</Link>
        </Button>
      </div>
    );
  }

  const isClassification = model.modelType === 'CLASSIFICATION';

  return (
    <div className="space-y-6 sm:space-y-8">
      {/* Header */}
      <div className="flex items-center gap-4">
        <Button variant="ghost" size="icon" asChild>
          <Link to="/models">
            <ArrowLeft className="w-4 h-4" />
          </Link>
        </Button>
        <div>
          <h1 className="text-2xl sm:text-3xl font-semibold">{model.modelName}</h1>
          <p className="text-muted-foreground">
            {model.modelType} • Trained {new Date(model.trainingDate).toLocaleDateString()}
          </p>
        </div>
      </div>

      {/* Basic Metrics */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 sm:gap-6">
        {isClassification ? (
          <>
            <Card>
              <CardContent className="p-6">
                <div className="flex items-center gap-3 mb-3">
                  <div className="w-10 h-10 rounded-lg bg-primary/10 flex items-center justify-center">
                    <Target className="w-5 h-5 text-primary" />
                  </div>
                </div>
                <p className="text-sm text-muted-foreground mb-1">Accuracy</p>
                <p className="text-3xl font-semibold">
                  {metrics?.accuracy != null ? `${(metrics.accuracy * 100).toFixed(1)}%` : 'N/A'}
                </p>
              </CardContent>
            </Card>
            <Card>
              <CardContent className="p-6">
                <div className="flex items-center gap-3 mb-3">
                  <div className="w-10 h-10 rounded-lg bg-primary/10 flex items-center justify-center">
                    <Zap className="w-5 h-5 text-primary" />
                  </div>
                </div>
                <p className="text-sm text-muted-foreground mb-1">Precision</p>
                <p className="text-3xl font-semibold">
                  {metrics?.precision != null ? metrics.precision.toFixed(3) : 'N/A'}
                </p>
              </CardContent>
            </Card>
            <Card>
              <CardContent className="p-6">
                <div className="flex items-center gap-3 mb-3">
                  <div className="w-10 h-10 rounded-lg bg-primary/10 flex items-center justify-center">
                    <TrendingUp className="w-5 h-5 text-primary" />
                  </div>
                </div>
                <p className="text-sm text-muted-foreground mb-1">Recall</p>
                <p className="text-3xl font-semibold">
                  {metrics?.recall != null ? metrics.recall.toFixed(3) : 'N/A'}
                </p>
              </CardContent>
            </Card>
            <Card>
              <CardContent className="p-6">
                <div className="flex items-center gap-3 mb-3">
                  <div className="w-10 h-10 rounded-lg bg-primary/10 flex items-center justify-center">
                    <BarChart3 className="w-5 h-5 text-primary" />
                  </div>
                </div>
                <p className="text-sm text-muted-foreground mb-1">F1 Score</p>
                <p className="text-3xl font-semibold">
                  {metrics?.f1Score != null ? metrics.f1Score.toFixed(3) : 'N/A'}
                </p>
              </CardContent>
            </Card>
          </>
        ) : (
          <>
            <Card>
              <CardContent className="p-6">
                <div className="flex items-center gap-3 mb-3">
                  <div className="w-10 h-10 rounded-lg bg-primary/10 flex items-center justify-center">
                    <Target className="w-5 h-5 text-primary" />
                  </div>
                </div>
                <p className="text-sm text-muted-foreground mb-1">R² Score</p>
                <p className="text-3xl font-semibold">
                  {metrics?.r2Score != null ? metrics.r2Score.toFixed(3) : 'N/A'}
                </p>
              </CardContent>
            </Card>
            <Card>
              <CardContent className="p-6">
                <div className="flex items-center gap-3 mb-3">
                  <div className="w-10 h-10 rounded-lg bg-primary/10 flex items-center justify-center">
                    <BarChart3 className="w-5 h-5 text-primary" />
                  </div>
                </div>
                <p className="text-sm text-muted-foreground mb-1">RMSE</p>
                <p className="text-3xl font-semibold">
                  {metrics?.rmse != null ? metrics.rmse.toFixed(3) : 'N/A'}
                </p>
              </CardContent>
            </Card>
            <Card>
              <CardContent className="p-6">
                <div className="flex items-center gap-3 mb-3">
                  <div className="w-10 h-10 rounded-lg bg-primary/10 flex items-center justify-center">
                    <TrendingUp className="w-5 h-5 text-primary" />
                  </div>
                </div>
                <p className="text-sm text-muted-foreground mb-1">MAE</p>
                <p className="text-3xl font-semibold">
                  {metrics?.mae != null ? metrics.mae.toFixed(3) : 'N/A'}
                </p>
              </CardContent>
            </Card>
            <Card>
              <CardContent className="p-6">
                <div className="flex items-center gap-3 mb-3">
                  <div className="w-10 h-10 rounded-lg bg-primary/10 flex items-center justify-center">
                    <Zap className="w-5 h-5 text-primary" />
                  </div>
                </div>
                <p className="text-sm text-muted-foreground mb-1">MSE</p>
                <p className="text-3xl font-semibold">
                  {metrics?.mse != null ? metrics.mse.toFixed(3) : 'N/A'}
                </p>
              </CardContent>
            </Card>
          </>
        )}
      </div>

      {/* Visualizations */}
      {isClassification && metrics?.confusionMatrix && metrics?.classLabels && (
        <ConfusionMatrix matrix={metrics.confusionMatrix} labels={metrics.classLabels} />
      )}

      {isClassification && metrics?.rocCurve && metrics.rocCurve.length > 0 && (
        <RocCurve data={metrics.rocCurve} />
      )}

      {metrics?.featureImportance && Object.keys(metrics.featureImportance).length > 0 && (
        <FeatureImportanceChart data={metrics.featureImportance} />
      )}

      {/* Model Info */}
      <Card>
        <CardHeader>
          <CardTitle>Model Information</CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          <div>
            <p className="text-sm text-muted-foreground">Target Variable</p>
            <p className="font-medium">{model.targetVariable}</p>
          </div>
          <div>
            <p className="text-sm text-muted-foreground">Features ({model.featureNames.length})</p>
            <p className="font-medium">{model.featureNames.join(', ')}</p>
          </div>
          {model.trainingTime != null && (
            <div>
              <p className="text-sm text-muted-foreground">Training Time</p>
              <p className="font-medium">{model.trainingTime} seconds</p>
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  );
}
