import { Plus, X, Star } from 'lucide-react';
import { Button } from './ui/button';
import { Card } from './ui/card';
import { Badge } from './ui/badge';
import { RadarChart, PolarGrid, PolarAngleAxis, PolarRadiusAxis, Radar, ResponsiveContainer, LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, BarChart, Bar } from 'recharts';

export function ModelComparison() {
  const selectedModels = [
    { id: 'v1', name: 'Churn Predictor v1', algorithm: 'Logistic Reg.', accuracy: 82.1, precision: 0.79, recall: 0.81, f1: 0.80, trainingTime: '12s', features: 8 },
    { id: 'v2', name: 'Churn Predictor v2', algorithm: 'Logistic Reg.', accuracy: 85.4, precision: 0.83, recall: 0.86, f1: 0.84, trainingTime: '15s', features: 10 },
    { id: 'v3', name: 'Churn Predictor v3', algorithm: 'Logistic Reg.', accuracy: 89.2, precision: 0.88, recall: 0.87, f1: 0.87, trainingTime: '18s', features: 12 },
  ];

  const radarData = [
    { metric: 'Accuracy', v1: 82.1, v2: 85.4, v3: 89.2 },
    { metric: 'Precision', v1: 79, v2: 83, v3: 88 },
    { metric: 'Recall', v1: 81, v2: 86, v3: 87 },
    { metric: 'F1 Score', v1: 80, v2: 84, v3: 87 },
  ];

  const versionTrendData = [
    { version: 'v1', accuracy: 82.1 },
    { version: 'v2', accuracy: 85.4 },
    { version: 'v3', accuracy: 89.2 },
  ];

  const featureImportance = [
    { feature: 'Account Tenure', v1: 0.30, v2: 0.32, v3: 0.35 },
    { feature: 'Monthly Charges', v1: 0.25, v2: 0.28, v3: 0.30 },
    { feature: 'Contract Type', v1: 0.28, v2: 0.25, v3: 0.27 },
    { feature: 'Payment Method', v1: 0.15, v2: 0.18, v3: 0.20 },
    { feature: 'Tech Support', v1: 0, v2: 0.12, v3: 0.15 },
    { feature: 'Streaming Services', v1: 0, v2: 0, v3: 0.14 },
  ];

  const getBestModel = (metric: string) => {
    const values = selectedModels.map(m => {
      switch(metric) {
        case 'accuracy': return m.accuracy;
        case 'precision': return m.precision * 100;
        case 'recall': return m.recall * 100;
        case 'f1': return m.f1 * 100;
        case 'time': return parseInt(m.trainingTime);
        default: return 0;
      }
    });
    const bestIndex = metric === 'time' ? values.indexOf(Math.min(...values)) : values.indexOf(Math.max(...values));
    return selectedModels[bestIndex].id;
  };

  return (
    <div className="p-8 space-y-6">
      {/* Header */}
      <div>
        <h1>Compare Models</h1>
        <p className="text-muted-foreground mt-1">Compare performance metrics across multiple models</p>
      </div>

      {/* Model Selection Bar */}
      <Card className="p-4">
        <div className="flex items-center gap-3">
          {selectedModels.map((model) => (
            <div key={model.id} className="flex-1 p-4 border border-primary/30 bg-primary/5 rounded-lg relative">
              <button className="absolute top-2 right-2 w-6 h-6 rounded-full bg-background hover:bg-error/10 flex items-center justify-center border border-border hover:border-error transition-colors">
                <X className="w-4 h-4 hover:text-error" />
              </button>
              <p className="font-medium mb-1">{model.name}</p>
              <p className="text-sm text-muted-foreground">{model.algorithm}</p>
            </div>
          ))}
          <Button variant="outline" className="h-full px-8 border-dashed">
            <Plus className="w-4 h-4 mr-2" />
            Add Model
          </Button>
        </div>
      </Card>

      {/* Comparison Table */}
      <Card className="p-6">
        <h3 className="mb-6">Performance Metrics</h3>
        <div className="overflow-x-auto">
          <table className="w-full">
            <thead>
              <tr className="border-b border-border">
                <th className="text-left py-3 px-4 font-medium">Metric</th>
                <th className="text-left py-3 px-4 font-medium">Churn v1</th>
                <th className="text-left py-3 px-4 font-medium">Churn v2</th>
                <th className="text-left py-3 px-4 font-medium">Churn v3</th>
                <th className="text-left py-3 px-4 font-medium">Best</th>
              </tr>
            </thead>
            <tbody>
              <tr className="border-b border-border/50">
                <td className="py-4 px-4 font-medium">Accuracy</td>
                <td className="py-4 px-4">
                  <span className={getBestModel('accuracy') === 'v1' ? 'text-primary font-medium' : ''}>
                    {selectedModels[0].accuracy}%
                  </span>
                </td>
                <td className="py-4 px-4">
                  <span className={getBestModel('accuracy') === 'v2' ? 'text-primary font-medium' : ''}>
                    {selectedModels[1].accuracy}%
                  </span>
                </td>
                <td className="py-4 px-4">
                  <span className={getBestModel('accuracy') === 'v3' ? 'text-primary font-medium' : ''}>
                    {selectedModels[2].accuracy}%
                  </span>
                </td>
                <td className="py-4 px-4">
                  <Badge className="bg-primary/10 text-primary border-primary/30">
                    <Star className="w-3 h-3 mr-1" />
                    v3
                  </Badge>
                </td>
              </tr>
              <tr className="border-b border-border/50">
                <td className="py-4 px-4 font-medium">Precision</td>
                <td className="py-4 px-4">
                  <span className={getBestModel('precision') === 'v1' ? 'text-primary font-medium' : ''}>
                    {selectedModels[0].precision}
                  </span>
                </td>
                <td className="py-4 px-4">
                  <span className={getBestModel('precision') === 'v2' ? 'text-primary font-medium' : ''}>
                    {selectedModels[1].precision}
                  </span>
                </td>
                <td className="py-4 px-4">
                  <span className={getBestModel('precision') === 'v3' ? 'text-primary font-medium' : ''}>
                    {selectedModels[2].precision}
                  </span>
                </td>
                <td className="py-4 px-4">
                  <Badge className="bg-primary/10 text-primary border-primary/30">
                    <Star className="w-3 h-3 mr-1" />
                    v3
                  </Badge>
                </td>
              </tr>
              <tr className="border-b border-border/50">
                <td className="py-4 px-4 font-medium">Recall</td>
                <td className="py-4 px-4">
                  <span className={getBestModel('recall') === 'v1' ? 'text-primary font-medium' : ''}>
                    {selectedModels[0].recall}
                  </span>
                </td>
                <td className="py-4 px-4">
                  <span className={getBestModel('recall') === 'v2' ? 'text-primary font-medium' : ''}>
                    {selectedModels[1].recall}
                  </span>
                </td>
                <td className="py-4 px-4">
                  <span className={getBestModel('recall') === 'v3' ? 'text-primary font-medium' : ''}>
                    {selectedModels[2].recall}
                  </span>
                </td>
                <td className="py-4 px-4">
                  <Badge className="bg-primary/10 text-primary border-primary/30">
                    <Star className="w-3 h-3 mr-1" />
                    v3
                  </Badge>
                </td>
              </tr>
              <tr className="border-b border-border/50">
                <td className="py-4 px-4 font-medium">F1 Score</td>
                <td className="py-4 px-4">
                  <span className={getBestModel('f1') === 'v1' ? 'text-primary font-medium' : ''}>
                    {selectedModels[0].f1}
                  </span>
                </td>
                <td className="py-4 px-4">
                  <span className={getBestModel('f1') === 'v2' ? 'text-primary font-medium' : ''}>
                    {selectedModels[1].f1}
                  </span>
                </td>
                <td className="py-4 px-4">
                  <span className={getBestModel('f1') === 'v3' ? 'text-primary font-medium' : ''}>
                    {selectedModels[2].f1}
                  </span>
                </td>
                <td className="py-4 px-4">
                  <Badge className="bg-primary/10 text-primary border-primary/30">
                    <Star className="w-3 h-3 mr-1" />
                    v3
                  </Badge>
                </td>
              </tr>
              <tr className="border-b border-border/50">
                <td className="py-4 px-4 font-medium">Training Time</td>
                <td className="py-4 px-4">
                  <span className={getBestModel('time') === 'v1' ? 'text-primary font-medium' : ''}>
                    {selectedModels[0].trainingTime}
                  </span>
                </td>
                <td className="py-4 px-4">
                  <span className={getBestModel('time') === 'v2' ? 'text-primary font-medium' : ''}>
                    {selectedModels[1].trainingTime}
                  </span>
                </td>
                <td className="py-4 px-4">
                  <span className={getBestModel('time') === 'v3' ? 'text-primary font-medium' : ''}>
                    {selectedModels[2].trainingTime}
                  </span>
                </td>
                <td className="py-4 px-4">
                  <Badge className="bg-primary/10 text-primary border-primary/30">
                    <Star className="w-3 h-3 mr-1" />
                    v1
                  </Badge>
                </td>
              </tr>
              <tr>
                <td className="py-4 px-4 font-medium">Features Used</td>
                <td className="py-4 px-4">{selectedModels[0].features}</td>
                <td className="py-4 px-4">{selectedModels[1].features}</td>
                <td className="py-4 px-4">{selectedModels[2].features}</td>
                <td className="py-4 px-4">—</td>
              </tr>
            </tbody>
          </table>
        </div>
      </Card>

      {/* Visual Comparison Charts */}
      <div className="grid grid-cols-2 gap-6">
        {/* Metrics Radar Chart */}
        <Card className="p-6">
          <h3 className="mb-6">Metrics Radar Chart</h3>
          <div className="h-80">
            <ResponsiveContainer width="100%" height="100%">
              <RadarChart data={radarData}>
                <PolarGrid stroke="#3a3a5c" />
                <PolarAngleAxis dataKey="metric" tick={{ fill: '#a1a1aa' }} />
                <PolarRadiusAxis domain={[0, 100]} tick={{ fill: '#a1a1aa' }} />
                <Radar name="v1" dataKey="v1" stroke="#71717a" fill="#71717a" fillOpacity={0.2} strokeWidth={2} />
                <Radar name="v2" dataKey="v2" stroke="#7c3aed" fill="#7c3aed" fillOpacity={0.2} strokeWidth={2} />
                <Radar name="v3" dataKey="v3" stroke="#00d9ff" fill="#00d9ff" fillOpacity={0.3} strokeWidth={2} />
              </RadarChart>
            </ResponsiveContainer>
          </div>
          <div className="flex items-center justify-center gap-6 mt-4">
            <div className="flex items-center gap-2">
              <div className="w-3 h-3 rounded bg-[#71717a]"></div>
              <span className="text-sm">v1</span>
            </div>
            <div className="flex items-center gap-2">
              <div className="w-3 h-3 rounded bg-secondary"></div>
              <span className="text-sm">v2</span>
            </div>
            <div className="flex items-center gap-2">
              <div className="w-3 h-3 rounded bg-primary"></div>
              <span className="text-sm">v3</span>
            </div>
          </div>
        </Card>

        {/* Accuracy Over Versions */}
        <Card className="p-6">
          <h3 className="mb-6">Accuracy Over Versions</h3>
          <div className="h-80">
            <ResponsiveContainer width="100%" height="100%">
              <LineChart data={versionTrendData}>
                <CartesianGrid strokeDasharray="3 3" stroke="#3a3a5c" />
                <XAxis dataKey="version" stroke="#a1a1aa" />
                <YAxis domain={[75, 95]} stroke="#a1a1aa" />
                <Tooltip 
                  contentStyle={{ backgroundColor: '#252540', border: '1px solid #3a3a5c' }}
                  labelStyle={{ color: '#ffffff' }}
                />
                <Line type="monotone" dataKey="accuracy" stroke="#00d9ff" strokeWidth={3} dot={{ fill: '#00d9ff', r: 6 }} />
              </LineChart>
            </ResponsiveContainer>
          </div>
          <p className="text-center text-sm text-muted-foreground mt-4">
            +7.1% improvement from v1 to v3
          </p>
        </Card>
      </div>

      {/* Feature Importance Comparison */}
      <Card className="p-6">
        <h3 className="mb-6">Feature Importance Comparison</h3>
        <div className="h-96">
          <ResponsiveContainer width="100%" height="100%">
            <BarChart data={featureImportance} layout="vertical" margin={{ left: 120 }}>
              <CartesianGrid strokeDasharray="3 3" stroke="#3a3a5c" />
              <XAxis type="number" domain={[0, 0.4]} stroke="#a1a1aa" />
              <YAxis type="category" dataKey="feature" stroke="#a1a1aa" />
              <Tooltip 
                contentStyle={{ backgroundColor: '#252540', border: '1px solid #3a3a5c' }}
                labelStyle={{ color: '#ffffff' }}
              />
              <Bar dataKey="v1" fill="#71717a" radius={[0, 4, 4, 0]} />
              <Bar dataKey="v2" fill="#7c3aed" radius={[0, 4, 4, 0]} />
              <Bar dataKey="v3" fill="#00d9ff" radius={[0, 4, 4, 0]} />
            </BarChart>
          </ResponsiveContainer>
        </div>
        <div className="flex items-center justify-center gap-6 mt-4">
          <div className="flex items-center gap-2">
            <div className="w-3 h-3 rounded bg-[#71717a]"></div>
            <span className="text-sm">v1</span>
          </div>
          <div className="flex items-center gap-2">
            <div className="w-3 h-3 rounded bg-secondary"></div>
            <span className="text-sm">v2</span>
          </div>
          <div className="flex items-center gap-2">
            <div className="w-3 h-3 rounded bg-primary"></div>
            <span className="text-sm">v3</span>
          </div>
        </div>
      </Card>
    </div>
  );
}
