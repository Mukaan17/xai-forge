import { HelpCircle, User, LayoutDashboard, Database, BrainCircuit, Layers, Target, History, Activity, Settings, LogOut, Shield, Bell, Palette, Plug, AlertTriangle } from 'lucide-react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { useState, useEffect, useRef } from 'react';
import { Button } from '@/shared/components/ui/button';
import { useAuth } from '@/features/auth/hooks/useAuth';
import { useAuthStore } from '@/features/auth/store/authStore';
import { InlineSearch } from '@/features/search/components/InlineSearch';
import { KeyboardShortcutsModal } from '@/shared/components/KeyboardShortcutsModal';
import { Breadcrumbs } from '@/shared/components/Breadcrumbs';
import { NotificationPopover } from '@/shared/components/ui/notification-popover';
import { HelpPopover } from '@/shared/components/ui/help-popover';
import { ProfilePopover } from '@/shared/components/ui/profile-popover';
import { SkipLink } from '@/shared/components/accessibility/SkipLink';

interface NavigationProps {
  children?: React.ReactNode;
}

export function Navigation({ children }: NavigationProps) {
  const location = useLocation();
  const navigate = useNavigate();
  const { logout, user } = useAuth();
  const { isAuthenticated } = useAuthStore();
  const [shortcutsOpen, setShortcutsOpen] = useState(false);
  const [gKeyPressed, setGKeyPressed] = useState(false);
  const searchInputRef = useRef<HTMLInputElement>(null);

  // Keyboard shortcut: Cmd+K or Ctrl+K to focus search
  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      if ((e.metaKey || e.ctrlKey) && e.key === 'k') {
        e.preventDefault();
        // Focus the search input
        if (searchInputRef.current) {
          searchInputRef.current.focus();
        }
      }
      if ((e.metaKey || e.ctrlKey) && e.key === '/') {
        e.preventDefault();
        setShortcutsOpen(true);
      }
      if (e.key === '?' && !e.metaKey && !e.ctrlKey) {
        e.preventDefault();
        setShortcutsOpen(true);
      }
    };

    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, []);

  // Handle 'g' key navigation shortcuts (g+d, g+m, etc.)
  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      const target = e.target as HTMLElement;
      if (
        target.tagName === 'INPUT' ||
        target.tagName === 'TEXTAREA' ||
        target.isContentEditable
      ) {
        return;
      }

      if (e.key === 'g' && !e.metaKey && !e.ctrlKey && !e.shiftKey && !e.altKey) {
        setGKeyPressed(true);
        setTimeout(() => setGKeyPressed(false), 1000); // Reset after 1 second
      } else if (gKeyPressed && !e.metaKey && !e.ctrlKey) {
        switch (e.key.toLowerCase()) {
          case 'd':
            e.preventDefault();
            navigate('/dashboard');
            setGKeyPressed(false);
            break;
          case 'm':
            e.preventDefault();
            navigate('/models');
            setGKeyPressed(false);
            break;
          case 'p':
            e.preventDefault();
            navigate('/predictions');
            setGKeyPressed(false);
            break;
          case 'h':
            e.preventDefault();
            navigate('/predictions/history');
            setGKeyPressed(false);
            break;
          case 'a':
            e.preventDefault();
            navigate('/activity');
            setGKeyPressed(false);
            break;
          case 's':
            e.preventDefault();
            navigate('/settings');
            setGKeyPressed(false);
            break;
          default:
            setGKeyPressed(false);
        }
      }
    };

    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [gKeyPressed, navigate]);

  const navItems = [
    { 
      id: 'dashboard', label: 'Dashboard', icon: LayoutDashboard, path: '/dashboard' },
    { id: 'datasets', label: 'Datasets', icon: Database, path: '/datasets' },
    { id: 'models', label: 'Models', icon: Layers, path: '/models' },
    { id: 'predictions', label: 'Predictions', icon: Target, path: '/predictions' },
    { id: 'history', label: 'History', icon: History, path: '/predictions/history' },
    { id: 'activity', label: 'Activity Log', icon: Activity, path: '/activity' },
  ];

  const settingsTabs = [
    { id: 'profile', label: 'Profile', icon: User },
    { id: 'security', label: 'Security', icon: Shield },
    { id: 'notifications', label: 'Notifications', icon: Bell },
    { id: 'appearance', label: 'Appearance', icon: Palette },
    { id: 'api', label: 'API', icon: Plug },
    { id: 'data', label: 'Data', icon: Database },
    { id: 'danger', label: 'Danger Zone', icon: AlertTriangle },
  ];

  const isSettingsPage = location.pathname === '/settings';
  const selectedSettingsTab = new URLSearchParams(location.search).get('tab') || 'profile';

  const handleSettingsTabClick = (tabId: string) => {
    navigate(`/settings?tab=${tabId}`);
  };

  const handleLogout = () => {
    logout();
  };

  return (
    <>
      {/* Top Navigation Bar */}
      <div className="fixed top-0 left-0 right-0 h-16 bg-card border-b border-border z-50 flex items-center px-4 sm:px-6">
        {/* Logo */}
        <Link to="/dashboard" className="flex items-center gap-2 sm:gap-3 flex-shrink-0 min-w-[140px] sm:min-w-[200px]">
          <div className="w-8 h-8 rounded-lg bg-gradient-to-br from-primary to-secondary flex items-center justify-center">
            <BrainCircuit className="w-5 h-5 text-white" />
          </div>
          <span className="font-semibold tracking-tight hidden sm:inline">XAI-Forge</span>
        </Link>

        {/* Global Search - Centered */}
        <div className="absolute left-1/2 -translate-x-1/2 w-full max-w-xl px-4">
          <InlineSearch onFocusRef={searchInputRef} />
        </div>

        {/* Right Actions */}
        <div className="flex items-center gap-2 sm:gap-4 flex-shrink-0 ml-auto">
          {/* Notifications */}
          <NotificationPopover />

          {/* Help */}
          <HelpPopover />

          {/* User Menu */}
          <ProfilePopover user={user} onLogout={handleLogout} />
        </div>
      </div>

      {/* Left Sidebar */}
      <aside className="fixed left-0 top-16 bottom-0 w-60 bg-card border-r border-border z-40 overflow-y-auto hidden lg:block">
        <nav className="p-4 space-y-2" role="navigation" aria-label={isSettingsPage ? "Settings navigation" : "Main navigation"}>
          {isSettingsPage ? (
            <>
              <div className="mb-4 pb-4 border-b border-border">
                <h2 className="px-4 text-sm font-semibold text-muted-foreground uppercase tracking-wider">Settings</h2>
              </div>
              {settingsTabs.map((tab) => {
                const Icon = tab.icon;
                const isActive = selectedSettingsTab === tab.id;
                
                return (
                  <button
                    key={tab.id}
                    onClick={() => handleSettingsTabClick(tab.id)}
                    className={`w-full flex items-center gap-3 px-4 py-3 rounded-lg transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 text-left ${
                      isActive
                        ? 'bg-primary/10 text-primary border-l-4 border-primary'
                        : 'text-muted-foreground hover:bg-muted/30 hover:text-foreground'
                    }`}
                    aria-current={isActive ? 'page' : undefined}
                    aria-label={`Navigate to ${tab.label}`}
                  >
                    <Icon className="w-5 h-5" aria-hidden="true" />
                    <span className="font-medium">{tab.label}</span>
                  </button>
                );
              })}
            </>
          ) : (
            navItems.map((item) => {
            const Icon = item.icon;
            // Check exact match first
            let isActive = location.pathname === item.path;
            
            // If not exact match, check if path starts with item.path, but only if there isn't a more specific route
            if (!isActive && location.pathname.startsWith(item.path + '/')) {
              // Check if there's a more specific nav item that also matches
              const hasMoreSpecificMatch = navItems.some(otherItem => 
                otherItem.path !== item.path && 
                location.pathname.startsWith(otherItem.path) &&
                otherItem.path.length > item.path.length
              );
              // Only mark as active if there's no more specific match
              isActive = !hasMoreSpecificMatch;
            }
            
            return (
              <Link
                key={item.id}
                to={item.path}
                className={`flex items-center gap-3 px-4 py-3 rounded-lg transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 ${
                  isActive
                    ? 'bg-primary/10 text-primary border-l-4 border-primary'
                    : 'text-muted-foreground hover:bg-muted/30 hover:text-foreground'
                }`}
                aria-current={isActive ? 'page' : undefined}
                aria-label={`Navigate to ${item.label}`}
              >
                <Icon className="w-5 h-5" aria-hidden="true" />
                <span className="font-medium">{item.label}</span>
              </Link>
            );
            })
          )}
        </nav>
      </aside>

      {/* Main Content */}
      <main 
        id="main-content"
        className="pt-16 lg:pl-60 min-h-screen"
        role="main"
        tabIndex={-1}
        aria-label="Main content"
      >
        <div className="p-4 sm:p-6 lg:p-8">
          <Breadcrumbs />
          {children}
        </div>
      </main>

      {/* Keyboard Shortcuts Modal */}
      <KeyboardShortcutsModal open={shortcutsOpen} onOpenChange={setShortcutsOpen} />

      {/* Notification Center */}
    </>
  );
}

