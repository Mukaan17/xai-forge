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
      // Theme switching removed - always dark theme

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
    }),
    {
      name: 'ui-storage',
    }
  )
);

export default useUIStore;
