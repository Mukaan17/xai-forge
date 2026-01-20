"use client";

import * as React from "react";
import { motion } from "framer-motion";
import { cn } from "@/lib/utils";

// Animation variants for the container to stagger children
const containerVariants = {
  hidden: { opacity: 0 },
  visible: {
    opacity: 1,
    transition: {
      staggerChildren: 0.03,
      delayChildren: 0.1,
    },
  },
};

// Animation variants for each grid item
const itemVariants = {
  hidden: { opacity: 0, y: 20 },
  visible: {
    opacity: 1,
    y: 0,
    transition: {
      type: "spring",
      stiffness: 100,
      damping: 10,
    },
  },
};

/**
 * Props for the BentoGridShowcase component.
 * Accepts all feature cards as children in a flexible grid.
 */
interface BentoGridShowcaseProps {
  children: React.ReactNode;
  className?: string;
}

/**
 * A modern, minimal bento grid layout with tight spacing.
 * Uses span-based positioning with dense flow to eliminate gaps and prevent overlaps.
 */
export const BentoGridShowcase = ({
  children,
  className,
}: BentoGridShowcaseProps) => {
  const childrenArray = React.Children.toArray(children);
  
  // Get grid spans for each card
  // Order: 0=Dataset, 1=Security, 2=FeatureImportance, 3=Performance, 4=Search,
  // 5=Models, 6=Predictions, 7=Export, 8=Notifications, 9=History,
  // 10=Accuracy, 11=Activity, 12=Comparison, 13=XAI, 14=Training, 15=Settings
  const getCardLayout = (index: number) => {
    const base = "col-span-1 row-span-1";
    let mdClasses = "";
    let lgClasses = "";
    
    switch (index) {
      case 0: // Dataset - tall, spans 4 rows
        mdClasses = "md:col-span-1 md:row-span-4";
        lgClasses = "lg:col-span-1 lg:row-span-4";
        break;
      case 1: // Security - standard
      case 2: // Feature Importance - standard
      case 3: // Performance - standard
      case 4: // Search - standard
      case 5: // Models - standard
      case 7: // Export - standard
      case 8: // Notifications - standard
      case 9: // History - standard
      case 11: // Activity - standard
      case 12: // Comparison - standard
      case 14: // Training - standard
      case 15: // Settings - standard
        mdClasses = "md:col-span-1 md:row-span-1";
        lgClasses = "lg:col-span-1 lg:row-span-1";
        break;
      case 6: // Predictions - wide, spans 2 columns
        mdClasses = "md:col-span-2 md:row-span-1";
        lgClasses = "lg:col-span-2 lg:row-span-1";
        break;
      case 10: // Accuracy - tall, spans 2 rows
        mdClasses = "md:col-span-1 md:row-span-2";
        lgClasses = "lg:col-span-1 lg:row-span-2";
        break;
      case 13: // XAI - tall, spans 2 rows
        mdClasses = "md:col-span-1 md:row-span-2";
        lgClasses = "lg:col-span-1 lg:row-span-2";
        break;
    }
    
    return cn(base, mdClasses, lgClasses);
  };
  
  return (
    <motion.section
      variants={containerVariants}
      initial="hidden"
      animate="visible"
      className={cn(
        // Minimal spacing
        "grid w-full grid-cols-1 gap-1.5 md:gap-2",
        // Responsive columns
        "md:grid-cols-2 lg:grid-cols-4",
        // Auto rows with consistent minimum height
        "auto-rows-[minmax(140px,auto)]",
        // Dense packing to fill gaps automatically - prevents overlaps
        "grid-flow-row-dense",
        className
      )}
    >
      {childrenArray.map((child, index) => {
        const layoutClasses = getCardLayout(index);
        
        return (
          <motion.div
            key={index}
            variants={itemVariants}
            className={cn(
              // Ensure proper grid cell behavior
              "h-full w-full",
              // Prevent overflow and overlaps
              "min-h-0 min-w-0",
              // Ensure proper positioning
              "relative",
              layoutClasses
            )}
          >
            {child}
          </motion.div>
        );
      })}
    </motion.section>
  );
};
