import { Download, Filter, Calendar, Lock, BrainCircuit, Database, Target, Key, AlertTriangle } from 'lucide-react';
import { Button } from '@/shared/components/ui/button';
import { Card } from '@/shared/components/ui/card';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/shared/components/ui/select';
import { Skeleton } from '@/shared/components/ui/skeleton';
import { useQuery } from '@tanstack/react-query';
import { apiClient } from '@/shared/lib/api/client';
import { useState } from 'react';

interface ActivityEvent {
  id: number;
  eventType: string;
  details: string;
  ipAddress?: string;
  userAgent?: string;
  timestamp: string;
}

interface ActivityDay {
  date: string;
  events: ActivityEvent[];
}

const getEventIcon = (eventType: string) => {
  if (eventType.includes('LOGIN')) return Lock;
  if (eventType.includes('PREDICTION')) return Target;
  if (eventType.includes('MODEL')) return BrainCircuit;
  if (eventType.includes('DATASET')) return Database;
  if (eventType.includes('API_KEY')) return Key;
  return AlertTriangle;
};

const getEventColor = (eventType: string) => {
  if (eventType.includes('LOGIN_SUCCESS') || eventType.includes('PREDICTION') || eventType.includes('MODEL_TRAINED')) {
    return 'text-[#22c55e]'; // success color
  }
  if (eventType.includes('LOGIN_FAILED')) {
    return 'text-destructive';
  }
  if (eventType.includes('API_KEY')) {
    return 'text-[#facc15]'; // warning color
  }
  return 'text-primary';
};

const formatEventType = (eventType: string) => {
  return eventType
    .split('_')
    .map(word => word.charAt(0) + word.slice(1).toLowerCase())
    .join(' ');
};

const formatEventDetails = (event: ActivityEvent) => {
  const details: string[] = [];
  if (event.details) {
    try {
      const parsed = JSON.parse(event.details);
      Object.entries(parsed).forEach(([key, value]) => {
        details.push(`${key}: ${value}`);
      });
    } catch {
      details.push(event.details);
    }
  }
  if (event.ipAddress) {
    details.push(`IP: ${event.ipAddress}`);
  }
  if (event.userAgent) {
    details.push(`Browser: ${event.userAgent}`);
  }
  return details.join('\n');
};

export function ActivityLogPage() {
  const [timeFilter, setTimeFilter] = useState('30');

  const { data: activities = [], isLoading } = useQuery<ActivityEvent[]>({
    queryKey: ['activity-log', timeFilter],
    queryFn: async () => {
      const days = timeFilter === 'all' ? undefined : parseInt(timeFilter);
      const response = await apiClient.get<ActivityEvent[]>('/v1/activity', {
        params: days ? { days } : {}
      });
      return response;
    },
  });

  // Group activities by date
  const groupedActivities: ActivityDay[] = activities.reduce((acc, event) => {
    const date = new Date(event.timestamp).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'long',
      day: 'numeric'
    });
    
    const existingDay = acc.find(day => day.date === date);
    if (existingDay) {
      existingDay.events.push(event);
    } else {
      acc.push({ date, events: [event] });
    }
    return acc;
  }, [] as ActivityDay[]);

  // Sort events within each day by timestamp (newest first)
  groupedActivities.forEach(day => {
    day.events.sort((a, b) => new Date(b.timestamp).getTime() - new Date(a.timestamp).getTime());
  });

  // Sort days by date (newest first)
  groupedActivities.sort((a, b) => new Date(b.date).getTime() - new Date(a.date).getTime());

  const formatTime = (timestamp: string) => {
    const date = new Date(timestamp);
    return date.toLocaleTimeString('en-US', { hour: 'numeric', minute: '2-digit' });
  };

  return (
    <div className="p-4 sm:p-6 lg:p-8 space-y-4 sm:space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl sm:text-3xl font-bold">Activity Log</h1>
          <p className="text-muted-foreground mt-1 text-sm sm:text-base">Complete audit trail of all actions in your account</p>
        </div>
        <div className="flex flex-col sm:flex-row gap-2 sm:gap-3 w-full sm:w-auto">
          <Button variant="outline" className="w-full sm:w-auto">
            <Filter className="w-4 h-4 mr-2" />
            Filter
          </Button>
          <Select value={timeFilter} onValueChange={setTimeFilter}>
            <SelectTrigger className="w-full sm:w-40">
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="7">Last 7 days</SelectItem>
              <SelectItem value="30">Last 30 days</SelectItem>
              <SelectItem value="90">Last 90 days</SelectItem>
              <SelectItem value="all">All time</SelectItem>
            </SelectContent>
          </Select>
          <Button className="bg-primary text-primary-foreground hover:bg-primary/90 w-full sm:w-auto">
            <Download className="w-4 h-4 mr-2" />
            Export
          </Button>
        </div>
      </div>

      {/* Activity Timeline */}
      {isLoading ? (
        <div className="space-y-8">
          {Array.from({ length: 3 }).map((_, dayIndex) => (
            <div key={dayIndex}>
              <div className="flex items-center gap-3 mb-6">
                <Calendar className="w-5 h-5 text-muted-foreground" />
                <Skeleton className="h-6 w-40" />
                <div className="flex-1 h-px bg-border"></div>
              </div>
              <div className="space-y-4 ml-8">
                {Array.from({ length: 3 }).map((_, eventIndex) => (
                  <Card key={eventIndex} className="p-4">
                    <div className="flex gap-4">
                      <div className="flex flex-col items-center">
                        <Skeleton className="w-10 h-10 rounded-lg" />
                        {eventIndex < 2 && (
                          <div className="w-px h-full bg-border mt-2"></div>
                        )}
                      </div>
                      <div className="flex-1 space-y-2">
                        <div className="flex items-center justify-between">
                          <Skeleton className="h-5 w-32" />
                          <Skeleton className="h-4 w-16" />
                        </div>
                        <Skeleton className="h-4 w-full" />
                        <Skeleton className="h-4 w-3/4" />
                      </div>
                    </div>
                  </Card>
                ))}
              </div>
            </div>
          ))}
        </div>
      ) : groupedActivities.length === 0 ? (
        <Card className="p-12 text-center">
          <p className="text-muted-foreground">No activity found</p>
          <p className="text-sm text-muted-foreground mt-2">Your activity will appear here as you use the platform</p>
        </Card>
      ) : (
        <div className="space-y-8">
          {groupedActivities.map((day, dayIndex) => (
            <div key={dayIndex}>
              <div className="flex items-center gap-3 mb-6">
                <Calendar className="w-5 h-5 text-muted-foreground" />
                <h3 className="text-xl font-semibold">{day.date}</h3>
                <div className="flex-1 h-px bg-border"></div>
              </div>

              <div className="space-y-4 ml-8">
                {day.events.map((event, eventIndex) => {
                  const Icon = getEventIcon(event.eventType);
                  const color = getEventColor(event.eventType);
                  return (
                    <Card key={event.id} className="p-4 hover:border-primary/30 transition-colors">
                      <div className="flex gap-4">
                        <div className="flex flex-col items-center">
                          <div className={`w-10 h-10 rounded-lg bg-gradient-to-br ${color} from-current/10 to-current/5 flex items-center justify-center flex-shrink-0`}>
                            <Icon className={`w-5 h-5 ${color}`} />
                          </div>
                          {eventIndex < day.events.length - 1 && (
                            <div className="w-px h-full bg-border mt-2"></div>
                          )}
                        </div>
                        <div className="flex-1">
                          <div className="flex items-center justify-between mb-1">
                            <p className="font-medium">{formatEventType(event.eventType)}</p>
                            <span className="text-sm text-muted-foreground">{formatTime(event.timestamp)}</span>
                          </div>
                          <pre className="text-sm text-muted-foreground whitespace-pre-line font-sans">
                            {formatEventDetails(event)}
                          </pre>
                        </div>
                      </div>
                    </Card>
                  );
                })}
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
