import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useAuthStore } from '../store/authStore';
import { authApi, LoginRequest, RegisterRequest } from '../api/authApi';
import { useNavigate } from 'react-router-dom';
import { toast } from '@/shared/lib/toast';

export function useAuth() {
  const { user, isAuthenticated, setAuth, logout: logoutStore } = useAuthStore();
  const queryClient = useQueryClient();
  const navigate = useNavigate();
  
  const loginMutation = useMutation({
    mutationFn: authApi.login,
    onSuccess: (data) => {
      localStorage.setItem('token', data.token);
      setAuth(data.user, data.token);
      toast.success('Welcome back!');
      navigate('/dashboard');
    },
    onError: (error: any) => {
      // Extract error message from API error if available
      const errorMessage = error?.message || error?.detail || 'Login failed. Please check your credentials.';
      toast.error(errorMessage);
    },
    retry: false, // Disable retry to prevent multiple error messages
  });
  
  const registerMutation = useMutation({
    mutationFn: authApi.register,
    onSuccess: (data) => {
      localStorage.setItem('token', data.token);
      setAuth(data.user, data.token);
      toast.success('Account created successfully! Please check your email to verify your account.');
      navigate('/dashboard');
    },
    onError: (error) => {
      toast.error('Registration failed. Please try again.');
    },
  });
  
  const { data: currentUser } = useQuery({
    queryKey: ['auth', 'me'],
    queryFn: authApi.getCurrentUser,
    enabled: isAuthenticated,
    retry: false,
  });
  
  const logout = () => {
    logoutStore();
    authApi.logout();
    queryClient.clear();
    navigate('/');
    toast.info('Logged out successfully');
  };
  
  return {
    user: currentUser || user,
    isAuthenticated: isAuthenticated && !!user,
    login: loginMutation.mutate,
    register: registerMutation.mutate,
    logout,
    isLoading: loginMutation.isPending || registerMutation.isPending,
  };
}

