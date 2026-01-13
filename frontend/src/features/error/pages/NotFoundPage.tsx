import * as React from "react";
import { useNavigate, useLocation } from "react-router-dom";
import { ArrowLeft, Home, Ghost } from "lucide-react";
import { cn } from "@/lib/utils";
import { Button } from "@/shared/components/ui/button";
import {
  Empty,
  EmptyContent,
  EmptyDescription,
  EmptyHeader,
  EmptyMedia,
  EmptyTitle,
} from "@/shared/components/ui/empty";
import MistBackground from "@/shared/components/ui/mist-background";
import { motion, AnimatePresence } from "framer-motion";

interface NotFound404Props {
  title?: string;
  description?: string;
  className?: string;
}

export function NotFoundPage({
  title = "Page Not Found",
  description = "The page you're looking for doesn't exist. It may have been moved or deleted.",
  className,
}: NotFound404Props) {
  const navigate = useNavigate();
  const [isExiting, setIsExiting] = React.useState(false);
  const targetPathRef = React.useRef<string | null>(null);

  const handleHomeClick = () => {
    navigate('/');
  };

  const handleBackClick = () => {
    // Get the previous path from sessionStorage
    const previousPath = sessionStorage.getItem('xai-forge-previous-path') || '/';
    targetPathRef.current = previousPath;
    
    // Set flag to indicate we're navigating back
    sessionStorage.setItem('xai-forge-navigating-back', 'true');
    
    // Start exit animation
    setIsExiting(true);
  };

  // Navigate after exit animation completes
  React.useEffect(() => {
    if (isExiting && targetPathRef.current) {
      const timer = setTimeout(() => {
        navigate(targetPathRef.current!);
      }, 400); // Match animation duration
      return () => clearTimeout(timer);
    }
  }, [isExiting, navigate]);

  return (
    <AnimatePresence mode="wait">
      {!isExiting && (
        <motion.div
          key="not-found-page"
          initial={{ opacity: 1, x: 0 }}
          exit={{ opacity: 0, x: -100 }}
          transition={{ duration: 0.4, ease: [0.22, 1, 0.36, 1] }}
          className={cn(
            "relative min-h-screen w-full overflow-hidden flex items-center justify-center px-6",
            className
          )}
          style={{ background: 'transparent' }}
        >
          <MistBackground />
          <Empty className="border-border/40 relative z-20">
        <EmptyHeader>
          <EmptyMedia variant="icon">
            <Ghost className="h-16 w-16 text-primary/60" />
          </EmptyMedia>
          <EmptyTitle className="text-5xl font-bold bg-gradient-to-r from-primary via-primary/80 to-secondary bg-clip-text text-transparent mb-2">
            404
          </EmptyTitle>
          <EmptyDescription className="text-lg text-muted-foreground max-w-md">{description}</EmptyDescription>
        </EmptyHeader>
        <EmptyContent>
          <div className="flex flex-col items-center justify-center gap-3 sm:flex-row">
            <Button onClick={handleHomeClick} className="group">
              <Home className="h-4 w-4 mr-2 transition-transform group-hover:scale-110" />
              Go Home
            </Button>

            <Button onClick={handleBackClick} variant="outline" className="group hover:text-foreground">
              <ArrowLeft className="h-4 w-4 mr-2 transition-transform group-hover:-translate-x-1" />
              Go Back
            </Button>
          </div>
        </EmptyContent>
      </Empty>
    </motion.div>
      )}
    </AnimatePresence>
  );
}
