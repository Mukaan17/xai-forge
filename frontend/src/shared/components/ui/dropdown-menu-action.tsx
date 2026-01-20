"use client";

import { Button } from "./button";
import { ChevronDown, MoreVertical } from "lucide-react";
import React, { useState, useEffect } from "react";
import { motion, AnimatePresence } from "framer-motion";
import { cn } from "@/lib/utils";

type DropdownMenuActionProps = {
  options: {
    label: string;
    onClick: () => void;
    Icon?: React.ComponentType<{ className?: string }>;
    variant?: "default" | "destructive";
  }[];
  children?: React.ReactNode;
  align?: "left" | "right";
  className?: string;
  buttonClassName?: string;
  variant?: "default" | "icon";
  icon?: React.ComponentType<{ className?: string }>;
};

export const DropdownMenuAction = ({ 
  options, 
  children, 
  align = "right",
  className,
  buttonClassName,
  variant: buttonVariant = "default",
  icon: IconComponent,
}: DropdownMenuActionProps) => {
  const [isOpen, setIsOpen] = useState(false);

  const toggleDropdown = () => {
    setIsOpen(!isOpen);
  };

  // Close dropdown when clicking outside
  useEffect(() => {
    if (!isOpen) return;

    const handleClickOutside = (event: MouseEvent) => {
      const target = event.target as HTMLElement;
      if (!target.closest('[data-dropdown-menu]')) {
        setIsOpen(false);
      }
    };

    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, [isOpen]);

  const Icon = IconComponent || MoreVertical;

  return (
    <div className={cn("relative", className)} data-dropdown-menu>
      <Button
        onClick={toggleDropdown}
        variant="ghost"
        size={buttonVariant === "icon" ? "icon" : "default"}
        className={cn(
          buttonVariant === "icon" 
            ? "h-8 w-8" 
            : "px-4 py-2 bg-card hover:bg-muted/50 border-border/50 shadow-sm rounded-lg",
          buttonClassName
        )}
      >
        {buttonVariant === "icon" ? (
          <Icon className="h-4 w-4" />
        ) : (
          <>
            {children ?? "Menu"}
            <motion.span
              className="ml-2"
              animate={{ rotate: isOpen ? 180 : 0 }}
              transition={{ duration: 0.3, ease: "easeInOut" }}
            >
              <ChevronDown className="h-4 w-4" />
            </motion.span>
          </>
        )}
      </Button>

      <AnimatePresence>
        {isOpen && (
          <motion.div
            initial={{ y: -5, scale: 0.95, opacity: 0 }}
            animate={{ y: 0, scale: 1, opacity: 1 }}
            exit={{ y: -5, scale: 0.95, opacity: 0 }}
            transition={{ duration: 0.2, ease: "easeOut" }}
            className={cn(
              "absolute z-50 w-48 mt-2 p-1 bg-card rounded-lg shadow-lg border border-border backdrop-blur-sm flex flex-col gap-1",
              align === "right" ? "right-0" : "left-0"
            )}
          >
            {options && options.length > 0 ? (
              options.map((option, index) => {
                const Icon = option.Icon;
                return (
                  <motion.button
                    key={option.label}
                    initial={{ opacity: 0, x: 10 }}
                    animate={{ opacity: 1, x: 0 }}
                    exit={{ opacity: 0, x: 10 }}
                    transition={{
                      duration: 0.2,
                      delay: index * 0.05,
                    }}
                    whileHover={{
                      backgroundColor: option.variant === "destructive" 
                        ? "rgba(239, 68, 68, 0.1)" 
                        : "rgba(0, 0, 0, 0.05)",
                      transition: { duration: 0.2 },
                    }}
                    whileTap={{
                      scale: 0.98,
                      transition: { duration: 0.1 },
                    }}
                    onClick={() => {
                      option.onClick();
                      setIsOpen(false);
                    }}
                    className={cn(
                      "px-3 py-2 cursor-pointer text-sm rounded-md w-full text-left flex items-center gap-2 transition-colors",
                      option.variant === "destructive"
                        ? "text-destructive hover:bg-destructive/10"
                        : "text-foreground hover:bg-muted/50"
                    )}
                  >
                    {Icon && <Icon className="h-4 w-4" />}
                    {option.label}
                  </motion.button>
                );
              })
            ) : (
              <div className="px-4 py-2 text-muted-foreground text-xs">No options</div>
            )}
          </motion.div>
        )}
      </AnimatePresence>
    </div>
  );
};
