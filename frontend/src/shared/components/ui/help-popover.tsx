"use client";

import React, { useState, useEffect } from "react";
import { HelpCircle, ExternalLink } from "lucide-react";
import { motion, AnimatePresence } from "framer-motion";
import { Button } from "./button";
import { cn } from "@/lib/utils";

interface HelpPopoverProps {
  buttonClassName?: string;
  popoverClassName?: string;
}

export const HelpPopover = ({
  buttonClassName,
  popoverClassName,
}: HelpPopoverProps) => {
  const [isOpen, setIsOpen] = useState(false);

  const toggleOpen = () => setIsOpen(!isOpen);

  // Close popover when clicking outside
  useEffect(() => {
    if (!isOpen) return;

    const handleClickOutside = (event: MouseEvent) => {
      const target = event.target as HTMLElement;
      if (!target.closest('[data-help-popover]')) {
        setIsOpen(false);
      }
    };

    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, [isOpen]);

  const helpItems = [
    {
      label: "Documentation",
      href: "https://docs.xai-forge.com",
      description: "Browse our comprehensive documentation",
    },
    {
      label: "Support",
      href: "https://github.com/xai-forge/support",
      description: "Get help from our support team",
    },
  ];

  return (
    <div className="relative" data-help-popover>
      <Button
        onClick={toggleOpen}
        size="icon"
        variant="ghost"
        className={cn(
          "relative",
          buttonClassName || "w-10 h-10"
        )}
        aria-label="Help and support (Press ? or Cmd+/ or Ctrl+/)"
        aria-keyshortcuts="? Meta+/ Ctrl+/"
        aria-haspopup="true"
      >
        <HelpCircle className="w-5 h-5" />
      </Button>

      <AnimatePresence>
        {isOpen && (
          <motion.div
            initial={{ opacity: 0, y: 10, scale: 0.95 }}
            animate={{ opacity: 1, y: 0, scale: 1 }}
            exit={{ opacity: 0, y: 10, scale: 0.95 }}
            transition={{ duration: 0.2 }}
            className={cn(
              "absolute right-0 mt-2 w-72 rounded-xl shadow-lg border border-border z-50",
              popoverClassName || "bg-card backdrop-blur-sm"
            )}
          >
            <div className="p-4 border-b border-border flex justify-between items-center bg-card/95 backdrop-blur-sm sticky top-0 z-10 rounded-t-xl">
              <h3 className="text-sm font-semibold text-foreground">Help & Support</h3>
            </div>

            <div className="divide-y divide-border">
              {helpItems.map((item, index) => (
                <motion.a
                  key={item.label}
                  href={item.href}
                  target="_blank"
                  rel="noopener noreferrer"
                  initial={{ opacity: 0, x: 20 }}
                  animate={{ opacity: 1, x: 0 }}
                  transition={{ duration: 0.2, delay: index * 0.05 }}
                  className="block p-4 hover:bg-muted/50 cursor-pointer transition-colors group"
                  onClick={() => setIsOpen(false)}
                >
                  <div className="flex items-start justify-between gap-3">
                    <div className="flex-1 min-w-0">
                      <div className="flex items-center gap-2 mb-1">
                        <h4 className="text-sm font-medium text-foreground group-hover:text-primary transition-colors">
                          {item.label}
                        </h4>
                        <ExternalLink className="w-3 h-3 text-muted-foreground opacity-0 group-hover:opacity-100 transition-opacity" />
                      </div>
                      <p className="text-xs text-muted-foreground">
                        {item.description}
                      </p>
                    </div>
                  </div>
                </motion.a>
              ))}
            </div>
          </motion.div>
        )}
      </AnimatePresence>
    </div>
  );
};
