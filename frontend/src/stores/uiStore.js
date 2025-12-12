/**
 * UI state store using Zustand
 */
import { create } from 'zustand';
import { persist } from 'zustand/middleware';

const useUIStore = create(
  persist(
    (set) => ({
      // State
      sidebarCollapsed: false,
      notificationsPanelOpen: false,
      theme: 'light', // 'light' | 'dark' | 'system'

      // Actions
      toggleSidebar: () => {
        set((state) => ({ sidebarCollapsed: !state.sidebarCollapsed }));
      },

      setSidebarCollapsed: (collapsed) => {
        set({ sidebarCollapsed: collapsed });
      },

      toggleNotificationsPanel: () => {
        set((state) => ({ notificationsPanelOpen: !state.notificationsPanelOpen }));
      },

      setNotificationsPanelOpen: (open) => {
        set({ notificationsPanelOpen: open });
      },

      setTheme: (theme) => {
        set({ theme });
        // Apply theme to document
        if (theme === 'dark') {
          document.documentElement.classList.add('dark');
        } else {
          document.documentElement.classList.remove('dark');
        }
      },
    }),
    {
      name: 'ui-storage',
    }
  )
);

export default useUIStore;
