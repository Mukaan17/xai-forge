import { Dialog, DialogContent, DialogHeader, DialogTitle } from '@/shared/components/ui/dialog';
import { Badge } from '@/shared/components/ui/badge';

interface KeyboardShortcutsModalProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
}

const shortcuts = [
  {
    category: 'Navigation',
    items: [
      { keys: ['g', 'd'], description: 'Go to Dashboard' },
      { keys: ['g', 'm'], description: 'Go to Models' },
      { keys: ['g', 'p'], description: 'Go to Predictions' },
      { keys: ['g', 'h'], description: 'Go to History' },
      { keys: ['g', 'a'], description: 'Go to Activity Log' },
      { keys: ['g', 's'], description: 'Go to Settings' },
    ],
  },
  {
    category: 'Actions',
    items: [
      { keys: ['⌘', 'K'], description: 'Open global search', mac: true, windows: 'Ctrl+K' },
      { keys: ['⌘', '/'], description: 'Show keyboard shortcuts', mac: true, windows: 'Ctrl+/' },
      { keys: ['?'], description: 'Show help overlay' },
    ],
  },
  {
    category: 'General',
    items: [
      { keys: ['Esc'], description: 'Close modal/dialog' },
      { keys: ['⌘', 'Enter'], description: 'Submit form', mac: true, windows: 'Ctrl+Enter' },
    ],
  },
];

export function KeyboardShortcutsModal({ open, onOpenChange }: KeyboardShortcutsModalProps) {
  const isMac = navigator.platform.toUpperCase().indexOf('MAC') >= 0;

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-[600px] max-h-[80vh] overflow-y-auto">
        <DialogHeader>
          <DialogTitle>Keyboard Shortcuts</DialogTitle>
        </DialogHeader>
        <div className="space-y-6 py-4">
          {shortcuts.map((category) => (
            <div key={category.category}>
              <h3 className="text-sm font-semibold mb-3 text-muted-foreground uppercase">
                {category.category}
              </h3>
              <div className="space-y-2">
                {category.items.map((item, idx) => (
                  <div
                    key={idx}
                    className="flex items-center justify-between py-2 border-b border-border/50 last:border-0"
                  >
                    <span className="text-sm">{item.description}</span>
                    <div className="flex items-center gap-1">
                      {item.keys.map((key, keyIdx) => {
                        // Handle multi-key sequences (like 'g' then 'd')
                        if (keyIdx > 0 && item.keys.length > 1) {
                          return (
                            <span key={keyIdx} className="text-xs text-muted-foreground mx-1">
                              then
                            </span>
                          );
                        }
                        // Handle platform-specific keys
                        if (key === '⌘' || key === 'Ctrl') {
                          const displayKey = isMac ? '⌘' : 'Ctrl';
                          return (
                            <Badge
                              key={keyIdx}
                              variant="outline"
                              className="font-mono text-xs px-2 py-0.5"
                            >
                              {displayKey}
                            </Badge>
                          );
                        }
                        return (
                          <Badge
                            key={keyIdx}
                            variant="outline"
                            className="font-mono text-xs px-2 py-0.5"
                          >
                            {key}
                          </Badge>
                        );
                      })}
                      {item.windows && !isMac && (
                        <span className="text-xs text-muted-foreground ml-2">
                          ({item.windows})
                        </span>
                      )}
                    </div>
                  </div>
                ))}
              </div>
            </div>
          ))}
        </div>
      </DialogContent>
    </Dialog>
  );
}
