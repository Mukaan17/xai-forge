import { useQuery } from '@tanstack/react-query';
import { apiClient } from '@/shared/lib/api/client';
import { Card, CardContent, CardHeader, CardTitle } from '@/shared/components/ui/card';
import { Button } from '@/shared/components/ui/button';
import { BrainCircuit, Plus } from 'lucide-react';
import { Link } from 'react-router-dom';

interface ModelDto {
  id: number;
  modelName: string;
  modelType: string;
  trainingDate: string;
  accuracy: number | null;
  status: string;
}

export function ModelsPage() {
  const { data: models, isLoading } = useQuery<ModelDto[]>({
    queryKey: ['models'],
    queryFn: async () => {
      const response = await apiClient.get<ModelDto[]>('/v1/models');
      return response;
    },
  });

  return (
    <div className="p-4 sm:p-6 lg:p-8 space-y-4 sm:space-y-6">
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
        <h1 className="text-xl sm:text-2xl font-semibold">Models</h1>
        <Button asChild className="w-full sm:w-auto">
          <Link to="/models/train">
            <Plus className="w-4 h-4 mr-2" />
            Train Model
          </Link>
        </Button>
      </div>

      {isLoading ? (
        <div>Loading models...</div>
      ) : (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4 sm:gap-6">
          {models?.map((model) => (
            <Card key={model.id}>
              <CardHeader>
                <CardTitle className="flex items-center gap-2">
                  <BrainCircuit className="w-5 h-5" />
                  {model.modelName}
                </CardTitle>
              </CardHeader>
              <CardContent>
                <p className="text-sm text-muted-foreground">
                  Type: {model.modelType} • Status: {model.status}
                </p>
                {model.accuracy && (
                  <p className="text-sm font-medium mt-2">
                    Accuracy: {(model.accuracy * 100).toFixed(1)}%
                  </p>
                )}
              </CardContent>
            </Card>
          ))}
        </div>
      )}
    </div>
  );
}

