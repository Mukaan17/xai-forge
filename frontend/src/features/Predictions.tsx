import { useState } from 'react';
import { Sparkles, Info, Download, Copy, Share2, AlertCircle } from 'lucide-react';
import { Button } from './ui/button';
import { Input } from './ui/input';
import { Label } from './ui/label';
import { Card } from './ui/card';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from './ui/select';
import { Progress } from './ui/progress';
import { Badge } from './ui/badge';
import { Tooltip, TooltipContent, TooltipProvider, TooltipTrigger } from './ui/tooltip';

interface PredictionsProps {
  onNavigate: (page: string) => void;
}

export function Predictions({ onNavigate }: PredictionsProps) {
  const [selectedModel, setSelectedModel] = useState('churn_predictor_v2');
  const [predictionMade, setPredictionMade] = useState(false);
  const [showExplanation, setShowExplanation] = useState(false);

  const [formData, setFormData] = useState({
    age: '35',
    income: '75000',
    region: 'northeast',
    accountTenure: '6',
    monthlyCharges: '95',
    contractType: 'monthly',
    techSupport: 'no',
    internetService: 'fiber',
  });

  const models = [
    { id: 'churn_predictor_v2', name: 'Churn Predictor v2', accuracy: '87.3%' },
    { id: 'revenue_forecaster', name: 'Revenue Forecaster', accuracy: '92.1%' },
    { id: 'lead_scoring', name: 'Lead Scoring Model', accuracy: '85.7%' },
  ];

  const featureImpacts = [
    { feature: 'Account Tenure (6 mo)', impact: 0.32, positive: true, value: '6 months' },
    { feature: 'Monthly Charges ($95)', impact: 0.28, positive: true, value: '$95' },
    { feature: 'Contract Type (Monthly)', impact: 0.21, positive: true, value: 'Month-to-month' },
    { feature: 'Tech Support (No)', impact: -0.24, positive: false, value: 'No subscription' },
    { feature: 'Internet Service (Fiber)', impact: -0.18, positive: false, value: 'Fiber optic' },
  ];

  const handleGeneratePrediction = () => {
    setPredictionMade(true);
    setTimeout(() => setShowExplanation(true), 500);
  };

  return (
    <div className="p-8">
      <div className="mb-6">
        <h1>Make Prediction</h1>
        <p className="text-muted-foreground mt-1">Generate predictions and understand model decisions</p>
      </div>

      <div className="grid grid-cols-5 gap-6">
        {/* Left Panel - Prediction Input */}
        <div className="col-span-2 space-y-6">
          <Card className="p-6">
            <h3 className="mb-4">Select Model</h3>
            <Select value={selectedModel} onValueChange={setSelectedModel}>
              <SelectTrigger>
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                {models.map((model) => (
                  <SelectItem key={model.id} value={model.id}>
                    <div className="flex items-center gap-2">
                      <span>{model.name}</span>
                      <Badge variant="outline" className="text-xs border-primary/30 text-primary">
                        {model.accuracy}
                      </Badge>
                    </div>
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </Card>

          <Card className="p-6">
            <h3 className="mb-4">Input Features</h3>
            <div className="space-y-4">
              {/* Age */}
              <div>
                <div className="flex items-center justify-between mb-2">
                  <Label>Age</Label>
                  <span className="text-xs text-muted-foreground">Range: 18-100</span>
                </div>
                <Input
                  type="number"
                  value={formData.age}
                  onChange={(e) => setFormData({ ...formData, age: e.target.value })}
                  placeholder="35"
                />
              </div>

              {/* Annual Income */}
              <div>
                <div className="flex items-center justify-between mb-2">
                  <Label>Annual Income</Label>
                  <span className="text-xs text-muted-foreground">USD</span>
                </div>
                <Input
                  type="number"
                  value={formData.income}
                  onChange={(e) => setFormData({ ...formData, income: e.target.value })}
                  placeholder="75000"
                />
              </div>

              {/* Region */}
              <div>
                <Label className="mb-2 block">Region</Label>
                <Select value={formData.region} onValueChange={(value) => setFormData({ ...formData, region: value })}>
                  <SelectTrigger>
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="northeast">Northeast</SelectItem>
                    <SelectItem value="southeast">Southeast</SelectItem>
                    <SelectItem value="midwest">Midwest</SelectItem>
                    <SelectItem value="west">West</SelectItem>
                  </SelectContent>
                </Select>
              </div>

              {/* Account Tenure */}
              <div>
                <div className="flex items-center justify-between mb-2">
                  <Label>Account Tenure</Label>
                  <span className="text-xs text-muted-foreground">Months</span>
                </div>
                <Input
                  type="number"
                  value={formData.accountTenure}
                  onChange={(e) => setFormData({ ...formData, accountTenure: e.target.value })}
                  placeholder="24"
                />
              </div>

              {/* Monthly Charges */}
              <div>
                <div className="flex items-center justify-between mb-2">
                  <Label>Monthly Charges</Label>
                  <span className="text-xs text-muted-foreground">USD</span>
                </div>
                <Input
                  type="number"
                  value={formData.monthlyCharges}
                  onChange={(e) => setFormData({ ...formData, monthlyCharges: e.target.value })}
                  placeholder="95"
                />
              </div>

              {/* Contract Type */}
              <div>
                <Label className="mb-2 block">Contract Type</Label>
                <Select value={formData.contractType} onValueChange={(value) => setFormData({ ...formData, contractType: value })}>
                  <SelectTrigger>
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="monthly">Month-to-month</SelectItem>
                    <SelectItem value="yearly">One year</SelectItem>
                    <SelectItem value="two-year">Two year</SelectItem>
                  </SelectContent>
                </Select>
              </div>

              {/* Tech Support */}
              <div>
                <Label className="mb-2 block">Tech Support</Label>
                <Select value={formData.techSupport} onValueChange={(value) => setFormData({ ...formData, techSupport: value })}>
                  <SelectTrigger>
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="yes">Yes</SelectItem>
                    <SelectItem value="no">No</SelectItem>
                  </SelectContent>
                </Select>
              </div>

              {/* Internet Service */}
              <div>
                <Label className="mb-2 block">Internet Service</Label>
                <Select value={formData.internetService} onValueChange={(value) => setFormData({ ...formData, internetService: value })}>
                  <SelectTrigger>
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="fiber">Fiber optic</SelectItem>
                    <SelectItem value="dsl">DSL</SelectItem>
                    <SelectItem value="none">No</SelectItem>
                  </SelectContent>
                </Select>
              </div>
            </div>

            <Button
              onClick={handleGeneratePrediction}
              className="w-full mt-6 bg-primary text-primary-foreground hover:bg-primary/90"
            >
              <Sparkles className="w-4 h-4 mr-2" />
              Generate Prediction
            </Button>
          </Card>

          {/* Result Card */}
          {predictionMade && (
            <Card className="p-6 border-2 border-error/30 bg-error/5">
              <h4 className="mb-4">PREDICTION RESULT</h4>
              
              <div className="mb-4">
                <div className="flex items-center justify-between text-sm mb-2">
                  <span className="text-muted-foreground">Confidence</span>
                  <span>87.3%</span>
                </div>
                <div className="w-full h-3 bg-muted rounded-full overflow-hidden">
                  <div className="h-full bg-gradient-to-r from-error to-warning rounded-full" style={{ width: '87.3%' }}></div>
                </div>
              </div>

              <div className="text-center py-6">
                <p className="text-3xl font-semibold text-error mb-1">WILL CHURN</p>
                <p className="text-muted-foreground">Confidence: 87.3%</p>
              </div>

              <Button
                variant="outline"
                className="w-full border-primary/30 hover:bg-primary/10"
                onClick={() => setShowExplanation(true)}
              >
                Explain This Prediction
              </Button>
            </Card>
          )}
        </div>

        {/* Right Panel - Explanation View */}
        <div className="col-span-3">
          <Card className="p-6 min-h-[600px]">
            {!showExplanation ? (
              <div className="flex flex-col items-center justify-center h-full text-center py-20">
                <div className="w-24 h-24 rounded-full bg-muted flex items-center justify-center mb-6">
                  <Sparkles className="w-12 h-12 text-muted-foreground" />
                </div>
                <h3 className="mb-2">No Prediction Yet</h3>
                <p className="text-muted-foreground max-w-md">
                  Fill in the input form and generate a prediction to see detailed explanations here
                </p>
              </div>
            ) : (
              <div className="space-y-6">
                {/* Header */}
                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-2">
                    <h3>Why this prediction?</h3>
                    <TooltipProvider>
                      <Tooltip>
                        <TooltipTrigger>
                          <Info className="w-4 h-4 text-muted-foreground" />
                        </TooltipTrigger>
                        <TooltipContent>
                          <p className="max-w-xs">
                            This explanation uses LIME (Local Interpretable Model-agnostic Explanations) to show which features influenced the prediction.
                          </p>
                        </TooltipContent>
                      </Tooltip>
                    </TooltipProvider>
                  </div>
                  <div className="flex gap-2">
                    <Button variant="outline" size="sm">
                      <Download className="w-4 h-4 mr-2" />
                      Download PDF
                    </Button>
                    <Button variant="outline" size="sm">
                      <Copy className="w-4 h-4 mr-2" />
                      Copy
                    </Button>
                    <Button variant="outline" size="sm">
                      <Share2 className="w-4 h-4 mr-2" />
                      Share
                    </Button>
                  </div>
                </div>

                {/* Feature Impact Chart */}
                <div>
                  <h4 className="mb-4">Feature Impact Analysis</h4>
                  <div className="space-y-4">
                    {featureImpacts.map((item, index) => {
                      const maxImpact = 0.4;
                      const widthPercent = (Math.abs(item.impact) / maxImpact) * 100;
                      
                      return (
                        <div key={index}>
                          <div className="flex items-center justify-between text-sm mb-2">
                            <span className="font-medium">{item.feature}</span>
                            <span className={item.positive ? 'text-error' : 'text-primary'}>
                              {item.positive ? '+' : ''}{item.impact.toFixed(2)}
                            </span>
                          </div>
                          <div className="flex items-center gap-2">
                            <div className="flex-1 h-8 bg-muted rounded-lg overflow-hidden flex items-center">
                              {item.positive ? (
                                <>
                                  <div className="flex-1"></div>
                                  <div
                                    className="h-full bg-gradient-to-r from-error/60 to-error rounded-r-lg"
                                    style={{ width: `${widthPercent}%` }}
                                  ></div>
                                </>
                              ) : (
                                <>
                                  <div
                                    className="h-full bg-gradient-to-l from-primary/60 to-primary rounded-l-lg"
                                    style={{ width: `${widthPercent}%` }}
                                  ></div>
                                  <div className="flex-1"></div>
                                </>
                              )}
                            </div>
                          </div>
                        </div>
                      );
                    })}
                  </div>
                  
                  {/* Legend */}
                  <div className="flex items-center justify-center gap-6 mt-6 text-sm">
                    <div className="flex items-center gap-2">
                      <div className="w-3 h-3 rounded bg-error"></div>
                      <span className="text-muted-foreground">Increases churn risk</span>
                    </div>
                    <div className="flex items-center gap-2">
                      <div className="w-3 h-3 rounded bg-primary"></div>
                      <span className="text-muted-foreground">Decreases churn risk</span>
                    </div>
                  </div>
                </div>

                {/* Human-Readable Summary */}
                <Card className="p-6 bg-primary/5 border-primary/20">
                  <div className="flex items-start gap-3">
                    <div className="w-10 h-10 rounded-lg bg-primary/10 flex items-center justify-center flex-shrink-0">
                      <AlertCircle className="w-5 h-5 text-primary" />
                    </div>
                    <div className="space-y-3">
                      <h4 className="text-primary">EXPLANATION SUMMARY</h4>
                      
                      <div>
                        <p className="mb-2">This customer is likely to churn primarily because:</p>
                        <ul className="space-y-1 ml-4">
                          <li className="flex items-start gap-2">
                            <span className="text-error mt-1">•</span>
                            <span>Short account tenure (6 months) increases risk</span>
                          </li>
                          <li className="flex items-start gap-2">
                            <span className="text-error mt-1">•</span>
                            <span>High monthly charges ($95) correlate with churn</span>
                          </li>
                          <li className="flex items-start gap-2">
                            <span className="text-error mt-1">•</span>
                            <span>Month-to-month contract shows lower commitment</span>
                          </li>
                        </ul>
                      </div>

                      <div>
                        <p className="mb-2">Factors reducing churn risk:</p>
                        <ul className="space-y-1 ml-4">
                          <li className="flex items-start gap-2">
                            <span className="text-primary mt-1">•</span>
                            <span>Has tech support subscription</span>
                          </li>
                          <li className="flex items-start gap-2">
                            <span className="text-primary mt-1">•</span>
                            <span>Uses fiber internet (higher engagement)</span>
                          </li>
                        </ul>
                      </div>
                    </div>
                  </div>
                </Card>

                {/* Recommendations */}
                <Card className="p-6 bg-warning/5 border-warning/20">
                  <h4 className="mb-3 text-warning">💡 Recommended Actions</h4>
                  <ul className="space-y-2">
                    <li className="flex items-start gap-2">
                      <span className="text-warning mt-1">1.</span>
                      <span>Offer incentive to switch to annual contract (reduces month-to-month risk)</span>
                    </li>
                    <li className="flex items-start gap-2">
                      <span className="text-warning mt-1">2.</span>
                      <span>Consider loyalty discount to offset high monthly charges</span>
                    </li>
                    <li className="flex items-start gap-2">
                      <span className="text-warning mt-1">3.</span>
                      <span>Reach out with retention offer before account hits 12-month mark</span>
                    </li>
                  </ul>
                </Card>
              </div>
            )}
          </Card>
        </div>
      </div>
    </div>
  );
}
