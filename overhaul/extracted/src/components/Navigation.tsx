import { Bell, Search, HelpCircle, User, ChevronLeft, LayoutDashboard, Database, Upload, BrainCircuit, Sparkles, History, TrendingUp, Settings, Layers, Target, GitCompare, Activity, ChevronDown, LogOut } from 'lucide-react';
import { useState, useEffect, useRef } from 'react';
import { Button } from './ui/button';
import { Input } from './ui/input';
import { Badge } from './ui/badge';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from './ui/dropdown-menu';

interface NavigationProps {
  currentPage: string;
  onNavigate: (page: string) => void;
  onNotificationsClick?: () => void;
}

export function Navigation({ currentPage, onNavigate, onNotificationsClick }: NavigationProps) {
  const [sidebarCollapsed, setSidebarCollapsed] = useState(false);
  const [showUserMenu, setShowUserMenu] = useState(false);

  const navItems = [
    { id: 'dashboard', label: 'Dashboard', icon: LayoutDashboard },
    { id: 'datasets', label: 'Datasets', icon: Database },
    { id: 'models-train', label: 'Train Model', icon: BrainCircuit },
    { id: 'models-all', label: 'Models', icon: Layers },
    { id: 'predictions-new', label: 'Predictions', icon: Target },
    { id: 'models-compare', label: 'Compare Models', icon: GitCompare },
    { id: 'predictions-history', label: 'History', icon: History },
    { id: 'activity-log', label: 'Activity Log', icon: Activity },
    { id: 'help', label: 'Help Center', icon: HelpCircle },
  ];

  const handleNavigate = (page: string) => {
    onNavigate(page);
    setShowUserMenu(false);
  };

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

          {/* User Menu */}
          <DropdownMenu>
            <DropdownMenuTrigger asChild>
              <Button variant="ghost" size="icon" className="rounded-full">
                <div className="w-8 h-8 rounded-full bg-primary/10 border border-primary/20 flex items-center justify-center">
                  <User className="w-4 h-4 text-primary" />
                </div>
              </Button>
            </DropdownMenuTrigger>
            <DropdownMenuContent align="end" className="w-56">
              <div className="px-2 py-2 border-b border-border mb-2">
                <p className="font-medium">Alex Johnson</p>
                <p className="text-sm text-muted-foreground">alex.johnson@company.com</p>
              </div>
              <DropdownMenuItem onClick={() => onNavigate('profile')} className="cursor-pointer">
                <User className="w-4 h-4 mr-2" />
                Profile
              </DropdownMenuItem>
              <DropdownMenuItem onClick={() => onNavigate('settings')} className="cursor-pointer">
                <Settings className="w-4 h-4 mr-2" />
                Settings
              </DropdownMenuItem>
              <DropdownMenuItem onClick={() => onNavigate('activity-log')} className="cursor-pointer">
                <Activity className="w-4 h-4 mr-2" />
                Activity Log
              </DropdownMenuItem>
              <DropdownMenuSeparator />
              <DropdownMenuItem onClick={() => onNavigate('help')} className="cursor-pointer">
                <HelpCircle className="w-4 h-4 mr-2" />
                Help Center
              </DropdownMenuItem>
              <DropdownMenuSeparator />
              <DropdownMenuItem className="text-error cursor-pointer">
                <LogOut className="w-4 h-4 mr-2" />
                Logout
              </DropdownMenuItem>
            </DropdownMenuContent>
          </DropdownMenu>
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
              
              {/* Children */}
              {!sidebarCollapsed && item.children && currentPage.startsWith(item.id) && (
                <div className="ml-8 mt-1 space-y-0.5">
                  {item.children.map((child) => (
                    <button
                      key={child.id}
                      onClick={() => onNavigate(child.id)}
                      className={`w-full text-left px-3 py-1.5 rounded text-sm transition-colors ${
                        currentPage === child.id
                          ? 'text-primary'
                          : 'text-muted-foreground hover:text-foreground'
                      }`}
                    >
                      {child.label}
                    </button>
                  ))}
                </div>
              )}
            </div>
          ))}
        </nav>
      </div>
    </>
  );
}