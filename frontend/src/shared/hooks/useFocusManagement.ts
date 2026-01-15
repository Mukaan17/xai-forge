import { useEffect, useRef } from 'react';

/**
 * Hook for managing focus in modals and dialogs
 * Traps focus within the element and restores focus on close
 * 
 * @since 1.0.0
 */
export function useFocusManagement(isOpen: boolean, autoFocus?: boolean) {
  const containerRef = useRef<HTMLElement>(null);
  const previousFocusRef = useRef<HTMLElement | null>(null);

  useEffect(() => {
    if (!isOpen) return;

    // Store the previously focused element
    previousFocusRef.current = document.activeElement as HTMLElement;

    // Focus the container or first focusable element
    if (containerRef.current) {
      if (autoFocus) {
        const firstFocusable = containerRef.current.querySelector(
          'button, [href], input, select, textarea, [tabindex]:not([tabindex="-1"])'
        ) as HTMLElement;
        
        if (firstFocusable) {
          firstFocusable.focus();
        } else {
          containerRef.current.focus();
        }
      }

      // Trap focus within the container
      const handleTab = (e: KeyboardEvent) => {
        if (e.key !== 'Tab') return;

        const focusableElements = containerRef.current?.querySelectorAll(
          'button, [href], input, select, textarea, [tabindex]:not([tabindex="-1"])'
        );

        if (!focusableElements || focusableElements.length === 0) return;

        const firstElement = focusableElements[0] as HTMLElement;
        const lastElement = focusableElements[focusableElements.length - 1] as HTMLElement;

        if (e.shiftKey) {
          // Shift + Tab
          if (document.activeElement === firstElement) {
            e.preventDefault();
            lastElement.focus();
          }
        } else {
          // Tab
          if (document.activeElement === lastElement) {
            e.preventDefault();
            firstElement.focus();
          }
        }
      };

      document.addEventListener('keydown', handleTab);
      return () => {
        document.removeEventListener('keydown', handleTab);
        // Restore focus when closing
        if (previousFocusRef.current) {
          previousFocusRef.current.focus();
        }
      };
    }
  }, [isOpen, autoFocus]);

  return containerRef;
}
