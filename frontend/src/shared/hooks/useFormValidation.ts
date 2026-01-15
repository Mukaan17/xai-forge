import { useState, useCallback } from 'react';

export interface ValidationRule {
  validator: (value: string) => boolean;
  message: string;
}

export interface FieldValidation {
  rules: ValidationRule[];
  required?: boolean;
}

export interface FormValidationConfig {
  [fieldName: string]: FieldValidation;
}

export interface ValidationErrors {
  [fieldName: string]: string | null;
}

export interface UseFormValidationReturn {
  errors: ValidationErrors;
  validateField: (fieldName: string, value: string) => boolean;
  validateForm: (values: Record<string, string>) => boolean;
  clearError: (fieldName: string) => void;
  clearAllErrors: () => void;
}

export function useFormValidation(config: FormValidationConfig): UseFormValidationReturn {
  const [errors, setErrors] = useState<ValidationErrors>({});

  const validateField = useCallback(
    (fieldName: string, value: string): boolean => {
      const fieldConfig = config[fieldName];
      if (!fieldConfig) return true;

      // Check required
      if (fieldConfig.required && (!value || value.trim() === '')) {
        setErrors((prev) => ({
          ...prev,
          [fieldName]: `${fieldName} is required`,
        }));
        return false;
      }

      // Skip other validations if field is empty and not required
      if (!value || value.trim() === '') {
        setErrors((prev) => ({
          ...prev,
          [fieldName]: null,
        }));
        return true;
      }

      // Run validation rules
      for (const rule of fieldConfig.rules) {
        if (!rule.validator(value)) {
          setErrors((prev) => ({
            ...prev,
            [fieldName]: rule.message,
          }));
          return false;
        }
      }

      // All validations passed
      setErrors((prev) => ({
        ...prev,
        [fieldName]: null,
      }));
      return true;
    },
    [config]
  );

  const validateForm = useCallback(
    (values: Record<string, string>): boolean => {
      let isValid = true;
      const newErrors: ValidationErrors = {};

      for (const fieldName in config) {
        const value = values[fieldName] || '';
        const fieldValid = validateField(fieldName, value);
        if (!fieldValid) {
          isValid = false;
        }
      }

      return isValid;
    },
    [config, validateField]
  );

  const clearError = useCallback((fieldName: string) => {
    setErrors((prev) => ({
      ...prev,
      [fieldName]: null,
    }));
  }, []);

  const clearAllErrors = useCallback(() => {
    setErrors({});
  }, []);

  return {
    errors,
    validateField,
    validateForm,
    clearError,
    clearAllErrors,
  };
}

// Common validation rules
export const validationRules = {
  email: {
    validator: (value: string) => {
      const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
      return emailRegex.test(value);
    },
    message: 'Please enter a valid email address',
  },
  minLength: (min: number) => ({
    validator: (value: string) => value.length >= min,
    message: `Must be at least ${min} characters`,
  }),
  maxLength: (max: number) => ({
    validator: (value: string) => value.length <= max,
    message: `Must be no more than ${max} characters`,
  }),
  username: {
    validator: (value: string) => {
      const usernameRegex = /^[a-zA-Z0-9_]{3,50}$/;
      return usernameRegex.test(value);
    },
    message: 'Username must be 3-50 characters and contain only letters, numbers, and underscores',
  },
  password: {
    validator: (value: string) => {
      return value.length >= 6;
    },
    message: 'Password must be at least 6 characters',
  },
  strongPassword: {
    validator: (value: string) => {
      // At least 8 characters, one uppercase, one lowercase, one number
      const strongPasswordRegex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).{8,}$/;
      return strongPasswordRegex.test(value);
    },
    message: 'Password must be at least 8 characters with uppercase, lowercase, and number',
  },
  notEmpty: {
    validator: (value: string) => value.trim() !== '',
    message: 'This field cannot be empty',
  },
  number: {
    validator: (value: string) => !isNaN(Number(value)) && value.trim() !== '',
    message: 'Please enter a valid number',
  },
  positiveNumber: {
    validator: (value: string) => {
      const num = Number(value);
      return !isNaN(num) && num > 0;
    },
    message: 'Please enter a positive number',
  },
  range: (min: number, max: number) => ({
    validator: (value: string) => {
      const num = Number(value);
      return !isNaN(num) && num >= min && num <= max;
    },
    message: `Must be between ${min} and ${max}`,
  }),
};
