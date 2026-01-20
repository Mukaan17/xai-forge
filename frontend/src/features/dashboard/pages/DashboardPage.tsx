import { useQuery } from '@tanstack/react-query';
import { useState, useEffect } from 'react';
import { apiClient } from '@/shared/lib/api/client';
import { Card, CardContent, CardHeader, CardTitle } from '@/shared/components/ui/card';
import { Button } from '@/shared/components/ui/button';
import { Upload, BrainCircuit, Target, TrendingUp, Database } from 'lucide-react';
import { Link } from 'react-router-dom';
import { Skeleton } from '@/shared/components/ui/skeleton';
import { OnboardingFlow } from '@/features/OnboardingFlow';
import { WeeklyUsageChart } from '../components/WeeklyUsageChart';
import { ModelTypeChart } from '../components/ModelTypeChart';
import { DatasetSizesChart } from '../components/DatasetSizesChart';
import { ActivityAreaChart } from '../components/ActivityAreaChart';
import { GradientText } from '@/shared/components/ui/gradient-text';
import { motion } from 'framer-motion';

interface DashboardStats {
  totalDatasets: number;
  totalModels: number;
  totalPredictions: number;
  averageAccuracy: number;
  recentActivity: Array<{
    type: string;
    description: string;
    timestamp: string;
  }>;
  modelsByType: Record<string, number>;
  weeklyUsage: Array<{
    day: string;
    predictions: number;
  }>;
  datasetSizes: Record<string, number>;
}

export function DashboardPage() {
  const [showOnboarding, setShowOnboarding] = useState(false);

  // Check if onboarding should be shown on mount
  // Only show if:
  // 1. User hasn't permanently opted out (localStorage)
  // 2. User hasn't seen it in this session (sessionStorage)
  useEffect(() => {
    const onboardingCompleted = localStorage.getItem('xai-forge-onboarding-completed');
    const onboardingShownThisSession = sessionStorage.getItem('xai-forge-onboarding-shown');
    
    // Only show if user hasn't permanently opted out AND hasn't seen it this session
    if (!onboardingCompleted && !onboardingShownThisSession) {
      setShowOnboarding(true);
    }
  }, []);

  const { data: stats, isLoading } = useQuery<DashboardStats>({
    queryKey: ['dashboard', 'stats'],
    queryFn: async () => {
      const response = await apiClient.get<DashboardStats>('/v1/dashboard/stats');
      return response;
    },
  });

  if (isLoading) {
    return (
      <div className="p-8 space-y-8">
        <Skeleton className="h-32 w-full" />
        <div className="grid grid-cols-4 gap-6">
          {[1, 2, 3, 4].map((i) => (
            <Skeleton key={i} className="h-32" />
          ))}
        </div>
      </div>
    );
  }

  if (!stats) {
    return <div className="p-8">Failed to load dashboard stats</div>;
  }

  const kpiData = [
    {
      label: 'Total Datasets',
      value: stats.totalDatasets.toString(),
      icon: Database,
      color: 'text-primary',
    },
    {
      label: 'Trained Models',
      value: stats.totalModels.toString(),
      icon: BrainCircuit,
      color: 'text-secondary',
    },
    {
      label: 'Predictions Made',
      value: stats.totalPredictions.toString(),
      icon: Target,
      color: 'text-success',
    },
    {
      label: 'Avg. Model Accuracy',
      value: `${(stats.averageAccuracy * 100).toFixed(1)}%`,
      icon: TrendingUp,
      color: 'text-warning',
    },
  ];

  return (
    <>
      {showOnboarding && (
        <OnboardingFlow
          onComplete={() => setShowOnboarding(false)}
          onSkip={() => setShowOnboarding(false)}
        />
      )}
      <motion.div 
        className="space-y-6 sm:space-y-8"
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ type: "spring", bounce: 0.3, duration: 1.5 }}
      >
      {/* Welcome Banner */}
      <Card className="p-4 sm:p-6 bg-gradient-to-r from-primary/10 via-secondary/10 to-transparent border-primary/20">
        <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
          <div>
            <h1 className="text-xl sm:text-2xl font-semibold mb-1">
              Welcome back
            </h1>
            <p className="text-muted-foreground text-sm sm:text-base">Here's your ML workspace overview</p>
          </div>
          <div className="flex flex-col sm:flex-row gap-2 sm:gap-3 w-full sm:w-auto">
            <Button asChild className="bg-primary text-primary-foreground hover:bg-primary/90 w-full sm:w-auto">
              <Link to="/datasets">
                <Upload className="w-4 h-4 mr-2" />
                Upload Dataset
              </Link>
            </Button>
            <Button asChild variant="outline" className="border-primary/30 hover:bg-primary/10 w-full sm:w-auto">
              <Link to="/models/train">
                <BrainCircuit className="w-4 h-4 mr-2" />
                Train Model
              </Link>
            </Button>
          </div>
        </div>
      </Card>

      {/* KPI Metric Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 sm:gap-6">
        {kpiData.map((kpi, index) => {
          const Icon = kpi.icon;
          return (
            <motion.div
              key={index}
              initial={{ opacity: 0, y: 20, filter: "blur(12px)" }}
              animate={{ opacity: 1, y: 0, filter: "blur(0px)" }}
              transition={{
                type: "spring",
                bounce: 0.3,
                duration: 1.5,
                delay: index * 0.1,
              }}
            >
              <Card className="p-6 hover:border-primary/30 transition-all duration-300">
                <div className="flex items-start justify-between mb-4">
                  <div className={`w-12 h-12 rounded-full bg-gradient-to-br ${kpi.color} from-current/10 to-current/5 flex items-center justify-center transition-transform duration-300 hover:scale-110`}>
                    <Icon className={`w-6 h-6 ${kpi.color}`} />
                  </div>
                </div>
                <div className="space-y-1">
                  <p className="text-muted-foreground">{kpi.label}</p>
                  <p className="text-3xl font-semibold tracking-tight">
                    <GradientText variant="primary">{kpi.value}</GradientText>
                  </p>
                </div>
              </Card>
            </motion.div>
          );
        })}
      </div>

      {/* Charts Section */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <WeeklyUsageChart data={stats.weeklyUsage} />
        <ModelTypeChart data={stats.modelsByType} />
      </div>

      {/* Additional Charts */}
      {Object.keys(stats.datasetSizes).length > 0 && (
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
          <DatasetSizesChart data={stats.datasetSizes} />
          <ActivityAreaChart data={stats.weeklyUsage} />
        </div>
      )}

      {/* Recent Activity */}
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ type: "spring", bounce: 0.3, duration: 1.5, delay: 0.4 }}
      >
        <Card>
          <CardHeader>
            <CardTitle>
              <GradientText variant="primary">Recent Activity</GradientText>
            </CardTitle>
          </CardHeader>
        <CardContent>
          <div className="space-y-4">
            {stats.recentActivity.length > 0 ? (
              stats.recentActivity.map((activity, index) => (
                <div key={index} className="flex items-center gap-4 p-3 rounded-lg hover:bg-muted/30">
                  <div className="w-2 h-2 rounded-full bg-primary" />
                  <div className="flex-1">
                    <p className="font-medium">{activity.type}</p>
                    <p className="text-sm text-muted-foreground">{activity.description}</p>
                  </div>
                  <p className="text-sm text-tertiary">{activity.timestamp}</p>
                </div>
              ))
            ) : (
              <p className="text-muted-foreground text-center py-8">No recent activity</p>
            )}
          </div>
        </CardContent>
      </Card>
      </motion.div>
    </motion.div>
    </>
  );
}

