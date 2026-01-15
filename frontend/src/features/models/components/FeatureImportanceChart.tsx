import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, Legend } from 'recharts';
import { Card, CardContent, CardHeader, CardTitle } from '@/shared/components/ui/card';

interface FeatureImportanceChartProps {
  data: Record<string, number>;
}

export function FeatureImportanceChart({ data }: FeatureImportanceChartProps) {
  if (!data || Object.keys(data).length === 0) {
    return (
      <Card>
        <CardHeader>
          <CardTitle>Feature Importance</CardTitle>
        </CardHeader>
        <CardContent>
          <p className="text-muted-foreground text-center py-8">No feature importance data available</p>
        </CardContent>
      </Card>
    );
  }

  // Convert to array and sort by importance
  const chartData = Object.entries(data)
    .map(([feature, importance]) => ({
      feature,
      importance,
    }))
    .sort((a, b) => b.importance - a.importance)
    .slice(0, 20); // Show top 20 features

  // Find max importance for domain
  const maxImportance = Math.max(...chartData.map(d => d.importance));

  return (
    <Card>
      <CardHeader>
        <CardTitle>Feature Importance</CardTitle>
      </CardHeader>
      <CardContent>
        <ResponsiveContainer width="100%" height={400}>
          <BarChart data={chartData} layout="vertical" margin={{ left: 120, right: 20 }}>
            <CartesianGrid strokeDasharray="3 3" className="stroke-muted" />
            <XAxis 
              type="number" 
              domain={[0, maxImportance * 1.1]}
              className="text-xs"
              tick={{ fill: 'hsl(var(--muted-foreground))' }}
              label={{ value: 'Importance', position: 'insideBottom', offset: -5 }}
            />
            <YAxis 
              type="category" 
              dataKey="feature" 
              className="text-xs"
              tick={{ fill: 'hsl(var(--muted-foreground))' }}
              width={110}
            />
            <Tooltip 
              contentStyle={{
                backgroundColor: 'hsl(var(--card))',
                border: '1px solid hsl(var(--border))',
                borderRadius: '8px',
              }}
              formatter={(value: number) => [`${value.toFixed(4)}`, 'Importance']}
            />
            <Legend />
            <Bar 
              dataKey="importance" 
              fill="hsl(var(--primary))"
              radius={[0, 4, 4, 0]}
              name="Importance"
            />
          </BarChart>
        </ResponsiveContainer>
        <div className="mt-4 text-sm text-muted-foreground">
          <p>Shows the relative importance of each feature in making predictions.</p>
          {chartData.length < Object.keys(data).length && (
            <p>Displaying top {chartData.length} features out of {Object.keys(data).length} total.</p>
          )}
        </div>
      </CardContent>
    </Card>
  );
}
