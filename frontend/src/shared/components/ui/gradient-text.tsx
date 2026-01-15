import * as React from "react";
import { cn } from "./utils";

interface GradientTextProps extends React.HTMLAttributes<HTMLSpanElement> {
  variant?: "primary" | "secondary" | "accent";
  children: React.ReactNode;
}

/**
 * GradientText component that applies gradient text effects matching the hero design language
 * 
 * Variants:
 * - primary: from-primary via-secondary to-primary (default)
 * - secondary: from-secondary via-primary to-secondary
 * - accent: from-accent via-primary to-accent
 */
export function GradientText({ 
  className, 
  variant = "primary", 
  children,
  ...props 
}: GradientTextProps) {
  const gradientClasses = {
    primary: "bg-gradient-to-r from-primary via-secondary to-primary bg-clip-text text-transparent",
    secondary: "bg-gradient-to-r from-secondary via-primary to-secondary bg-clip-text text-transparent",
    accent: "bg-gradient-to-r from-accent via-primary to-accent bg-clip-text text-transparent",
  };

  return (
    <span
      className={cn(gradientClasses[variant], className)}
      {...props}
    >
      {children}
    </span>
  );
}
