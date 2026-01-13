import { useState } from 'react';
import { Search, Download, ChevronDown, ChevronUp, FileText } from 'lucide-react';
import { Button } from './ui/button';
import { Input } from './ui/input';
import { Card } from './ui/card';
import { Badge } from './ui/badge';
import { Checkbox } from './ui/checkbox';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from './ui/select';

interface PredictionHistoryProps {
  onNavigate: (page: string) => void;
}

export function PredictionHistory({ onNavigate }: PredictionHistoryProps) {
  const [selectedIds, setSelectedIds] = useState<number[]>([]);
  const [expandedRows, setExpandedRows] = useState<number[]>([]);

  const predictions = [
    {
      id: 1547,
      model: 'Churn Predictor',
      input: 'Age: 35, Tenure: 6',
      prediction: 'Will Churn',
      confidence: '87%',
      date: '2h ago',
      inputs: {
        age: 35,
        accountTenure: '6 months',
        monthlyCharges: '$95',
        contractType: 'Month-to-month',
        techSupport: 'No',
      },
      topFactors: [
        'Short tenure (+32%)',
        'High charges (+28%)',
        'Monthly contract (+21%)',
      ],
    },
    {
      id: 1546,
      model: 'Revenue Model',
      input: 'Q4, Region: East',
      prediction: '$125,420',
      confidence: '92%',
      date: '3h ago',
      inputs: {
        quarter: 'Q4',
        region: 'East',
        salesTeam: '12 members',
        leadCount: '450',
      },
      topFactors: [
        'Strong Q4 seasonality (+35%)',
        'High lead volume (+28%)',
        'Experienced team (+18%)',
      ],
    },
    {
      id: 1545,
      model: 'Churn Predictor',
      input: 'Age: 52, Tenure: 24',
      prediction: "Won't Churn",
      confidence: '94%',
      date: '5h ago',
      inputs: {
        age: 52,
        accountTenure: '24 months',
        monthlyCharges: '$75',
        contractType: 'Two year',
        techSupport: 'Yes',
      },
      topFactors: [
        'Long tenure (-42%)',
        'Two year contract (-35%)',
        'Has tech support (-25%)',
      ],
    },
    {
      id: 1544,
      model: 'Churn Predictor',
      input: 'Age: 28, Tenure: 2',
      prediction: 'Will Churn',
      confidence: '76%',
      date: '1d ago',
      inputs: {
        age: 28,
        accountTenure: '2 months',
        monthlyCharges: '$89',
        contractType: 'Month-to-month',
        techSupport: 'No',
      },
      topFactors: [
        'Very short tenure (+40%)',
        'High charges (+25%)',
        'Monthly contract (+22%)',
      ],
    },
    {
      id: 1543,
      model: 'Risk Classifier',
      input: 'Score: 720, Debt: 0',
      prediction: 'Low Risk',
      confidence: '89%',
      date: '1d ago',
      inputs: {
        creditScore: 720,
        totalDebt: '$0',
        income: '$85,000',
        employmentYears: '8',
      },
      topFactors: [
        'Good credit score (+38%)',
        'No debt (+35%)',
        'Stable employment (+20%)',
      ],
    },
  ];

  const toggleSelectAll = () => {
    if (selectedIds.length === predictions.length) {
      setSelectedIds([]);
    } else {
      setSelectedIds(predictions.map(p => p.id));
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

  return (
    <div className="p-8 space-y-6">
      {/* Header */}
      <div className="flex items-start justify-between">
        <div>
          <h1>Prediction History</h1>
          <p className="text-muted-foreground mt-1">View and analyze past predictions across all models</p>
        </div>
        <Button className="bg-primary text-primary-foreground hover:bg-primary/90">
          <Download className="w-4 h-4 mr-2" />
          Export CSV
        </Button>
      </div>

      {/* Filters Bar */}
      <Card className="p-4">
        <div className="flex items-center gap-4">
          <div className="flex-1 relative">
            <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-muted-foreground" />
            <Input
              placeholder="Search predictions..."
              className="pl-10 bg-background border-border/50"
            />
          </div>
          <Select defaultValue="all">
            <SelectTrigger className="w-48">
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="all">All Models</SelectItem>
              <SelectItem value="churn">Churn Predictor</SelectItem>
              <SelectItem value="revenue">Revenue Model</SelectItem>
              <SelectItem value="risk">Risk Classifier</SelectItem>
            </SelectContent>
          </Select>
          <Select defaultValue="30">
            <SelectTrigger className="w-48">
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="7">Last 7 days</SelectItem>
              <SelectItem value="30">Last 30 days</SelectItem>
              <SelectItem value="90">Last 90 days</SelectItem>
              <SelectItem value="all">All time</SelectItem>
            </SelectContent>
          </Select>
          <Select defaultValue="all-results">
            <SelectTrigger className="w-40">
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="all-results">All Results</SelectItem>
              <SelectItem value="churn">Will Churn</SelectItem>
              <SelectItem value="no-churn">Won&apos;t Churn</SelectItem>
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
              <Button variant="outline" size="sm" className="text-error border-error/30 hover:bg-error hover:text-white">
                Delete
              </Button>
            </div>
          </div>
        </Card>
      )}

      {/* Predictions Table */}
      <Card className="p-6">
        <div className="overflow-x-auto">
          <table className="w-full">
            <thead>
              <tr className="border-b border-border">
                <th className="text-left py-3 px-4">
                  <Checkbox
                    checked={selectedIds.length === predictions.length}
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
              {predictions.map((pred) => (
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
                    <td className="py-4 px-4">{pred.model}</td>
                    <td className="py-4 px-4 text-muted-foreground">{pred.input}</td>
                    <td className="py-4 px-4">
                      <Badge
                        variant={pred.prediction.includes("Won't") || pred.prediction === 'Low Risk' ? 'outline' : 'default'}
                        className={
                          pred.prediction.includes("Won't") || pred.prediction === 'Low Risk'
                            ? 'border-success/30 text-success'
                            : pred.prediction.includes('Will')
                            ? 'bg-error/20 text-error border-error/30'
                            : 'border-primary/30 text-primary'
                        }
                      >
                        {pred.prediction}
                      </Badge>
                    </td>
                    <td className="py-4 px-4">{pred.confidence}</td>
                    <td className="py-4 px-4 text-muted-foreground">{pred.date}</td>
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
                            <h4 className="mb-3">Input Values</h4>
                            <div className="space-y-2 text-sm">
                              {Object.entries(pred.inputs).map(([key, value]) => (
                                <div key={key} className="flex items-center justify-between">
                                  <span className="text-muted-foreground capitalize">{key.replace(/([A-Z])/g, ' $1').trim()}:</span>
                                  <span>{value}</span>
                                </div>
                              ))}
                            </div>
                          </div>
                          <div>
                            <h4 className="mb-3">Quick Explanation</h4>
                            <p className="text-sm text-muted-foreground mb-2">Top factors:</p>
                            <ul className="space-y-1 text-sm">
                              {pred.topFactors.map((factor, i) => (
                                <li key={i} className="flex items-start gap-2">
                                  <span className="text-primary mt-0.5">•</span>
                                  <span>{factor}</span>
                                </li>
                              ))}
                            </ul>
                            <Button
                              variant="outline"
                              size="sm"
                              className="mt-4 border-primary/30 hover:bg-primary/10"
                              onClick={() => onNavigate('predictions-new')}
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
          <p className="text-sm text-muted-foreground">Showing 1-20 of 156</p>
          <div className="flex items-center gap-2">
            <Button variant="outline" size="sm" disabled>
              {'<'}
            </Button>
            <Button variant="outline" size="sm" className="bg-primary text-primary-foreground">
              1
            </Button>
            <Button variant="outline" size="sm">
              2
            </Button>
            <Button variant="outline" size="sm">
              3
            </Button>
            <Button variant="outline" size="sm">
              {'>'}
            </Button>
          </div>
        </div>
      </Card>
    </div>
  );
}