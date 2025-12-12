import { BrainCircuit, Database, Target, AlertCircle, CheckCircle2, X } from 'lucide-react';
import { Button } from './ui/button';
import { Badge } from './ui/badge';
import { Tabs, TabsList, TabsTrigger } from './ui/tabs';

interface NotificationCenterProps {
  onClose?: () => void;
  onNavigate: (page: string) => void;
}

export function NotificationCenter({ onClose, onNavigate }: NotificationCenterProps) {
  const notifications = [
    {
      id: 1,
      icon: BrainCircuit,
      color: 'text-secondary',
      title: 'Model Training Complete',
      message: '"Churn Predictor v3" finished training',
      detail: 'Accuracy: 89.2%',
      time: '2 hours ago',
      unread: true,
      actions: [
        { label: 'View Model', onClick: () => onNavigate('models-all') },
        { label: 'Make Prediction', onClick: () => onNavigate('predictions-new') },
      ],
      category: 'training',
      date: 'today',
    },
    {
      id: 2,
      icon: Database,
      color: 'text-primary',
      title: 'Dataset Upload Complete',
      message: '"customer_data_q4.csv" processed successfully',
      detail: '15,420 rows • 12 features detected',
      time: '5 hours ago',
      unread: true,
      actions: [
        { label: 'View Dataset', onClick: () => onNavigate('datasets') },
      ],
      category: 'general',
      date: 'today',
    },
    {
      id: 3,
      icon: Target,
      color: 'text-success',
      title: 'Prediction Complete',
      message: 'Batch prediction completed',
      detail: '150 predictions made',
      time: '6 hours ago',
      unread: true,
      actions: [
        { label: 'View Results', onClick: () => onNavigate('predictions-history') },
      ],
      category: 'general',
      date: 'today',
    },
    {
      id: 4,
      icon: AlertCircle,
      color: 'text-error',
      title: 'Training Failed',
      message: '"Revenue Model v2" encountered an error',
      detail: 'Error: Insufficient training data (min 100 rows required)',
      time: 'Yesterday',
      unread: false,
      actions: [
        { label: 'View Details', onClick: () => {} },
        { label: 'Retry', onClick: () => {} },
      ],
      category: 'training',
      date: 'yesterday',
    },
    {
      id: 5,
      icon: CheckCircle2,
      color: 'text-success',
      title: 'Weekly Summary',
      message: 'Your weekly activity summary is ready',
      detail: '8 predictions made • 2 models trained',
      time: 'Yesterday',
      unread: false,
      actions: [
        { label: 'View Report', onClick: () => {} },
      ],
      category: 'general',
      date: 'yesterday',
    },
    {
      id: 6,
      icon: AlertCircle,
      color: 'text-warning',
      title: 'Security Alert',
      message: 'New login detected from Chrome on MacOS',
      detail: 'Location: New York, US',
      time: 'Dec 5, 2024',
      unread: false,
      actions: [
        { label: 'Review Activity', onClick: () => onNavigate('settings-security') },
      ],
      category: 'security',
      date: 'earlier',
    },
  ];

  const groupedNotifications = {
    today: notifications.filter(n => n.date === 'today'),
    yesterday: notifications.filter(n => n.date === 'yesterday'),
    earlier: notifications.filter(n => n.date === 'earlier'),
  };

  const unreadCount = notifications.filter(n => n.unread).length;

  return (
    <div className="fixed inset-y-0 right-0 w-[450px] bg-background border-l border-border shadow-2xl z-50 flex flex-col">
      {/* Header */}
      <div className="p-6 border-b border-border">
        <div className="flex items-center justify-between mb-4">
          <h2>Notifications</h2>
          {onClose && (
            <Button variant="ghost" size="icon" onClick={onClose}>
              <X className="w-5 h-5" />
            </Button>
          )}
        </div>
        <div className="flex gap-2">
          <Button variant="outline" size="sm">
            Mark All Read
          </Button>
        </div>
      </div>

      {/* Filter Tabs */}
      <Tabs defaultValue="all" className="px-6 pt-4">
        <TabsList className="w-full">
          <TabsTrigger value="all" className="flex-1">All</TabsTrigger>
          <TabsTrigger value="unread" className="flex-1">
            Unread ({unreadCount})
          </TabsTrigger>
          <TabsTrigger value="training" className="flex-1">Training</TabsTrigger>
          <TabsTrigger value="security" className="flex-1">Security</TabsTrigger>
        </TabsList>
      </Tabs>

      {/* Notification List */}
      <div className="flex-1 overflow-y-auto">
        {/* Today */}
        {groupedNotifications.today.length > 0 && (
          <div className="px-6 py-4">
            <p className="text-sm font-medium text-muted-foreground mb-4">TODAY</p>
            <div className="space-y-3">
              {groupedNotifications.today.map((notification) => (
                <div
                  key={notification.id}
                  className={`p-4 rounded-lg border transition-colors ${
                    notification.unread
                      ? 'bg-primary/5 border-primary/20 hover:bg-primary/10'
                      : 'bg-muted/30 border-border hover:bg-muted/50'
                  }`}
                >
                  <div className="flex gap-3">
                    <div className={`w-10 h-10 rounded-lg bg-gradient-to-br ${notification.color} from-current/10 to-current/5 flex items-center justify-center flex-shrink-0`}>
                      <notification.icon className={`w-5 h-5 ${notification.color}`} />
                    </div>
                    <div className="flex-1 min-w-0">
                      <div className="flex items-start justify-between gap-2 mb-1">
                        <p className="font-medium">{notification.title}</p>
                        {notification.unread && (
                          <div className="w-2 h-2 rounded-full bg-primary flex-shrink-0 mt-2"></div>
                        )}
                      </div>
                      <p className="text-sm text-muted-foreground mb-1">{notification.message}</p>
                      <p className="text-sm text-primary mb-2">{notification.detail}</p>
                      <div className="flex flex-wrap gap-2 mb-2">
                        {notification.actions.map((action, i) => (
                          <Button key={i} variant="ghost" size="sm" className="h-7 text-xs" onClick={action.onClick}>
                            {action.label}
                          </Button>
                        ))}
                      </div>
                      <p className="text-xs text-tertiary">{notification.time}</p>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </div>
        )}

        {/* Yesterday */}
        {groupedNotifications.yesterday.length > 0 && (
          <div className="px-6 py-4 border-t border-border">
            <p className="text-sm font-medium text-muted-foreground mb-4">YESTERDAY</p>
            <div className="space-y-3">
              {groupedNotifications.yesterday.map((notification) => (
                <div
                  key={notification.id}
                  className="p-4 rounded-lg border bg-muted/30 border-border hover:bg-muted/50 transition-colors"
                >
                  <div className="flex gap-3">
                    <div className={`w-10 h-10 rounded-lg bg-gradient-to-br ${notification.color} from-current/10 to-current/5 flex items-center justify-center flex-shrink-0`}>
                      <notification.icon className={`w-5 h-5 ${notification.color}`} />
                    </div>
                    <div className="flex-1 min-w-0">
                      <p className="font-medium mb-1">{notification.title}</p>
                      <p className="text-sm text-muted-foreground mb-1">{notification.message}</p>
                      <p className="text-sm text-primary mb-2">{notification.detail}</p>
                      <div className="flex flex-wrap gap-2 mb-2">
                        {notification.actions.map((action, i) => (
                          <Button key={i} variant="ghost" size="sm" className="h-7 text-xs" onClick={action.onClick}>
                            {action.label}
                          </Button>
                        ))}
                      </div>
                      <p className="text-xs text-tertiary">{notification.time}</p>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </div>
        )}

        {/* Earlier */}
        {groupedNotifications.earlier.length > 0 && (
          <div className="px-6 py-4 border-t border-border">
            <p className="text-sm font-medium text-muted-foreground mb-4">EARLIER</p>
            <div className="space-y-3">
              {groupedNotifications.earlier.map((notification) => (
                <div
                  key={notification.id}
                  className="p-4 rounded-lg border bg-muted/30 border-border hover:bg-muted/50 transition-colors"
                >
                  <div className="flex gap-3">
                    <div className={`w-10 h-10 rounded-lg bg-gradient-to-br ${notification.color} from-current/10 to-current/5 flex items-center justify-center flex-shrink-0`}>
                      <notification.icon className={`w-5 h-5 ${notification.color}`} />
                    </div>
                    <div className="flex-1 min-w-0">
                      <p className="font-medium mb-1">{notification.title}</p>
                      <p className="text-sm text-muted-foreground mb-1">{notification.message}</p>
                      <p className="text-sm text-primary mb-2">{notification.detail}</p>
                      <div className="flex flex-wrap gap-2 mb-2">
                        {notification.actions.map((action, i) => (
                          <Button key={i} variant="ghost" size="sm" className="h-7 text-xs" onClick={action.onClick}>
                            {action.label}
                          </Button>
                        ))}
                      </div>
                      <p className="text-xs text-tertiary">{notification.time}</p>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </div>
        )}
      </div>
    </div>
  );
}