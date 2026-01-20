import { useRef } from 'react';
import { ActionSearchBar } from '@/shared/components/ui/action-search-bar';

interface InlineSearchProps {
  className?: string;
  onFocusRef?: React.RefObject<HTMLInputElement>;
}

export function InlineSearch({ className, onFocusRef }: InlineSearchProps) {
  return (
    <ActionSearchBar 
      className={className} 
      inputRef={onFocusRef}
    />
  );
}
