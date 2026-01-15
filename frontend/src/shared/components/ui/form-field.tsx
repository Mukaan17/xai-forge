import { ReactNode } from 'react';
import { Label } from './label';
import { Input } from './input';
import { cn } from '@/lib/utils';
import { AlertCircle } from 'lucide-react';

interface FormFieldProps {
  label: string;
  name: string;
  value: string;
  onChange: (value: string) => void;
  onBlur?: () => void;
  type?: string;
  placeholder?: string;
  error?: string | null;
  required?: boolean;
  disabled?: boolean;
  className?: string;
  children?: ReactNode;
}

export function FormField({
  label,
  name,
  value,
  onChange,
  onBlur,
  type = 'text',
  placeholder,
  error,
  required = false,
  disabled = false,
  className,
  children,
}: FormFieldProps) {
  return (
    <div className={cn('space-y-2', className)}>
      <Label htmlFor={name} className={cn(error && 'text-destructive')}>
        {label}
        {required && (
          <span className="text-destructive ml-1" aria-label="required field" aria-hidden="false">*</span>
        )}
      </Label>
      {children || (
        <Input
          id={name}
          name={name}
          type={type}
          value={value}
          onChange={(e) => onChange(e.target.value)}
          onBlur={onBlur}
          placeholder={placeholder}
          disabled={disabled}
          className={cn(
            error && 'border-destructive focus-visible:ring-destructive'
          )}
          aria-invalid={error ? "true" : "false"}
          aria-describedby={error ? `${name}-error` : undefined}
          aria-required={required}
        />
      )}
      {error && (
        <div
          id={`${name}-error`}
          className="flex items-center gap-1.5 text-sm text-destructive"
          role="alert"
          aria-live="polite"
          aria-atomic="true"
        >
          <AlertCircle className="w-4 h-4" aria-hidden="true" />
          <span>{error}</span>
        </div>
      )}
    </div>
  );
}
