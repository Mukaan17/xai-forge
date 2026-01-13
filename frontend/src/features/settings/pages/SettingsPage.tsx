import { useState, useEffect } from 'react';
import { User, Shield, Bell, Palette, Plug, Database, Download, AlertTriangle, Laptop, Smartphone, Monitor, Sun, Moon, Check, Copy, Plus } from 'lucide-react';
import { Card, CardContent, CardHeader, CardTitle } from '@/shared/components/ui/card';
import { Button } from '@/shared/components/ui/button';
import { Input } from '@/shared/components/ui/input';
import { Label } from '@/shared/components/ui/label';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/shared/components/ui/tabs';
import { Switch } from '@/shared/components/ui/switch';
import { Checkbox } from '@/shared/components/ui/checkbox';
import { RadioGroup, RadioGroupItem } from '@/shared/components/ui/radio-group';
import { Badge } from '@/shared/components/ui/badge';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger } from '@/shared/components/ui/dialog';
import { DestructiveButton } from '@/shared/components/ui/destructive-button';
import { useAuth } from '@/features/auth/hooks/useAuth';
import { useAuthStore } from '@/features/auth/store/authStore';
import { settingsApi } from '../api/settingsApi';
import { toast } from '@/shared/lib/toast';
import { useNavigate } from 'react-router-dom';

export function SettingsPage() {
  const { user, logout } = useAuth();
  const { updateUser } = useAuthStore();
  const navigate = useNavigate();
  
  const [selectedTab, setSelectedTab] = useState('profile');
  const [deleteAccountModalOpen, setDeleteAccountModalOpen] = useState(false);
  const [deleteConfirmText, setDeleteConfirmText] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  
  // Profile form state
  const [profile, setProfile] = useState({
    firstName: user?.firstName || '',
    lastName: user?.lastName || '',
    email: user?.email || '',
    organization: '',
    role: '',
  });
  
  // Password form state
  const [passwordForm, setPasswordForm] = useState({
    currentPassword: '',
    newPassword: '',
    confirmPassword: '',
  });
  
  // Notification preferences
  const [notificationPrefs, setNotificationPrefs] = useState({
    trainingComplete: { email: true, inApp: true, push: false },
    trainingFailed: { email: true, inApp: true, push: true },
    datasetUpload: { email: false, inApp: true, push: false },
    weeklySummary: { email: true, inApp: false, push: false },
    securityAlerts: { email: true, inApp: true, push: true },
    productUpdates: { email: false, inApp: false, push: false },
    tips: { email: false, inApp: true, push: false },
  });
  
  // Appearance state
  const [theme, setTheme] = useState('dark');
  const [accentColor, setAccentColor] = useState('#00d9ff');
  
  // API Keys state (placeholder for now)
  const [apiKeys] = useState([
    { id: '1', name: 'Production Key', key: 'xai_live_sk••••••••••••••••••3f2a', created: 'Dec 1', lastUsed: '2 hours ago' },
    { id: '2', name: 'Development Key', key: 'xai_test_sk••••••••••••••••••8b1c', created: 'Nov 15', lastUsed: '5 days ago' },
  ]);
  
  useEffect(() => {
    if (user) {
      setProfile({
        firstName: user.firstName || '',
        lastName: user.lastName || '',
        email: user.email || '',
        organization: '',
        role: '',
      });
    }
  }, [user]);

  const handleSaveProfile = async () => {
    setIsLoading(true);
    try {
      const updatedUser = await settingsApi.updateProfile({
        firstName: profile.firstName,
        lastName: profile.lastName,
        email: profile.email,
        organization: profile.organization,
        role: profile.role,
      });
      updateUser(updatedUser);
      toast.success('Profile updated', {
        description: 'Your profile information has been saved',
      });
    } catch (error: any) {
      toast.error('Failed to update profile', {
        description: error?.message || 'Please try again',
      });
    } finally {
      setIsLoading(false);
    }
  };

  const handleChangePassword = async () => {
    if (passwordForm.newPassword !== passwordForm.confirmPassword) {
      toast.error('Passwords do not match');
      return;
    }
    
    if (passwordForm.newPassword.length < 8) {
      toast.error('Password must be at least 8 characters');
      return;
    }
    
    setIsLoading(true);
    try {
      await settingsApi.changePassword({
        currentPassword: passwordForm.currentPassword,
        newPassword: passwordForm.newPassword,
      });
      toast.success('Password updated', {
        description: 'Your password has been changed successfully',
      });
      setPasswordForm({
        currentPassword: '',
        newPassword: '',
        confirmPassword: '',
      });
    } catch (error: any) {
      toast.error('Failed to update password', {
        description: error?.message || 'Please check your current password',
      });
    } finally {
      setIsLoading(false);
    }
  };

  const handleSavePreferences = async () => {
    setIsLoading(true);
    try {
      await settingsApi.updatePreferences({
        theme,
        accentColor,
        notificationPreferences: JSON.stringify(notificationPrefs),
      });
      toast.success('Preferences saved', {
        description: 'Your preferences have been updated',
      });
    } catch (error: any) {
      toast.error('Failed to save preferences', {
        description: error?.message || 'Please try again',
      });
    } finally {
      setIsLoading(false);
    }
  };

  const handleDeleteAccount = async () => {
    if (deleteConfirmText !== 'DELETE MY ACCOUNT') {
      toast.error('Confirmation text does not match');
      return;
    }
    
    setIsLoading(true);
    try {
      console.log('Attempting to delete account...');
      const response = await settingsApi.deleteAccount();
      console.log('Delete account response:', response);
      
      // Clear all local storage
      localStorage.clear();
      
      // Clear all session storage
      sessionStorage.clear();
      
      // Clear auth state
      logout();
      
      toast.success('Account deleted', {
        description: 'Your account and all data have been permanently deleted',
      });
      
      // Small delay to ensure toast is shown before navigation
      setTimeout(() => {
        navigate('/login');
      }, 500);
    } catch (error: any) {
      console.error('Delete account error:', error);
      
      // Check if it's an authentication error
      if (error?.status === 401 || error?.code === 'UNAUTHORIZED' || error?.message?.includes('Authentication')) {
        toast.error('Session expired', {
          description: 'Please log in again and try deleting your account.',
        });
        // Redirect to login after a short delay
        setTimeout(() => {
          logout();
          navigate('/login');
        }, 2000);
        return;
      }
      
      // Extract error message
      let errorMessage = 'Please try again';
      if (error?.message) {
        errorMessage = error.message;
      } else if (error?.detail) {
        errorMessage = error.detail;
      } else if (error?.error) {
        errorMessage = typeof error.error === 'string' ? error.error : error.error.message || errorMessage;
      }
      
      toast.error('Failed to delete account', {
        description: errorMessage,
      });
    } finally {
      setIsLoading(false);
      setDeleteAccountModalOpen(false);
      setDeleteConfirmText('');
    }
  };

  const handleCopyApiKey = (key: string) => {
    navigator.clipboard.writeText(key);
    toast.success('Copied to clipboard');
  };

  return (
    <div className="p-4 sm:p-6 lg:p-8">
      <div className="mb-4 sm:mb-6">
        <h1 className="text-2xl sm:text-3xl font-semibold">Settings</h1>
        <p className="text-muted-foreground mt-1 text-sm sm:text-base">Manage your account settings and preferences</p>
      </div>

      <Tabs value={selectedTab} onValueChange={setSelectedTab} className="space-y-4 sm:space-y-6">
        <TabsList className="grid grid-cols-2 sm:grid-cols-4 lg:grid-cols-7 w-full gap-2 sm:gap-0 overflow-x-auto">
          <TabsTrigger value="profile" className="flex items-center gap-2">
            <User className="w-4 h-4" />
            Profile
          </TabsTrigger>
          <TabsTrigger value="security" className="flex items-center gap-2">
            <Shield className="w-4 h-4" />
            Security
          </TabsTrigger>
          <TabsTrigger value="notifications" className="flex items-center gap-2">
            <Bell className="w-4 h-4" />
            Notifications
          </TabsTrigger>
          <TabsTrigger value="appearance" className="flex items-center gap-2">
            <Palette className="w-4 h-4" />
            Appearance
          </TabsTrigger>
          <TabsTrigger value="api" className="flex items-center gap-2">
            <Plug className="w-4 h-4" />
            API
          </TabsTrigger>
          <TabsTrigger value="data" className="flex items-center gap-2">
            <Database className="w-4 h-4" />
            Data
          </TabsTrigger>
          <TabsTrigger value="danger" className="flex items-center gap-2">
            <AlertTriangle className="w-4 h-4" />
            Danger Zone
          </TabsTrigger>
        </TabsList>

        {/* Profile Tab */}
        <TabsContent value="profile" className="space-y-6">
          <Card className="p-6">
            <h3 className="mb-6">Personal Information</h3>
            <div className="space-y-4 max-w-xl">
              <div>
                <Label>First Name</Label>
                <Input 
                  value={profile.firstName} 
                  onChange={(e) => setProfile({ ...profile, firstName: e.target.value })} 
                  className="mt-2" 
                />
              </div>
              <div>
                <Label>Last Name</Label>
                <Input 
                  value={profile.lastName} 
                  onChange={(e) => setProfile({ ...profile, lastName: e.target.value })} 
                  className="mt-2" 
                />
              </div>
              <div>
                <Label>Email Address</Label>
                <Input 
                  type="email" 
                  value={profile.email} 
                  onChange={(e) => setProfile({ ...profile, email: e.target.value })} 
                  className="mt-2" 
                />
              </div>
              <div>
                <Label>Company</Label>
                <Input 
                  value={profile.organization} 
                  onChange={(e) => setProfile({ ...profile, organization: e.target.value })} 
                  className="mt-2" 
                />
              </div>
              <div>
                <Label>Role</Label>
                <Input 
                  value={profile.role} 
                  onChange={(e) => setProfile({ ...profile, role: e.target.value })} 
                  className="mt-2" 
                />
              </div>
              <Button 
                onClick={handleSaveProfile} 
                disabled={isLoading}
                className="bg-primary text-primary-foreground hover:bg-primary/90"
              >
                <Check className="w-4 h-4 mr-2" />
                {isLoading ? 'Saving...' : 'Save Changes'}
              </Button>
            </div>
          </Card>

          <Card className="p-6">
            <h3 className="mb-6">Profile Picture</h3>
            <div className="flex items-center gap-6">
              <div className="w-24 h-24 rounded-full bg-gradient-to-br from-primary to-secondary flex items-center justify-center">
                <User className="w-12 h-12 text-white" />
              </div>
              <div className="space-y-2">
                <Button variant="outline">Upload New Photo</Button>
                <p className="text-sm text-muted-foreground">JPG, PNG or GIF. Max size 2MB.</p>
              </div>
            </div>
          </Card>
        </TabsContent>

        {/* Security Tab */}
        <TabsContent value="security" className="space-y-6">
          <Card className="p-6">
            <h3 className="mb-6">Change Password</h3>
            <div className="space-y-4 max-w-xl">
              <div>
                <Label>Current Password</Label>
                <Input 
                  type="password" 
                  placeholder="••••••••••••" 
                  value={passwordForm.currentPassword}
                  onChange={(e) => setPasswordForm({ ...passwordForm, currentPassword: e.target.value })}
                  className="mt-2" 
                />
              </div>
              <div>
                <Label>New Password</Label>
                <Input 
                  type="password" 
                  value={passwordForm.newPassword}
                  onChange={(e) => setPasswordForm({ ...passwordForm, newPassword: e.target.value })}
                  className="mt-2" 
                />
                <p className="text-sm text-muted-foreground mt-1">Min 8 chars, 1 uppercase, 1 number</p>
              </div>
              <div>
                <Label>Confirm Password</Label>
                <Input 
                  type="password" 
                  value={passwordForm.confirmPassword}
                  onChange={(e) => setPasswordForm({ ...passwordForm, confirmPassword: e.target.value })}
                  className="mt-2" 
                />
              </div>
              <Button 
                onClick={handleChangePassword} 
                disabled={isLoading}
                className="bg-primary text-primary-foreground hover:bg-primary/90"
              >
                {isLoading ? 'Updating...' : 'Update Password'}
              </Button>
            </div>
          </Card>

          <Card className="p-6">
            <div className="flex items-start justify-between mb-4">
              <div>
                <h3 className="mb-2">Two-Factor Authentication</h3>
                <p className="text-muted-foreground">
                  Add an extra layer of security to your account.
                </p>
              </div>
              <Badge variant="outline" className="border-muted text-muted-foreground">
                {user?.twoFactorEnabled ? 'Enabled' : 'Disabled'}
              </Badge>
            </div>
            <Button variant="outline" onClick={() => toast.info('2FA setup coming soon')}>
              {user?.twoFactorEnabled ? 'Disable 2FA' : 'Enable 2FA'}
            </Button>
          </Card>

          <Card className="p-6">
            <h3 className="mb-6">Active Sessions</h3>
            <div className="space-y-4">
              <div className="flex items-center justify-between p-4 border border-border rounded-lg">
                <div className="flex items-center gap-4">
                  <div className="w-10 h-10 rounded-lg bg-muted flex items-center justify-center">
                    <Monitor className="w-5 h-5" />
                  </div>
                  <div>
                    <div className="flex items-center gap-2">
                      <p className="font-medium">Current Session</p>
                      <Badge variant="outline" className="border-primary/30 text-primary">Current</Badge>
                    </div>
                    <p className="text-sm text-muted-foreground">Now</p>
                  </div>
                </div>
              </div>
            </div>
          </Card>
        </TabsContent>

        {/* Notifications Tab */}
        <TabsContent value="notifications" className="space-y-6">
          <Card className="p-6">
            <h3 className="mb-6">Notification Preferences</h3>
            <div className="overflow-x-auto">
              <table className="w-full">
                <thead>
                  <tr className="border-b border-border">
                    <th className="text-left py-3 px-4 font-medium"></th>
                    <th className="text-center py-3 px-4 font-medium">Email</th>
                    <th className="text-center py-3 px-4 font-medium">In-App</th>
                    <th className="text-center py-3 px-4 font-medium">Push</th>
                  </tr>
                </thead>
                <tbody>
                  {[
                    { key: 'trainingComplete', label: 'Model Training Complete' },
                    { key: 'trainingFailed', label: 'Training Failed' },
                    { key: 'datasetUpload', label: 'Dataset Upload Complete' },
                    { key: 'weeklySummary', label: 'Weekly Usage Summary' },
                    { key: 'securityAlerts', label: 'Security Alerts' },
                    { key: 'productUpdates', label: 'Product Updates' },
                    { key: 'tips', label: 'Tips & Tutorials' },
                  ].map((pref) => (
                    <tr key={pref.key} className="border-b border-border/50">
                      <td className="py-4 px-4">{pref.label}</td>
                      <td className="py-4 px-4 text-center">
                        <Checkbox 
                          checked={notificationPrefs[pref.key as keyof typeof notificationPrefs].email}
                          onCheckedChange={(checked) => {
                            setNotificationPrefs({
                              ...notificationPrefs,
                              [pref.key]: { 
                                ...notificationPrefs[pref.key as keyof typeof notificationPrefs], 
                                email: checked as boolean 
                              }
                            });
                          }}
                        />
                      </td>
                      <td className="py-4 px-4 text-center">
                        <Checkbox 
                          checked={notificationPrefs[pref.key as keyof typeof notificationPrefs].inApp}
                          onCheckedChange={(checked) => {
                            setNotificationPrefs({
                              ...notificationPrefs,
                              [pref.key]: { 
                                ...notificationPrefs[pref.key as keyof typeof notificationPrefs], 
                                inApp: checked as boolean 
                              }
                            });
                          }}
                        />
                      </td>
                      <td className="py-4 px-4 text-center">
                        <Checkbox 
                          checked={notificationPrefs[pref.key as keyof typeof notificationPrefs].push}
                          onCheckedChange={(checked) => {
                            setNotificationPrefs({
                              ...notificationPrefs,
                              [pref.key]: { 
                                ...notificationPrefs[pref.key as keyof typeof notificationPrefs], 
                                push: checked as boolean 
                              }
                            });
                          }}
                        />
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
            <Button 
              onClick={handleSavePreferences}
              disabled={isLoading}
              className="mt-6 bg-primary text-primary-foreground hover:bg-primary/90"
            >
              {isLoading ? 'Saving...' : 'Save Preferences'}
            </Button>
          </Card>
        </TabsContent>

        {/* Appearance Tab */}
        <TabsContent value="appearance" className="space-y-6">
          <Card className="p-6">
            <h3 className="mb-6">Theme</h3>
            <RadioGroup value={theme} onValueChange={setTheme} className="grid grid-cols-3 gap-4">
              {[
                { id: 'dark', label: 'Dark Mode', icon: Moon },
                { id: 'light', label: 'Light Mode', icon: Sun },
                { id: 'system', label: 'System', icon: Laptop },
              ].map((themeOption) => (
                <label key={themeOption.id} className="cursor-pointer">
                  <RadioGroupItem value={themeOption.id} className="sr-only" />
                  <div className={`border-2 rounded-lg p-4 hover:border-primary/50 transition-colors ${
                    theme === themeOption.id ? 'border-primary' : 'border-border'
                  }`}>
                    <themeOption.icon className="w-6 h-6 mb-3" />
                    <p className="font-medium">{themeOption.label}</p>
                    <div className="mt-3 h-20 rounded border border-border bg-gradient-to-b from-muted to-background"></div>
                  </div>
                </label>
              ))}
            </RadioGroup>
          </Card>

          <Card className="p-6">
            <h3 className="mb-6">Accent Color</h3>
            <p className="text-muted-foreground mb-4">
              Choose your preferred accent color for buttons and highlights.
            </p>
            <div className="flex gap-4">
              {[
                { id: 'teal', color: '#00d9ff' },
                { id: 'purple', color: '#7c3aed' },
                { id: 'blue', color: '#3b82f6' },
                { id: 'green', color: '#10b981' },
                { id: 'orange', color: '#f59e0b' },
              ].map((color) => (
                <button
                  key={color.id}
                  onClick={() => setAccentColor(color.color)}
                  className={`w-12 h-12 rounded-full border-2 hover:scale-110 transition-transform ${
                    accentColor === color.color ? 'border-white ring-2 ring-offset-2' : 'border-border'
                  }`}
                  style={{ backgroundColor: color.color }}
                />
              ))}
            </div>
            <Button 
              onClick={handleSavePreferences}
              disabled={isLoading}
              className="mt-6 bg-primary text-primary-foreground hover:bg-primary/90"
            >
              {isLoading ? 'Saving...' : 'Save Preferences'}
            </Button>
          </Card>
        </TabsContent>

        {/* API Tab */}
        <TabsContent value="api" className="space-y-6">
          <Card className="p-6">
            <div className="flex items-center justify-between mb-6">
              <div>
                <h3 className="mb-1">API Keys</h3>
                <p className="text-muted-foreground">Manage API keys for programmatic access.</p>
              </div>
              <Button className="bg-primary text-primary-foreground hover:bg-primary/90" onClick={() => toast.info('API key generation coming soon')}>
                <Plus className="w-4 h-4 mr-2" />
                Generate New API Key
              </Button>
            </div>

            <div className="space-y-3">
              {apiKeys.map((apiKey) => (
                <div key={apiKey.id} className="p-4 border border-border rounded-lg">
                  <div className="flex items-center justify-between mb-2">
                    <p className="font-medium">{apiKey.name}</p>
                    <span className="text-sm text-muted-foreground">Created {apiKey.created}</span>
                  </div>
                  <div className="flex items-center justify-between">
                    <code className="text-sm font-mono bg-muted px-2 py-1 rounded">{apiKey.key}</code>
                    <div className="flex gap-2">
                      <Button variant="ghost" size="sm" onClick={() => handleCopyApiKey(apiKey.key)}>
                        <Copy className="w-4 h-4 mr-1" />
                        Copy
                      </Button>
                      <Button 
                        variant="ghost" 
                        size="sm" 
                        className="text-destructive"
                        onClick={() => toast.info('API key revocation coming soon')}
                      >
                        Revoke
                      </Button>
                    </div>
                  </div>
                  <p className="text-sm text-muted-foreground mt-2">Last used: {apiKey.lastUsed}</p>
                </div>
              ))}
            </div>
          </Card>
        </TabsContent>

        {/* Data Management Tab */}
        <TabsContent value="data" className="space-y-6">
          <Card className="p-6">
            <h3 className="mb-6">Storage Usage</h3>
            <p className="text-muted-foreground mb-4">Used: 0 GB of 10 GB (0%)</p>
            <div className="w-full h-4 bg-muted rounded-full overflow-hidden mb-4">
              <div className="h-full bg-primary" style={{ width: '0%' }}></div>
            </div>
            <div className="space-y-2">
              <div className="flex items-center justify-between text-sm">
                <span className="text-muted-foreground">Datasets</span>
                <span>0 GB</span>
              </div>
              <div className="flex items-center justify-between text-sm">
                <span className="text-muted-foreground">Models</span>
                <span>0 GB</span>
              </div>
              <div className="flex items-center justify-between text-sm">
                <span className="text-muted-foreground">Predictions</span>
                <span>0 GB</span>
              </div>
            </div>
          </Card>

          <Card className="p-6">
            <h3 className="mb-6">Export Your Data</h3>
            <p className="text-muted-foreground mb-4">Download a copy of all your data from XAI-Forge.</p>
            <Button onClick={() => toast.info('Data export coming soon')} variant="outline">
              <Download className="w-4 h-4 mr-2" />
              Request Data Export
            </Button>
          </Card>
        </TabsContent>

        {/* Danger Zone Tab */}
        <TabsContent value="danger" className="space-y-6">
          <Card className="p-6 border-destructive/50">
            <h3 className="mb-6 text-destructive">Danger Zone</h3>
            <div className="space-y-4">
              <div className="p-4 border border-error/50 rounded-lg bg-error/5">
                <div className="flex items-center justify-between">
                  <div>
                    <p className="font-medium mb-1 text-destructive">Delete Account</p>
                    <p className="text-sm text-muted-foreground">Permanently delete your account and all data</p>
                  </div>
                  <Dialog open={deleteAccountModalOpen} onOpenChange={setDeleteAccountModalOpen}>
                    <DialogTrigger asChild>
                      <DestructiveButton>
                        Delete Account
                      </DestructiveButton>
                    </DialogTrigger>
                    <DialogContent>
                      <DialogHeader>
                        <DialogTitle className="text-destructive">Delete Account</DialogTitle>
                      </DialogHeader>
                      <div className="space-y-4">
                        <div className="p-4 bg-error/10 border border-error rounded-lg">
                          <p className="text-sm text-destructive mb-2">⚠️ This action cannot be undone!</p>
                          <p className="text-sm text-muted-foreground">
                            All your datasets, models, and predictions will be permanently deleted.
                          </p>
                        </div>
                        <div>
                          <Label>Type "DELETE MY ACCOUNT" to confirm</Label>
                          <Input 
                            value={deleteConfirmText}
                            onChange={(e) => setDeleteConfirmText(e.target.value)}
                            className="mt-2"
                            placeholder="DELETE MY ACCOUNT"
                          />
                        </div>
                        <div className="flex gap-3">
                          <Button 
                            variant="outline" 
                            onClick={() => {
                              setDeleteAccountModalOpen(false);
                              setDeleteConfirmText('');
                            }}
                            className="flex-1"
                          >
                            Cancel
                          </Button>
                          <Button 
                            onClick={handleDeleteAccount}
                            disabled={deleteConfirmText !== 'DELETE MY ACCOUNT' || isLoading}
                            className="flex-1 bg-destructive text-destructive-foreground hover:bg-destructive/90"
                          >
                            {isLoading ? 'Deleting...' : 'Delete Forever'}
                          </Button>
                        </div>
                      </div>
                    </DialogContent>
                  </Dialog>
                </div>
              </div>
            </div>
          </Card>
        </TabsContent>
      </Tabs>
    </div>
  );
}
