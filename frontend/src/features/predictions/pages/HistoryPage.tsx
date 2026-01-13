import { useState } from 'react';
import { Search, Download, ChevronDown, ChevronUp, FileText } from 'lucide-react';
import { Button } from '@/shared/components/ui/button';
import { Input } from '@/shared/components/ui/input';
import { Card } from '@/shared/components/ui/card';
import { Badge } from '@/shared/components/ui/badge';
import { Checkbox } from '@/shared/components/ui/checkbox';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/shared/components/ui/select';
import { Skeleton } from '@/shared/components/ui/skeleton';
import { useQuery } from '@tanstack/react-query';
import { apiClient } from '@/shared/lib/api/client';
import { useNavigate } from 'react-router-dom';

interface PredictionRecord {
  id: number;
  modelId: number;
  modelName?: string;
  inputData: Record<string, any>;
  prediction: string;
  confidence: number;
  explanation?: string;
  createdAt: string;
}

export function HistoryPage() {
  const navigate = useNavigate();
  const [selectedIds, setSelectedIds] = useState<number[]>([]);
  const [expandedRows, setExpandedRows] = useState<number[]>([]);
  const [searchQuery, setSearchQuery] = useState('');
  const [modelFilter, setModelFilter] = useState('all');
  const [timeFilter, setTimeFilter] = useState('30');

  const { data: predictions = [], isLoading } = useQuery<PredictionRecord[]>({
    queryKey: ['prediction-history', timeFilter],
    queryFn: async () => {
      const response = await apiClient.get<PredictionRecord[]>('/v1/predictions/history', {
        params: { days: timeFilter === 'all' ? undefined : parseInt(timeFilter) }
      });
      return response;
    },
  });

  const filteredPredictions = predictions.filter(pred => {
    const matchesSearch = searchQuery === '' || 
      pred.modelName?.toLowerCase().includes(searchQuery.toLowerCase()) ||
      pred.prediction.toLowerCase().includes(searchQuery.toLowerCase());
    const matchesModel = modelFilter === 'all' || pred.modelName === modelFilter;
    return matchesSearch && matchesModel;
  });

  const toggleSelectAll = () => {
    if (selectedIds.length === filteredPredictions.length) {
      setSelectedIds([]);
    } else {
      setSelectedIds(filteredPredictions.map(p => p.id));
    }
  };

  const toggleSelect = (id: number) => {
    setSelectedIds(prev =>
      prev.includes(id) ? prev.filter(i => i !== id) : [...prev, id]
    );
  };

  const toggleExpandRow = (id: number) => {
    setExpandedRows(prev =>
      prev.includes(id) ? prev.filter(i => i !== id) : [...prev, id]
    );
  };

  const formatDate = (dateString: string) => {
    const date = new Date(dateString);
    const now = new Date();
    const diffMs = now.getTime() - date.getTime();
    const diffMins = Math.floor(diffMs / 60000);
    const diffHours = Math.floor(diffMs / 3600000);
    const diffDays = Math.floor(diffMs / 86400000);

    if (diffMins < 60) return `${diffMins}m ago`;
    if (diffHours < 24) return `${diffHours}h ago`;
    if (diffDays < 7) return `${diffDays}d ago`;
    return date.toLocaleDateString();
  };

  const formatInputSummary = (inputData: Record<string, any>) => {
    const entries = Object.entries(inputData).slice(0, 2);
    return entries.map(([key, value]) => `${key}: ${value}`).join(', ');
  };

  return (
    <div className="p-4 sm:p-6 lg:p-8 space-y-4 sm:space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl sm:text-3xl font-bold">Prediction History</h1>
          <p className="text-muted-foreground mt-1 text-sm sm:text-base">View and analyze past predictions across all models</p>
        </div>
        <Button className="bg-primary text-primary-foreground hover:bg-primary/90 w-full sm:w-auto">
          <Download className="w-4 h-4 mr-2" />
          Export CSV
        </Button>
      </div>

      {/* Filters Bar */}
      <Card className="p-4">
        <div className="flex flex-col sm:flex-row items-stretch sm:items-center gap-3 sm:gap-4">
          <div className="flex-1 relative min-w-0">
            <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-muted-foreground" />
            <Input
              placeholder="Search predictions..."
              className="pl-10 bg-background border-border/50 w-full"
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
            />
          </div>
          <Select value={modelFilter} onValueChange={setModelFilter}>
            <SelectTrigger className="w-full sm:w-48">
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="all">All Models</SelectItem>
              {Array.from(new Set(predictions.map(p => p.modelName).filter(Boolean))).map(model => (
                <SelectItem key={model} value={model}>{model}</SelectItem>
              ))}
            </SelectContent>
          </Select>
          <Select value={timeFilter} onValueChange={setTimeFilter}>
            <SelectTrigger className="w-full sm:w-48">
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="7">Last 7 days</SelectItem>
              <SelectItem value="30">Last 30 days</SelectItem>
              <SelectItem value="90">Last 90 days</SelectItem>
              <SelectItem value="all">All time</SelectItem>
            </SelectContent>
          </Select>
        </div>
      </Card>

      {/* Bulk Actions Bar */}
      {selectedIds.length > 0 && (
        <Card className="p-4 bg-primary/5 border-primary/20">
          <div className="flex items-center justify-between">
            <p className="font-medium">
              {selectedIds.length} prediction{selectedIds.length > 1 ? 's' : ''} selected
            </p>
            <div className="flex gap-2">
              <Button variant="outline" size="sm">
                <Download className="w-4 h-4 mr-2" />
                Export
              </Button>
              <Button variant="outline" size="sm">
                <FileText className="w-4 h-4 mr-2" />
                Re-explain
              </Button>
              <Button variant="outline" size="sm" className="text-destructive border-destructive/30 hover:bg-destructive hover:text-white">
                Delete
              </Button>
            </div>
          </div>
        </Card>
      )}

      {/* Predictions Table */}
      <Card className="p-6">
        {isLoading ? (
          <div className="space-y-4">
            <div className="overflow-x-auto">
              <table className="w-full">
                <thead>
                  <tr className="border-b border-border">
                    <th className="text-left py-3 px-4">
                      <Skeleton className="h-4 w-4" />
                    </th>
                    <th className="text-left py-3 px-4 text-muted-foreground font-medium">ID</th>
                    <th className="text-left py-3 px-4 text-muted-foreground font-medium">Model</th>
                    <th className="text-left py-3 px-4 text-muted-foreground font-medium">Input Summary</th>
                    <th className="text-left py-3 px-4 text-muted-foreground font-medium">Prediction</th>
                    <th className="text-left py-3 px-4 text-muted-foreground font-medium">Conf.</th>
                    <th className="text-left py-3 px-4 text-muted-foreground font-medium">Date</th>
                    <th className="text-left py-3 px-4"></th>
                  </tr>
                </thead>
                <tbody>
                  {Array.from({ length: 5 }).map((_, i) => (
                    <tr key={i} className="border-b border-border/50">
                      <td className="py-4 px-4">
                        <Skeleton className="h-4 w-4" />
                      </td>
                      <td className="py-4 px-4">
                        <Skeleton className="h-4 w-12" />
                      </td>
                      <td className="py-4 px-4">
                        <Skeleton className="h-4 w-24" />
                      </td>
                      <td className="py-4 px-4">
                        <Skeleton className="h-4 w-32" />
                      </td>
                      <td className="py-4 px-4">
                        <Skeleton className="h-6 w-20" />
                      </td>
                      <td className="py-4 px-4">
                        <Skeleton className="h-4 w-12" />
                      </td>
                      <td className="py-4 px-4">
                        <Skeleton className="h-4 w-16" />
                      </td>
                      <td className="py-4 px-4">
                        <Skeleton className="h-4 w-4" />
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        ) : filteredPredictions.length === 0 ? (
          <div className="text-center py-12">
            <p className="text-muted-foreground">No predictions found</p>
            <p className="text-sm text-muted-foreground mt-2">Make your first prediction to see it here</p>
          </div>
        ) : (
          <>
            <div className="overflow-x-auto">
              <table className="w-full">
                <thead>
                  <tr className="border-b border-border">
                    <th className="text-left py-3 px-4">
                      <Checkbox
                        checked={selectedIds.length === filteredPredictions.length && filteredPredictions.length > 0}
                        onCheckedChange={toggleSelectAll}
                      />
                    </th>
                    <th className="text-left py-3 px-4 text-muted-foreground font-medium">ID</th>
                    <th className="text-left py-3 px-4 text-muted-foreground font-medium">Model</th>
                    <th className="text-left py-3 px-4 text-muted-foreground font-medium">Input Summary</th>
                    <th className="text-left py-3 px-4 text-muted-foreground font-medium">Prediction</th>
                    <th className="text-left py-3 px-4 text-muted-foreground font-medium">Conf.</th>
                    <th className="text-left py-3 px-4 text-muted-foreground font-medium">Date</th>
                    <th className="text-left py-3 px-4"></th>
                  </tr>
                </thead>
                <tbody>
                  {filteredPredictions.map((pred) => (
                    <>
                      <tr
                        key={pred.id}
                        className="border-b border-border/50 hover:bg-muted/30 transition-colors cursor-pointer"
                        onClick={() => toggleExpandRow(pred.id)}
                      >
                        <td className="py-4 px-4" onClick={(e) => e.stopPropagation()}>
                          <Checkbox
                            checked={selectedIds.includes(pred.id)}
                            onCheckedChange={() => toggleSelect(pred.id)}
                          />
                        </td>
                        <td className="py-4 px-4 font-mono text-sm">#{pred.id}</td>
                        <td className="py-4 px-4">{pred.modelName || `Model ${pred.modelId}`}</td>
                        <td className="py-4 px-4 text-muted-foreground">{formatInputSummary(pred.inputData)}</td>
                        <td className="py-4 px-4">
                          <Badge variant="outline" className="border-primary/30 text-primary">
                            {pred.prediction}
                          </Badge>
                        </td>
                        <td className="py-4 px-4">{(pred.confidence * 100).toFixed(0)}%</td>
                        <td className="py-4 px-4 text-muted-foreground">{formatDate(pred.createdAt)}</td>
                        <td className="py-4 px-4">
                          {expandedRows.includes(pred.id) ? (
                            <ChevronUp className="w-4 h-4 text-muted-foreground" />
                          ) : (
                            <ChevronDown className="w-4 h-4 text-muted-foreground" />
                          )}
                        </td>
                      </tr>
                      {expandedRows.includes(pred.id) && (
                        <tr className="bg-muted/20">
                          <td colSpan={8} className="p-6">
                            <div className="grid grid-cols-2 gap-6">
                              <div>
                                <h4 className="mb-3 font-medium">Input Values</h4>
                                <div className="space-y-2 text-sm">
                                  {Object.entries(pred.inputData).map(([key, value]) => (
                                    <div key={key} className="flex items-center justify-between">
                                      <span className="text-muted-foreground capitalize">{key.replace(/([A-Z])/g, ' $1').trim()}:</span>
                                      <span>{String(value)}</span>
                                    </div>
                                  ))}
                                </div>
                              </div>
                              <div>
                                <h4 className="mb-3 font-medium">Quick Explanation</h4>
                                {pred.explanation ? (
                                  <p className="text-sm text-muted-foreground whitespace-pre-line">{pred.explanation}</p>
                                ) : (
                                  <p className="text-sm text-muted-foreground">No explanation available</p>
                                )}
                                <Button
                                  variant="outline"
                                  size="sm"
                                  className="mt-4 border-primary/30 hover:bg-primary/10"
                                  onClick={() => navigate(`/predictions?modelId=${pred.modelId}&predictionId=${pred.id}`)}
                                >
                                  <FileText className="w-4 h-4 mr-2" />
                                  View Full Explanation
                                </Button>
                              </div>
                            </div>
                          </td>
                        </tr>
                      )}
                    </>
                  ))}
                </tbody>
              </table>
            </div>

            {/* Pagination */}
            <div className="flex items-center justify-between mt-6 pt-6 border-t border-border">
              <p className="text-sm text-muted-foreground">Showing {filteredPredictions.length} prediction{filteredPredictions.length !== 1 ? 's' : ''}</p>
            </div>
          </>
        )}
      </Card>
    </div>
  );
}
