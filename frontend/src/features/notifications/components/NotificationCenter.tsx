import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { X, CheckCheck, Trash2, BrainCircuit, Database, Target, AlertCircle, CheckCircle2, Bell, Loader2 } from 'lucide-react';
import { Button } from '@/shared/components/ui/button';
import { Badge } from '@/shared/components/ui/badge';
import { Skeleton } from '@/shared/components/ui/skeleton';
import { useNotifications } from '../hooks/useNotifications';
import { NotificationDto } from '../api/notifications';
import { cn } from '@/lib/utils';
// Simple date formatting without date-fns dependency
const formatTime = (dateString: string) => {
  try {
    const date = new Date(dateString);
    const now = new Date();
    const diffMs = now.getTime() - date.getTime();
    const diffMins = Math.floor(diffMs / 60000);
    const diffHours = Math.floor(diffMs / 3600000);
    const diffDays = Math.floor(diffMs / 86400000);

    if (diffMins < 1) return 'Just now';
    if (diffMins < 60) return `${diffMins}m ago`;
    if (diffHours < 24) return `${diffHours}h ago`;
    if (diffDays < 7) return `${diffDays}d ago`;
    return date.toLocaleDateString();
  } catch {
    return dateString;
  }
};

interface NotificationCenterProps {
  onClose: () => void;
}

const getNotificationIcon = (type: string) => {
  switch (type) {
    case 'TRAINING_COMPLETE':
      return BrainCircuit;
    case 'TRAINING_FAILED':
      return AlertCircle;
    case 'DATASET_UPLOADED':
      return Database;
    case 'PREDICTION_COMPLETE':
      return Target;
    case 'SECURITY_ALERT':
      return AlertCircle;
    default:
      return Bell;
  }
};

const getNotificationColor = (type: string) => {
  switch (type) {
    case 'TRAINING_COMPLETE':
      return 'text-success';
    case 'TRAINING_FAILED':
      return 'text-destructive';
    case 'DATASET_UPLOADED':
      return 'text-primary';
    case 'PREDICTION_COMPLETE':
      return 'text-secondary';
    case 'SECURITY_ALERT':
      return 'text-destructive';
    default:
      return 'text-muted-foreground';
  }
};

export function NotificationCenter({ onClose }: NotificationCenterProps) {
  const navigate = useNavigate();
  const [currentPage, setCurrentPage] = useState(0);
  const { notifications, unreadCount, isLoading, markAsRead, markAllAsRead, deleteNotification } = useNotifications(currentPage, 20);

  const handleNotificationClick = (notification: NotificationDto) => {
    if (!notification.read) {
      markAsRead(notification.id);
    }
    
    // Navigate based on notification type
    if (notification.type === 'TRAINING_COMPLETE' || notification.type === 'TRAINING_FAILED') {
      navigate('/models');
    } else if (notification.type === 'DATASET_UPLOADED') {
      navigate('/datasets');
    } else if (notification.type === 'PREDICTION_COMPLETE') {
      navigate('/predictions/history');
    }
  };


  return (
    <>
      {/* Backdrop */}
      <div 
        className="fixed inset-0 bg-black/50 z-40"
        onClick={onClose}
      />

      {/* Notification Panel */}
      <div className="fixed right-0 top-0 bottom-0 w-full sm:w-96 bg-card border-l border-border z-50 shadow-xl flex flex-col">
        {/* Header */}
        <div className="flex items-center justify-between p-4 border-b border-border">
          <div className="flex items-center gap-2">
            <Bell className="w-5 h-5" />
            <h2 className="text-lg font-semibold">Notifications</h2>
            {unreadCount > 0 && (
              <Badge variant="destructive" className="ml-2">
                {unreadCount}
              </Badge>
            )}
          </div>
          <div className="flex items-center gap-2">
            {unreadCount > 0 && (
              <Button
                variant="ghost"
                size="sm"
                onClick={() => markAllAsRead()}
                title="Mark all as read"
              >
                <CheckCheck className="w-4 h-4" />
              </Button>
            )}
            <Button
              variant="ghost"
              size="icon"
              onClick={onClose}
            >
              <X className="w-4 h-4" />
            </Button>
          </div>
        </div>

        {/* Notifications List */}
        <div className="flex-1 overflow-y-auto">
          {isLoading ? (
            <div className="p-4 space-y-4">
              {[1, 2, 3].map((i) => (
                <Skeleton key={i} className="h-20 w-full" />
              ))}
            </div>
          ) : notifications.length === 0 ? (
            <div className="flex flex-col items-center justify-center h-full p-8 text-center">
              <Bell className="w-12 h-12 text-muted-foreground mb-4" />
              <p className="text-muted-foreground">No notifications</p>
            </div>
          ) : (
            <div className="divide-y divide-border">
              {notifications.map((notification) => {
                const Icon = getNotificationIcon(notification.type);
                const iconColor = getNotificationColor(notification.type);
                
                return (
                  <div
                    key={notification.id}
                    className={cn(
                      "p-4 hover:bg-muted/50 transition-colors cursor-pointer relative group",
                      !notification.read && "bg-primary/5"
                    )}
                    onClick={() => handleNotificationClick(notification)}
                  >
                    <div className="flex items-start gap-3">
                      <div className={cn("w-10 h-10 rounded-full flex items-center justify-center flex-shrink-0", iconColor, "bg-current/10")}>
                        <Icon className="w-5 h-5" />
                      </div>
                      <div className="flex-1 min-w-0">
                        <div className="flex items-start justify-between gap-2">
                          <h3 className={cn("font-medium text-sm", !notification.read && "font-semibold")}>
                            {notification.title}
                          </h3>
                          {!notification.read && (
                            <div className="w-2 h-2 rounded-full bg-primary flex-shrink-0 mt-1" />
                          )}
                        </div>
                        <p className="text-sm text-muted-foreground mt-1 line-clamp-2">
                          {notification.message}
                        </p>
                        {notification.detail && (
                          <p className="text-xs text-muted-foreground mt-1">
                            {notification.detail}
                          </p>
                        )}
                        <p className="text-xs text-muted-foreground mt-2">
                          {formatTime(notification.createdAt)}
                        </p>
                      </div>
                      <Button
                        variant="ghost"
                        size="icon"
                        className="opacity-0 group-hover:opacity-100 transition-opacity h-6 w-6"
                        onClick={(e) => {
                          e.stopPropagation();
                          deleteNotification(notification.id);
                        }}
                      >
                        <Trash2 className="w-3 h-3" />
                      </Button>
                    </div>
                  </div>
                );
              })}
            </div>
          )}
        </div>

        {/* Footer */}
        {notifications.length > 0 && (
          <div className="border-t border-border p-4">
            <Button
              variant="outline"
              className="w-full"
              onClick={() => navigate('/notifications')}
            >
              View All Notifications
            </Button>
          </div>
        )}
      </div>
    </>
  );
}
