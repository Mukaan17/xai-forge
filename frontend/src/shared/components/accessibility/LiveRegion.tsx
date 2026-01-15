import { useEffect, useRef } from 'react';

interface LiveRegionProps {
  message: string;
  priority?: 'polite' | 'assertive';
  'aria-live'?: 'polite' | 'assertive' | 'off';
}

/**
 * ARIA live region component for screen reader announcements
 * 
 * @since 1.0.0
 */
export function LiveRegion({ 
  message, 
  priority = 'polite',
  'aria-live': ariaLive = priority 
}: LiveRegionProps) {
  const regionRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (message && regionRef.current) {
      // Clear previous message
      regionRef.current.textContent = '';
      // Set new message after a brief delay to ensure screen readers announce it
      setTimeout(() => {
        if (regionRef.current) {
          regionRef.current.textContent = message;
        }
      }, 100);
    }
  }, [message]);

  return (
    <div
      ref={regionRef}
      role="status"
      aria-live={ariaLive}
      aria-atomic="true"
      className="sr-only"
    />
  );
}
