import { Bell, Search, HelpCircle, User, ChevronLeft, LayoutDashboard, Database, BrainCircuit, Layers, Target, GitCompare, Activity, History, LogOut, Settings } from 'lucide-react';
import { useState, useEffect, useRef } from 'react';
import { Button } from './ui/button';
import { Input } from './ui/input';
import { Badge } from './ui/badge';

interface NavigationProps {
  currentPage: string;
  onNavigate: (page: string) => void;
  onNotificationsClick?: () => void;
}

export function NavigationFixed({ currentPage, onNavigate, onNotificationsClick }: NavigationProps) {
  const [sidebarCollapsed, setSidebarCollapsed] = useState(false);
  const [showUserMenu, setShowUserMenu] = useState(false);
  const menuRef = useRef<HTMLDivElement>(null);

  const navItems = [
    { id: 'dashboard', label: 'Dashboard', icon: LayoutDashboard },
    { id: 'datasets', label: 'Datasets', icon: Database },
    { id: 'models-train', label: 'Train Model', icon: BrainCircuit },
    { id: 'models-all', label: 'Models', icon: Layers },
    { id: 'predictions-new', label: 'Predictions', icon: Target },
    { id: 'models-compare', label: 'Compare Models', icon: GitCompare },
    { id: 'predictions-history', label: 'History', icon: History },
  ];

  const handleNavigate = (page: string) => {
    onNavigate(page);
    setShowUserMenu(false);
  };

  // Close menu when clicking outside
  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (menuRef.current && !menuRef.current.contains(event.target as Node)) {
        setShowUserMenu(false);
      }
    };

    if (showUserMenu) {
      document.addEventListener('mousedown', handleClickOutside);
    }
    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
    };
  }, [showUserMenu]);

  return (
    <>
      {/* Top Navigation Bar */}
      <div className="fixed top-0 left-0 right-0 h-16 bg-background-secondary border-b border-border z-50 flex items-center px-6 gap-6">
        {/* Logo */}
        <div className="flex items-center gap-3 min-w-[200px]">
          <div className="w-8 h-8 rounded-lg bg-gradient-to-br from-primary to-secondary flex items-center justify-center">
            <BrainCircuit className="w-5 h-5 text-white" />
          </div>
          <span className="font-semibold tracking-tight">XAI-Forge</span>
        </div>

        {/* Global Search */}
        <div className="flex-1 max-w-xl relative">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-muted-foreground" />
          <Input
            placeholder="Search datasets, models, predictions..."
            className="pl-10 bg-background border-border/50 focus-visible:ring-primary"
          />
        </div>

        {/* Right Actions */}
        <div className="flex items-center gap-4">
          {/* Notifications */}
          <Button variant="ghost" size="icon" className="relative" onClick={onNotificationsClick}>
            <Bell className="w-5 h-5" />
            <Badge className="absolute -top-1 -right-1 w-5 h-5 flex items-center justify-center p-0 bg-error border-0">
              3
            </Badge>
          </Button>

          {/* Help */}
          <Button variant="ghost" size="icon" onClick={() => onNavigate('help')}>
            <HelpCircle className="w-5 h-5" />
          </Button>

          {/* User Menu - Custom Implementation */}
          <div className="relative" ref={menuRef}>
            <Button 
              variant="ghost" 
              size="icon" 
              className="rounded-full" 
              onClick={() => setShowUserMenu(!showUserMenu)}
            >
              <div className="w-8 h-8 rounded-full bg-primary/10 border border-primary/20 flex items-center justify-center">
                <User className="w-4 h-4 text-primary" />
              </div>
            </Button>

            {/* Dropdown Menu */}
            {showUserMenu && (
              <div className="absolute right-0 top-full mt-2 w-56 bg-popover border border-border rounded-md shadow-xl z-[9999] py-1">
                {/* User Info Header */}
                <div className="px-3 py-2 border-b border-border">
                  <p className="font-medium">Alex Johnson</p>
                  <p className="text-sm text-muted-foreground">alex.johnson@company.com</p>
                </div>

                {/* Menu Items */}
                <button
                  onClick={() => handleNavigate('profile')}
                  className="w-full flex items-center gap-2 px-3 py-2 text-sm hover:bg-accent/10 transition-colors text-left"
                >
                  <User className="w-4 h-4" />
                  Profile
                </button>

                <button
                  onClick={() => handleNavigate('settings')}
                  className="w-full flex items-center gap-2 px-3 py-2 text-sm hover:bg-accent/10 transition-colors text-left"
                >
                  <Settings className="w-4 h-4" />
                  Settings
                </button>

                <button
                  onClick={() => handleNavigate('activity-log')}
                  className="w-full flex items-center gap-2 px-3 py-2 text-sm hover:bg-accent/10 transition-colors text-left"
                >
                  <Activity className="w-4 h-4" />
                  Activity Log
                </button>

                <div className="border-t border-border my-1"></div>

                <button
                  onClick={() => handleNavigate('help')}
                  className="w-full flex items-center gap-2 px-3 py-2 text-sm hover:bg-accent/10 transition-colors text-left"
                >
                  <HelpCircle className="w-4 h-4" />
                  Help Center
                </button>

                <div className="border-t border-border my-1"></div>

                <button
                  onClick={() => {
                    setShowUserMenu(false);
                    // Logout logic would go here
                  }}
                  className="w-full flex items-center gap-2 px-3 py-2 text-sm hover:bg-error/10 transition-colors text-error text-left"
                >
                  <LogOut className="w-4 h-4" />
                  Logout
                </button>
              </div>
            )}
          </div>
        </div>
      </div>

      {/* Left Sidebar */}
      <div 
        className={`fixed top-16 left-0 bottom-0 bg-sidebar border-r border-sidebar-border transition-all duration-300 z-40 ${
          sidebarCollapsed ? 'w-16' : 'w-60'
        }`}
      >
        {/* Collapse Toggle */}
        <button
          onClick={() => setSidebarCollapsed(!sidebarCollapsed)}
          className="absolute -right-3 top-6 w-6 h-6 rounded-full bg-surface border border-border flex items-center justify-center hover:bg-accent/10 transition-colors"
        >
          <ChevronLeft className={`w-4 h-4 transition-transform ${sidebarCollapsed ? 'rotate-180' : ''}`} />
        </button>

        {/* Navigation Items */}
        <nav className="p-4 space-y-1">
          {navItems.map((item) => (
            <div key={item.id}>
              <button
                onClick={() => handleNavigate(item.id)}
                className={`w-full flex items-center gap-3 px-3 py-2.5 rounded-lg transition-colors ${
                  currentPage === item.id
                    ? 'bg-primary text-primary-foreground'
                    : 'hover:bg-sidebar-accent text-sidebar-foreground'
                }`}
              >
                <item.icon className="w-5 h-5 flex-shrink-0" />
                {!sidebarCollapsed && <span>{item.label}</span>}
              </button>
            </div>
          ))}
        </nav>
      </div>
    </>
  );
}