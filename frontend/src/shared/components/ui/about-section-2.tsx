"use client";
import { TimelineContent } from "@/shared/components/ui/timeline-animation";
import { useRef } from "react";

export default function AboutSection2() {
  const heroRef = useRef<HTMLDivElement>(null);
  const revealVariants = {
    visible: (i: number) => ({
      y: 0,
      opacity: 1,
      filter: "blur(0px)",
      transition: {
        delay: i * 1.5,
        duration: 0.7,
      },
    }),
    hidden: {
      filter: "blur(10px)",
      y: 40,
      opacity: 0,
    },
  };
  const textVariants = {
    visible: (i: number) => ({
      filter: "blur(0px)",
      opacity: 1,
      transition: {
        delay: i * 0.3,
        duration: 0.7,
      },
    }),
    hidden: {
      filter: "blur(10px)",
      opacity: 0,
    },
  };
  return (
    <section id="mission" className="pt-32 pb-16 px-4 bg-background scroll-mt-20">
      <div className="max-w-6xl mx-auto" ref={heroRef}>
        <div className="flex flex-col lg:flex-row items-start gap-8">
          {/* Right side - Content */}
          <div className="flex-1">
            <TimelineContent
              as="h1"
              animationNum={0}
              timelineRef={heroRef}
              customVariants={revealVariants}
              className="sm:text-4xl text-2xl md:text-5xl !leading-[110%] font-semibold text-foreground mb-8"
            >
              We are{" "}
              <TimelineContent
                as="span"
                animationNum={1}
                timelineRef={heroRef}
                customVariants={textVariants}
                className="text-primary border-2 border-primary inline-block xl:h-16 border-dotted px-2 rounded-md"
              >
                rethinking
              </TimelineContent>{" "}
              machine learning to be more transparent and always you-first. Our
              goal is to continually raise the bar and{" "}
              <TimelineContent
                as="span"
                animationNum={2}
                timelineRef={heroRef}
                customVariants={textVariants}
                className="text-secondary border-2 border-secondary inline-block xl:h-16 border-dotted px-2 rounded-md"
              >
                demystify
              </TimelineContent>{" "}
              how AI decisions{" "}
              <TimelineContent
                as="span"
                animationNum={3}
                timelineRef={heroRef}
                customVariants={textVariants}
                className="text-primary border-2 border-primary inline-block xl:h-16 border-dotted px-2 rounded-md"
              >
                work for you.
              </TimelineContent>
            </TimelineContent>

            <div className="mt-12">
              <TimelineContent
                as="div"
                animationNum={4}
                timelineRef={heroRef}
                customVariants={textVariants}
                className="sm:text-xl text-xs"
              >
                <div className="font-medium text-foreground mb-1 capitalize">
                  We are XAI-Forge and we will
                </div>
                <div className="text-muted-foreground font-semibold uppercase">
                  empower your ML journey
                </div>
              </TimelineContent>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
}
