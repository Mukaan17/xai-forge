/**
 * Authentication store using Zustand
 */
import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import { authAPI } from '../api/auth';

const useAuthStore = create(
  persist(
    (set, get) => ({
      // State
      user: null,
      isAuthenticated: false,
      isLoading: false,
      error: null,
      token: null,

      // Actions
      setUser: (user) => {
        set({ user, isAuthenticated: !!user });
      },

      setToken: (token) => {
        set({ token });
        if (token) {
          localStorage.setItem('token', token);
        } else {
          localStorage.removeItem('token');
        }
      },

      login: async (credentials) => {
        set({ isLoading: true, error: null });
        try {
          const response = await authAPI.login(credentials);
          const { token, user } = response;
          
          set({
            user,
            token,
            isAuthenticated: true,
            isLoading: false,
            error: null,
          });
          
          if (token) {
            localStorage.setItem('token', token);
            localStorage.setItem('user', JSON.stringify(user));
          }
          
          return { success: true, user, token };
        } catch (error) {
          const errorMessage = error.response?.data?.message || error.message || 'Login failed';
          set({
            error: errorMessage,
            isLoading: false,
            isAuthenticated: false,
          });
          return { success: false, error: errorMessage };
        }
      },

      register: async (userData) => {
        set({ isLoading: true, error: null });
        try {
          const response = await authAPI.register(userData);
          const { token, user } = response;
          
          set({
            user,
            token,
            isAuthenticated: true,
            isLoading: false,
            error: null,
          });
          
          if (token) {
            localStorage.setItem('token', token);
            localStorage.setItem('user', JSON.stringify(user));
          }
          
          return { success: true, user, token };
        } catch (error) {
          const errorMessage = error.response?.data?.message || error.message || 'Registration failed';
          set({
            error: errorMessage,
            isLoading: false,
            isAuthenticated: false,
          });
          return { success: false, error: errorMessage };
        }
      },

      logout: async () => {
        try {
          await authAPI.logout();
        } catch (error) {
          // Ignore errors on logout
        } finally {
          set({
            user: null,
            token: null,
            isAuthenticated: false,
            error: null,
          });
          localStorage.removeItem('token');
          localStorage.removeItem('user');
        }
      },

      fetchUser: async () => {
        set({ isLoading: true, error: null });
        try {
          const user = await authAPI.getCurrentUser();
          set({
            user,
            isAuthenticated: true,
            isLoading: false,
            error: null,
          });
          localStorage.setItem('user', JSON.stringify(user));
          return user;
        } catch (error) {
          set({
            error: error.response?.data?.message || 'Failed to fetch user',
            isLoading: false,
            isAuthenticated: false,
          });
          // Clear invalid token
          localStorage.removeItem('token');
          localStorage.removeItem('user');
          throw error;
        }
      },

      clearError: () => {
        set({ error: null });
      },
    }),
    {
      name: 'auth-storage',
      partialize: (state) => ({
        user: state.user,
        token: state.token,
        isAuthenticated: state.isAuthenticated,
      }),
    }
  )
);

export default useAuthStore;
