import * as React from "react";
import { motion } from "framer-motion";
import { BentoCard, BentoGrid } from "@/shared/components/ui/bento-grid";
import {
  Database,
  Brain,
  Sparkles,
  Zap,
  Target,
} from "lucide-react";

// Most impactful features for the bento grid
const features = [
  {
    Icon: Database,
    name: "Dataset Management",
    description: "Upload and manage CSV datasets with ease. View headers, row counts, and organize your data efficiently. Parse and validate datasets before training.",
    detailedDescription: "Our comprehensive dataset management system allows you to seamlessly upload, validate, and organize your CSV files. With intuitive tools for viewing dataset headers, row counts, and data types, you can quickly understand your data structure before training models. The system automatically parses and validates datasets, ensuring data quality and preventing errors during the training process.",
    features: [
      "CSV file upload with drag-and-drop support",
      "Automatic data validation and type detection",
      "Dataset preview with header and row count information",
      "Data organization and categorization tools",
      "Export capabilities for processed datasets"
    ],
    href: "/datasets",
    cta: "Learn More",
    background: (
      <div className="absolute inset-0 bg-gradient-to-br from-primary/10 via-primary/5 to-transparent dark:from-primary/20 dark:via-primary/10 dark:to-transparent" />
    ),
    className: "lg:row-start-1 lg:row-end-3 lg:col-start-1 lg:col-end-2",
  },
  {
    Icon: Brain,
    name: "ML Model Training",
    description: "Train classification and regression models using state-of-the-art algorithms with Tribuo. Support for multiple model types and hyperparameter tuning.",
    detailedDescription: "Leverage the power of Tribuo, Oracle's machine learning library, to train sophisticated models for both classification and regression tasks. Our platform supports a wide range of algorithms including Random Forest, Gradient Boosting, Logistic Regression, and Neural Networks. With built-in hyperparameter tuning capabilities, you can optimize your models for peak performance.",
    features: [
      "Multiple algorithm support (Random Forest, Gradient Boosting, etc.)",
      "Automated hyperparameter tuning",
      "Real-time training progress monitoring",
      "Model versioning and comparison tools",
      "Support for both classification and regression tasks"
    ],
    href: "/models",
    cta: "Learn More",
    background: (
      <div className="absolute inset-0 bg-gradient-to-br from-secondary/10 via-secondary/5 to-transparent dark:from-secondary/20 dark:via-secondary/10 dark:to-transparent" />
    ),
    className: "lg:col-start-2 lg:col-end-4 lg:row-start-1 lg:row-end-2",
  },
  {
    Icon: Sparkles,
    name: "Explainable AI",
    description: "Get human-understandable explanations for model predictions using LIME. Understand why your models make specific decisions with transparent, interpretable insights.",
    detailedDescription: "Explainable AI is at the core of our platform. Using LIME (Local Interpretable Model-agnostic Explanations), we provide clear, human-understandable explanations for every prediction. Understand which features influenced your model's decision, see feature importance scores, and gain confidence in your model's reliability through transparent, interpretable insights.",
    features: [
      "LIME-based explanation generation",
      "Feature importance visualization",
      "Local and global interpretability",
      "Interactive explanation dashboards",
      "Export explanations for documentation"
    ],
    href: "/predictions",
    cta: "Learn More",
    background: (
      <div className="absolute inset-0 bg-gradient-to-br from-primary/10 via-secondary/5 to-transparent dark:from-primary/20 dark:via-secondary/10 dark:to-transparent" />
    ),
    className: "lg:col-start-1 lg:col-end-2 lg:row-start-3 lg:row-end-4",
  },
  {
    Icon: Zap,
    name: "Smart Predictions",
    description: "Get real-time predictions with confidence scores and LIME-based explanations. Track prediction history and analyze model performance with detailed insights.",
    detailedDescription: "Make predictions in real-time with our intuitive prediction interface. Each prediction comes with confidence scores, probability distributions, and detailed LIME-based explanations. Track your prediction history, analyze patterns, and continuously improve your models based on real-world performance data.",
    features: [
      "Real-time prediction generation",
      "Confidence scores and probability distributions",
      "Complete prediction history tracking",
      "Batch prediction capabilities",
      "Performance analytics and insights"
    ],
    href: "/predictions",
    cta: "Learn More",
    background: (
      <div className="absolute inset-0 bg-gradient-to-br from-secondary/10 via-primary/5 to-transparent dark:from-secondary/20 dark:via-primary/10 dark:to-transparent" />
    ),
    className: "lg:col-start-2 lg:col-end-3 lg:row-start-2 lg:row-end-4",
  },
  {
    Icon: Target,
    name: "Model Accuracy",
    description: "Track model performance with comprehensive metrics including accuracy, precision, recall, and F1 scores. Visualize ROC curves and confusion matrices for deep insights.",
    detailedDescription: "Comprehensive model evaluation tools help you understand your model's performance from every angle. Access detailed metrics including accuracy, precision, recall, F1 scores, and more. Visualize performance through ROC curves, confusion matrices, and feature importance charts to make data-driven decisions about your models.",
    features: [
      "Comprehensive performance metrics (Accuracy, Precision, Recall, F1)",
      "ROC curve visualization",
      "Interactive confusion matrices",
      "Feature importance analysis",
      "Model comparison and benchmarking tools"
    ],
    href: "/models",
    cta: "Learn More",
    background: (
      <div className="absolute inset-0 bg-gradient-to-br from-primary/10 via-primary/5 to-secondary/5 dark:from-primary/20 dark:via-primary/10 dark:to-secondary/10" />
    ),
    className: "lg:col-start-3 lg:col-end-4 lg:row-start-2 lg:row-end-4",
  },
];

// Main Feature Section Component
export function FeaturesSection() {
  return (
    <div id="features" className="w-full px-6 py-16 md:px-10 md:py-24 scroll-mt-20 bg-gradient-to-b from-background via-background to-background/95">
      <motion.div
        className="mb-16 text-center"
        initial={{ opacity: 0, y: 20 }}
        whileInView={{ opacity: 1, y: 0 }}
        viewport={{ once: true }}
        transition={{ duration: 0.6 }}
      >
        <h2 className="text-4xl md:text-5xl font-bold tracking-tight bg-gradient-to-r from-primary via-secondary to-primary bg-clip-text text-transparent mb-4">
          Platform Features
        </h2>
        <p className="text-lg md:text-xl text-muted-foreground max-w-3xl mx-auto leading-relaxed">
          Build, train, and deploy machine learning models with complete transparency.
          <br className="hidden md:block" />
          <span className="block mt-2">Everything you need to manage your ML workflow from datasets to predictions.</span>
        </p>
      </motion.div>

      <div className="max-w-7xl mx-auto">
        <BentoGrid className="lg:grid-rows-3">
          {features.map((feature) => (
            <BentoCard key={feature.name} {...feature} />
          ))}
        </BentoGrid>
      </div>
    </div>
  );
}
