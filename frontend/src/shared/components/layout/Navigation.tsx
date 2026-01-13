import { Bell, Search, HelpCircle, User, LayoutDashboard, Database, BrainCircuit, Layers, Target, History, Activity, Settings, LogOut } from 'lucide-react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
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

interface NavigationProps {
  children?: React.ReactNode;
}

export function Navigation({ children }: NavigationProps) {
  const location = useLocation();
  const navigate = useNavigate();
  const { logout, user } = useAuth();
  const { isAuthenticated } = useAuthStore();

  const navItems = [
    { id: 'dashboard', label: 'Dashboard', icon: LayoutDashboard, path: '/dashboard' },
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
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-muted-foreground" />
          <Input
            placeholder="Search datasets, models, predictions..."
            className="pl-10 bg-background border-border/50 focus-visible:ring-primary w-full"
          />
        </div>

        {/* Right Actions */}
        <div className="flex items-center gap-2 sm:gap-4 flex-shrink-0 ml-auto">
          {/* Notifications */}
          <DropdownMenu>
            <DropdownMenuTrigger asChild>
              <Button variant="ghost" size="icon" className="relative">
                <Bell className="w-5 h-5" />
                <Badge className="absolute -top-1 -right-1 w-5 h-5 flex items-center justify-center p-0 bg-destructive border-0 text-destructive-foreground pointer-events-none">
                  0
                </Badge>
              </Button>
            </DropdownMenuTrigger>
            <DropdownMenuContent align="end" alignOffset={0} sideOffset={8} className="w-80 z-[100]">
              <div className="px-2 py-2 border-b border-border mb-2">
                <p className="font-medium">Notifications</p>
              </div>
              <div className="px-2 py-4 text-center text-muted-foreground text-sm">
                No new notifications
              </div>
            </DropdownMenuContent>
          </DropdownMenu>

          {/* Help */}
          <DropdownMenu>
            <DropdownMenuTrigger asChild>
              <Button variant="ghost" size="icon">
                <HelpCircle className="w-5 h-5" />
              </Button>
            </DropdownMenuTrigger>
            <DropdownMenuContent align="end" alignOffset={0} sideOffset={8} className="w-64 z-[100]">
              <div className="px-2 py-2 border-b border-border mb-2">
                <p className="font-medium">Help & Support</p>
              </div>
              <DropdownMenuItem asChild>
                <a href="https://docs.xai-forge.com" target="_blank" rel="noopener noreferrer" className="cursor-pointer w-full">
                  Documentation
                </a>
              </DropdownMenuItem>
              <DropdownMenuItem asChild>
                <a href="https://github.com/xai-forge/support" target="_blank" rel="noopener noreferrer" className="cursor-pointer w-full">
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
      <div className="fixed left-0 top-16 bottom-0 w-60 bg-card border-r border-border z-40 overflow-y-auto hidden lg:block">
        <nav className="p-4 space-y-2">
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
                className={`flex items-center gap-3 px-4 py-3 rounded-lg transition-colors ${
                  isActive
                    ? 'bg-primary/10 text-primary border-l-4 border-primary'
                    : 'text-muted-foreground hover:bg-muted/30 hover:text-foreground'
                }`}
              >
                <Icon className="w-5 h-5" />
                <span className="font-medium">{item.label}</span>
              </Link>
            );
          })}
        </nav>
      </div>

      {/* Main Content */}
      <div className="pt-16 lg:pl-60 min-h-screen">
        {children}
      </div>
    </>
  );
}

