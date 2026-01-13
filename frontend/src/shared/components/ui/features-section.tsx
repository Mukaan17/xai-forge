import * as React from "react";
import {
  Card,
  CardContent,
  CardDescription,
  CardFooter,
  CardHeader,
  CardTitle,
} from "@/shared/components/ui/card";
import { Badge } from "@/shared/components/ui/badge";
import { BentoGridShowcase } from "@/shared/components/ui/bento-product-features";
import {
  Database,
  Brain,
  BarChart3,
  Sparkles,
  FileText,
  Zap,
} from "lucide-react";

// Feature cards customized for ML Operations Platform
const DatasetManagementCard = () => (
  <Card className="flex h-full flex-col border-border/60 bg-card">
    <CardHeader>
      <div className="mb-4 flex h-12 w-12 items-center justify-center rounded-full bg-primary/10 dark:bg-primary/20">
        <Database className="h-6 w-6 text-primary" />
      </div>
      <CardTitle className="text-xl font-semibold">Dataset Management</CardTitle>
      <CardDescription className="text-muted-foreground">
        Upload and manage CSV datasets with ease. View headers, row counts, and organize your data efficiently. Parse and validate datasets before training.
      </CardDescription>
    </CardHeader>
    <CardFooter className="mt-auto">
      <Badge variant="outline" className="border-primary/30 text-primary">
        CSV Support
      </Badge>
    </CardFooter>
  </Card>
);

const ModelsTrainedCard = () => (
  <Card className="h-full border-border/60 bg-card">
    <CardContent className="flex h-full flex-col justify-between p-6">
      <div>
        <CardTitle className="text-base font-medium">Models Trained</CardTitle>
        <CardDescription className="text-muted-foreground">Active ML Models</CardDescription>
      </div>
      <div className="flex -space-x-2 overflow-hidden">
        <div className="inline-block h-8 w-8 rounded-full ring-2 ring-background bg-primary/20 flex items-center justify-center">
          <Brain className="h-4 w-4 text-primary" />
        </div>
        <div className="inline-block h-8 w-8 rounded-full ring-2 ring-background bg-secondary/20 flex items-center justify-center">
          <BarChart3 className="h-4 w-4 text-secondary" />
        </div>
        <div className="inline-block h-8 w-8 rounded-full ring-2 ring-background bg-primary/20 flex items-center justify-center">
          <Sparkles className="h-4 w-4 text-primary" />
        </div>
      </div>
    </CardContent>
  </Card>
);

const AccuracyCard = () => (
  <Card className="h-full border-border/60 bg-card">
    <CardContent className="flex h-full flex-col justify-between p-6">
      <div className="flex items-start justify-between">
        <div>
          <CardTitle className="text-base font-medium">Model Accuracy</CardTitle>
          <CardDescription className="text-muted-foreground">Prediction Confidence</CardDescription>
        </div>
        <Badge variant="outline" className="border-primary/30 text-primary">
          High
        </Badge>
      </div>
      <div>
        <span className="text-6xl font-bold bg-gradient-to-r from-primary to-secondary bg-clip-text text-transparent">94%</span>
      </div>
      <div className="flex justify-between text-xs text-muted-foreground">
        <span>Average Accuracy</span>
        <span>Real-time</span>
      </div>
    </CardContent>
  </Card>
);

const ExplainableAICard = () => (
  <Card className="relative h-full w-full overflow-hidden border-border/60 bg-card">
    {/* Dotted background */}
    <div
      className="absolute inset-0 opacity-10"
      style={{
        backgroundImage: "radial-gradient(hsl(var(--foreground)) 1px, transparent 1px)",
        backgroundSize: "16px 16px",
      }}
    />
    <CardContent className="relative z-10 flex h-full items-center justify-center p-6">
      <div className="text-center">
        <Sparkles className="h-12 w-12 mx-auto mb-2 text-primary" />
        <span className="text-6xl font-bold text-foreground/90">XAI</span>
        <p className="text-sm text-muted-foreground mt-2">Explainable AI</p>
      </div>
    </CardContent>
  </Card>
);

const ModelTrainingCard = () => (
  <Card className="h-full border-border/60 bg-card">
    <CardContent className="flex h-full flex-col justify-end p-6">
      <CardTitle className="text-base font-medium">ML Model Training</CardTitle>
      <CardDescription className="text-muted-foreground">
        Train classification and regression models using state-of-the-art algorithms with Tribuo.
      </CardDescription>
    </CardContent>
  </Card>
);

const PredictionsCard = () => (
  <Card className="h-full border-border/60 bg-card">
    <CardContent className="flex h-full flex-wrap items-center justify-between gap-4 p-6">
      <div>
        <CardTitle className="text-base font-medium">Smart Predictions</CardTitle>
        <CardDescription className="text-muted-foreground">
          Get real-time predictions with confidence scores and LIME-based explanations.
        </CardDescription>
      </div>
      <div className="flex items-center gap-2">
        <div className="flex h-7 w-7 items-center justify-center rounded-md border bg-background font-mono text-xs font-medium text-muted-foreground">
          <Zap className="h-3 w-3" />
        </div>
        <span className="text-muted-foreground">+</span>
        <div className="flex h-7 w-7 items-center justify-center rounded-md border bg-background font-mono text-xs font-medium text-muted-foreground">
          <FileText className="h-3 w-3" />
        </div>
      </div>
    </CardContent>
  </Card>
);

// Main Feature Section Component
export function FeaturesSection() {
  return (
    <div id="features" className="w-full px-6 py-16 md:px-10 md:py-24 scroll-mt-20">
      <div className="mb-12 text-center">
        <h2 className="text-4xl font-bold tracking-tight bg-gradient-to-r from-primary via-secondary to-primary bg-clip-text text-transparent mb-4">
          Platform Features
        </h2>
        <p className="text-lg text-muted-foreground max-w-2xl mx-auto">
          Build, train, and deploy machine learning models with complete transparency.
          <br />
          Everything you need to manage your ML workflow from datasets to predictions.
        </p>
      </div>

      <BentoGridShowcase
        integration={<DatasetManagementCard />}
        trackers={<ModelsTrainedCard />}
        statistic={<AccuracyCard />}
        focus={<ExplainableAICard />}
        productivity={<ModelTrainingCard />}
        shortcuts={<PredictionsCard />}
      />
    </div>
  );
}
