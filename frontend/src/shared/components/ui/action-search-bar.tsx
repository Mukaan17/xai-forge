"use client";

import { useState, useEffect, useRef } from "react";
import { Input } from "@/shared/components/ui/input";
import { motion, AnimatePresence } from "framer-motion";
import { useQuery } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import {
    Search,
    Send,
    Database,
    BrainCircuit,
    Target,
    Loader2,
} from "lucide-react";
import { searchApi, SearchResult } from '@/features/search/api/searchApi';
import { cn } from './utils';

function useDebounce<T>(value: T, delay: number = 500): T {
    const [debouncedValue, setDebouncedValue] = useState<T>(value);

    useEffect(() => {
        const timer = setTimeout(() => {
            setDebouncedValue(value);
        }, delay);

        return () => {
            clearTimeout(timer);
        };
    }, [value, delay]);

    return debouncedValue;
}

export interface Action {
    id: string;
    label: string;
    icon: React.ReactNode;
    description?: string;
    short?: string;
    end?: string;
    url?: string;
    type?: 'dataset' | 'model' | 'prediction';
}

interface ActionSearchBarProps {
    className?: string;
    inputRef?: React.RefObject<HTMLInputElement>;
    onKeyDown?: (e: React.KeyboardEvent) => void;
}

function ActionSearchBar({ className, inputRef, onKeyDown }: ActionSearchBarProps) {
    const [query, setQuery] = useState("");
    const [isFocused, setIsFocused] = useState(false);
    const [selectedIndex, setSelectedIndex] = useState(0);
    const navigate = useNavigate();
    const containerRef = useRef<HTMLDivElement>(null);
    const debouncedQuery = useDebounce(query, 200);

    const { data: results, isLoading } = useQuery({
        queryKey: ['search', debouncedQuery],
        queryFn: () => searchApi.search(debouncedQuery, 10),
        enabled: debouncedQuery.length >= 2,
        staleTime: 30000,
    });

    // Convert search results to actions
    const allActions: Action[] = results
        ? [
            ...results.datasets.map((result) => ({
                id: `dataset-${result.id}`,
                label: result.name,
                icon: <Database className="h-4 w-4 text-primary" />,
                description: result.description,
                end: "Dataset",
                url: result.url,
                type: 'dataset' as const,
            })),
            ...results.models.map((result) => ({
                id: `model-${result.id}`,
                label: result.name,
                icon: <BrainCircuit className="h-4 w-4 text-secondary" />,
                description: result.description,
                end: "Model",
                url: result.url,
                type: 'model' as const,
            })),
            ...results.predictions.map((result) => ({
                id: `prediction-${result.id}`,
                label: result.name,
                icon: <Target className="h-4 w-4 text-accent" />,
                description: result.description,
                end: "Prediction",
                url: result.url,
                type: 'prediction' as const,
            })),
        ]
        : [];

    // Close dropdown when clicking outside
    useEffect(() => {
        const handleClickOutside = (event: MouseEvent) => {
            if (containerRef.current && !containerRef.current.contains(event.target as Node)) {
                setIsFocused(false);
            }
        };

        if (isFocused) {
            document.addEventListener('mousedown', handleClickOutside);
            return () => document.removeEventListener('mousedown', handleClickOutside);
        }
    }, [isFocused]);

    // Reset selected index when query changes
    useEffect(() => {
        setSelectedIndex(0);
    }, [query]);

    const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        setQuery(e.target.value);
        if (e.target.value.length >= 2) {
            setIsFocused(true);
        }
    };

    const handleKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
        // Call custom handler first if provided
        if (onKeyDown) {
            onKeyDown(e);
            if (e.defaultPrevented) {
                return;
            }
        }

        // Handle keyboard navigation
        if (allActions.length > 0 && isFocused) {
            if (e.key === 'ArrowDown') {
                e.preventDefault();
                setSelectedIndex((prev) => Math.min(prev + 1, allActions.length - 1));
            } else if (e.key === 'ArrowUp') {
                e.preventDefault();
                setSelectedIndex((prev) => Math.max(prev - 1, 0));
            } else if (e.key === 'Enter' && allActions[selectedIndex]) {
                e.preventDefault();
                handleSelect(allActions[selectedIndex]);
            } else if (e.key === 'Escape') {
                e.preventDefault();
                setIsFocused(false);
                setQuery('');
            }
        } else if (e.key === 'Escape') {
            e.preventDefault();
            setIsFocused(false);
            setQuery('');
        }
    };

    const handleSelect = (action: Action) => {
        if (action.url) {
            navigate(action.url);
            setQuery('');
            setIsFocused(false);
            setSelectedIndex(0);
        }
    };

    const handleFocus = () => {
        setIsFocused(true);
        setSelectedIndex(0);
    };

    const handleBlur = () => {
        // Delay to allow click events on results
        setTimeout(() => setIsFocused(false), 200);
    };

    const container = {
        hidden: { opacity: 0, height: 0 },
        show: {
            opacity: 1,
            height: "auto",
            transition: {
                height: {
                    duration: 0.4,
                },
                staggerChildren: 0.05,
            },
        },
        exit: {
            opacity: 0,
            height: 0,
            transition: {
                height: {
                    duration: 0.3,
                },
                opacity: {
                    duration: 0.2,
                },
            },
        },
    };

    const item = {
        hidden: { opacity: 0, y: 20 },
        show: {
            opacity: 1,
            y: 0,
            transition: {
                duration: 0.3,
            },
        },
        exit: {
            opacity: 0,
            y: -10,
            transition: {
                duration: 0.2,
            },
        },
    };

    const showResults = isFocused && (query.length >= 2 || allActions.length > 0);
    const hasResults = allActions.length > 0 && query.length >= 2;

    return (
        <div ref={containerRef} className={cn("relative w-full", className)}>
            <div className="relative">
                <Input
                    ref={inputRef}
                    type="text"
                    placeholder="Search datasets, models, predictions..."
                    value={query}
                    onChange={handleInputChange}
                    onFocus={handleFocus}
                    onBlur={handleBlur}
                    onKeyDown={handleKeyDown}
                    className="pl-3 pr-9 py-1.5 h-9 text-sm rounded-lg focus-visible:ring-offset-0 bg-card/80 border-border/50"
                />
                <div className="absolute right-3 top-1/2 -translate-y-1/2 h-4 w-4">
                    <AnimatePresence mode="popLayout">
                        {isLoading && query.length >= 2 ? (
                            <motion.div
                                key="loading"
                                initial={{ y: -20, opacity: 0 }}
                                animate={{ y: 0, opacity: 1 }}
                                exit={{ y: 20, opacity: 0 }}
                                transition={{ duration: 0.2 }}
                            >
                                <Loader2 className="w-4 h-4 text-muted-foreground animate-spin" />
                            </motion.div>
                        ) : query.length > 0 ? (
                            <motion.div
                                key="send"
                                initial={{ y: -20, opacity: 0 }}
                                animate={{ y: 0, opacity: 1 }}
                                exit={{ y: 20, opacity: 0 }}
                                transition={{ duration: 0.2 }}
                            >
                                <Send className="w-4 h-4 text-muted-foreground" />
                            </motion.div>
                        ) : (
                            <motion.div
                                key="search"
                                initial={{ y: -20, opacity: 0 }}
                                animate={{ y: 0, opacity: 1 }}
                                exit={{ y: 20, opacity: 0 }}
                                transition={{ duration: 0.2 }}
                            >
                                <Search className="w-4 h-4 text-muted-foreground" />
                            </motion.div>
                        )}
                    </AnimatePresence>
                </div>
            </div>

            <AnimatePresence>
                {showResults && (
                    <motion.div
                        className="absolute top-full left-0 right-0 mt-2 border rounded-md shadow-lg overflow-hidden bg-card border-border z-50 max-h-[400px] overflow-y-auto"
                        variants={container}
                        initial="hidden"
                        animate="show"
                        exit="exit"
                    >
                        {isLoading && query.length >= 2 ? (
                            <div className="flex items-center justify-center py-8">
                                <Loader2 className="w-5 h-5 animate-spin text-muted-foreground" />
                            </div>
                        ) : query.length < 2 ? (
                            <div className="px-4 py-8 text-center text-muted-foreground">
                                <p>Type at least 2 characters to search</p>
                            </div>
                        ) : allActions.length === 0 ? (
                            <div className="px-4 py-8 text-center text-muted-foreground">
                                <p>No results found for &quot;{query}&quot;</p>
                            </div>
                        ) : (
                            <>
                                <motion.ul>
                                    {allActions.map((action, index) => (
                                        <motion.li
                                            key={action.id}
                                            className={cn(
                                                "px-3 py-2 flex items-center justify-between cursor-pointer rounded-md transition-colors",
                                                selectedIndex === index
                                                    ? "bg-primary/10 text-primary"
                                                    : "hover:bg-muted/50"
                                            )}
                                            variants={item}
                                            layout
                                            onClick={() => handleSelect(action)}
                                            onMouseEnter={() => setSelectedIndex(index)}
                                        >
                                            <div className="flex items-center gap-2 flex-1 min-w-0">
                                                <span className="text-muted-foreground flex-shrink-0">
                                                    {action.icon}
                                                </span>
                                                <div className="flex-1 min-w-0">
                                                    <span className="text-sm font-medium text-foreground block truncate">
                                                        {action.label}
                                                    </span>
                                                    {action.description && (
                                                        <span className="text-xs text-muted-foreground block truncate">
                                                            {action.description}
                                                        </span>
                                                    )}
                                                </div>
                                            </div>
                                            <div className="flex items-center gap-2 ml-2 flex-shrink-0">
                                                {action.short && (
                                                    <span className="text-xs text-muted-foreground">
                                                        {action.short}
                                                    </span>
                                                )}
                                                {action.end && (
                                                    <span className="text-xs text-muted-foreground text-right">
                                                        {action.end}
                                                    </span>
                                                )}
                                            </div>
                                        </motion.li>
                                    ))}
                                </motion.ul>
                                {hasResults && (
                                    <div className="mt-2 px-3 py-2 border-t border-border bg-muted/30">
                                        <div className="flex items-center justify-between text-xs text-muted-foreground">
                                            <span>
                                                {results?.totalCount} result{results?.totalCount !== 1 ? 's' : ''} found
                                            </span>
                                            <div className="flex items-center gap-4">
                                                <span>
                                                    <kbd className="px-1.5 py-0.5 bg-background rounded text-xs border border-border">↑↓</kbd> Navigate
                                                </span>
                                                <span>
                                                    <kbd className="px-1.5 py-0.5 bg-background rounded text-xs border border-border">Enter</kbd> Select
                                                </span>
                                                <span>
                                                    <kbd className="px-1.5 py-0.5 bg-background rounded text-xs border border-border">Esc</kbd> Close
                                                </span>
                                            </div>
                                        </div>
                                    </div>
                                )}
                            </>
                        )}
                    </motion.div>
                )}
            </AnimatePresence>
        </div>
    );
}

export { ActionSearchBar, Action };
