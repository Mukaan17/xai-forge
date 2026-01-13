import * as React from 'react';
import { QueryClientProvider } from '@tanstack/react-query';
import { BrowserRouter, Routes, Route, Navigate, useLocation, Outlet } from 'react-router-dom';
import { ToastProvider } from '@/shared/components/ui/toast';
import { queryClient } from '@/shared/lib/query/queryClient';
import { useAuthStore } from '@/features/auth/store/authStore';
import { LoginPage } from '@/features/auth/pages/LoginPage';
import { RegisterPage } from '@/features/auth/pages/RegisterPage';
import { ForgotPasswordPage } from '@/features/auth/pages/ForgotPasswordPage';
import { LandingPage } from '@/features/landing/pages/LandingPage';
import { DashboardPage } from '@/features/dashboard/pages/DashboardPage';
import { DatasetsPage } from '@/features/datasets/pages/DatasetsPage';
import { ModelsPage } from '@/features/models/pages/ModelsPage';
import { PredictionsPage } from '@/features/predictions/pages/PredictionsPage';
import { HistoryPage } from '@/features/predictions/pages/HistoryPage';
import { ActivityLogPage } from '@/features/activity/pages/ActivityLogPage';
import { SettingsPage } from '@/features/settings/pages/SettingsPage';
import { NotFoundPage } from '@/features/error/pages/NotFoundPage';
import { Navigation } from '@/shared/components/layout/Navigation';
import { motion, AnimatePresence } from 'framer-motion';

function ProtectedRoute({ children }: { children: React.ReactNode }) {
  const { isAuthenticated } = useAuthStore();
  return isAuthenticated ? <>{children}</> : <Navigate to="/login" replace />;
}

function PublicRoute({ children }: { children: React.ReactNode }) {
  const { isAuthenticated } = useAuthStore();
  return !isAuthenticated ? <>{children}</> : <Navigate to="/dashboard" replace />;
}

function AppRoutes() {
  const location = useLocation();
  const previousPathnameRef = React.useRef<string>(location.pathname);
  const previousPathForAnimationRef = React.useRef<string>(location.pathname);
  const [animationDirection, setAnimationDirection] = React.useState(100);
  
  // Helper function to check if a path is a dashboard route
  const isDashboardRoutePath = (path: string) => {
    return path.startsWith('/dashboard') || 
           path.startsWith('/datasets') || 
           path.startsWith('/models') || 
           path.startsWith('/predictions') || 
           path.startsWith('/activity') || 
           path.startsWith('/settings');
  };
  
  // Track navigation history for back navigation
  React.useEffect(() => {
    const currentPath = location.pathname;
    const previousPath = previousPathnameRef.current;
    
    // Store previous path in sessionStorage for back navigation
    if (previousPath && previousPath !== currentPath) {
      sessionStorage.setItem('xai-forge-previous-path', previousPath);
    }
    
    previousPathnameRef.current = currentPath;
  }, [location.pathname]);
  
  // Set navigation flag when user is on any page other than hero
  // This helps detect when user navigates back to hero from another page
  React.useEffect(() => {
    if (location.pathname !== '/') {
      // User is on a page other than hero - set flag to indicate navigation happened
      sessionStorage.setItem('xai-forge-navigated-to-hero', 'true');
    } else {
      // User is on hero - don't clear the flag here, let hero component handle it
      // This ensures the flag persists until hero component checks it
    }
  }, [location.pathname]);

  // Check for back navigation and update animation direction
  React.useEffect(() => {
    const isBackNav = sessionStorage.getItem('xai-forge-navigating-back') === 'true';
    const isClosingToHero = sessionStorage.getItem('xai-forge-closing-to-hero') === 'true';
    const pathnameChanged = previousPathnameRef.current !== location.pathname;
    
    if (isClosingToHero && pathnameChanged) {
      // Closing login/signup pages - exit to left, hero enters from right
      setAnimationDirection(100);
      // Clear the flag after animation
      setTimeout(() => {
        sessionStorage.removeItem('xai-forge-closing-to-hero');
      }, 500);
    } else if (isBackNav && pathnameChanged) {
      // Back navigation - slide from left
      setAnimationDirection(-100);
      // Clear the flag after animation
      setTimeout(() => {
        sessionStorage.removeItem('xai-forge-navigating-back');
      }, 500);
    } else if (!isBackNav && pathnameChanged) {
      // Forward navigation - slide from right
      setAnimationDirection(100);
    }
    
    previousPathnameRef.current = location.pathname;
  }, [location.pathname]);
  
  // Check if current and previous routes are both dashboard routes
  // Use separate ref for animation check to avoid race conditions
  const isDashboardRoute = isDashboardRoutePath(location.pathname);
  const wasDashboardRoute = isDashboardRoutePath(previousPathForAnimationRef.current);
  
  // Only animate if transitioning between different route types (public <-> dashboard)
  // Don't animate when navigating within dashboard routes
  const shouldAnimate = !(isDashboardRoute && wasDashboardRoute);
  
  // Update the animation ref after determining if we should animate
  React.useEffect(() => {
    previousPathForAnimationRef.current = location.pathname;
  }, [location.pathname]);
  
  // Use location.key for reliable tracking - React Router provides unique keys for each navigation
  const navigationKey = shouldAnimate ? (location.key || `${location.pathname}-${Date.now()}`) : 'dashboard-layout';
  
  return (
    <AnimatePresence mode="wait" initial={false}>
      <motion.div
        key={navigationKey}
        initial={shouldAnimate ? { opacity: 0, x: animationDirection } : false}
        animate={shouldAnimate ? { opacity: 1, x: 0 } : { opacity: 1, x: 0 }}
        exit={shouldAnimate ? { opacity: 0, x: -animationDirection } : false}
        transition={shouldAnimate ? { duration: 0.4, ease: [0.22, 1, 0.36, 1] } : { duration: 0 }}
        className="w-full"
        style={{ 
          backgroundColor: 'var(--color-background, #0f0f1a)',
          minHeight: '100vh'
        }}
      >
        <Routes location={location}>
          <Route 
            path="/" 
            element={
              <PublicRoute>
                <LandingPage />
              </PublicRoute>
            } 
          />
          <Route 
            path="/login" 
            element={
              <PublicRoute>
                <LoginPage />
              </PublicRoute>
            } 
          />
        <Route 
          path="/register" 
          element={
            <PublicRoute>
              <RegisterPage />
            </PublicRoute>
          } 
        />
        <Route 
          path="/forgot-password" 
          element={
            <PublicRoute>
              <ForgotPasswordPage />
            </PublicRoute>
          } 
        />
      {/* Dashboard Layout - single Navigation wrapper for all dashboard routes */}
      <Route
        element={
          <ProtectedRoute>
            <Navigation>
              <Outlet />
            </Navigation>
          </ProtectedRoute>
        }
      >
        <Route path="/dashboard" element={<DashboardPage />} />
        <Route path="/datasets" element={<DatasetsPage />} />
        <Route path="/datasets/*" element={<DatasetsPage />} />
        <Route path="/models" element={<ModelsPage />} />
        <Route path="/models/*" element={<ModelsPage />} />
        <Route path="/predictions" element={<PredictionsPage />} />
        <Route path="/predictions/history" element={<HistoryPage />} />
        <Route path="/activity" element={<ActivityLogPage />} />
        <Route path="/settings" element={<SettingsPage />} />
      </Route>
      <Route
        path="*"
        element={<NotFoundPage />}
      />
        </Routes>
      </motion.div>
    </AnimatePresence>
  );
}

function App() {
  // Initialize navigation tracking on app mount
  React.useEffect(() => {
    // If we're not on the hero page initially, set the flag
    if (window.location.pathname !== '/') {
      sessionStorage.setItem('xai-forge-navigated-to-hero', 'true');
    }

    // Listen for popstate events (browser back/forward buttons)
    // This must be set up before React Router processes the navigation
    const handlePopState = (event: PopStateEvent) => {
      // Set flag when browser back/forward is used
      sessionStorage.setItem('xai-forge-navigating-back', 'true');
    };

    window.addEventListener('popstate', handlePopState);
    return () => {
      window.removeEventListener('popstate', handlePopState);
    };
  }, []);

  return (
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <ToastProvider position="top-right">
          <div className="min-h-screen bg-background">
            <AppRoutes />
          </div>
        </ToastProvider>
      </BrowserRouter>
    </QueryClientProvider>
  );
}

export default App;
