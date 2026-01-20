import { BrainCircuit, Calendar, Database, TrendingUp, Download, Play, Trash2, GitCompare, Edit2, Target, Zap } from 'lucide-react';
import { DropdownMenuAction } from '@/shared/components/ui/dropdown-menu-action';
import { Button } from './ui/button';
import { Card } from './ui/card';
import { Badge } from './ui/badge';
import { Tabs, TabsContent, TabsList, TabsTrigger } from './ui/tabs';
import { LineChart, Line, BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, Cell, AreaChart, Area } from 'recharts';
import { useStore } from '../lib/store';
import { toast } from 'sonner';

interface ModelDetailsProps {
  onNavigate: (page: string) => void;
}

export function ModelDetails({ onNavigate }: ModelDetailsProps) {
  const { models, deleteModel } = useStore();

  const handleDeleteModel = (id: string, name: string) => {
    deleteModel(id);
    toast.success('Model deleted', {
      description: `${name} has been removed`,
    });
  };

  const selectedModel = models[0] || {
    id: '1',
    name: 'Churn Predictor v3',
    algorithm: 'Logistic Regression',
    dataset: 'customer_churn_2024.csv',
    accuracy: 89.2,
    precision: 0.88,
    recall: 0.87,
    f1Score: 0.87,
    trainedDate: '2024-12-08',
    status: 'ready',
    trainingTime: '18s',
    features: 12,
  };

  const featureImportance = [
    { feature: 'account_tenure', importance: 0.32 },
    { feature: 'monthly_charges', importance: 0.28 },
    { feature: 'contract_type', importance: 0.21 },
    { feature: 'tech_support', importance: 0.19 },
    { feature: 'internet_service', importance: 0.18 },
    { feature: 'age', importance: 0.15 },
    { feature: 'region', importance: 0.12 },
  ].sort((a, b) => b.importance - a.importance);

  const confusionMatrix = [
    [850, 120],
    [95, 935],
  ];

  const rocCurveData = Array.from({ length: 20 }, (_, i) => ({
    fpr: i * 0.05,
    tpr: Math.min(1, (i * 0.05) + 0.15 + Math.random() * 0.1),
  }));

  const trainingHistory = Array.from({ length: 10 }, (_, i) => ({
    epoch: i + 1,
    train: 0.75 + (i * 0.015) + Math.random() * 0.02,
    val: 0.73 + (i * 0.013) + Math.random() * 0.02,
  }));

  const recentPredictions = [
    { input: 'Customer #4521, Age: 35, Tenure: 6mo', prediction: 'Will Churn', confidence: '87.3%', date: 'Dec 8, 2024' },
    { input: 'Customer #4522, Age: 42, Tenure: 24mo', prediction: 'Will Not Churn', confidence: '92.1%', date: 'Dec 8, 2024' },
    { input: 'Customer #4523, Age: 28, Tenure: 3mo', prediction: 'Will Churn', confidence: '79.5%', date: 'Dec 7, 2024' },
    { input: 'Customer #4524, Age: 51, Tenure: 48mo', prediction: 'Will Not Churn', confidence: '95.8%', date: 'Dec 7, 2024' },
  ];

  return (
    <div className="p-8 space-y-6">
      {/* Header Section */}
      <div className="flex items-start justify-between">
        <div className="flex items-center gap-4">
          <div className="w-16 h-16 rounded-xl bg-gradient-to-br from-primary to-secondary flex items-center justify-center">
            <TrendingUp className="w-8 h-8 text-white" />
          </div>
          <div>
            <div className="flex items-center gap-3 mb-1">
              <h1>Churn Predictor v2</h1>
              <Button variant="ghost" size="icon" className="w-8 h-8">
                <Edit2 className="w-4 h-4" />
              </Button>
            </div>
            <div className="flex items-center gap-3">
              <Badge className="bg-success/10 text-success border-success/20">Ready</Badge>
              <span className="text-muted-foreground">Created Dec 8, 2024</span>
              <span className="text-muted-foreground">•</span>
              <span className="text-muted-foreground">Dataset: customer_data_2024.csv</span>
              <span className="text-muted-foreground">•</span>
              <span className="text-muted-foreground">Algorithm: Logistic Regression</span>
            </div>
          </div>
        </div>
        <div className="flex gap-3">
          <Button onClick={() => onNavigate('predictions-new')} className="bg-primary text-primary-foreground hover:bg-primary/90">
            <Play className="w-4 h-4 mr-2" />
            Make Prediction
          </Button>
          <DropdownMenuAction
            options={[
              {
                label: "Edit Model",
                onClick: () => console.log("Edit model"),
                Icon: Edit2,
              },
              {
                label: "Download",
                onClick: () => console.log("Download model"),
                Icon: Download,
              },
              {
                label: "Compare",
                onClick: () => onNavigate('models-compare'),
                Icon: GitCompare,
              },
              {
                label: "Delete",
                onClick: () => console.log("Delete model"),
                Icon: Trash2,
                variant: "destructive",
              },
            ]}
            align="right"
            variant="icon"
          />
        </div>
      </div>

      {/* Tabs */}
      <Tabs defaultValue="overview" className="space-y-6">
        <TabsList>
          <TabsTrigger value="overview">Overview</TabsTrigger>
          <TabsTrigger value="performance">Performance</TabsTrigger>
          <TabsTrigger value="predictions">Predictions</TabsTrigger>
          <TabsTrigger value="settings">Settings</TabsTrigger>
        </TabsList>

        {/* Overview Tab */}
        <TabsContent value="overview" className="space-y-6">
          {/* Model Summary Card */}
          <Card className="p-6">
            <h3 className="mb-4">Model Summary</h3>
            <div className="grid grid-cols-3 gap-6">
              <div>
                <p className="text-sm text-muted-foreground mb-1">Algorithm</p>
                <p className="font-medium">Logistic Regression</p>
              </div>
              <div>
                <p className="text-sm text-muted-foreground mb-1">Target Variable</p>
                <p className="font-medium">churn_status</p>
              </div>
              <div>
                <p className="text-sm text-muted-foreground mb-1">Features Used</p>
                <div className="flex flex-wrap gap-1 mt-1">
                  {['account_tenure', 'monthly_charges', 'contract_type', 'age', '+3 more'].map((f, i) => (
                    <Badge key={i} variant="outline" className="border-primary/30 text-primary text-xs">
                      {f}
                    </Badge>
                  ))}
                </div>
              </div>
            </div>
          </Card>

          {/* Performance Metrics Grid */}
          <div className="grid grid-cols-4 gap-6">
            {[
              { label: 'Accuracy', value: '87.3%', icon: Target },
              { label: 'Precision', value: '0.85', icon: Zap },
              { label: 'Recall', value: '0.89', icon: TrendingUp },
              { label: 'F1 Score', value: '0.87', icon: TrendingUp },
            ].map((metric, i) => (
              <Card key={i} className="p-6">
                <div className="flex items-center gap-3 mb-3">
                  <div className="w-10 h-10 rounded-lg bg-primary/10 flex items-center justify-center">
                    <metric.icon className="w-5 h-5 text-primary" />
                  </div>
                </div>
                <p className="text-sm text-muted-foreground mb-1">{metric.label}</p>
                <p className="text-3xl font-semibold">{metric.value}</p>
              </Card>
            ))}
          </div>

          {/* Feature Importance Chart */}
          <Card className="p-6">
            <h3 className="mb-6">Feature Importance</h3>
            <div className="h-80">
              <ResponsiveContainer width="100%" height="100%">
                <BarChart data={featureImportance} layout="vertical" margin={{ left: 120 }}>
                  <XAxis type="number" domain={[0, 0.35]} />
                  <YAxis type="category" dataKey="feature" />
                  <Tooltip />
                  <Bar dataKey="importance" fill="#00d9ff" radius={[0, 4, 4, 0]} />
                </BarChart>
              </ResponsiveContainer>
            </div>
          </Card>

          {/* Confusion Matrix */}
          <Card className="p-6">
            <h3 className="mb-6">Confusion Matrix</h3>
            <div className="max-w-md mx-auto">
              <div className="grid grid-cols-3 gap-2">
                <div></div>
                <div className="text-center text-sm text-muted-foreground font-medium">Predicted: No</div>
                <div className="text-center text-sm text-muted-foreground font-medium">Predicted: Yes</div>
                
                <div className="flex items-center justify-end pr-4 text-sm text-muted-foreground font-medium">Actual: No</div>
                <div className="aspect-square bg-success/20 rounded-lg flex items-center justify-center border-2 border-success/40">
                  <span className="text-2xl font-semibold">{confusionMatrix[0][0]}</span>
                </div>
                <div className="aspect-square bg-error/20 rounded-lg flex items-center justify-center border-2 border-error/40">
                  <span className="text-2xl font-semibold">{confusionMatrix[0][1]}</span>
                </div>
                
                <div className="flex items-center justify-end pr-4 text-sm text-muted-foreground font-medium">Actual: Yes</div>
                <div className="aspect-square bg-error/20 rounded-lg flex items-center justify-center border-2 border-error/40">
                  <span className="text-2xl font-semibold">{confusionMatrix[1][0]}</span>
                </div>
                <div className="aspect-square bg-success/20 rounded-lg flex items-center justify-center border-2 border-success/40">
                  <span className="text-2xl font-semibold">{confusionMatrix[1][1]}</span>
                </div>
              </div>
            </div>
          </Card>
        </TabsContent>

        {/* Performance Tab */}
        <TabsContent value="performance" className="space-y-6">
          {/* ROC Curve */}
          <Card className="p-6">
            <h3 className="mb-6">ROC Curve</h3>
            <div className="h-96">
              <ResponsiveContainer width="100%" height="100%">
                <AreaChart data={rocCurveData}>
                  <CartesianGrid strokeDasharray="3 3" stroke="#3a3a5c" />
                  <XAxis dataKey="fpr" label={{ value: 'False Positive Rate', position: 'insideBottom', offset: -5 }} />
                  <YAxis label={{ value: 'True Positive Rate', angle: -90, position: 'insideLeft' }} />
                  <Tooltip />
                  <Area type="monotone" dataKey="tpr" stroke="#00d9ff" fill="#00d9ff" fillOpacity={0.2} strokeWidth={2} />
                  <Line type="linear" dataKey="fpr" stroke="#71717a" strokeDasharray="5 5" dot={false} />
                </AreaChart>
              </ResponsiveContainer>
            </div>
            <p className="text-center text-sm text-muted-foreground mt-4">AUC: 0.92</p>
          </Card>

          {/* Training History */}
          <Card className="p-6">
            <h3 className="mb-6">Training History</h3>
            <div className="h-80">
              <ResponsiveContainer width="100%" height="100%">
                <LineChart data={trainingHistory}>
                  <CartesianGrid strokeDasharray="3 3" stroke="#3a3a5c" />
                  <XAxis dataKey="epoch" label={{ value: 'Epoch', position: 'insideBottom', offset: -5 }} />
                  <YAxis label={{ value: 'Accuracy', angle: -90, position: 'insideLeft' }} domain={[0.7, 1]} />
                  <Tooltip />
                  <Line type="monotone" dataKey="train" stroke="#00d9ff" strokeWidth={2} name="Training" />
                  <Line type="monotone" dataKey="val" stroke="#7c3aed" strokeWidth={2} name="Validation" />
                </LineChart>
              </ResponsiveContainer>
            </div>
          </Card>
        </TabsContent>

        {/* Predictions Tab */}
        <TabsContent value="predictions" className="space-y-6">
          <Card className="p-6">
            <div className="flex items-center justify-between mb-6">
              <h3>Prediction History</h3>
              <Button onClick={() => onNavigate('predictions-new')} className="bg-primary text-primary-foreground hover:bg-primary/90">
                New Prediction
              </Button>
            </div>
            <div className="overflow-x-auto">
              <table className="w-full">
                <thead>
                  <tr className="border-b border-border">
                    <th className="text-left py-3 px-4 text-muted-foreground font-medium">Input Summary</th>
                    <th className="text-left py-3 px-4 text-muted-foreground font-medium">Prediction</th>
                    <th className="text-left py-3 px-4 text-muted-foreground font-medium">Confidence</th>
                    <th className="text-left py-3 px-4 text-muted-foreground font-medium">Date</th>
                    <th className="text-left py-3 px-4 text-muted-foreground font-medium">Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {recentPredictions.map((pred, i) => (
                    <tr key={i} className="border-b border-border/50 hover:bg-muted/30 transition-colors">
                      <td className="py-4 px-4 text-muted-foreground">{pred.input}</td>
                      <td className="py-4 px-4">
                        <Badge variant={pred.prediction.includes('Not') ? 'outline' : 'default'} className={pred.prediction.includes('Not') ? 'border-success/30 text-success' : 'bg-error/20 text-error border-error/30'}>
                          {pred.prediction}
                        </Badge>
                      </td>
                      <td className="py-4 px-4">{pred.confidence}</td>
                      <td className="py-4 px-4 text-muted-foreground">{pred.date}</td>
                      <td className="py-4 px-4">
                        <Button variant="ghost" size="sm" className="text-primary">
                          Explain
                        </Button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </Card>
        </TabsContent>
      </Tabs>
    </div>
  );
}