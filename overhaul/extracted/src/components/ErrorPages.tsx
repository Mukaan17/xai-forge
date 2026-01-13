import { Home, Mail, RefreshCw, FileQuestion, AlertTriangle } from 'lucide-react';
import { Button } from './ui/button';
import { Card } from './ui/card';

interface ErrorPagesProps {
  type: '404' | '500' | 'training-error';
  onNavigate?: (page: string) => void;
}

export function ErrorPages({ type, onNavigate }: ErrorPagesProps) {
  if (type === '404') {
    return (
      <div className="min-h-[80vh] flex items-center justify-center p-8">
        <div className="text-center max-w-lg">
          <div className="text-8xl mb-6">¯\_(ツ)_/¯</div>
          <h1 className="mb-4">404 — Page Not Found</h1>
          <p className="text-muted-foreground mb-8">
            The page you&apos;re looking for doesn&apos;t exist or has been moved to a new location.
          </p>
          <div className="flex items-center justify-center gap-4">
            <Button
              onClick={() => onNavigate?.('dashboard')}
              className="bg-primary text-primary-foreground hover:bg-primary/90"
            >
              <Home className="w-4 h-4 mr-2" />
              Back to Dashboard
            </Button>
            <Button variant="outline">
              <Mail className="w-4 h-4 mr-2" />
              Contact Support
            </Button>
          </div>
        </div>
      </div>
    );
  }

  if (type === '500') {
    return (
      <div className="min-h-[80vh] flex items-center justify-center p-8">
        <div className="text-center max-w-lg">
          <div className="w-24 h-24 rounded-full bg-error/10 flex items-center justify-center mx-auto mb-6">
            <AlertTriangle className="w-12 h-12 text-error" />
          </div>
          <h1 className="mb-4">Something went wrong</h1>
          <p className="text-muted-foreground mb-4">
            Our servers encountered an unexpected error. We&apos;ve been notified and are working on it.
          </p>
          <p className="text-sm text-muted-foreground mb-8 font-mono">
            Error ID: ERR-2024-1209-8F3A
          </p>
          <div className="flex items-center justify-center gap-4">
            <Button
              onClick={() => window.location.reload()}
              className="bg-primary text-primary-foreground hover:bg-primary/90"
            >
              <RefreshCw className="w-4 h-4 mr-2" />
              Try Again
            </Button>
            <Button variant="outline">
              <Mail className="w-4 h-4 mr-2" />
              Report Issue
            </Button>
          </div>
        </div>
      </div>
    );
  }

  if (type === 'training-error') {
    return (
      <Card className="p-6 border-error/30 bg-error/5 max-w-2xl mx-auto">
        <div className="flex items-start gap-4">
          <div className="w-12 h-12 rounded-lg bg-error/10 flex items-center justify-center flex-shrink-0">
            <AlertTriangle className="w-6 h-6 text-error" />
          </div>
          <div className="flex-1">
            <h3 className="text-error mb-3">TRAINING FAILED</h3>
            <p className="mb-4">Model "Revenue Predictor v2" failed to train.</p>
            
            <div className="p-4 bg-background rounded-lg mb-4">
              <p className="font-medium mb-2">ERROR: InsufficientDataException</p>
              <p className="text-sm text-muted-foreground">
                The dataset contains only 45 rows. Minimum required: 100 rows.
              </p>
            </div>

            <div className="mb-6">
              <p className="font-medium mb-2">SUGGESTIONS:</p>
              <ul className="space-y-1 text-sm text-muted-foreground">
                <li>• Upload a larger dataset with more samples</li>
                <li>• Combine multiple datasets if possible</li>
                <li>• Reduce the number of features selected</li>
              </ul>
            </div>

            <div className="flex gap-3">
              <Button variant="outline" size="sm">
                <FileQuestion className="w-4 h-4 mr-2" />
                View Error Logs
              </Button>
              <Button variant="outline" size="sm" onClick={() => onNavigate?.('datasets-upload')}>
                Upload New Dataset
              </Button>
              <Button size="sm" className="bg-primary text-primary-foreground hover:bg-primary/90">
                <RefreshCw className="w-4 h-4 mr-2" />
                Retry Training
              </Button>
            </div>
          </div>
        </div>
      </Card>
    );
  }

  return null;
}
