import { Bell, Search, HelpCircle, User, LayoutDashboard, Database, BrainCircuit, Layers, Target, History, Activity, Settings, LogOut } from 'lucide-react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { useState, useEffect } from 'react';
import { Button } from '@/shared/components/ui/button';
import { Input } from '@/shared/components/ui/input';
import { Badge } from '@/shared/components/ui/badge';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '@/shared/components/ui/dropdown-menu';
import { useAuth } from '@/features/auth/hooks/useAuth';
import { useAuthStore } from '@/features/auth/store/authStore';
import { SearchModal } from '@/features/search/components/SearchModal';
import { KeyboardShortcutsModal } from '@/shared/components/KeyboardShortcutsModal';
import { Breadcrumbs } from '@/shared/components/Breadcrumbs';
import { NotificationCenter } from '@/features/notifications/components/NotificationCenter';
import { useQuery } from '@tanstack/react-query';
import { notificationsApi } from '@/features/notifications/api/notificationsApi';
import { SkipLink } from '@/shared/components/accessibility/SkipLink';

interface NavigationProps {
  children?: React.ReactNode;
}

export function Navigation({ children }: NavigationProps) {
  const location = useLocation();
  const navigate = useNavigate();
  const { logout, user } = useAuth();
  const { isAuthenticated } = useAuthStore();
  const [searchOpen, setSearchOpen] = useState(false);
  const [shortcutsOpen, setShortcutsOpen] = useState(false);
  const [notificationsOpen, setNotificationsOpen] = useState(false);
  const [gKeyPressed, setGKeyPressed] = useState(false);

  // Fetch unread notification count
  const { data: unreadCountData } = useQuery({
    queryKey: ['notifications', 'unread-count'],
    queryFn: () => notificationsApi.getUnreadCount(),
    refetchInterval: 30000, // Refetch every 30 seconds
    enabled: isAuthenticated,
  });

  const unreadCount = unreadCountData?.count || 0;

  // Keyboard shortcut: Cmd+K or Ctrl+K to open search
  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      if ((e.metaKey || e.ctrlKey) && e.key === 'k') {
        e.preventDefault();
        setSearchOpen(true);
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

  const handleLogout = () => {
    logout();
  };

  return (
    <>
      {/* Top Navigation Bar */}
      <div className="fixed top-0 left-0 right-0 h-16 bg-card border-b border-border z-50 flex items-center px-4 sm:px-6 gap-3 sm:gap-6">
        {/* Logo */}
        <Link to="/dashboard" className="flex items-center gap-2 sm:gap-3 flex-shrink-0 min-w-[140px] sm:min-w-[200px]">
          <div className="w-8 h-8 rounded-lg bg-gradient-to-br from-primary to-secondary flex items-center justify-center">
            <BrainCircuit className="w-5 h-5 text-white" />
          </div>
          <span className="font-semibold tracking-tight hidden sm:inline">XAI-Forge</span>
        </Link>

        {/* Global Search */}
        <div className="flex-1 max-w-xl relative min-w-0">
          <button
            onClick={() => setSearchOpen(true)}
            className="w-full flex items-center gap-3 px-3 py-2 rounded-lg border border-border/50 bg-background hover:bg-muted/50 transition-colors text-left"
            aria-label="Search datasets, models, predictions (Press Cmd+K or Ctrl+K)"
            aria-keyshortcuts="Meta+K Ctrl+K"
          >
            <Search className="w-4 h-4 text-muted-foreground flex-shrink-0" aria-hidden="true" />
            <span className="text-sm text-muted-foreground flex-1">Search datasets, models, predictions...</span>
            <kbd className="hidden sm:inline-flex h-5 select-none items-center gap-1 rounded border bg-muted px-1.5 font-mono text-[10px] font-medium text-muted-foreground opacity-100" aria-hidden="true">
              <span className="text-xs">⌘</span>K
            </kbd>
          </button>
        </div>

        {/* Right Actions */}
        <div className="flex items-center gap-2 sm:gap-4 flex-shrink-0 ml-auto">
          {/* Notifications */}
          <Button 
            variant="ghost" 
            size="icon" 
            className="relative"
            onClick={() => setNotificationsOpen(true)}
            aria-label={`Notifications${unreadCount > 0 ? `, ${unreadCount} unread` : ''}`}
            aria-describedby={unreadCount > 0 ? "notification-badge" : undefined}
          >
            <Bell className="w-5 h-5" aria-hidden="true" />
            {unreadCount > 0 && (
              <Badge 
                id="notification-badge"
                className="absolute -top-1 -right-1 w-5 h-5 flex items-center justify-center p-0 bg-destructive border-0 text-destructive-foreground pointer-events-none text-xs"
                aria-label={`${unreadCount} unread notifications`}
              >
                {unreadCount > 99 ? '99+' : unreadCount}
              </Badge>
            )}
          </Button>

          {/* Help */}
          <DropdownMenu>
            <DropdownMenuTrigger asChild>
              <Button 
                variant="ghost" 
                size="icon"
                aria-label="Help and support (Press ? or Cmd+/ or Ctrl+/)"
                aria-keyshortcuts="? Meta+/ Ctrl+/"
                aria-haspopup="true"
              >
                <HelpCircle className="w-5 h-5" aria-hidden="true" />
              </Button>
            </DropdownMenuTrigger>
            <DropdownMenuContent 
              align="end" 
              alignOffset={0} 
              sideOffset={8} 
              className="w-64 z-[100]"
              role="menu"
              aria-label="Help menu"
            >
              <div className="px-2 py-2 border-b border-border mb-2" role="none">
                <p className="font-medium">Help & Support</p>
              </div>
              <DropdownMenuItem asChild role="menuitem">
                <a 
                  href="https://docs.xai-forge.com" 
                  target="_blank" 
                  rel="noopener noreferrer" 
                  className="cursor-pointer w-full"
                  aria-label="Open documentation in new tab"
                >
                  Documentation
                </a>
              </DropdownMenuItem>
              <DropdownMenuItem asChild role="menuitem">
                <a 
                  href="https://github.com/xai-forge/support" 
                  target="_blank" 
                  rel="noopener noreferrer" 
                  className="cursor-pointer w-full"
                  aria-label="Open support page in new tab"
                >
                  Support
                </a>
              </DropdownMenuItem>
            </DropdownMenuContent>
          </DropdownMenu>

          {/* User Menu */}
          <DropdownMenu>
            <DropdownMenuTrigger asChild>
              <Button variant="ghost" size="icon" className="rounded-full">
                <div className="w-8 h-8 rounded-full bg-primary/10 border border-primary/20 flex items-center justify-center">
                  <User className="w-4 h-4 text-primary" />
                </div>
              </Button>
            </DropdownMenuTrigger>
            <DropdownMenuContent align="end" alignOffset={0} sideOffset={8} className="w-56 z-[100]">
              <div className="px-2 py-2 border-b border-border mb-2">
                <p className="font-medium">{user?.username || 'User'}</p>
                <p className="text-sm text-muted-foreground">{user?.email || ''}</p>
              </div>
              <DropdownMenuItem asChild>
                <Link to="/settings" className="cursor-pointer w-full">
                  <Settings className="w-4 h-4 mr-2" />
                  Settings
                </Link>
              </DropdownMenuItem>
              <DropdownMenuSeparator />
              <DropdownMenuItem onClick={handleLogout} className="cursor-pointer text-destructive focus:text-destructive">
                <LogOut className="w-4 h-4 mr-2" />
                Logout
              </DropdownMenuItem>
            </DropdownMenuContent>
          </DropdownMenu>
        </div>
      </div>

      {/* Left Sidebar */}
      <aside className="fixed left-0 top-16 bottom-0 w-60 bg-card border-r border-border z-40 overflow-y-auto hidden lg:block">
        <nav className="p-4 space-y-2" role="navigation" aria-label="Main navigation">
          {navItems.map((item) => {
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
          })}
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

      {/* Search Modal */}
      <SearchModal open={searchOpen} onOpenChange={setSearchOpen} />

      {/* Keyboard Shortcuts Modal */}
      <KeyboardShortcutsModal open={shortcutsOpen} onOpenChange={setShortcutsOpen} />

      {/* Notification Center */}
      {notificationsOpen && (
        <NotificationCenter onClose={() => setNotificationsOpen(false)} />
      )}
    </>
  );
}

