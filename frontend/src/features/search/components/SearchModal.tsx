import { useState, useEffect, useRef } from 'react';
import { useQuery } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import { Search, Database, BrainCircuit, Target, X, Loader2 } from 'lucide-react';
import { Dialog, DialogContent } from '@/shared/components/ui/dialog';
import { Input } from '@/shared/components/ui/input';
import { searchApi, SearchResult } from '../api/searchApi';
import { cn } from '@/lib/utils';

interface SearchModalProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
}

export function SearchModal({ open, onOpenChange }: SearchModalProps) {
  const [query, setQuery] = useState('');
  const [selectedIndex, setSelectedIndex] = useState(0);
  const navigate = useNavigate();
  const inputRef = useRef<HTMLInputElement>(null);

  const { data: results, isLoading } = useQuery({
    queryKey: ['search', query],
    queryFn: () => searchApi.search(query, 10),
    enabled: query.length >= 2 && open,
    staleTime: 30000,
  });

  useEffect(() => {
    if (open) {
      // Focus input when modal opens
      setTimeout(() => {
        inputRef.current?.focus();
      }, 100);
      setQuery('');
      setSelectedIndex(0);
    }
  }, [open]);

  useEffect(() => {
    if (!open) {
      setQuery('');
      setSelectedIndex(0);
    }
  }, [open]);

  const allResults: SearchResult[] = results
    ? [...results.datasets, ...results.models, ...results.predictions]
    : [];

  const handleKeyDown = (e: React.KeyboardEvent) => {
    if (e.key === 'ArrowDown') {
      e.preventDefault();
      setSelectedIndex((prev) => Math.min(prev + 1, allResults.length - 1));
    } else if (e.key === 'ArrowUp') {
      e.preventDefault();
      setSelectedIndex((prev) => Math.max(prev - 1, 0));
    } else if (e.key === 'Enter' && allResults[selectedIndex]) {
      e.preventDefault();
      handleSelect(allResults[selectedIndex]);
    } else if (e.key === 'Escape') {
      onOpenChange(false);
    }
  };

  const handleSelect = (result: SearchResult) => {
    navigate(result.url);
    onOpenChange(false);
  };

  const getIcon = (type: string) => {
    switch (type) {
      case 'dataset':
        return Database;
      case 'model':
        return BrainCircuit;
      case 'prediction':
        return Target;
      default:
        return Search;
    }
  };

  const getTypeLabel = (type: string) => {
    switch (type) {
      case 'dataset':
        return 'Dataset';
      case 'model':
        return 'Model';
      case 'prediction':
        return 'Prediction';
      default:
        return type;
    }
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-[600px] p-0 gap-0">
        <div className="flex items-center border-b border-border px-4">
          <Search className="w-4 h-4 text-muted-foreground mr-2" />
          <Input
            ref={inputRef}
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            onKeyDown={handleKeyDown}
            placeholder="Search datasets, models, predictions..."
            className="border-0 focus-visible:ring-0 focus-visible:ring-offset-0 h-14 text-base"
            autoFocus
          />
          {query && (
            <button
              onClick={() => setQuery('')}
              className="ml-2 p-1 hover:bg-muted rounded"
            >
              <X className="w-4 h-4" />
            </button>
          )}
        </div>

        <div className="max-h-[400px] overflow-y-auto">
          {isLoading && query.length >= 2 ? (
            <div className="flex items-center justify-center py-8">
              <Loader2 className="w-5 h-5 animate-spin text-muted-foreground" />
            </div>
          ) : query.length < 2 ? (
            <div className="px-4 py-8 text-center text-muted-foreground">
              <p>Type at least 2 characters to search</p>
            </div>
          ) : allResults.length === 0 ? (
            <div className="px-4 py-8 text-center text-muted-foreground">
              <p>No results found for &quot;{query}&quot;</p>
            </div>
          ) : (
            <div className="py-2">
              {results && (
                <>
                  {results.datasets.length > 0 && (
                    <div className="px-4 py-2">
                      <p className="text-xs font-semibold text-muted-foreground uppercase mb-2">
                        Datasets ({results.datasets.length})
                      </p>
                      {results.datasets.map((result, idx) => {
                        const globalIdx = idx;
                        const Icon = getIcon(result.type);
                        return (
                          <div
                            key={result.id}
                            className={cn(
                              'flex items-center gap-3 px-3 py-2 rounded-lg cursor-pointer transition-colors',
                              selectedIndex === globalIdx
                                ? 'bg-primary/10 text-primary'
                                : 'hover:bg-muted/50'
                            )}
                            onClick={() => handleSelect(result)}
                            onMouseEnter={() => setSelectedIndex(globalIdx)}
                          >
                            <Icon className="w-4 h-4 flex-shrink-0" />
                            <div className="flex-1 min-w-0">
                              <p className="font-medium truncate">{result.name}</p>
                              <p className="text-xs text-muted-foreground truncate">
                                {result.description}
                              </p>
                            </div>
                            <span className="text-xs text-muted-foreground">
                              {getTypeLabel(result.type)}
                            </span>
                          </div>
                        );
                      })}
                    </div>
                  )}

                  {results.models.length > 0 && (
                    <div className="px-4 py-2">
                      <p className="text-xs font-semibold text-muted-foreground uppercase mb-2">
                        Models ({results.models.length})
                      </p>
                      {results.models.map((result, idx) => {
                        const globalIdx = results.datasets.length + idx;
                        const Icon = getIcon(result.type);
                        return (
                          <div
                            key={result.id}
                            className={cn(
                              'flex items-center gap-3 px-3 py-2 rounded-lg cursor-pointer transition-colors',
                              selectedIndex === globalIdx
                                ? 'bg-primary/10 text-primary'
                                : 'hover:bg-muted/50'
                            )}
                            onClick={() => handleSelect(result)}
                            onMouseEnter={() => setSelectedIndex(globalIdx)}
                          >
                            <Icon className="w-4 h-4 flex-shrink-0" />
                            <div className="flex-1 min-w-0">
                              <p className="font-medium truncate">{result.name}</p>
                              <p className="text-xs text-muted-foreground truncate">
                                {result.description}
                              </p>
                            </div>
                            <span className="text-xs text-muted-foreground">
                              {getTypeLabel(result.type)}
                            </span>
                          </div>
                        );
                      })}
                    </div>
                  )}

                  {results.predictions.length > 0 && (
                    <div className="px-4 py-2">
                      <p className="text-xs font-semibold text-muted-foreground uppercase mb-2">
                        Predictions ({results.predictions.length})
                      </p>
                      {results.predictions.map((result, idx) => {
                        const globalIdx = results.datasets.length + results.models.length + idx;
                        const Icon = getIcon(result.type);
                        return (
                          <div
                            key={result.id}
                            className={cn(
                              'flex items-center gap-3 px-3 py-2 rounded-lg cursor-pointer transition-colors',
                              selectedIndex === globalIdx
                                ? 'bg-primary/10 text-primary'
                                : 'hover:bg-muted/50'
                            )}
                            onClick={() => handleSelect(result)}
                            onMouseEnter={() => setSelectedIndex(globalIdx)}
                          >
                            <Icon className="w-4 h-4 flex-shrink-0" />
                            <div className="flex-1 min-w-0">
                              <p className="font-medium truncate">{result.name}</p>
                              <p className="text-xs text-muted-foreground truncate">
                                {result.description}
                              </p>
                            </div>
                            <span className="text-xs text-muted-foreground">
                              {getTypeLabel(result.type)}
                            </span>
                          </div>
                        );
                      })}
                    </div>
                  )}
                </>
              )}
            </div>
          )}
        </div>

        {allResults.length > 0 && (
          <div className="border-t border-border px-4 py-2 text-xs text-muted-foreground flex items-center justify-between">
            <span>
              {results?.totalCount} result{results?.totalCount !== 1 ? 's' : ''} found
            </span>
            <span className="flex items-center gap-4">
              <span>
                <kbd className="px-1.5 py-0.5 bg-muted rounded text-xs">↑↓</kbd> Navigate
              </span>
              <span>
                <kbd className="px-1.5 py-0.5 bg-muted rounded text-xs">Enter</kbd> Select
              </span>
              <span>
                <kbd className="px-1.5 py-0.5 bg-muted rounded text-xs">Esc</kbd> Close
              </span>
            </span>
          </div>
        )}
      </DialogContent>
    </Dialog>
  );
}
