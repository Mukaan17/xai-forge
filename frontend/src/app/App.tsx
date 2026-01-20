import * as React from 'react';
import { Suspense, lazy } from 'react';
import { QueryClientProvider } from '@tanstack/react-query';
import { BrowserRouter, Routes, Route, Navigate, useLocation, Outlet } from 'react-router-dom';
import { ToastProvider } from '@/shared/components/ui/toast';
import { queryClient } from '@/shared/lib/query/queryClient';
import { useAuthStore } from '@/features/auth/store/authStore';
import { Navigation } from '@/shared/components/layout/Navigation';
import { motion, AnimatePresence } from 'framer-motion';
import { useTheme } from '@/shared/hooks/useTheme';
import { Loader2 } from 'lucide-react';

// Lazy load pages for code splitting
const LoginPage = lazy(() => import('@/features/auth/pages/LoginPage').then(m => ({ default: m.LoginPage })));
const RegisterPage = lazy(() => import('@/features/auth/pages/RegisterPage').then(m => ({ default: m.RegisterPage })));
const ForgotPasswordPage = lazy(() => import('@/features/auth/pages/ForgotPasswordPage').then(m => ({ default: m.ForgotPasswordPage })));
const VerifyEmailPage = lazy(() => import('@/features/auth/pages/VerifyEmailPage').then(m => ({ default: m.VerifyEmailPage })));
const LandingPage = lazy(() => import('@/features/landing/pages/LandingPage').then(m => ({ default: m.LandingPage })));
const DashboardPage = lazy(() => import('@/features/dashboard/pages/DashboardPage').then(m => ({ default: m.DashboardPage })));
const DatasetsPage = lazy(() => import('@/features/datasets/pages/DatasetsPage').then(m => ({ default: m.DatasetsPage })));
const ModelsPage = lazy(() => import('@/features/models/pages/ModelsPage').then(m => ({ default: m.ModelsPage })));
const TrainModelPage = lazy(() => import('@/features/models/pages/TrainModelPage').then(m => ({ default: m.TrainModelPage })));
const ModelDetailsPage = lazy(() => import('@/features/models/pages/ModelDetailsPage').then(m => ({ default: m.ModelDetailsPage })));
const PredictionsPage = lazy(() => import('@/features/predictions/pages/PredictionsPage').then(m => ({ default: m.PredictionsPage })));
const HistoryPage = lazy(() => import('@/features/predictions/pages/HistoryPage').then(m => ({ default: m.HistoryPage })));
const ActivityLogPage = lazy(() => import('@/features/activity/pages/ActivityLogPage').then(m => ({ default: m.ActivityLogPage })));
const SettingsPage = lazy(() => import('@/features/settings/pages/SettingsPage').then(m => ({ default: m.SettingsPage })));
const NotFoundPage = lazy(() => import('@/features/error/pages/NotFoundPage').then(m => ({ default: m.NotFoundPage })));

// Loading fallback component
const PageLoader = () => (
  <div className="flex items-center justify-center min-h-screen">
    <Loader2 className="w-8 h-8 animate-spin text-primary" />
  </div>
);

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
  const animationDirectionRef = React.useRef(100);
  const heroExitDirectionRef = React.useRef<number | null>(null);
  const heroExitXRef = React.useRef<number>(-100); // Default: exit left
  
  // Initialize theme
  useTheme();
  
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
    
    // Check if navigating from hero to login/signup
    if (previousPath === '/' && (currentPath === '/login' || currentPath === '/register')) {
      // Store that hero should exit left when navigating to login/signup
      // This will be used when we're back on hero and navigating to login/signup again
      heroExitDirectionRef.current = -100;
      // Don't clear immediately - keep it for future navigations
      // Only clear when we're actually on hero and not navigating to login/signup
    } else if (currentPath === '/' && previousPath !== '/') {
      // We're back on hero - keep the ref if it was set, only clear if we came from somewhere other than login/signup
      if (previousPath !== '/login' && previousPath !== '/register') {
        heroExitDirectionRef.current = null;
      }
    } else if (currentPath !== '/' && currentPath !== '/login' && currentPath !== '/register') {
      // We're on a different page - clear the ref
      heroExitDirectionRef.current = null;
    }
    
    // Store previous path in sessionStorage for back navigation
    if (previousPath && previousPath !== currentPath) {
      sessionStorage.setItem('xai-forge-previous-path', previousPath);
    }
    
    previousPathnameRef.current = currentPath;
  }, [location.pathname]);
  
  // Set navigation flag when user is on any page other than hero
  // This helps detect when user navigates back to hero from another page
  // Set it synchronously to avoid race conditions
  if (location.pathname !== '/') {
    // User is on a page other than hero - set flag to indicate navigation happened
    // This flag will be checked by hero component when navigating back
    // Set it synchronously (not in useEffect) to ensure it's available immediately
    sessionStorage.setItem('xai-forge-navigated-to-hero', 'true');
  }
  
  // Also set it in useEffect as backup
  React.useEffect(() => {
    if (location.pathname !== '/') {
      sessionStorage.setItem('xai-forge-navigated-to-hero', 'true');
    }
    // Don't clear the flag when on hero - let hero component handle it after it's checked
    // This ensures smooth transitions when navigating back to hero
  }, [location.pathname]);

  // Check for back navigation and update animation direction
  React.useEffect(() => {
    const isBackNav = sessionStorage.getItem('xai-forge-navigating-back') === 'true';
    const isClosingToHero = sessionStorage.getItem('xai-forge-closing-to-hero') === 'true';
    const pathnameChanged = previousPathnameRef.current !== location.pathname;
    
    // Check if we're closing to hero (must be on hero page now and flag is set)
    if (isClosingToHero && location.pathname === '/') {
      // Get the pathname we're closing from (stored when close button was clicked)
      const closingFromPath = sessionStorage.getItem('xai-forge-closing-from-path');
      const isLoginOrSignup = closingFromPath === '/login' || closingFromPath === '/register';
      
      if (isLoginOrSignup) {
        // Closing login/signup pages - exit to right, hero enters from left
        animationDirectionRef.current = -100;
        setAnimationDirection(-100);
      } else {
        // Closing other pages - exit to left, hero enters from right
        animationDirectionRef.current = 100;
        setAnimationDirection(100);
      }
      // Clear the flags after animation
      setTimeout(() => {
        sessionStorage.removeItem('xai-forge-closing-to-hero');
        sessionStorage.removeItem('xai-forge-closing-from-path');
      }, 500);
    } else if (isBackNav && pathnameChanged) {
      // Back navigation - slide from left
      animationDirectionRef.current = -100;
      setAnimationDirection(-100);
      // Clear the flag after animation
      setTimeout(() => {
        sessionStorage.removeItem('xai-forge-navigating-back');
      }, 500);
    } else if (!isBackNav && pathnameChanged) {
      // Forward navigation - slide from right
      animationDirectionRef.current = 100;
      setAnimationDirection(100);
    }
    
    previousPathnameRef.current = location.pathname;
  }, [location.pathname]);
  
  // Read previous pathname BEFORE updating (to detect transitions)
  const previousPathname = previousPathnameRef.current;
  const currentPath = location.pathname;
  const previousPath = previousPathname;
  
  // Check if transitioning between dashboard and settings (special case - should animate)
  const isDashboardToSettings = previousPath === '/dashboard' && currentPath === '/settings';
  const isSettingsToDashboard = previousPath === '/settings' && currentPath === '/dashboard';
  const isDashboardSettingsTransition = isDashboardToSettings || isSettingsToDashboard;
  
  // Check if transitioning between hero and login/signup (special case - should animate)
  const isHeroToLogin = previousPath === '/' && (currentPath === '/login' || currentPath === '/register');
  const isLoginToHero = (previousPath === '/login' || previousPath === '/register') && currentPath === '/';
  const isHeroLoginTransition = isHeroToLogin || isLoginToHero;
  
  // Check if current and previous routes are both dashboard routes
  const isDashboardRoute = isDashboardRoutePath(currentPath);
  const wasDashboardRoute = isDashboardRoutePath(previousPath);
  
  // Only animate if transitioning between different route types (public <-> dashboard)
  // OR if transitioning between dashboard and settings (special case)
  // OR if transitioning between hero and login/signup (special case)
  // Don't animate when navigating within other dashboard routes
  const shouldAnimate = !(isDashboardRoute && wasDashboardRoute) || isDashboardSettingsTransition || isHeroLoginTransition;
  
  // Update previous pathname ref AFTER we've used it (for next render)
  React.useEffect(() => {
    if (location.pathname !== previousPathnameRef.current) {
      previousPathnameRef.current = location.pathname;
    }
  }, [location.pathname]);
  
  // Also update the other ref for compatibility with existing code
  React.useEffect(() => {
    previousPathForAnimationRef.current = location.pathname;
  }, [location.pathname]);
  
  // Compute animation values synchronously based on sessionStorage flags
  // Login/signup: always enter and exit from/to right (x: 100)
  // Hero: exit left when login/signup enter, enter from left when login/signup exit
  const animationValues = React.useMemo(() => {
    const isClosingToHero = sessionStorage.getItem('xai-forge-closing-to-hero') === 'true';
    const closingFromPath = sessionStorage.getItem('xai-forge-closing-from-path');
    const isLoginOrSignup = closingFromPath === '/login' || closingFromPath === '/register';
    const isCurrentlyOnLoginOrSignup = location.pathname === '/login' || location.pathname === '/register';
    const isCurrentlyOnHero = location.pathname === '/';
    
    // Hero <-> Login/Signup transitions (check this first, before general login/signup logic)
    const heroPrevPath = previousPathname;
    const isHeroToLogin = heroPrevPath === '/' && (location.pathname === '/login' || location.pathname === '/register');
    const isLoginToHero = (heroPrevPath === '/login' || heroPrevPath === '/register') && location.pathname === '/';
    
    // When navigating FROM hero TO login/signup
    if (isHeroToLogin) {
      // Login/signup enters from right, hero exits left (handled by hero's exitX when it was the current component)
      return {
        initialX: 100,  // Login/signup enters from right
        exitX: 100      // Login/signup will exit to right (when navigating back to hero)
      };
    }
    
    // When navigating FROM login/signup TO hero
    if (isLoginToHero) {
      // Hero enters from left (opposite of login/signup exit), login/signup exits right (handled when it was current)
      return {
        initialX: -100, // Hero enters from left
        exitX: -100     // Hero will exit to left (when navigating to login/signup)
      };
    }
    
    // Login/signup pages: always enter and exit from/to right
    if (isCurrentlyOnLoginOrSignup) {
      return {
        initialX: 100,  // Always enter from right
        exitX: 100      // Always exit to right (when navigating to hero)
      };
    }
    
    // When currently on hero (not in a transition)
    if (isCurrentlyOnHero) {
      // Hero should ALWAYS exit left when navigating to login/signup
      // This ensures consistency: hero exits left, login/signup enters from right
      heroExitXRef.current = -100;
      return {
        initialX: animationDirection,
        exitX: -100  // ALWAYS exit left when on hero (opposite of login/signup entry from right)
      };
    }
    
    if (isClosingToHero && !isLoginOrSignup) {
      // Other pages closing to hero: exit left, entry from right
      if (location.pathname === '/') {
        return {
          initialX: 100,
          exitX: -animationDirection
        };
      } else {
        return {
          initialX: animationDirection,
          exitX: -100
        };
      }
    }
    
    // Dashboard <-> Settings transitions (special handling)
    // Use the previousPathname from the outer scope (captured before ref update)
    const dashboardPrevPath = previousPathname;
    const isDashboardToSettings = dashboardPrevPath === '/dashboard' && location.pathname === '/settings';
    const isSettingsToDashboard = dashboardPrevPath === '/settings' && location.pathname === '/dashboard';
    
    // When we just navigated TO settings (from dashboard)
    if (isDashboardToSettings) {
      return {
        initialX: 100,  // Settings enters from right
        exitX: 100      // Settings will exit to right (when navigating away from settings)
      };
    }
    
    // When we just navigated TO dashboard (from settings)
    if (isSettingsToDashboard) {
      return {
        initialX: -100, // Dashboard enters from left
        exitX: -100    // Dashboard will exit to left (when navigating away from dashboard)
      };
    }
    
    // When currently on dashboard (not in a transition)
    if (location.pathname === '/dashboard') {
      return {
        initialX: animationDirection,
        exitX: -100  // Dashboard exits to left (when navigating to settings)
      };
    }
    
    // When currently on settings (not in a transition)
    if (location.pathname === '/settings') {
      return {
        initialX: animationDirection,
        exitX: 100  // Settings exits to right (when navigating to dashboard)
      };
    }
    
    // Default: use animationDirection
    // But if we're coming from hero (previous path was '/'), default exit should be left
    if (previousPathname === '/') {
      // Coming from hero - exit left by default
      return {
        initialX: animationDirection,
        exitX: -100
      };
    }
    
    // Default: use animationDirection
    return {
      initialX: animationDirection,
      exitX: -animationDirection
    };
  }, [location.pathname, animationDirection, previousPathname]);


  // Use location.key for reliable tracking - React Router provides unique keys for each navigation
  // For dashboard/settings and hero/login transitions, ensure the key changes to trigger animation
  const navigationKey = React.useMemo(() => {
    if (!shouldAnimate) return 'dashboard-layout';
    // Always use location.key if available (React Router provides unique keys)
    // This is the most reliable way to ensure unique keys for each navigation
    if (location.key) {
      return `route-${location.key}`;
    }
    // Fallback: create unique key based on pathname and previous pathname
    // This ensures the key changes even when location.key is not available
    if (isDashboardSettingsTransition || isHeroLoginTransition) {
      return `${location.pathname}-from-${previousPathname}`;
    }
    // For all route changes, include previous pathname to ensure key uniqueness
    if (previousPathname !== location.pathname) {
      return `${location.pathname}-from-${previousPathname || 'initial'}`;
    }
    // Default: use pathname (shouldn't happen often, but fallback)
    return location.pathname;
  }, [shouldAnimate, isDashboardSettingsTransition, isHeroLoginTransition, location.pathname, location.key, previousPathname]);
  
  return (
    <AnimatePresence mode="wait" onExitComplete={() => {}}>
      <motion.div
        key={navigationKey}
        initial={shouldAnimate ? { opacity: 0, x: animationValues.initialX } : false}
        animate={{ opacity: 1, x: 0 }}
        exit={shouldAnimate ? { opacity: 0, x: animationValues.exitX } : false}
        transition={shouldAnimate ? { 
          duration: 0.35, 
          ease: [0.22, 1, 0.36, 1],
          opacity: { duration: 0.3 }
        } : { duration: 0 }}
        className="w-full"
        style={{ 
          backgroundColor: 'var(--color-background, #0f0f1a)',
          minHeight: '100vh',
          willChange: shouldAnimate ? 'transform, opacity' : 'auto',
          pointerEvents: 'auto'
        }}
      >
        <Routes>
          <Route 
            path="/" 
            element={
              <PublicRoute>
                <Suspense fallback={<PageLoader />}>
                  <LandingPage />
                </Suspense>
              </PublicRoute>
            } 
          />
          <Route 
            path="/login" 
            element={
              <PublicRoute>
                <Suspense fallback={<PageLoader />}>
                  <LoginPage />
                </Suspense>
              </PublicRoute>
            } 
          />
        <Route 
          path="/register" 
          element={
            <PublicRoute>
              <Suspense fallback={<PageLoader />}>
                <RegisterPage />
              </Suspense>
            </PublicRoute>
          } 
        />
        <Route 
          path="/forgot-password" 
          element={
            <PublicRoute>
              <Suspense fallback={<PageLoader />}>
                <ForgotPasswordPage />
              </Suspense>
            </PublicRoute>
          } 
        />
        <Route 
          path="/verify-email" 
          element={
            <PublicRoute>
              <Suspense fallback={<PageLoader />}>
                <VerifyEmailPage />
              </Suspense>
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
        <Route path="/dashboard" element={
          <Suspense fallback={<PageLoader />}>
            <DashboardPage />
          </Suspense>
        } />
        <Route path="/datasets" element={
          <Suspense fallback={<PageLoader />}>
            <DatasetsPage />
          </Suspense>
        } />
        <Route path="/datasets/*" element={
          <Suspense fallback={<PageLoader />}>
            <DatasetsPage />
          </Suspense>
        } />
        <Route path="/models" element={
          <Suspense fallback={<PageLoader />}>
            <ModelsPage />
          </Suspense>
        } />
        <Route path="/models/train" element={
          <Suspense fallback={<PageLoader />}>
            <TrainModelPage />
          </Suspense>
        } />
        <Route path="/models/:id" element={
          <Suspense fallback={<PageLoader />}>
            <ModelDetailsPage />
          </Suspense>
        } />
        <Route path="/models/*" element={
          <Suspense fallback={<PageLoader />}>
            <ModelsPage />
          </Suspense>
        } />
        <Route path="/predictions" element={
          <Suspense fallback={<PageLoader />}>
            <PredictionsPage />
          </Suspense>
        } />
        <Route path="/predictions/history" element={
          <Suspense fallback={<PageLoader />}>
            <HistoryPage />
          </Suspense>
        } />
        <Route path="/activity" element={
          <Suspense fallback={<PageLoader />}>
            <ActivityLogPage />
          </Suspense>
        } />
        <Route path="/settings" element={
          <Suspense fallback={<PageLoader />}>
            <SettingsPage />
          </Suspense>
        } />
      </Route>
      <Route
        path="*"
        element={
          <Suspense fallback={<PageLoader />}>
            <NotFoundPage />
          </Suspense>
        }
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
