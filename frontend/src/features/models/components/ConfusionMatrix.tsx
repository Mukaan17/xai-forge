import { Card, CardContent, CardHeader, CardTitle } from '@/shared/components/ui/card';

interface ConfusionMatrixProps {
  matrix: number[][];
  labels: string[];
}

export function ConfusionMatrix({ matrix, labels }: ConfusionMatrixProps) {
  if (!matrix || matrix.length === 0 || !labels || labels.length === 0) {
    return (
      <Card>
        <CardHeader>
          <CardTitle>Confusion Matrix</CardTitle>
        </CardHeader>
        <CardContent>
          <p className="text-muted-foreground text-center py-8">No confusion matrix data available</p>
        </CardContent>
      </Card>
    );
  }

  // Find max value for normalization
  const maxValue = Math.max(...matrix.flat());

  return (
    <Card>
      <CardHeader>
        <CardTitle>Confusion Matrix</CardTitle>
      </CardHeader>
      <CardContent>
        <div className="overflow-x-auto">
          <div className="inline-block min-w-full">
            <table className="border-collapse">
              <thead>
                <tr>
                  <th className="border border-border p-2 text-sm font-medium text-muted-foreground"></th>
                  {labels.map((label, idx) => (
                    <th
                      key={idx}
                      className="border border-border p-2 text-sm font-medium text-muted-foreground min-w-[80px]"
                    >
                      Predicted: {label}
                    </th>
                  ))}
                </tr>
              </thead>
              <tbody>
                {matrix.map((row, rowIdx) => (
                  <tr key={rowIdx}>
                    <td className="border border-border p-2 text-sm font-medium text-muted-foreground">
                      Actual: {labels[rowIdx]}
                    </td>
                    {row.map((value, colIdx) => {
                      const intensity = maxValue > 0 ? value / maxValue : 0;
                      const bgColor = `rgba(59, 130, 246, ${0.3 + intensity * 0.7})`; // Blue gradient
                      
                      return (
                        <td
                          key={colIdx}
                          className="border border-border p-3 text-center font-semibold"
                          style={{ backgroundColor: bgColor }}
                        >
                          {value}
                        </td>
                      );
                    })}
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
        <div className="mt-4 text-sm text-muted-foreground">
          <p>Rows represent actual values, columns represent predicted values.</p>
        </div>
      </CardContent>
    </Card>
  );
}
