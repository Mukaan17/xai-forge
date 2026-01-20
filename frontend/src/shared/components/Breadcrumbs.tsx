import * as React from 'react';
import { Link, useLocation } from 'react-router-dom';
import { Home } from 'lucide-react';
import {
  Breadcrumb,
  BreadcrumbItem,
  BreadcrumbLink,
  BreadcrumbList,
  BreadcrumbPage,
  BreadcrumbSeparator,
} from '@/shared/components/ui/breadcrumb';

interface BreadcrumbItemData {
  label: string;
  path: string;
  icon?: React.ComponentType<{ className?: string }>;
}

export function Breadcrumbs() {
  const location = useLocation();
  const pathnames = location.pathname.split('/').filter((x) => x);

  // Build breadcrumb items
  const breadcrumbs: BreadcrumbItemData[] = [
    { label: 'Dashboard', path: '/dashboard', icon: Home },
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
    <Breadcrumb>
      <BreadcrumbList>
        {breadcrumbs.map((crumb, index) => {
          const isLast = index === breadcrumbs.length - 1;
          const Icon = crumb.icon;

          return (
            <React.Fragment key={crumb.path}>
              <BreadcrumbItem>
                {isLast ? (
                  <BreadcrumbPage>{crumb.label}</BreadcrumbPage>
                ) : (
                  <BreadcrumbLink asChild>
                    <Link to={crumb.path} className="inline-flex items-center gap-1.5">
                      {Icon && <Icon className="w-4 h-4" aria-hidden="true" />}
                      {crumb.label}
                    </Link>
                  </BreadcrumbLink>
                )}
              </BreadcrumbItem>
              {!isLast && <BreadcrumbSeparator />}
            </React.Fragment>
          );
        })}
      </BreadcrumbList>
    </Breadcrumb>
  );
}
