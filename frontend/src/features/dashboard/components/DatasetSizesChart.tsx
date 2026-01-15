import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, Legend } from 'recharts';
import { Card, CardContent, CardHeader, CardTitle } from '@/shared/components/ui/card';

interface DatasetSizesChartProps {
  data: Record<string, number>;
}

export function DatasetSizesChart({ data }: DatasetSizesChartProps) {
  // Convert record to array format and format sizes
  const chartData = Object.entries(data)
    .map(([name, size]) => ({
      name: name.length > 20 ? name.substring(0, 20) + '...' : name,
      size: size / (1024 * 1024), // Convert to MB
      fullName: name,
    }))
    .sort((a, b) => b.size - a.size)
    .slice(0, 10); // Show top 10

  if (chartData.length === 0) {
    return (
      <Card>
        <CardHeader>
          <CardTitle>Dataset Sizes</CardTitle>
        </CardHeader>
        <CardContent>
          <p className="text-muted-foreground text-center py-8">No datasets yet</p>
        </CardContent>
      </Card>
    );
  }

  return (
    <Card>
      <CardHeader>
        <CardTitle>Top Dataset Sizes</CardTitle>
      </CardHeader>
      <CardContent>
        <ResponsiveContainer width="100%" height={300}>
          <BarChart data={chartData}>
            <CartesianGrid strokeDasharray="3 3" className="stroke-muted" />
            <XAxis 
              dataKey="name" 
              className="text-xs"
              tick={{ fill: 'hsl(var(--muted-foreground))' }}
              angle={-45}
              textAnchor="end"
              height={80}
            />
            <YAxis 
              className="text-xs"
              tick={{ fill: 'hsl(var(--muted-foreground))' }}
              label={{ value: 'Size (MB)', angle: -90, position: 'insideLeft' }}
            />
            <Tooltip 
              contentStyle={{
                backgroundColor: 'hsl(var(--card))',
                border: '1px solid hsl(var(--border))',
                borderRadius: '8px',
              }}
              formatter={(value: number) => [`${value.toFixed(2)} MB`, 'Size']}
            />
            <Legend />
            <Bar 
              dataKey="size" 
              fill="hsl(var(--primary))"
              name="Size (MB)"
            />
          </BarChart>
        </ResponsiveContainer>
      </CardContent>
    </Card>
  );
}
