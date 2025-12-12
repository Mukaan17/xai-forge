import { useState } from 'react';
import { User, Shield, Bell, Palette, Plug, Database, Download, AlertTriangle, Laptop, Smartphone, Monitor, Sun, Moon, Eye, Copy, Plus, X, Trash2, Check } from 'lucide-react';
import { Button } from './ui/button';
import { Input } from './ui/input';
import { Label } from './ui/label';
import { Card } from './ui/card';
import { Tabs, TabsContent, TabsList, TabsTrigger } from './ui/tabs';
import { Switch } from './ui/switch';
import { Checkbox } from './ui/checkbox';
import { RadioGroup, RadioGroupItem } from './ui/radio-group';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from './ui/select';
import { Badge } from './ui/badge';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger } from './ui/dialog';
import { useStore } from '../lib/store';
import { toast } from 'sonner@2.0.3';

export function SettingsEnhanced() {
  const { theme, setTheme, accentColor, setAccentColor } = useStore();
  const [selectedTab, setSelectedTab] = useState('profile');
  const [apiKeyModalOpen, setApiKeyModalOpen] = useState(false);
  const [deleteAccountModalOpen, setDeleteAccountModalOpen] = useState(false);
  const [deleteConfirmText, setDeleteConfirmText] = useState('');
  const [generatedKey, setGeneratedKey] = useState('');
  
  // Profile form state
  const [fullName, setFullName] = useState('Alex Johnson');
  const [email, setEmail] = useState('alex.johnson@company.com');
  const [company, setCompany] = useState('DataCorp Inc.');
  const [role, setRole] = useState('Data Scientist');
  
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

  const [apiKeys, setApiKeys] = useState([
    { id: '1', name: 'Production Key', key: 'xai_live_sk••••••••••••••••••3f2a', created: 'Dec 1', lastUsed: '2 hours ago' },
    { id: '2', name: 'Development Key', key: 'xai_test_sk••••••••••••••••••8b1c', created: 'Nov 15', lastUsed: '5 days ago' },
  ]);

  const handleSaveProfile = () => {
    toast.success('Profile updated', {
      description: 'Your profile information has been saved',
    });
  };

  const handleChangePassword = () => {
    toast.success('Password updated', {
      description: 'Your password has been changed successfully',
    });
  };

  const handleGenerateApiKey = () => {
    const newKey = `xai_${Math.random().toString(36).substring(2, 15)}${Math.random().toString(36).substring(2, 15)}`;
    setGeneratedKey(newKey);
    const newApiKey = {
      id: Date.now().toString(),
      name: 'New API Key',
      key: `${newKey.substring(0, 20)}••••••••••••••••••`,
      created: 'Just now',
      lastUsed: 'Never',
    };
    setApiKeys([...apiKeys, newApiKey]);
    
    toast.success('API key generated', {
      description: 'Copy it now - you won\'t be able to see it again',
    });
  };

  const handleCopyApiKey = (key: string) => {
    navigator.clipboard.writeText(key);
    toast.success('Copied to clipboard');
  };

  const handleRevokeApiKey = (id: string, name: string) => {
    setApiKeys(apiKeys.filter(k => k.id !== id));
    toast.success('API key revoked', {
      description: `${name} has been deleted`,
    });
  };

  const handleThemeChange = (newTheme: 'dark' | 'light' | 'system') => {
    setTheme(newTheme);
    toast.success('Theme updated', {
      description: `Switched to ${newTheme} mode`,
    });
  };

  const handleAccentColorChange = (color: string) => {
    setAccentColor(color);
    toast.success('Accent color updated');
  };

  const handleExportData = () => {
    toast.success('Export started', {
      description: 'Your data will be emailed to you within 24 hours',
    });
  };

  const handleDeleteAccount = () => {
    if (deleteConfirmText === 'DELETE MY ACCOUNT') {
      toast.error('Account deletion initiated', {
        description: 'Your account will be permanently deleted in 7 days',
      });
      setDeleteAccountModalOpen(false);
      setDeleteConfirmText('');
    } else {
      toast.error('Confirmation text doesn\'t match');
    }
  };

  return (
    <div className="p-8">
      <div className="mb-6">
        <h1>Settings</h1>
        <p className="text-muted-foreground mt-1">Manage your account settings and preferences</p>
      </div>

      <Tabs value={selectedTab} onValueChange={setSelectedTab} className="space-y-6">
        <TabsList className="grid grid-cols-7 w-full max-w-4xl">
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
                <Label>Full Name</Label>
                <Input value={fullName} onChange={(e) => setFullName(e.target.value)} className="mt-2" />
              </div>
              <div>
                <Label>Email Address</Label>
                <Input type="email" value={email} onChange={(e) => setEmail(e.target.value)} className="mt-2" />
              </div>
              <div>
                <Label>Company</Label>
                <Input value={company} onChange={(e) => setCompany(e.target.value)} className="mt-2" />
              </div>
              <div>
                <Label>Role</Label>
                <Input value={role} onChange={(e) => setRole(e.target.value)} className="mt-2" />
              </div>
              <Button onClick={handleSaveProfile} className="bg-primary text-primary-foreground hover:bg-primary/90">
                <Check className="w-4 h-4 mr-2" />
                Save Changes
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
                <Input type="password" placeholder="••••••••••••" className="mt-2" />
              </div>
              <div>
                <Label>New Password</Label>
                <Input type="password" className="mt-2" />
                <p className="text-sm text-muted-foreground mt-1">Min 8 chars, 1 uppercase, 1 number</p>
              </div>
              <div>
                <Label>Confirm Password</Label>
                <Input type="password" className="mt-2" />
              </div>
              <Button onClick={handleChangePassword} className="bg-primary text-primary-foreground hover:bg-primary/90">
                Update Password
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
              <Badge variant="outline" className="border-muted text-muted-foreground">Disabled</Badge>
            </div>
            <Button variant="outline" onClick={() => toast.success('2FA setup initiated')}>
              Enable 2FA
            </Button>
          </Card>

          <Card className="p-6">
            <h3 className="mb-6">Active Sessions</h3>
            <div className="space-y-4">
              {[
                { device: 'Chrome on MacOS', location: 'New York, US', current: true, lastActive: 'Now' },
                { device: 'Safari on iPhone', location: 'New York, US', current: false, lastActive: '2 days ago' },
              ].map((session, i) => (
                <div key={i} className="flex items-center justify-between p-4 border border-border rounded-lg">
                  <div className="flex items-center gap-4">
                    <div className="w-10 h-10 rounded-lg bg-muted flex items-center justify-center">
                      {session.device.includes('iPhone') ? <Smartphone className="w-5 h-5" /> : <Monitor className="w-5 h-5" />}
                    </div>
                    <div>
                      <div className="flex items-center gap-2">
                        <p className="font-medium">{session.device}</p>
                        {session.current && (
                          <Badge variant="outline" className="border-primary/30 text-primary">Current</Badge>
                        )}
                      </div>
                      <p className="text-sm text-muted-foreground">{session.location} • {session.lastActive}</p>
                    </div>
                  </div>
                  {!session.current && (
                    <Button 
                      variant="ghost" 
                      size="sm" 
                      className="text-error"
                      onClick={() => toast.success('Session revoked')}
                    >
                      Revoke
                    </Button>
                  )}
                </div>
              ))}
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
                              [pref.key]: { ...notificationPrefs[pref.key as keyof typeof notificationPrefs], email: checked as boolean }
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
                              [pref.key]: { ...notificationPrefs[pref.key as keyof typeof notificationPrefs], inApp: checked as boolean }
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
                              [pref.key]: { ...notificationPrefs[pref.key as keyof typeof notificationPrefs], push: checked as boolean }
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
              className="mt-6 bg-primary text-primary-foreground hover:bg-primary/90"
              onClick={() => toast.success('Notification preferences saved')}
            >
              Save Preferences
            </Button>
          </Card>
        </TabsContent>

        {/* Appearance Tab */}
        <TabsContent value="appearance" className="space-y-6">
          <Card className="p-6">
            <h3 className="mb-6">Theme</h3>
            <RadioGroup value={theme} onValueChange={(v: any) => handleThemeChange(v)} className="grid grid-cols-3 gap-4">
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
                  onClick={() => handleAccentColorChange(color.color)}
                  className={`w-12 h-12 rounded-full border-2 hover:scale-110 transition-transform ${
                    accentColor === color.color ? 'border-white ring-2 ring-offset-2' : 'border-border'
                  }`}
                  style={{ backgroundColor: color.color }}
                />
              ))}
            </div>
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
              <Dialog open={apiKeyModalOpen} onOpenChange={setApiKeyModalOpen}>
                <DialogTrigger asChild>
                  <Button className="bg-primary text-primary-foreground hover:bg-primary/90">
                    <Plus className="w-4 h-4 mr-2" />
                    Generate New API Key
                  </Button>
                </DialogTrigger>
                <DialogContent>
                  <DialogHeader>
                    <DialogTitle>Generate New API Key</DialogTitle>
                  </DialogHeader>
                  {!generatedKey ? (
                    <div className="space-y-4">
                      <div>
                        <Label>Key Name</Label>
                        <Input placeholder="Production API Key" className="mt-2" />
                      </div>
                      <div className="flex gap-3">
                        <Button variant="outline" onClick={() => setApiKeyModalOpen(false)} className="flex-1">
                          Cancel
                        </Button>
                        <Button 
                          onClick={handleGenerateApiKey}
                          className="flex-1 bg-primary text-primary-foreground hover:bg-primary/90"
                        >
                          Generate Key
                        </Button>
                      </div>
                    </div>
                  ) : (
                    <div className="space-y-4">
                      <div className="p-4 bg-warning/10 border border-warning rounded-lg">
                        <p className="text-sm text-warning mb-2">⚠️ Copy this key now!</p>
                        <p className="text-sm text-muted-foreground">You won't be able to see it again.</p>
                      </div>
                      <div>
                        <Label>Your new API key</Label>
                        <div className="flex gap-2 mt-2">
                          <Input value={generatedKey} readOnly className="font-mono" />
                          <Button onClick={() => handleCopyApiKey(generatedKey)}>
                            <Copy className="w-4 h-4" />
                          </Button>
                        </div>
                      </div>
                      <Button 
                        onClick={() => {
                          setApiKeyModalOpen(false);
                          setGeneratedKey('');
                        }}
                        className="w-full"
                      >
                        Done
                      </Button>
                    </div>
                  )}
                </DialogContent>
              </Dialog>
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
                        className="text-error"
                        onClick={() => handleRevokeApiKey(apiKey.id, apiKey.name)}
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
            <p className="text-muted-foreground mb-4">Used: 2.4 GB of 10 GB (24%)</p>
            <div className="w-full h-4 bg-muted rounded-full overflow-hidden mb-4">
              <div className="h-full bg-primary" style={{ width: '24%' }}></div>
            </div>
            <div className="space-y-2">
              <div className="flex items-center justify-between text-sm">
                <span className="text-muted-foreground">Datasets</span>
                <span>1.8 GB</span>
              </div>
              <div className="flex items-center justify-between text-sm">
                <span className="text-muted-foreground">Models</span>
                <span>0.5 GB</span>
              </div>
              <div className="flex items-center justify-between text-sm">
                <span className="text-muted-foreground">Predictions</span>
                <span>0.1 GB</span>
              </div>
            </div>
          </Card>

          <Card className="p-6">
            <h3 className="mb-6">Export Your Data</h3>
            <p className="text-muted-foreground mb-4">Download a copy of all your data from XAI-Forge.</p>
            <Button onClick={handleExportData} variant="outline">
              <Download className="w-4 h-4 mr-2" />
              Request Data Export
            </Button>
          </Card>
        </TabsContent>

        {/* Danger Zone Tab */}
        <TabsContent value="danger" className="space-y-6">
          <Card className="p-6 border-error/50">
            <h3 className="mb-6 text-error">Danger Zone</h3>
            <div className="space-y-4">
              <div className="p-4 border border-border rounded-lg">
                <div className="flex items-center justify-between">
                  <div>
                    <p className="font-medium mb-1">Delete All Models</p>
                    <p className="text-sm text-muted-foreground">Permanently delete all trained models</p>
                  </div>
                  <Button 
                    variant="outline" 
                    className="border-error text-error hover:bg-error hover:text-white"
                    onClick={() => toast.error('Models deletion cancelled')}
                  >
                    Delete Models
                  </Button>
                </div>
              </div>

              <div className="p-4 border border-error/50 rounded-lg bg-error/5">
                <div className="flex items-center justify-between">
                  <div>
                    <p className="font-medium mb-1 text-error">Delete Account</p>
                    <p className="text-sm text-muted-foreground">Permanently delete your account and all data</p>
                  </div>
                  <Dialog open={deleteAccountModalOpen} onOpenChange={setDeleteAccountModalOpen}>
                    <DialogTrigger asChild>
                      <Button variant="outline" className="border-error text-error hover:bg-error hover:text-white">
                        Delete Account
                      </Button>
                    </DialogTrigger>
                    <DialogContent>
                      <DialogHeader>
                        <DialogTitle className="text-error">Delete Account</DialogTitle>
                      </DialogHeader>
                      <div className="space-y-4">
                        <div className="p-4 bg-error/10 border border-error rounded-lg">
                          <p className="text-sm text-error mb-2">⚠️ This action cannot be undone!</p>
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
                            disabled={deleteConfirmText !== 'DELETE MY ACCOUNT'}
                            className="flex-1 bg-error text-white hover:bg-error/90"
                          >
                            Delete Forever
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
