"use client";
import * as React from "react";
import { motion, useInView, Variants, HTMLMotionProps } from "framer-motion";
import { cn } from "@/lib/utils";

interface TimelineContentProps {
  as?: "div" | "h1" | "h2" | "h3" | "span" | "p";
  animationNum: number;
  timelineRef: React.RefObject<HTMLElement>;
  customVariants?: Variants;
  className?: string;
  children: React.ReactNode;
}

export function TimelineContent({
  as = "div",
  animationNum,
  timelineRef,
  customVariants,
  className,
  children,
}: TimelineContentProps) {
  const ref = React.useRef<HTMLElement>(null);
  const isInView = useInView(ref, {
    once: true,
    margin: "-100px",
    root: timelineRef.current || undefined,
  });

  const defaultVariants: Variants = {
    visible: {
      opacity: 1,
      y: 0,
      filter: "blur(0px)",
      transition: {
        delay: animationNum * 0.1,
        duration: 0.7,
      },
    },
    hidden: {
      opacity: 0,
      y: 20,
      filter: "blur(10px)",
    },
  };

  const variants = customVariants || defaultVariants;

  const motionProps: HTMLMotionProps<typeof as> = {
    ref: ref as any,
    variants,
    initial: "hidden",
    animate: isInView ? "visible" : "hidden",
    className: cn(className),
  };

  switch (as) {
    case "h1":
      return <motion.h1 {...motionProps}>{children}</motion.h1>;
    case "h2":
      return <motion.h2 {...motionProps}>{children}</motion.h2>;
    case "h3":
      return <motion.h3 {...motionProps}>{children}</motion.h3>;
    case "span":
      return <motion.span {...motionProps}>{children}</motion.span>;
    case "p":
      return <motion.p {...motionProps}>{children}</motion.p>;
    default:
      return <motion.div {...motionProps}>{children}</motion.div>;
  }
}
