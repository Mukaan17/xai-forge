import { Link, useLocation } from 'react-router-dom';
import { ChevronRight, Home } from 'lucide-react';
import { cn } from '@/lib/utils';

interface BreadcrumbItem {
  label: string;
  path: string;
}

export function Breadcrumbs() {
  const location = useLocation();
  const pathnames = location.pathname.split('/').filter((x) => x);

  // Build breadcrumb items
  const breadcrumbs: BreadcrumbItem[] = [
    { label: 'Dashboard', path: '/dashboard' },
  ];

  // Add path segments
  let currentPath = '';
  pathnames.forEach((segment, index) => {
    currentPath += `/${segment}`;
    
    // Skip dashboard since it's already added
    if (segment === 'dashboard') {
      return;
    }

    // Generate label from segment
    let label = segment
      .split('-')
      .map((word) => word.charAt(0).toUpperCase() + word.slice(1))
      .join(' ');

    // Special cases for better labels
    const labelMap: Record<string, string> = {
      'models': 'Models',
      'datasets': 'Datasets',
      'predictions': 'Predictions',
      'history': 'History',
      'activity': 'Activity Log',
      'settings': 'Settings',
      'train': 'Train Model',
    };

    if (labelMap[segment]) {
      label = labelMap[segment];
    }

    // For dynamic routes (like /models/:id), try to get the actual name
    // This would require fetching the resource, but for now we'll use the ID
    if (!isNaN(Number(segment)) && index > 0) {
      const parentSegment = pathnames[index - 1];
      if (parentSegment === 'models') {
        label = `Model ${segment}`;
      } else if (parentSegment === 'datasets') {
        label = `Dataset ${segment}`;
      } else {
        label = segment;
      }
    }

    breadcrumbs.push({ label, path: currentPath });
  });

  // Don't show breadcrumbs on dashboard (only one level)
  if (breadcrumbs.length <= 1) {
    return null;
  }

  return (
    <nav className="flex items-center gap-2 text-sm text-muted-foreground mb-4" aria-label="Breadcrumb">
      <Link
        to="/dashboard"
        className="hover:text-foreground transition-colors flex items-center gap-1"
      >
        <Home className="w-4 h-4" />
        <span className="sr-only">Home</span>
      </Link>
      {breadcrumbs.map((crumb, index) => {
        const isLast = index === breadcrumbs.length - 1;
        return (
          <div key={crumb.path} className="flex items-center gap-2">
            <ChevronRight className="w-4 h-4" />
            {isLast ? (
              <span className="text-foreground font-medium">{crumb.label}</span>
            ) : (
              <Link
                to={crumb.path}
                className="hover:text-foreground transition-colors"
              >
                {crumb.label}
              </Link>
            )}
          </div>
        );
      })}
    </nav>
  );
}
