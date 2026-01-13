import * as React from "react"

interface SliderProps {
  value: number[];
  onValueChange: (value: number[]) => void;
  min: number;
  max: number;
  step: number;
  className?: string;
}

export function Slider({ value, onValueChange, min, max, step, className = "" }: SliderProps) {
  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    onValueChange([parseInt(e.target.value)]);
  };

  const percentage = ((value[0] - min) / (max - min)) * 100;

  return (
    <div className={`relative w-full ${className}`}>
      <input
        type="range"
        min={min}
        max={max}
        step={step}
        value={value[0]}
        onChange={handleChange}
        className="w-full h-2 bg-muted rounded-full appearance-none cursor-pointer slider"
        style={{
          background: `linear-gradient(to right, hsl(var(--primary)) 0%, hsl(var(--primary)) ${percentage}%, hsl(var(--muted)) ${percentage}%, hsl(var(--muted)) 100%)`,
        }}
      />
    </div>
  );
}
