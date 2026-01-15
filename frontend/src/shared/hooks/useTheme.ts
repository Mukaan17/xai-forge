import { useEffect, useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { settingsApi } from '@/features/settings/api/settingsApi';
import { useAuthStore } from '@/features/auth/store/authStore';

interface UserPreferences {
  theme?: string;
  accentColor?: string;
  notificationPreferences?: string;
}

/**
 * Hook for managing theme preferences
 * Handles fetching, applying, and saving theme settings
 */
export function useTheme() {
  const { isAuthenticated } = useAuthStore();
  const queryClient = useQueryClient();
  const [systemTheme, setSystemTheme] = useState<'light' | 'dark'>(
    window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light'
  );

  // Fetch user preferences
  const { data: preferences } = useQuery<UserPreferences>({
    queryKey: ['user-preferences'],
    queryFn: async () => {
      try {
        // Try to get preferences from user profile endpoint
        // For now, return defaults - preferences will be loaded from user profile
        return { theme: 'dark', accentColor: '#00d9ff' };
      } catch {
        return { theme: 'dark', accentColor: '#00d9ff' };
      }
    },
    enabled: isAuthenticated,
    staleTime: Infinity, // Preferences don't change often
  });

  // Save preferences mutation
  const savePreferencesMutation = useMutation({
    mutationFn: async (prefs: { theme?: string; accentColor?: string; notificationPreferences?: string }) => {
      await settingsApi.updatePreferences(prefs);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['user-preferences'] });
    },
  });

  // Get effective theme (system, light, or dark)
  const effectiveTheme = preferences?.theme === 'system' ? systemTheme : (preferences?.theme || 'dark');

  // Apply theme to document
  useEffect(() => {
    if (!isAuthenticated) return;

    const root = document.documentElement;
    
    // Apply dark/light class
    if (effectiveTheme === 'dark') {
      root.classList.add('dark');
      root.classList.remove('light');
    } else {
      root.classList.add('light');
      root.classList.remove('dark');
    }

    // Apply accent color via CSS variable
    const accentColor = preferences?.accentColor || '#00d9ff';
    root.style.setProperty('--color-primary', accentColor);
    root.style.setProperty('--color-accent', accentColor);
    root.style.setProperty('--color-ring', accentColor);
    
    // Update primary foreground color based on theme
    const primaryForeground = effectiveTheme === 'dark' ? '#0f0f1a' : '#ffffff';
    root.style.setProperty('--color-primary-foreground', primaryForeground);
    root.style.setProperty('--color-accent-foreground', primaryForeground);
  }, [effectiveTheme, preferences?.accentColor, isAuthenticated]);

  // Listen to system theme changes
  useEffect(() => {
    if (!isAuthenticated) return;
    if (preferences?.theme !== 'system') return;

    const mediaQuery = window.matchMedia('(prefers-color-scheme: dark)');
    const handleChange = (e: MediaQueryListEvent) => {
      setSystemTheme(e.matches ? 'dark' : 'light');
    };

    mediaQuery.addEventListener('change', handleChange);
    return () => mediaQuery.removeEventListener('change', handleChange);
  }, [preferences?.theme, isAuthenticated]);

  const setTheme = (theme: 'light' | 'dark' | 'system') => {
    // Save to localStorage immediately for instant feedback
    localStorage.setItem('xai-theme', theme);
    savePreferencesMutation.mutate({ theme });
  };

  const setAccentColor = (color: string) => {
    // Save to localStorage immediately for instant feedback
    localStorage.setItem('xai-accent-color', color);
    savePreferencesMutation.mutate({ accentColor: color });
  };

  return {
    theme: preferences?.theme || 'dark',
    effectiveTheme,
    accentColor: preferences?.accentColor || '#00d9ff',
    setTheme,
    setAccentColor,
    isLoading: savePreferencesMutation.isPending,
  };
}
