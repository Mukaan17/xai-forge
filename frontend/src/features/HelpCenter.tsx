import { useState } from 'react';
import { Search, BookOpen, Database, BrainCircuit, Lightbulb, Code, AlertCircle, ChevronRight, ExternalLink, MessageCircle, Bug, Sparkles } from 'lucide-react';
import { Input } from './ui/input';
import { Button } from './ui/button';
import { Card } from './ui/card';

export function HelpCenter() {
  const [selectedTopic, setSelectedTopic] = useState('quick-start');
  const [searchQuery, setSearchQuery] = useState('');

  const tableOfContents = [
    {
      category: 'Getting Started',
      icon: BookOpen,
      items: [
        { id: 'quick-start', label: 'Quick Start Guide' },
        { id: 'first-model', label: 'Your First Model' },
        { id: 'understanding-predictions', label: 'Understanding Predictions' },
      ],
    },
    {
      category: 'Datasets',
      icon: Database,
      items: [
        { id: 'supported-formats', label: 'Supported Formats' },
        { id: 'upload-guidelines', label: 'Upload Guidelines' },
        { id: 'data-requirements', label: 'Data Requirements' },
      ],
    },
    {
      category: 'Models',
      icon: BrainCircuit,
      items: [
        { id: 'training-model', label: 'Training a Model' },
        { id: 'algorithm-selection', label: 'Algorithm Selection' },
        { id: 'performance-metrics', label: 'Performance Metrics' },
      ],
    },
    {
      category: 'Explanations',
      icon: Lightbulb,
      items: [
        { id: 'understanding-lime', label: 'Understanding LIME' },
        { id: 'feature-importance', label: 'Feature Importance' },
        { id: 'reading-results', label: 'Reading Results' },
      ],
    },
    {
      category: 'API Reference',
      icon: Code,
      items: [
        { id: 'authentication', label: 'Authentication' },
        { id: 'endpoints', label: 'Endpoints' },
        { id: 'code-examples', label: 'Code Examples' },
      ],
    },
    {
      category: 'Troubleshooting',
      icon: AlertCircle,
      items: [
        { id: 'common-errors', label: 'Common Errors' },
        { id: 'faqs', label: 'FAQs' },
      ],
    },
  ];

  const content: Record<string, { title: string; sections: Array<{ heading?: string; text?: string; code?: string; tip?: string; image?: boolean; video?: boolean }> }> = {
    'quick-start': {
      title: 'Quick Start Guide',
      sections: [
        { text: 'Get up and running with XAI-Forge in 5 minutes.' },
        { heading: 'STEP 1: UPLOAD YOUR DATA' },
        { text: 'Navigate to Datasets → Upload and drag your CSV file into the upload area. XAI-Forge supports CSV, Excel, and JSON formats.' },
        { image: true },
        { tip: 'Ensure your dataset has at least 100 rows for reliable model training.' },
        { heading: 'STEP 2: TRAIN A MODEL' },
        { text: 'Select your dataset and choose a target variable (the column you want to predict). XAI-Forge will automatically detect whether this is a classification or regression problem.' },
        { text: 'Choose an algorithm from the dropdown. For beginners, we recommend starting with Logistic Regression for classification or Linear Regression for regression tasks.' },
        { video: true },
        { tip: 'For best results, ensure your dataset has at least 100 rows and no more than 30% missing values.' },
        { heading: 'STEP 3: MAKE PREDICTIONS' },
        { text: 'Once your model is trained, navigate to Predictions → Make Prediction. Enter values for each feature and click "Generate Prediction" to see the result along with a detailed explanation of why the model made that prediction.' },
        { heading: 'STEP 4: UNDERSTAND THE EXPLANATION' },
        { text: 'XAI-Forge uses LIME (Local Interpretable Model-agnostic Explanations) to show you which features contributed most to the prediction. Green bars indicate factors that increase the prediction, while red bars indicate factors that decrease it.' },
      ],
    },
    'first-model': {
      title: 'Your First Model',
      sections: [
        { text: 'This tutorial will walk you through creating your first machine learning model from start to finish.' },
        { heading: 'Prerequisites' },
        { text: '• A dataset with at least 100 rows\n• A clear target variable (what you want to predict)\n• Basic understanding of your data' },
        { heading: 'Step-by-Step Process' },
        { text: '1. Upload your dataset\n2. Review the data preview\n3. Select your target variable\n4. Choose an algorithm\n5. Configure training parameters\n6. Train the model\n7. Review performance metrics\n8. Make predictions' },
        { tip: 'Start with default parameters. You can always retrain with different settings later.' },
      ],
    },
    'supported-formats': {
      title: 'Supported Formats',
      sections: [
        { text: 'XAI-Forge supports the following file formats for dataset upload:' },
        { heading: 'CSV (Comma-Separated Values)' },
        { text: 'The most commonly used format. Ensure your CSV file has headers in the first row.' },
        { code: 'age,income,has_account,will_churn\n25,50000,yes,no\n45,75000,yes,no\n32,45000,no,yes' },
        { heading: 'Excel (.xlsx, .xls)' },
        { text: 'Excel spreadsheets are supported. Only the first sheet will be imported.' },
        { heading: 'JSON' },
        { text: 'JSON files with array of objects format are supported.' },
        { code: '[\n  {"age": 25, "income": 50000, "has_account": "yes", "will_churn": "no"},\n  {"age": 45, "income": 75000, "has_account": "yes", "will_churn": "no"}\n]' },
        { tip: 'For large datasets (>10MB), we recommend using CSV format for faster upload and processing.' },
      ],
    },
    'authentication': {
      title: 'Authentication',
      sections: [
        { text: 'All API requests require authentication using an API key.' },
        { heading: 'Generating an API Key' },
        { text: 'Navigate to Settings → API & Integrations and click "Generate New API Key". Choose appropriate permissions for your use case.' },
        { heading: 'Using Your API Key' },
        { text: 'Include your API key in the Authorization header of all requests:' },
        { code: 'curl -H "Authorization: Bearer YOUR_API_KEY" \\\n  https://api.xai-forge.com/v1/predictions' },
        { heading: 'Security Best Practices' },
        { text: '• Never commit API keys to version control\n• Use environment variables to store keys\n• Rotate keys regularly\n• Use different keys for development and production\n• Revoke unused keys immediately' },
        { tip: 'Production keys have higher rate limits but should be kept secure.' },
      ],
    },
    'common-errors': {
      title: 'Common Errors',
      sections: [
        { heading: 'InsufficientDataException' },
        { text: 'This error occurs when your dataset has fewer than 100 rows. Solution: Add more data or combine multiple datasets.' },
        { heading: 'MissingValuesException' },
        { text: 'More than 30% of your data is missing. Solution: Clean your data before upload or use imputation techniques.' },
        { heading: 'InvalidTargetException' },
        { text: 'The target column contains invalid values. Solution: Ensure your target column has consistent data types.' },
        { heading: 'ModelConvergenceException' },
        { text: 'The model failed to converge during training. Solution: Try normalizing your features or choosing a different algorithm.' },
        { tip: 'Most errors can be resolved by reviewing your data quality. Use the data preview feature to spot issues early.' },
      ],
    },
    'faqs': {
      title: 'Frequently Asked Questions',
      sections: [
        { heading: 'How much data do I need?' },
        { text: 'Minimum 100 rows, but we recommend at least 1,000 rows for reliable models. More data generally leads to better performance.' },
        { heading: 'Which algorithm should I choose?' },
        { text: 'For classification: Start with Logistic Regression. For regression: Start with Linear Regression. Both are fast and interpretable.' },
        { heading: 'How long does training take?' },
        { text: 'Most models train in under 30 seconds. Large datasets (>100K rows) may take 1-2 minutes.' },
        { heading: 'Can I export my trained models?' },
        { text: 'Yes! Navigate to Settings → Data Management → Export Your Data to download your models.' },
        { heading: 'Is my data secure?' },
        { text: 'All data is encrypted in transit and at rest. We never share your data with third parties. See our Privacy Policy for details.' },
      ],
    },
  };

  const currentContent = content[selectedTopic] || content['quick-start'];

  return (
    <div className="p-8 flex gap-8">
      {/* Left Sidebar - Table of Contents */}
      <div className="w-64 flex-shrink-0 space-y-6">
        <div>
          <h2 className="mb-4">Help Center</h2>
          <div className="relative">
            <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-muted-foreground" />
            <Input
              placeholder="Search documentation..."
              className="pl-10"
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
            />
          </div>
        </div>

        <nav className="space-y-1">
          {tableOfContents.map((section) => (
            <div key={section.category}>
              <div className="flex items-center gap-2 px-3 py-2 text-sm font-medium text-muted-foreground">
                <section.icon className="w-4 h-4" />
                {section.category}
              </div>
              {section.items.map((item) => (
                <button
                  key={item.id}
                  onClick={() => setSelectedTopic(item.id)}
                  className={`w-full text-left px-3 py-2 pl-9 text-sm rounded-lg transition-colors flex items-center justify-between group ${
                    selectedTopic === item.id
                      ? 'bg-primary/10 text-primary font-medium'
                      : 'text-muted-foreground hover:bg-muted/50 hover:text-foreground'
                  }`}
                >
                  {item.label}
                  {selectedTopic === item.id && (
                    <ChevronRight className="w-4 h-4" />
                  )}
                </button>
              ))}
            </div>
          ))}
        </nav>
      </div>

      {/* Right Content Area */}
      <div className="flex-1 max-w-4xl">
        <Card className="p-8">
          <h1 className="mb-6">{currentContent.title}</h1>
          
          <div className="prose prose-invert max-w-none space-y-6">
            {currentContent.sections.map((section, i) => (
              <div key={i}>
                {section.heading && (
                  <h3 className="text-xl font-semibold mb-3 mt-8 first:mt-0">{section.heading}</h3>
                )}
                {section.text && (
                  <p className="text-muted-foreground whitespace-pre-line leading-relaxed">
                    {section.text}
                  </p>
                )}
                {section.code && (
                  <pre className="bg-muted/30 border border-border rounded-lg p-4 overflow-x-auto">
                    <code className="text-sm font-mono text-primary">{section.code}</code>
                  </pre>
                )}
                {section.tip && (
                  <div className="flex gap-3 p-4 bg-primary/5 border border-primary/20 rounded-lg">
                    <Lightbulb className="w-5 h-5 text-primary flex-shrink-0 mt-0.5" />
                    <div>
                      <p className="font-medium text-primary mb-1">💡 TIP</p>
                      <p className="text-sm text-muted-foreground">{section.tip}</p>
                    </div>
                  </div>
                )}
                {section.image && (
                  <div className="bg-muted/20 border border-border rounded-lg p-8 flex items-center justify-center">
                    <p className="text-muted-foreground">[Screenshot placeholder]</p>
                  </div>
                )}
                {section.video && (
                  <div className="bg-muted/20 border border-border rounded-lg p-12 flex flex-col items-center justify-center">
                    <div className="w-16 h-16 rounded-full bg-primary/10 flex items-center justify-center mb-3">
                      <div className="w-0 h-0 border-l-8 border-l-primary border-y-6 border-y-transparent ml-1"></div>
                    </div>
                    <p className="text-muted-foreground">[Video tutorial embed placeholder]</p>
                  </div>
                )}
              </div>
            ))}
          </div>
        </Card>

        {/* Help Widget CTA */}
        <Card className="p-6 mt-6 bg-gradient-to-br from-primary/5 to-secondary/5 border-primary/20">
          <h3 className="mb-4">Still need help?</h3>
          <div className="grid grid-cols-3 gap-4">
            <Button variant="outline" className="h-auto flex-col gap-2 py-4">
              <MessageCircle className="w-6 h-6 text-primary" />
              <span className="text-sm">Contact Support</span>
            </Button>
            <Button variant="outline" className="h-auto flex-col gap-2 py-4">
              <Bug className="w-6 h-6 text-error" />
              <span className="text-sm">Report a Bug</span>
            </Button>
            <Button variant="outline" className="h-auto flex-col gap-2 py-4">
              <Sparkles className="w-6 h-6 text-secondary" />
              <span className="text-sm">Feature Request</span>
            </Button>
          </div>
        </Card>
      </div>
    </div>
  );
}
