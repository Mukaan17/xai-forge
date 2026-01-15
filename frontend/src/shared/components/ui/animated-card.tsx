import * as React from "react";
import { motion } from "framer-motion";
import { Card, CardContent, CardHeader, CardTitle, CardDescription, CardFooter, CardAction } from "./card";
import { cn } from "./utils";

interface AnimatedCardProps extends React.ComponentProps<typeof Card> {
  delay?: number;
  duration?: number;
  children: React.ReactNode;
}

/**
 * AnimatedCard component that applies spring animations matching the hero design language
 * 
 * Uses spring animations with bounce: 0.3 and configurable duration
 */
export function AnimatedCard({ 
  className, 
  delay = 0,
  duration = 1.5,
  children,
  ...props 
}: AnimatedCardProps) {
  return (
    <motion.div
      initial={{ opacity: 0, y: 20, filter: "blur(12px)" }}
      animate={{ opacity: 1, y: 0, filter: "blur(0px)" }}
      transition={{
        type: "spring",
        bounce: 0.3,
        duration,
        delay,
      }}
    >
      <Card className={cn(className)} {...props}>
        {children}
      </Card>
    </motion.div>
  );
}

// Re-export Card subcomponents for convenience
export {
  CardContent,
  CardHeader,
  CardTitle,
  CardDescription,
  CardFooter,
  CardAction,
};
