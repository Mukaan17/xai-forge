import { useState } from 'react';
import { ArrowRight, ArrowLeft, Upload, BrainCircuit, Sparkles, BookOpen, Database, X } from 'lucide-react';
import { Button } from './ui/button';
import { Checkbox } from './ui/checkbox';
import { Label } from './ui/label';

interface OnboardingFlowProps {
  onComplete: () => void;
  onSkip: () => void;
}

export function OnboardingFlow({ onComplete, onSkip }: OnboardingFlowProps) {
  const [step, setStep] = useState(1);
  const [dontShowAgain, setDontShowAgain] = useState(false);

  const handleComplete = () => {
    if (dontShowAgain) {
      localStorage.setItem('xai-forge-onboarding-completed', 'true');
    }
    onComplete();
  };

  return (
    <div className="fixed inset-0 bg-background/95 backdrop-blur-sm z-50 flex items-center justify-center p-6">
      <div className="w-full max-w-3xl bg-surface border border-border rounded-xl shadow-2xl overflow-hidden">
        {/* Step 1: Welcome */}
        {step === 1 && (
          <div className="p-12 text-center">
            <div className="w-20 h-20 rounded-full bg-gradient-to-br from-primary to-secondary mx-auto mb-6 flex items-center justify-center">
              <BrainCircuit className="w-10 h-10 text-white" />
            </div>
            <h1 className="mb-4">Welcome to XAI-Forge</h1>
            <p className="text-xl text-muted-foreground mb-8">
              Demystify Machine Learning with Explainable AI
            </p>
            <p className="text-muted-foreground max-w-xl mx-auto mb-12">
              XAI-Forge helps you build ML models and understand exactly why they make the predictions they do.
            </p>

            <div className="flex items-center justify-center gap-12 mb-12">
              <div className="text-center">
                <div className="w-16 h-16 rounded-xl bg-primary/10 flex items-center justify-center mx-auto mb-3">
                  <Upload className="w-8 h-8 text-primary" />
                </div>
                <p className="font-medium">Upload Data</p>
              </div>
              <div className="text-4xl text-muted-foreground">→</div>
              <div className="text-center">
                <div className="w-16 h-16 rounded-xl bg-secondary/10 flex items-center justify-center mx-auto mb-3">
                  <BrainCircuit className="w-8 h-8 text-secondary" />
                </div>
                <p className="font-medium">Train Model</p>
              </div>
              <div className="text-4xl text-muted-foreground">→</div>
              <div className="text-center">
                <div className="w-16 h-16 rounded-xl bg-success/10 flex items-center justify-center mx-auto mb-3">
                  <Sparkles className="w-8 h-8 text-success" />
                </div>
                <p className="font-medium">Explain Results</p>
              </div>
            </div>

            <Button onClick={() => setStep(2)} size="lg" className="bg-primary text-primary-foreground hover:bg-primary/90">
              Get Started
              <ArrowRight className="w-5 h-5 ml-2" />
            </Button>
            <Button variant="ghost" onClick={onSkip} className="block mx-auto mt-4">
              Skip tutorial
            </Button>
          </div>
        )}

        {/* Step 2: Quick Tour */}
        {step === 2 && (
          <div className="p-12">
            <div className="mb-8">
              <div className="flex items-center justify-center gap-2 mb-6">
                {[1, 2, 3, 4].map((i) => (
                  <div
                    key={i}
                    className={`w-2 h-2 rounded-full ${i === 2 ? 'bg-primary w-8' : 'bg-muted'}`}
                  ></div>
                ))}
              </div>
              <h2 className="text-center mb-3">Navigate Your Workspace</h2>
              <p className="text-center text-muted-foreground max-w-xl mx-auto">
                All your ML tools are organized in the left sidebar for easy access.
              </p>
            </div>

            <div className="relative bg-gradient-to-br from-background to-muted/30 rounded-lg p-8 mb-8 min-h-[400px] flex items-center justify-between border border-border">
              {/* Sidebar Preview */}
              <div className="w-56 h-96 bg-sidebar border border-sidebar-border rounded-lg p-4 shadow-xl">
                <div className="space-y-2">
                  {[
                    { icon: '📊', label: 'Dashboard', active: true },
                    { icon: '🗂️', label: 'Datasets', active: false },
                    { icon: '🧠', label: 'Train Model', active: false },
                    { icon: '📦', label: 'Models', active: false },
                    { icon: '🎯', label: 'Predictions', active: false },
                    { icon: '📈', label: 'History', active: false },
                  ].map((item, i) => (
                    <div
                      key={i}
                      className={`flex items-center gap-3 px-3 py-2.5 rounded-lg transition-colors ${
                        item.active
                          ? 'bg-primary text-primary-foreground'
                          : 'hover:bg-sidebar-accent text-sidebar-foreground'
                      }`}
                    >
                      <span className="text-lg">{item.icon}</span>
                      <span className="text-sm">{item.label}</span>
                    </div>
                  ))}
                </div>
              </div>

              {/* Info Card */}
              <div className="max-w-md ml-8">
                <div className="bg-surface border-2 border-primary/30 rounded-lg p-6 shadow-xl">
                  <div className="w-12 h-12 rounded-lg bg-primary/10 flex items-center justify-center mb-4">
                    <Database className="w-6 h-6 text-primary" />
                  </div>
                  <h3 className="mb-2">Your Navigation Hub</h3>
                  <p className="text-muted-foreground mb-4">
                    Access all features from the sidebar:
                  </p>
                  <ul className="space-y-2 text-sm text-muted-foreground">
                    <li className="flex items-center gap-2">
                      <span className="text-primary">✓</span>
                      Upload and manage datasets
                    </li>
                    <li className="flex items-center gap-2">
                      <span className="text-primary">✓</span>
                      Train and compare models
                    </li>
                    <li className="flex items-center gap-2">
                      <span className="text-primary">✓</span>
                      Make predictions and analyze results
                    </li>
                    <li className="flex items-center gap-2">
                      <span className="text-primary">✓</span>
                      View activity history and logs
                    </li>
                  </ul>
                </div>
              </div>
            </div>

            <div className="flex justify-between">
              <Button variant="outline" onClick={() => setStep(1)}>
                <ArrowLeft className="w-4 h-4 mr-2" />
                Back
              </Button>
              <Button onClick={() => setStep(3)} className="bg-primary text-primary-foreground hover:bg-primary/90">
                Next
                <ArrowRight className="w-4 h-4 ml-2" />
              </Button>
            </div>
          </div>
        )}

        {/* Step 3: Sample Dataset Option */}
        {step === 3 && (
          <div className="p-12 text-center">
            <div className="mb-8">
              <div className="flex items-center justify-center gap-2 mb-6">
                {[1, 2, 3, 4].map((i) => (
                  <div
                    key={i}
                    className={`w-2 h-2 rounded-full ${i === 3 ? 'bg-primary w-8' : 'bg-muted'}`}
                  ></div>
                ))}
              </div>
            </div>

            <h2 className="mb-4">Try it with sample data?</h2>
            <p className="text-muted-foreground max-w-xl mx-auto mb-8">
              We&apos;ve prepared a sample dataset so you can explore XAI-Forge without uploading your own data first.
            </p>

            <div className="max-w-2xl mx-auto mb-8">
              <div className="p-6 border-2 border-primary/30 bg-primary/5 rounded-lg hover:border-primary/50 transition-colors">
                <div className="flex items-start gap-4">
                  <div className="w-12 h-12 rounded-lg bg-primary/10 flex items-center justify-center flex-shrink-0">
                    <Database className="w-6 h-6 text-primary" />
                  </div>
                  <div className="flex-1 text-left">
                    <p className="font-medium mb-2">customer_churn_sample.csv</p>
                    <p className="text-sm text-muted-foreground mb-4">
                      5,000 rows • 12 features • Classification task
                    </p>
                    <Button className="bg-primary text-primary-foreground hover:bg-primary/90">
                      Load Sample Dataset
                    </Button>
                  </div>
                </div>
              </div>
            </div>

            <p className="text-muted-foreground mb-4">or</p>
            <Button variant="outline" size="lg">
              <Upload className="w-4 h-4 mr-2" />
              Upload My Own Data
            </Button>

            <div className="flex justify-between mt-12">
              <Button variant="outline" onClick={() => setStep(2)}>
                <ArrowLeft className="w-4 h-4 mr-2" />
                Back
              </Button>
              <Button onClick={() => setStep(4)} className="bg-primary text-primary-foreground hover:bg-primary/90">
                Next
                <ArrowRight className="w-4 h-4 ml-2" />
              </Button>
            </div>
          </div>
        )}

        {/* Step 4: Ready */}
        {step === 4 && (
          <div className="p-12 text-center">
            <div className="mb-8">
              <div className="flex items-center justify-center gap-2 mb-6">
                {[1, 2, 3, 4].map((i) => (
                  <div
                    key={i}
                    className={`w-2 h-2 rounded-full ${i === 4 ? 'bg-primary w-8' : 'bg-muted'}`}
                  ></div>
                ))}
              </div>
            </div>

            <div className="w-20 h-20 rounded-full bg-success/10 mx-auto mb-6 flex items-center justify-center">
              <span className="text-4xl">✅</span>
            </div>
            <h2 className="mb-8">You&apos;re all set!</h2>

            <div className="max-w-md mx-auto space-y-4 mb-8">
              <h4 className="text-left">Quick Actions</h4>
              <Button variant="outline" className="w-full justify-start h-auto p-4 hover:border-primary/30">
                <div className="w-10 h-10 rounded-lg bg-primary/10 flex items-center justify-center mr-4">
                  <Database className="w-5 h-5 text-primary" />
                </div>
                <div className="text-left">
                  <p className="font-medium">Upload Dataset</p>
                  <p className="text-sm text-muted-foreground">Start with your own data</p>
                </div>
              </Button>
              <Button variant="outline" className="w-full justify-start h-auto p-4 hover:border-primary/30">
                <div className="w-10 h-10 rounded-lg bg-secondary/10 flex items-center justify-center mr-4">
                  <BrainCircuit className="w-5 h-5 text-secondary" />
                </div>
                <div className="text-left">
                  <p className="font-medium">Train Model</p>
                  <p className="text-sm text-muted-foreground">Train your first ML model</p>
                </div>
              </Button>
              <Button variant="outline" className="w-full justify-start h-auto p-4 hover:border-primary/30">
                <div className="w-10 h-10 rounded-lg bg-muted flex items-center justify-center mr-4">
                  <BookOpen className="w-5 h-5" />
                </div>
                <div className="text-left">
                  <p className="font-medium">Read Docs</p>
                  <p className="text-sm text-muted-foreground">Learn more about XAI-Forge</p>
                </div>
              </Button>
            </div>

            <div className="flex items-center justify-center gap-2 mb-6">
              <Checkbox
                id="dont-show"
                checked={dontShowAgain}
                onCheckedChange={(checked) => setDontShowAgain(checked as boolean)}
              />
              <Label htmlFor="dont-show" className="cursor-pointer">
                Don&apos;t show this again
              </Label>
            </div>

            <Button onClick={handleComplete} size="lg" className="bg-primary text-primary-foreground hover:bg-primary/90">
              Go to Dashboard
            </Button>
          </div>
        )}
      </div>
    </div>
  );
}