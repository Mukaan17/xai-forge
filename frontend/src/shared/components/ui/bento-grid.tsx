import { ReactNode, useState } from "react";
import { ArrowRight, Check } from "lucide-react";
import { cn } from "@/lib/utils";
import { Button } from "./button";
import Modal from "./modal-drop";

const BentoGrid = ({
  children,
  className,
}: {
  children: ReactNode;
  className?: string;
}) => {
  return (
    <div
      className={cn(
        "grid w-full auto-rows-[22rem] grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4",
        className,
      )}
    >
      {children}
    </div>
  );
};

interface BentoCardProps {
  name: string;
  className?: string;
  background: ReactNode;
  Icon: any;
  description: string;
  href: string;
  cta: string;
  detailedDescription?: string;
  features?: string[];
}

const BentoCard = ({
  name,
  className,
  background,
  Icon,
  description,
  href,
  cta,
  detailedDescription,
  features,
}: BentoCardProps) => {
  const [isOpen, setIsOpen] = useState(false);

  return (
    <>
      <div
        key={name}
        className={cn(
          "group relative col-span-1 flex flex-col justify-between rounded-xl",
          // Light styles - using app theme
          "bg-card border border-border/40",
          "[box-shadow:0_0_0_1px_rgba(0,0,0,.03),0_2px_4px_rgba(0,0,0,.05),0_12px_24px_rgba(0,0,0,.05)]",
          // Dark styles - using app theme
          "dark:bg-card dark:border-border/40",
          "dark:[box-shadow:0_-20px_80px_-20px_rgba(255,255,255,.05)_inset]",
          "transition-all duration-300 hover:border-primary/50 hover:shadow-lg hover:shadow-primary/10",
          className,
        )}
      >
        {/* Inner container - overflow-hidden for card shape */}
        <div className="relative h-full overflow-hidden rounded-xl">
          <div className="absolute inset-0">{background}</div>
          {/* Content container with extra top padding to prevent icon cutoff on hover */}
          <div className="pointer-events-none z-10 flex transform-gpu flex-col gap-1 p-6 pt-14 transition-all duration-300 group-hover:-translate-y-10">
            <Icon className="h-12 w-12 origin-left transform-gpu text-primary transition-all duration-300 ease-in-out group-hover:scale-75" />
            <h3 className="text-xl font-semibold text-foreground">
              {name}
            </h3>
            <p className="max-w-lg text-muted-foreground">{description}</p>
          </div>

          <div
            className={cn(
              "pointer-events-none absolute bottom-0 flex w-full translate-y-10 transform-gpu flex-row items-center p-4 opacity-0 transition-all duration-300 group-hover:translate-y-0 group-hover:opacity-100",
            )}
          >
            <Button 
              variant="ghost" 
              size="sm" 
              className="pointer-events-auto"
              onClick={() => setIsOpen(true)}
            >
              {cta}
              <ArrowRight className="ml-2 h-4 w-4" />
            </Button>
          </div>
          <div className="pointer-events-none absolute inset-0 transform-gpu transition-all duration-300 group-hover:bg-primary/[.03] dark:group-hover:bg-primary/10" />
        </div>
      </div>

      <Modal
        isOpen={isOpen}
        onClose={() => setIsOpen(false)}
        title=""
        subtitle=""
        type="blur"
        animationType="scale"
        borderBottom={false}
        showCloseButton
        showEscText={false}
        className="max-w-xl"
        disablePadding
      >
        <div className="relative overflow-hidden rounded-2xl">
          {/* Background gradient matching the card */}
          <div className="absolute inset-0 opacity-50">{background}</div>
          
          {/* Content */}
          <div className="relative z-10 p-8 pt-16">
            {/* Large Icon */}
            <div className="flex justify-center mb-6">
              <div className="flex h-24 w-24 items-center justify-center rounded-2xl bg-gradient-to-br from-primary/20 via-primary/10 to-secondary/10 dark:from-primary/30 dark:via-primary/20 dark:to-secondary/20 shadow-lg">
                <Icon className="h-12 w-12 text-primary" />
              </div>
            </div>

            {/* Title */}
            <h2 className="text-3xl font-bold text-center mb-3 text-foreground">
              {name}
            </h2>

            {/* Short description */}
            <p className="text-center text-muted-foreground mb-8 text-base">
              {description}
            </p>

            {/* Visual features grid - more impactful */}
            {features && features.length > 0 && (
              <div className="grid grid-cols-2 gap-3">
                {features.slice(0, 4).map((feature, index) => (
                  <div
                    key={index}
                    className="flex items-center gap-2 p-3 rounded-lg bg-card/50 border border-border/40 backdrop-blur-sm min-h-[60px]"
                  >
                    <Check className="h-5 w-5 text-primary shrink-0" />
                    <span className="text-sm text-foreground/90 leading-tight">
                      {feature}
                    </span>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
      </Modal>
    </>
  );
};

export { BentoCard, BentoGrid };
