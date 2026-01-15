import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { apiClient } from '@/shared/lib/api/client';
import { Card, CardContent, CardHeader, CardTitle } from '@/shared/components/ui/card';
import { Button } from '@/shared/components/ui/button';
import { BrainCircuit, Plus } from 'lucide-react';
import { Link, useNavigate } from 'react-router-dom';
import { PaginatedResponse } from '@/shared/types/api.types';
import { PaginationControls } from '@/shared/components/PaginationControls';

interface ModelDto {
  id: number;
  modelName: string;
  modelType: string;
  trainingDate: string;
  accuracy: number | null;
  status: string;
}

export function ModelsPage() {
  const [currentPage, setCurrentPage] = useState(0);
  const pageSize = 12;
  const navigate = useNavigate();

  const { data: modelsResponse, isLoading } = useQuery<PaginatedResponse<ModelDto> | ModelDto[]>({
    queryKey: ['models', currentPage],
    queryFn: async () => {
      const response = await apiClient.get<PaginatedResponse<ModelDto> | ModelDto[]>('/v1/models', {
        params: { page: currentPage, size: pageSize },
      });
      return response;
    },
  });

  // Handle both paginated and non-paginated responses
  const models = Array.isArray(modelsResponse) 
    ? modelsResponse 
    : modelsResponse?.content || [];
  
  const pagination = Array.isArray(modelsResponse) 
    ? null 
    : modelsResponse;

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
      ) : models.length === 0 ? (
        <div className="text-center py-12">
          <p className="text-muted-foreground mb-4">No models trained yet</p>
          <Button asChild>
            <Link to="/models/train">
              <Plus className="w-4 h-4 mr-2" />
              Train Your First Model
            </Link>
          </Button>
        </div>
      ) : (
        <>
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4 sm:gap-6">
            {models.map((model) => (
              <Card 
                key={model.id} 
                className="hover:border-primary/30 transition-colors cursor-pointer"
                onClick={() => navigate(`/models/${model.id}`)}
              >
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
          
          {pagination && (
            <PaginationControls
              currentPage={currentPage}
              totalPages={pagination.totalPages}
              totalItems={pagination.totalItems}
              pageSize={pageSize}
              onPageChange={setCurrentPage}
              className="mt-6"
            />
          )}
        </>
      )}
    </div>
  );
}

