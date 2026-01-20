import { useEffect } from 'react';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import { settingsApi } from '@/features/settings/api/settingsApi';
import { useAuthStore } from '@/features/auth/store/authStore';

interface UserPreferences {
  accentColor?: string;
  notificationPreferences?: string;
}

/**
 * Hook for managing theme preferences
 * Always uses dark theme - theme switching has been removed
 */
export function useTheme() {
  const { isAuthenticated } = useAuthStore();
  const queryClient = useQueryClient();

  // Fetch user preferences (only accent color now, no theme)
  const { data: preferences } = useQuery<UserPreferences>({
    queryKey: ['user-preferences'],
    queryFn: async () => {
      try {
        // Try to get preferences from user profile endpoint
        // For now, return defaults - preferences will be loaded from user profile
        return { accentColor: '#00d9ff' };
      } catch (error) {
        console.error('Error fetching preferences:', error);
        return { accentColor: '#00d9ff' };
      }
    },
    enabled: isAuthenticated,
    staleTime: Infinity, // Preferences don't change often
    retry: false, // Don't retry on error to prevent blocking
  });

  // Always use dark theme
  const effectiveTheme = 'dark';

  // Apply dark theme to document (always dark)
  useEffect(() => {
    try {
      const root = document.documentElement;
      
      // Always apply dark theme
      root.classList.add('dark');
      root.classList.remove('light');

      // Apply accent color via CSS variable
      const accentColor = preferences?.accentColor || localStorage.getItem('xai-accent-color') || '#00d9ff';
      root.style.setProperty('--color-primary', accentColor);
      root.style.setProperty('--color-accent', accentColor);
      root.style.setProperty('--color-ring', accentColor);
      
      // Update primary foreground color for dark theme
      const primaryForeground = '#0f0f1a';
      root.style.setProperty('--color-primary-foreground', primaryForeground);
      root.style.setProperty('--color-accent-foreground', primaryForeground);
    } catch (error) {
      console.error('Error applying theme:', error);
    }
  }, [preferences?.accentColor, isAuthenticated]);

  const setAccentColor = async (color: string) => {
    // Save to localStorage immediately for instant feedback
    localStorage.setItem('xai-accent-color', color);
    
    // Update the query cache optimistically
    queryClient.setQueryData<UserPreferences>(['user-preferences'], (old) => ({
      ...old,
      accentColor: color,
    }));
    
    // Save to API if authenticated
    if (isAuthenticated) {
      try {
        await settingsApi.updatePreferences({ accentColor: color });
      } catch (error) {
        // If API call fails, revert the optimistic update
        console.error('Failed to save accent color:', error);
      }
    }
  };

  return {
    theme: 'dark',
    effectiveTheme: 'dark',
    accentColor: preferences?.accentColor || localStorage.getItem('xai-accent-color') || '#00d9ff',
    setAccentColor,
    isLoading: false,
  };
}
