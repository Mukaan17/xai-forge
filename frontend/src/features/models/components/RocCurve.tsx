import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, Legend } from 'recharts';
import { Card, CardContent, CardHeader, CardTitle } from '@/shared/components/ui/card';

interface RocPoint {
  falsePositiveRate: number;
  truePositiveRate: number;
  threshold: number;
}

interface RocCurveProps {
  data: RocPoint[];
}

export function RocCurve({ data }: RocCurveProps) {
  if (!data || data.length === 0) {
    return (
      <Card>
        <CardHeader>
          <CardTitle>ROC Curve</CardTitle>
        </CardHeader>
        <CardContent>
          <p className="text-muted-foreground text-center py-8">No ROC curve data available</p>
        </CardContent>
      </Card>
    );
  }

  // Format data for Recharts
  const chartData = data.map(point => ({
    fpr: point.falsePositiveRate,
    tpr: point.truePositiveRate,
    threshold: point.threshold,
  }));

  // Add diagonal reference line (random classifier)
  const referenceLine = [
    { fpr: 0, tpr: 0 },
    { fpr: 1, tpr: 1 },
  ];

  return (
    <Card>
      <CardHeader>
        <CardTitle>ROC Curve</CardTitle>
      </CardHeader>
      <CardContent>
        <ResponsiveContainer width="100%" height={400}>
          <LineChart data={chartData}>
            <CartesianGrid strokeDasharray="3 3" className="stroke-muted" />
            <XAxis 
              dataKey="fpr" 
              label={{ value: 'False Positive Rate', position: 'insideBottom', offset: -5 }}
              domain={[0, 1]}
              className="text-xs"
              tick={{ fill: 'hsl(var(--muted-foreground))' }}
            />
            <YAxis 
              label={{ value: 'True Positive Rate', angle: -90, position: 'insideLeft' }}
              domain={[0, 1]}
              className="text-xs"
              tick={{ fill: 'hsl(var(--muted-foreground))' }}
            />
            <Tooltip 
              contentStyle={{
                backgroundColor: 'hsl(var(--card))',
                border: '1px solid hsl(var(--border))',
                borderRadius: '8px',
              }}
              formatter={(value: number, name: string, props: any) => {
                if (name === 'tpr') {
                  return [`TPR: ${value.toFixed(3)}`, 'True Positive Rate'];
                }
                if (name === 'fpr') {
                  return [`FPR: ${value.toFixed(3)}`, 'False Positive Rate'];
                }
                if (name === 'reference') {
                  return null;
                }
                return [value, name];
              }}
            />
            <Legend />
            {/* Reference line (random classifier) */}
            <Line
              type="monotone"
              dataKey="tpr"
              data={referenceLine}
              stroke="hsl(var(--muted-foreground))"
              strokeDasharray="5 5"
              strokeWidth={1}
              dot={false}
              name="Random Classifier"
              legendType="line"
            />
            {/* Actual ROC curve */}
            <Line
              type="monotone"
              dataKey="tpr"
              stroke="hsl(var(--primary))"
              strokeWidth={2}
              dot={false}
              name="ROC Curve"
            />
          </LineChart>
        </ResponsiveContainer>
        <div className="mt-4 text-sm text-muted-foreground">
          <p>AUC (Area Under Curve) represents the model's ability to distinguish between classes.</p>
          <p>Higher AUC indicates better performance. AUC = 0.5 is equivalent to random guessing.</p>
        </div>
      </CardContent>
    </Card>
  );
}
