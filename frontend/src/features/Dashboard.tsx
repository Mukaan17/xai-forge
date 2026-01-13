import { Upload, BrainCircuit, Target, TrendingUp, Activity, Database, Zap, AlertCircle, Plus, Play, MoreVertical } from 'lucide-react';
import { Button } from './ui/button';
import { Card } from './ui/card';
import { Badge } from './ui/badge';
import { BarChart, Bar, LineChart, Line, PieChart, Pie, Cell, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts';
import { useStore } from '../lib/store';

interface DashboardProps {
  onNavigate: (page: string) => void;
}

export function Dashboard({ onNavigate }: DashboardProps) {
  const { datasets, models, predictions } = useStore();
  
  const avgAccuracy = models.length > 0 
    ? models.reduce((sum, m) => sum + m.accuracy, 0) / models.length 
    : 0;

  const kpiData = [
    { label: 'Total Datasets', value: datasets.length.toString(), icon: Database, subtext: '+3 this week', color: 'text-primary' },
    { label: 'Trained Models', value: models.length.toString(), icon: BrainCircuit, subtext: '8 active', color: 'text-secondary' },
    { label: 'Predictions Made', value: predictions.length.toString(), icon: Target, subtext: 'Last 30 days', color: 'text-success' },
    { label: 'Avg. Model Accuracy', value: `${avgAccuracy.toFixed(1)}%`, icon: TrendingUp, subtext: 'Across all models', color: 'text-warning' },
  ];

  const recentActivity = [
    { icon: BrainCircuit, action: 'Model trained', object: 'Churn Predictor v2', timestamp: '2 hours ago', color: 'text-secondary' },
    { icon: Database, action: 'Dataset uploaded', object: 'sales_q4.csv', timestamp: '5 hours ago', color: 'text-primary' },
    { icon: Target, action: 'Prediction made', object: 'Customer #4521', timestamp: 'Yesterday', color: 'text-success' },
    { icon: BrainCircuit, action: 'Model trained', object: 'Revenue Forecaster', timestamp: 'Yesterday', color: 'text-secondary' },
    { icon: Database, action: 'Dataset updated', object: 'customer_data_2024.csv', timestamp: '2 days ago', color: 'text-primary' },
  ];

  const recentModels = [
    { name: 'Churn Predictor v2', type: 'Classification', dataset: 'customer_data_2024.csv', accuracy: '89.2%', status: 'Ready', created: 'Dec 8, 2024' },
    { name: 'Revenue Forecaster', type: 'Regression', dataset: 'sales_history.csv', accuracy: '92.1%', status: 'Ready', created: 'Dec 7, 2024' },
    { name: 'Lead Scoring Model', type: 'Classification', dataset: 'leads_q4.csv', accuracy: '85.7%', status: 'Training', created: 'Dec 8, 2024' },
    { name: 'Price Optimizer', type: 'Regression', dataset: 'pricing_data.csv', accuracy: '88.4%', status: 'Ready', created: 'Dec 6, 2024' },
    { name: 'Customer Segmentation', type: 'Classification', dataset: 'customer_profiles.csv', accuracy: '91.3%', status: 'Ready', created: 'Dec 5, 2024' },
  ];

  const modelTypeData = [
    { name: 'Classification', value: 7 },
    { name: 'Regression', value: 5 },
  ];

  const weeklyUsageData = [
    { day: 'Mon', predictions: 245 },
    { day: 'Tue', predictions: 312 },
    { day: 'Wed', predictions: 289 },
    { day: 'Thu', predictions: 401 },
    { day: 'Fri', predictions: 356 },
    { day: 'Sat', predictions: 178 },
    { day: 'Sun', predictions: 156 },
  ];

  const datasetSizeData = [
    { name: 'Small (<1k rows)', value: 8 },
    { name: 'Medium (1k-10k)', value: 12 },
    { name: 'Large (>10k)', value: 4 },
  ];

  const COLORS = ['#00d9ff', '#7c3aed', '#10b981', '#f59e0b'];

  return (
    <div className="p-8 space-y-8">
      {/* Welcome Banner */}
      <Card className="p-6 bg-gradient-to-r from-primary/10 via-secondary/10 to-transparent border-primary/20">
        <div className="flex items-center justify-between">
          <div>
            <h1 className="mb-1">Welcome back, Sarah</h1>
            <p className="text-muted-foreground">Here&apos;s your ML workspace overview</p>
          </div>
          <div className="flex gap-3">
            <Button onClick={() => onNavigate('datasets-upload')} className="bg-primary text-primary-foreground hover:bg-primary/90">
              <Upload className="w-4 h-4 mr-2" />
              Upload Dataset
            </Button>
            <Button onClick={() => onNavigate('models-train')} variant="outline" className="border-primary/30 hover:bg-primary/10">
              <Plus className="w-4 h-4 mr-2" />
              Train Model
            </Button>
          </div>
        </div>
      </Card>

      {/* KPI Metric Cards */}
      <div className="grid grid-cols-4 gap-6">
        {kpiData.map((kpi, index) => (
          <Card key={index} className="p-6 hover:border-primary/30 transition-colors">
            <div className="flex items-start justify-between mb-4">
              <div className={`w-12 h-12 rounded-full bg-gradient-to-br ${kpi.color} from-current/10 to-current/5 flex items-center justify-center`}>
                <kpi.icon className={`w-6 h-6 ${kpi.color}`} />
              </div>
            </div>
            <div className="space-y-1">
              <p className="text-muted-foreground">{kpi.label}</p>
              <p className="text-3xl font-semibold tracking-tight">{kpi.value}</p>
              <p className="text-sm text-tertiary">{kpi.subtext}</p>
            </div>
          </Card>
        ))}
      </div>

      {/* Two-Column Layout */}
      <div className="grid grid-cols-12 gap-6">
        {/* Recent Activity Feed */}
        <Card className="col-span-8 p-6">
          <div className="flex items-center justify-between mb-6">
            <h3>Recent Activity</h3>
            <Button variant="ghost" size="sm" className="text-primary hover:text-primary/80">
              View All
            </Button>
          </div>
          <div className="space-y-4">
            {recentActivity.map((activity, index) => (
              <div key={index} className="flex items-center gap-4 p-3 rounded-lg hover:bg-muted/50 transition-colors">
                <div className={`w-10 h-10 rounded-lg bg-gradient-to-br ${activity.color} from-current/10 to-current/5 flex items-center justify-center flex-shrink-0`}>
                  <activity.icon className={`w-5 h-5 ${activity.color}`} />
                </div>
                <div className="flex-1 min-w-0">
                  <p className="truncate">
                    <span className="text-foreground">{activity.action}</span>
                    <span className="text-muted-foreground"> • </span>
                    <span className="text-primary">{activity.object}</span>
                  </p>
                </div>
                <span className="text-sm text-tertiary flex-shrink-0">{activity.timestamp}</span>
              </div>
            ))}
          </div>
        </Card>

        {/* Quick Stats Panel */}
        <div className="col-span-4 space-y-6">
          {/* Models by Type */}
          <Card className="p-6">
            <h4 className="mb-4">Models by Type</h4>
            <div className="h-48 flex items-center justify-center">
              <ResponsiveContainer width="100%" height="100%">
                <PieChart>
                  <Pie
                    data={modelTypeData}
                    cx="50%"
                    cy="50%"
                    innerRadius={50}
                    outerRadius={70}
                    paddingAngle={5}
                    dataKey="value"
                  >
                    {modelTypeData.map((entry, index) => (
                      <Cell key={`cell-${index}`} fill={COLORS[index]} />
                    ))}
                  </Pie>
                </PieChart>
              </ResponsiveContainer>
            </div>
            <div className="flex flex-col gap-2 mt-4">
              {modelTypeData.map((item, index) => (
                <div key={index} className="flex items-center justify-between text-sm">
                  <div className="flex items-center gap-2">
                    <div className="w-3 h-3 rounded-full" style={{ backgroundColor: COLORS[index] }}></div>
                    <span className="text-muted-foreground">{item.name}</span>
                  </div>
                  <span>{item.value}</span>
                </div>
              ))}
            </div>
          </Card>

          {/* Dataset Size Distribution */}
          <Card className="p-6">
            <h4 className="mb-4">Dataset Size Distribution</h4>
            <div className="space-y-3">
              {datasetSizeData.map((item, index) => (
                <div key={index}>
                  <div className="flex items-center justify-between text-sm mb-1">
                    <span className="text-muted-foreground">{item.name}</span>
                    <span>{item.value}</span>
                  </div>
                  <div className="w-full h-2 bg-muted rounded-full overflow-hidden">
                    <div
                      className="h-full rounded-full"
                      style={{
                        width: `${(item.value / 24) * 100}%`,
                        backgroundColor: COLORS[index],
                      }}
                    ></div>
                  </div>
                </div>
              ))}
            </div>
          </Card>

          {/* This Week's Usage */}
          <Card className="p-6">
            <h4 className="mb-4">This Week&apos;s Usage</h4>
            <div className="h-32">
              <ResponsiveContainer width="100%" height="100%">
                <LineChart data={weeklyUsageData}>
                  <Line type="monotone" dataKey="predictions" stroke="#00d9ff" strokeWidth={2} dot={false} />
                </LineChart>
              </ResponsiveContainer>
            </div>
            <p className="text-sm text-muted-foreground mt-2">Daily predictions this week</p>
          </Card>
        </div>
      </div>

      {/* Recent Models Table */}
      <Card className="p-6">
        <div className="flex items-center justify-between mb-6">
          <h3>Your Models</h3>
          <Button variant="ghost" size="sm" className="text-primary hover:text-primary/80" onClick={() => onNavigate('models-all')}>
            See All Models →
          </Button>
        </div>
        <div className="overflow-x-auto">
          <table className="w-full">
            <thead>
              <tr className="border-b border-border">
                <th className="text-left py-3 px-4 text-muted-foreground font-medium">Model Name</th>
                <th className="text-left py-3 px-4 text-muted-foreground font-medium">Type</th>
                <th className="text-left py-3 px-4 text-muted-foreground font-medium">Dataset</th>
                <th className="text-left py-3 px-4 text-muted-foreground font-medium">Accuracy</th>
                <th className="text-left py-3 px-4 text-muted-foreground font-medium">Status</th>
                <th className="text-left py-3 px-4 text-muted-foreground font-medium">Created</th>
                <th className="text-left py-3 px-4 text-muted-foreground font-medium">Actions</th>
              </tr>
            </thead>
            <tbody>
              {recentModels.map((model, index) => (
                <tr key={index} className="border-b border-border/50 hover:bg-muted/30 transition-colors">
                  <td className="py-4 px-4">{model.name}</td>
                  <td className="py-4 px-4">
                    <Badge variant="outline" className="border-primary/30 text-primary">
                      {model.type}
                    </Badge>
                  </td>
                  <td className="py-4 px-4 text-muted-foreground">{model.dataset}</td>
                  <td className="py-4 px-4">{model.accuracy}</td>
                  <td className="py-4 px-4">
                    {model.status === 'Ready' && (
                      <Badge className="bg-success/10 text-success border-success/20">Ready</Badge>
                    )}
                    {model.status === 'Training' && (
                      <Badge className="bg-warning/10 text-warning border-warning/20 animate-pulse">Training</Badge>
                    )}
                  </td>
                  <td className="py-4 px-4 text-muted-foreground">{model.created}</td>
                  <td className="py-4 px-4">
                    <div className="flex items-center gap-2">
                      <Button size="sm" variant="ghost" className="text-primary hover:text-primary/80" onClick={() => onNavigate('predictions-new')}>
                        <Play className="w-4 h-4 mr-1" />
                        Predict
                      </Button>
                      <Button size="sm" variant="ghost">
                        <MoreVertical className="w-4 h-4" />
                      </Button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </Card>
    </div>
  );
}