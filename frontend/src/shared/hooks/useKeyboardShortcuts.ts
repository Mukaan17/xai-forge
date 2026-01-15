import { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';

interface KeyboardShortcut {
  key: string;
  ctrlOrCmd?: boolean;
  shift?: boolean;
  alt?: boolean;
  action: () => void;
  description: string;
}

export function useKeyboardShortcuts(shortcuts: KeyboardShortcut[]) {
  const navigate = useNavigate();

  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      // Don't trigger shortcuts when user is typing in inputs, textareas, or contenteditable elements
      const target = e.target as HTMLElement;
      if (
        target.tagName === 'INPUT' ||
        target.tagName === 'TEXTAREA' ||
        target.isContentEditable
      ) {
        // Allow Cmd+K / Ctrl+K even in inputs (for search)
        if ((e.metaKey || e.ctrlKey) && e.key === 'k') {
          // This will be handled by the search modal
          return;
        }
        return;
      }

      for (const shortcut of shortcuts) {
        const matchesKey = e.key.toLowerCase() === shortcut.key.toLowerCase();
        const matchesCtrl = shortcut.ctrlOrCmd
          ? e.metaKey || e.ctrlKey
          : !e.metaKey && !e.ctrlKey;
        const matchesShift = shortcut.shift ? e.shiftKey : !e.shiftKey;
        const matchesAlt = shortcut.alt ? e.altKey : !e.altKey;

        if (matchesKey && matchesCtrl && matchesShift && matchesAlt) {
          e.preventDefault();
          shortcut.action();
          break;
        }
      }
    };

    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [shortcuts, navigate]);
}
