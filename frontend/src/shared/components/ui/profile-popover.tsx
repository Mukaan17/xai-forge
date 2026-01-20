"use client";

import React, { useState, useEffect } from "react";
import { User, Settings, LogOut } from "lucide-react";
import { motion, AnimatePresence } from "framer-motion";
import { Link } from "react-router-dom";
import { Button } from "./button";
import { cn } from "@/lib/utils";

interface ProfilePopoverProps {
  user?: {
    username?: string;
    email?: string;
  };
  onLogout?: () => void;
  buttonClassName?: string;
  popoverClassName?: string;
}

export const ProfilePopover = ({
  user,
  onLogout,
  buttonClassName,
  popoverClassName,
}: ProfilePopoverProps) => {
  const [isOpen, setIsOpen] = useState(false);

  const toggleOpen = () => setIsOpen(!isOpen);

  const handleLogout = () => {
    setIsOpen(false);
    onLogout?.();
  };

  // Close popover when clicking outside
  useEffect(() => {
    if (!isOpen) return;

    const handleClickOutside = (event: MouseEvent) => {
      const target = event.target as HTMLElement;
      if (!target.closest('[data-profile-popover]')) {
        setIsOpen(false);
      }
    };

    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, [isOpen]);

  const menuItems = [
    {
      label: "Settings",
      icon: Settings,
      href: "/settings",
      onClick: () => setIsOpen(false),
    },
  ];

  return (
    <div className="relative" data-profile-popover>
      <Button
        onClick={toggleOpen}
        size="icon"
        variant="ghost"
        className={cn(
          "relative rounded-full",
          buttonClassName || "w-10 h-10"
        )}
        aria-label="User menu"
        aria-haspopup="true"
      >
        <div className="w-8 h-8 rounded-full bg-primary/10 border border-primary/20 flex items-center justify-center">
          <User className="w-4 h-4 text-primary" />
        </div>
      </Button>

      <AnimatePresence>
        {isOpen && (
          <motion.div
            initial={{ opacity: 0, y: 10, scale: 0.95 }}
            animate={{ opacity: 1, y: 0, scale: 1 }}
            exit={{ opacity: 0, y: 10, scale: 0.95 }}
            transition={{ duration: 0.2 }}
            className={cn(
              "absolute right-0 mt-2 w-56 rounded-xl shadow-lg border border-border z-50",
              popoverClassName || "bg-card backdrop-blur-sm"
            )}
          >
            {/* User Info Header */}
            <div className="p-4 border-b border-border bg-card/95 backdrop-blur-sm rounded-t-xl">
              <p className="font-medium text-foreground">{user?.username || 'User'}</p>
              <p className="text-sm text-muted-foreground truncate">{user?.email || ''}</p>
            </div>

            {/* Menu Items */}
            <div className="divide-y divide-border">
              {menuItems.map((item, index) => {
                const Icon = item.icon;
                return (
                  <motion.div
                    key={item.label}
                    initial={{ opacity: 0, x: 20 }}
                    animate={{ opacity: 1, x: 0 }}
                    transition={{ duration: 0.2, delay: index * 0.05 }}
                  >
                    <Link
                      to={item.href}
                      onClick={item.onClick}
                      className="flex items-center gap-3 p-4 hover:bg-muted/50 cursor-pointer transition-colors group"
                    >
                      <Icon className="w-4 h-4 text-muted-foreground group-hover:text-primary transition-colors" />
                      <span className="text-sm text-foreground group-hover:text-primary transition-colors">
                        {item.label}
                      </span>
                    </Link>
                  </motion.div>
                );
              })}

              {/* Logout */}
              <motion.button
                initial={{ opacity: 0, x: 20 }}
                animate={{ opacity: 1, x: 0 }}
                transition={{ duration: 0.2, delay: menuItems.length * 0.05 }}
                onClick={handleLogout}
                className="w-full flex items-center gap-3 p-4 hover:bg-destructive/10 cursor-pointer transition-colors group text-left"
              >
                <LogOut className="w-4 h-4 text-destructive group-hover:text-destructive" />
                <span className="text-sm text-destructive font-medium">
                  Logout
                </span>
              </motion.button>
            </div>
          </motion.div>
        )}
      </AnimatePresence>
    </div>
  );
};
