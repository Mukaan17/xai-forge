"use client";

import { useState } from "react";
import { Button } from "@/shared/components/ui/button";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
  DialogClose,
} from "@/shared/components/ui/dialog";
import { Checkbox } from "@/shared/components/ui/checkbox";
import { Label } from "@/shared/components/ui/label";
import { cn } from "@/lib/utils";
import { ArrowRight, CheckCircle2, BrainCircuit } from "lucide-react";
import { AnimatePresence, motion } from "framer-motion";
import { useNavigate } from "react-router-dom";

interface OnboardingFlowProps {
  onComplete: () => void;
  onSkip: () => void;
}

export function OnboardingFlow({ onComplete, onSkip }: OnboardingFlowProps) {
  const [step, setStep] = useState(0);
  const [isOpen, setIsOpen] = useState(true);
  const [dontShowAgain, setDontShowAgain] = useState(false);
  const navigate = useNavigate();

  const steps = [
    {
      title: "Welcome to XAI-Forge",
      description: "Build, train, and understand machine learning models with complete transparency and explainability.",
    },
    {
      title: "Upload & Manage Datasets",
      description: "Easily upload CSV files, validate data, and organize your datasets before training models.",
    },
    {
      title: "Train ML Models",
      description: "Train classification and regression models with state-of-the-art algorithms and hyperparameter tuning.",
    },
    {
      title: "Explainable AI Insights",
      description: "Get human-understandable explanations for every prediction using LIME-based interpretability.",
    },
  ];

  const handleOpenChange = (open: boolean) => {
    if (!open) {
      setIsOpen(false);
      onSkip();
    }
  };

  const next = () => {
    if (step < steps.length - 1) {
      setStep(step + 1);
    } else {
      handleFinish();
    }
  };

  const handleFinish = () => {
    setIsOpen(false);
    
    // Mark onboarding as shown in this session
    sessionStorage.setItem('xai-forge-onboarding-shown', 'true');
    
    // If user checked "Don't show again", save to localStorage
    if (dontShowAgain) {
      localStorage.setItem('xai-forge-onboarding-completed', 'true');
    }
    
    onComplete();
  };

  const handleSkip = () => {
    setIsOpen(false);
    // Mark as shown in this session even if skipped
    sessionStorage.setItem('xai-forge-onboarding-shown', 'true');
    onSkip();
  };

  return (
    <Dialog open={isOpen} onOpenChange={handleOpenChange}>
      <DialogContent
        className={cn(
          "max-w-5xl sm:max-w-5xl p-0 overflow-hidden rounded-xl border shadow-2xl",
          "bg-card text-foreground",
          "border-border",
          "data-[state=open]:animate-none data-[state=closed]:animate-none"
        )}
      >
        <div className="flex flex-col md:flex-row w-full">
          {/* Sidebar - Industry standard: ~32% width */}
          <div className="w-full md:w-[32%] p-10 border-r border-border bg-card/50">
            <div className="flex flex-col gap-6">
              {/* Logo/Icon */}
              <div className="flex h-14 w-14 items-center justify-center rounded-xl bg-gradient-to-br from-primary to-secondary shrink-0">
                <BrainCircuit className="h-7 w-7 text-primary-foreground" />
              </div>
              
              {/* Header */}
              <div className="space-y-2">
                <h2 className="text-xl font-semibold text-foreground leading-tight">XAI-Forge Onboarding</h2>
                <p className="text-sm text-muted-foreground leading-relaxed">
                  Explore our features step-by-step to get the best out of your ML workflow.
                </p>
              </div>
              
              {/* Steps List - Industry standard: 8px spacing */}
              <div className="flex flex-col gap-4 mt-2">
                {steps.map((s, index) => (
                  <div
                    key={index}
                    className={cn(
                      "flex items-center gap-3 text-sm transition-colors",
                      index === step
                        ? "font-semibold text-foreground"
                        : "opacity-70 hover:opacity-100 text-muted-foreground"
                    )}
                  >
                    {index < step ? (
                      <CheckCircle2 className="w-5 h-5 text-primary shrink-0" />
                    ) : (
                      <div className={cn(
                        "w-2 h-2 rounded-full shrink-0 transition-colors",
                        index === step ? "bg-primary" : "bg-muted-foreground/40"
                      )} />
                    )}
                    <span className="font-normal leading-relaxed">{s.title}</span>
                  </div>
                ))}
              </div>
            </div>
          </div>

          {/* Main Content - Industry standard: ~68% width */}
          <div className="w-full md:w-[68%] p-12 flex flex-col justify-between min-h-[420px]">
            <div className="space-y-8">
              <DialogHeader className="space-y-4">
                <AnimatePresence mode="wait">
                  <motion.h2
                    key={steps[step].title}
                    initial={{ opacity: 0, y: 10 }}
                    animate={{ opacity: 1, y: 0 }}
                    exit={{ opacity: 0, y: -10 }}
                    transition={{ duration: 0.25 }}
                    className="text-3xl font-semibold text-foreground leading-tight"
                  >
                    {steps[step].title}
                  </motion.h2>
                </AnimatePresence>

                <div className="min-h-[72px]">
                  <AnimatePresence mode="wait">
                    <motion.p
                      key={steps[step].description}
                      initial={{ opacity: 0, y: 10 }}
                      animate={{ opacity: 1, y: 0 }}
                      exit={{ opacity: 0, y: -10 }}
                      transition={{ duration: 0.25 }}
                      className="text-muted-foreground text-base leading-relaxed max-w-2xl"
                    >
                      {steps[step].description}
                    </motion.p>
                  </AnimatePresence>
                </div>
              </DialogHeader>

              {/* Visual Content - Industry standard: proportional height */}
              <div className="w-full h-64 bg-gradient-to-br from-primary/10 via-secondary/10 to-transparent dark:from-primary/20 dark:via-secondary/20 dark:to-transparent rounded-lg flex items-center justify-center border border-border/40">
                {step === 0 && (
                  <div className="flex items-center gap-16">
                    <div className="text-center">
                      <div className="w-24 h-24 rounded-xl bg-primary/20 flex items-center justify-center mx-auto mb-4">
                        <BrainCircuit className="w-12 h-12 text-primary" />
                      </div>
                      <p className="text-base font-medium text-foreground">ML Models</p>
                    </div>
                    <ArrowRight className="w-8 h-8 text-muted-foreground" />
                    <div className="text-center">
                      <div className="w-24 h-24 rounded-xl bg-secondary/20 flex items-center justify-center mx-auto mb-4">
                        <CheckCircle2 className="w-12 h-12 text-secondary" />
                      </div>
                      <p className="text-base font-medium text-foreground">Explainable AI</p>
                    </div>
                  </div>
                )}
                {step === 1 && (
                  <div className="text-center p-8">
                    <div className="w-32 h-32 rounded-xl bg-primary/20 flex items-center justify-center mx-auto mb-6">
                      <BrainCircuit className="w-16 h-16 text-primary" />
                    </div>
                    <p className="text-base text-muted-foreground">Upload CSV files and manage your datasets</p>
                  </div>
                )}
                {step === 2 && (
                  <div className="text-center p-8">
                    <div className="w-32 h-32 rounded-xl bg-secondary/20 flex items-center justify-center mx-auto mb-6">
                      <BrainCircuit className="w-16 h-16 text-secondary" />
                    </div>
                    <p className="text-base text-muted-foreground">Train models with multiple algorithms</p>
                  </div>
                )}
                {step === 3 && (
                  <div className="text-center p-8">
                    <div className="w-32 h-32 rounded-xl bg-gradient-to-br from-primary/20 to-secondary/20 flex items-center justify-center mx-auto mb-6">
                      <CheckCircle2 className="w-16 h-16 text-primary" />
                    </div>
                    <p className="text-base text-muted-foreground">Get transparent explanations for every prediction</p>
                  </div>
                )}
              </div>
            </div>

            {/* Footer - Industry standard: 16px gap between buttons */}
            <div className="mt-10 space-y-4">
              {/* Don't show again checkbox - only on last step */}
              {step === steps.length - 1 && (
                <div className="flex items-center gap-3 pb-4 border-b border-border">
                  <Checkbox
                    id="dont-show-again"
                    checked={dontShowAgain}
                    onCheckedChange={(checked) => setDontShowAgain(checked as boolean)}
                  />
                  <Label
                    htmlFor="dont-show-again"
                    className="text-sm text-muted-foreground cursor-pointer leading-none peer-disabled:cursor-not-allowed peer-disabled:opacity-70"
                  >
                    Click here to not see this again
                  </Label>
                </div>
              )}
              
              <div className="flex justify-between items-center pt-4">
                <DialogClose asChild>
                  <Button variant="outline" onClick={handleSkip}>
                    Skip
                  </Button>
                </DialogClose>

                {step < steps.length - 1 ? (
                  <Button onClick={next}>
                    Continue
                    <ArrowRight className="ml-2 h-4 w-4" />
                  </Button>
                ) : (
                  <Button onClick={handleFinish}>
                    Get Started
                    <ArrowRight className="ml-2 h-4 w-4" />
                  </Button>
                )}
              </div>
            </div>
          </div>
        </div>
      </DialogContent>
    </Dialog>
  );
}
