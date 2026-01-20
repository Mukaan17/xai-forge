"use client";

import React, { useState, useEffect } from "react";
import { Bell } from "lucide-react";
import { motion, AnimatePresence } from "framer-motion";
import { Button } from "./button";
import { cn } from "@/lib/utils";
import { useNotifications } from "@/features/notifications/hooks/useNotifications";
import { NotificationDto } from "@/features/notifications/api/notificationsApi";

interface NotificationItemProps {
  notification: NotificationDto;
  index: number;
  onMarkAsRead: (id: number) => void;
}

const NotificationItem = ({
  notification,
  index,
  onMarkAsRead,
}: NotificationItemProps) => {
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

  return (
    <motion.div
      initial={{ opacity: 0, x: 20, filter: "blur(10px)" }}
      animate={{ opacity: 1, x: 0, filter: "blur(0px)" }}
      transition={{ duration: 0.3, delay: index * 0.05 }}
      key={notification.id}
      className={cn(
        "p-4 hover:bg-muted/50 cursor-pointer transition-colors",
        !notification.read && "bg-primary/5"
      )}
      onClick={() => !notification.read && onMarkAsRead(notification.id)}
    >
      <div className="flex justify-between items-start">
        <div className="flex items-center gap-2 flex-1 min-w-0">
          {!notification.read && (
            <span className="h-2 w-2 rounded-full bg-primary shrink-0" />
          )}
          <div className="flex-1 min-w-0">
            <h4 className={cn(
              "text-sm font-medium text-foreground truncate",
              !notification.read && "font-semibold"
            )}>
              {notification.title}
            </h4>
            <p className="text-xs text-muted-foreground mt-1 line-clamp-2">
              {notification.message}
            </p>
          </div>
        </div>

        <span className="text-xs text-muted-foreground shrink-0 ml-2">
          {formatTime(notification.createdAt)}
        </span>
      </div>
    </motion.div>
  );
};

interface NotificationListProps {
  notifications: NotificationDto[];
  onMarkAsRead: (id: number) => void;
}

const NotificationList = ({
  notifications,
  onMarkAsRead,
}: NotificationListProps) => (
  <div className="divide-y divide-border">
    {notifications.map((notification, index) => (
      <NotificationItem
        key={notification.id}
        notification={notification}
        index={index}
        onMarkAsRead={onMarkAsRead}
      />
    ))}
  </div>
);

interface NotificationPopoverProps {
  buttonClassName?: string;
  popoverClassName?: string;
}

export const NotificationPopover = ({
  buttonClassName,
  popoverClassName,
}: NotificationPopoverProps) => {
  const [isOpen, setIsOpen] = useState(false);
  const [currentPage] = useState(0);
  const { notifications, unreadCount, markAsRead, markAllAsRead } = useNotifications(currentPage, 10);

  const toggleOpen = () => setIsOpen(!isOpen);

  const handleMarkAllAsRead = () => {
    markAllAsRead();
  };

  const handleMarkAsRead = (id: number) => {
    markAsRead(id);
  };

  // Close popover when clicking outside
  useEffect(() => {
    if (!isOpen) return;

    const handleClickOutside = (event: MouseEvent) => {
      const target = event.target as HTMLElement;
      if (!target.closest('[data-notification-popover]')) {
        setIsOpen(false);
      }
    };

    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, [isOpen]);

  return (
    <div className="relative" data-notification-popover>
      <Button
        onClick={toggleOpen}
        size="icon"
        variant="ghost"
        className={cn(
          "relative",
          buttonClassName || "w-10 h-10"
        )}
        aria-label={`Notifications${unreadCount > 0 ? `, ${unreadCount} unread` : ''}`}
      >
        <Bell className="w-5 h-5" />
        {unreadCount > 0 && (
          <div className="absolute -top-1 -right-1 w-5 h-5 bg-destructive rounded-full flex items-center justify-center text-xs border-2 border-background text-destructive-foreground font-medium">
            {unreadCount > 99 ? '99+' : unreadCount}
          </div>
        )}
      </Button>

      <AnimatePresence>
        {isOpen && (
          <motion.div
            initial={{ opacity: 0, y: 10, scale: 0.95 }}
            animate={{ opacity: 1, y: 0, scale: 1 }}
            exit={{ opacity: 0, y: 10, scale: 0.95 }}
            transition={{ duration: 0.2 }}
            className={cn(
              "absolute right-0 mt-2 w-80 max-h-[400px] overflow-y-auto rounded-xl shadow-lg border border-border z-50",
              popoverClassName || "bg-card backdrop-blur-sm"
            )}
          >
            <div className="p-4 border-b border-border flex justify-between items-center bg-card/95 backdrop-blur-sm sticky top-0 z-10">
              <h3 className="text-sm font-semibold text-foreground">Notifications</h3>
              {unreadCount > 0 && (
                <Button
                  onClick={handleMarkAllAsRead}
                  variant="ghost"
                  size="sm"
                  className="text-xs h-7 px-2"
                >
                  Mark all as read
                </Button>
              )}
            </div>

            {notifications.length === 0 ? (
              <div className="flex flex-col items-center justify-center p-8 text-center">
                <Bell className="w-8 h-8 text-muted-foreground mb-2" />
                <p className="text-sm text-muted-foreground">No notifications</p>
              </div>
            ) : (
              <NotificationList
                notifications={notifications}
                onMarkAsRead={handleMarkAsRead}
              />
            )}
          </motion.div>
        )}
      </AnimatePresence>
    </div>
  );
};
