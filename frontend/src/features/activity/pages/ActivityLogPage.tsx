import { Download, Filter, Calendar, Lock, BrainCircuit, Database, Target, Key, AlertTriangle, Search, X, ChevronLeft, ChevronRight } from 'lucide-react';
import { Button } from '@/shared/components/ui/button';
import { Card } from '@/shared/components/ui/card';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/shared/components/ui/select';
import { Input } from '@/shared/components/ui/input';
import { Skeleton } from '@/shared/components/ui/skeleton';
import { Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle } from '@/shared/components/ui/dialog';
import { useQuery } from '@tanstack/react-query';
import { apiClient } from '@/shared/lib/api/client';
import { useState } from 'react';
import { motion, AnimatePresence } from 'framer-motion';

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

interface ActivityLogResponse {
  content: ActivityEvent[];
  totalElements: number;
  totalPages: number;
  page: number;
  size: number;
  hasNext: boolean;
  hasPrevious: boolean;
}

export function ActivityLogPage() {
  const [timeFilter, setTimeFilter] = useState('30');
  const [searchQuery, setSearchQuery] = useState('');
  const [eventTypeFilter, setEventTypeFilter] = useState<string>('all');
  const [showFilters, setShowFilters] = useState(false);
  const [currentPage, setCurrentPage] = useState(0);

  const { data: activityData, isLoading } = useQuery<ActivityLogResponse>({
    queryKey: ['activity-log', timeFilter, searchQuery, eventTypeFilter, currentPage],
    queryFn: async () => {
      const params: Record<string, string | number> = {
        page: currentPage,
        size: 50,
      };
      
      if (timeFilter !== 'all') {
        params.days = parseInt(timeFilter);
      }
      
      if (searchQuery) {
        params.search = searchQuery;
      }
      
      if (eventTypeFilter !== 'all') {
        params.eventType = eventTypeFilter;
      }
      
      const response = await apiClient.get<ActivityLogResponse>('/v1/activity', { params });
      return response;
    },
  });

  const activities = activityData?.content || [];

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
      <motion.div
        initial={{ opacity: 0, filter: 'blur(12px)', y: 12 }}
        animate={{ opacity: 1, filter: 'blur(0px)', y: 0 }}
        transition={{ type: 'spring', bounce: 0.3, duration: 1.5 }}
        className="space-y-4"
      >
        <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
          <div>
            <h1 className="text-2xl sm:text-3xl font-bold bg-gradient-to-r from-primary via-secondary to-primary bg-clip-text text-transparent">
              Activity Log
            </h1>
            <p className="text-muted-foreground mt-1 text-sm sm:text-base">Complete audit trail of all actions in your account</p>
          </div>
          <div className="flex flex-col sm:flex-row gap-2 sm:gap-3 w-full sm:w-auto">
            <Button
              variant="outline"
              className="w-full sm:w-auto hover:bg-primary/10 hover:border-primary/30 transition-colors"
              onClick={() => setShowFilters(true)}
            >
              <Filter className="w-4 h-4 mr-2" />
              Filter
            </Button>
            <Select value={timeFilter} onValueChange={setTimeFilter}>
              <SelectTrigger className="w-full sm:w-40 bg-input border-border">
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

        {/* Search Bar */}
        <div className="relative">
          <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 w-4 h-4 text-muted-foreground" />
          <Input
            type="text"
            placeholder="Search activities by details, IP address, or user agent..."
            value={searchQuery}
            onChange={(e) => {
              setSearchQuery(e.target.value);
              setCurrentPage(0); // Reset to first page on search
            }}
            className="pl-10 bg-input border-border focus:border-primary/30 transition-colors"
          />
          {searchQuery && (
            <Button
              variant="ghost"
              size="sm"
              className="absolute right-2 top-1/2 transform -translate-y-1/2 h-6 w-6 p-0"
              onClick={() => {
                setSearchQuery('');
                setCurrentPage(0);
              }}
            >
              <X className="w-4 h-4" />
            </Button>
          )}
        </div>
      </motion.div>

      {/* Filter Dialog */}
      <Dialog open={showFilters} onOpenChange={setShowFilters}>
        <DialogContent className="sm:max-w-md">
          <DialogHeader>
            <DialogTitle className="bg-gradient-to-r from-primary via-secondary to-primary bg-clip-text text-transparent">
              Filter Activities
            </DialogTitle>
            <DialogDescription>
              Filter activities by event type and other criteria
            </DialogDescription>
          </DialogHeader>
          <div className="space-y-4 py-4">
            <div className="space-y-2">
              <label className="text-sm font-medium">Event Type</label>
              <Select value={eventTypeFilter} onValueChange={(value) => {
                setEventTypeFilter(value);
                setCurrentPage(0);
              }}>
                <SelectTrigger className="bg-input border-border">
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="all">All Events</SelectItem>
                  <SelectItem value="LOGIN_SUCCESS">Login Success</SelectItem>
                  <SelectItem value="LOGIN_FAILED">Login Failed</SelectItem>
                  <SelectItem value="DATASET_UPLOADED">Dataset Uploaded</SelectItem>
                  <SelectItem value="DATASET_DELETED">Dataset Deleted</SelectItem>
                  <SelectItem value="MODEL_TRAINED">Model Trained</SelectItem>
                  <SelectItem value="MODEL_DELETED">Model Deleted</SelectItem>
                  <SelectItem value="PREDICTION_MADE">Prediction Made</SelectItem>
                  <SelectItem value="API_KEY_GENERATED">API Key Generated</SelectItem>
                  <SelectItem value="API_KEY_REVOKED">API Key Revoked</SelectItem>
                  <SelectItem value="PROFILE_UPDATED">Profile Updated</SelectItem>
                  <SelectItem value="PASSWORD_CHANGED">Password Changed</SelectItem>
                </SelectContent>
              </Select>
            </div>
            <div className="flex justify-end gap-2">
              <Button
                variant="outline"
                onClick={() => {
                  setEventTypeFilter('all');
                  setSearchQuery('');
                  setCurrentPage(0);
                }}
              >
                Clear Filters
              </Button>
              <Button
                onClick={() => setShowFilters(false)}
                className="bg-primary text-primary-foreground hover:bg-primary/90"
              >
                Apply
              </Button>
            </div>
          </div>
        </DialogContent>
      </Dialog>

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
        <motion.div
          initial={{ opacity: 0, filter: 'blur(12px)', y: 12 }}
          animate={{ opacity: 1, filter: 'blur(0px)', y: 0 }}
          transition={{ type: 'spring', bounce: 0.3, duration: 1.5 }}
        >
          <Card className="p-12 text-center border-primary/20">
            <p className="text-muted-foreground">No activity found</p>
            <p className="text-sm text-muted-foreground mt-2">
              {searchQuery || eventTypeFilter !== 'all' 
                ? 'Try adjusting your filters' 
                : 'Your activity will appear here as you use the platform'}
            </p>
          </Card>
        </motion.div>
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

      {/* Pagination */}
      {activityData && activityData.totalPages > 1 && (
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.2, type: 'spring', bounce: 0.3, duration: 1.5 }}
          className="flex items-center justify-between pt-4 border-t border-border"
        >
          <div className="text-sm text-muted-foreground">
            Showing {currentPage * activityData.size + 1} - {Math.min((currentPage + 1) * activityData.size, activityData.totalElements)} of {activityData.totalElements} activities
          </div>
          <div className="flex gap-2">
            <Button
              variant="outline"
              size="sm"
              onClick={() => setCurrentPage(p => Math.max(0, p - 1))}
              disabled={!activityData.hasPrevious}
              className="hover:bg-primary/10 hover:border-primary/30 transition-colors"
            >
              <ChevronLeft className="w-4 h-4 mr-1" />
              Previous
            </Button>
            <Button
              variant="outline"
              size="sm"
              onClick={() => setCurrentPage(p => p + 1)}
              disabled={!activityData.hasNext}
              className="hover:bg-primary/10 hover:border-primary/30 transition-colors"
            >
              Next
              <ChevronRight className="w-4 h-4 ml-1" />
            </Button>
          </div>
        </motion.div>
      )}

      {/* Active Filters Indicator */}
      {(searchQuery || eventTypeFilter !== 'all') && (
        <motion.div
          initial={{ opacity: 0, y: 10 }}
          animate={{ opacity: 1, y: 0 }}
          className="flex flex-wrap gap-2 items-center"
        >
          <span className="text-sm text-muted-foreground">Active filters:</span>
          {searchQuery && (
            <span className="px-2 py-1 bg-muted border border-border rounded-md text-sm flex items-center gap-1">
              Search: "{searchQuery}"
              <Button
                variant="ghost"
                size="sm"
                className="h-4 w-4 p-0"
                onClick={() => {
                  setSearchQuery('');
                  setCurrentPage(0);
                }}
              >
                <X className="w-3 h-3" />
              </Button>
            </span>
          )}
          {eventTypeFilter !== 'all' && (
            <span className="px-2 py-1 bg-muted border border-border rounded-md text-sm flex items-center gap-1">
              {formatEventType(eventTypeFilter)}
              <Button
                variant="ghost"
                size="sm"
                className="h-4 w-4 p-0"
                onClick={() => {
                  setEventTypeFilter('all');
                  setCurrentPage(0);
                }}
              >
                <X className="w-3 h-3" />
              </Button>
            </span>
          )}
        </motion.div>
      )}
    </div>
  );
}
