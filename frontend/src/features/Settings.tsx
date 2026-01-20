import { useState } from 'react';
import { User, Shield, Bell, Palette, Plug, Database, Download, AlertTriangle, Smartphone, Monitor, Eye, Copy, Plus, X, Trash2 } from 'lucide-react';
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

export function Settings() {
  const [selectedTab, setSelectedTab] = useState('profile');
  const [apiKeyModalOpen, setApiKeyModalOpen] = useState(false);
  const [deleteAccountModalOpen, setDeleteAccountModalOpen] = useState(false);
  const [deleteConfirmText, setDeleteConfirmText] = useState('');

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

        {/* Security Tab */}
        <TabsContent value="security" className="space-y-6">
          {/* Password Section */}
          <Card className="p-6">
            <h3 className="mb-6">Password</h3>
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
              <Button className="bg-primary text-primary-foreground hover:bg-primary/90">
                Update Password
              </Button>
            </div>
          </Card>

          {/* Two-Factor Authentication */}
          <Card className="p-6">
            <div className="flex items-start justify-between mb-4">
              <div>
                <h3 className="mb-2">Two-Factor Authentication</h3>
                <p className="text-muted-foreground">
                  Add an extra layer of security to your account by requiring a verification code in addition to your password.
                </p>
              </div>
              <Badge variant="outline" className="border-muted text-muted-foreground">Disabled</Badge>
            </div>
            <Button variant="outline">Enable 2FA</Button>
          </Card>

          {/* Active Sessions */}
          <Card className="p-6">
            <h3 className="mb-6">Active Sessions</h3>
            <div className="space-y-4">
              {[
                { device: 'Chrome on MacOS', location: 'New York, US', current: true, lastActive: 'Now' },
                { device: 'Safari on iPhone', location: 'New York, US', current: false, lastActive: '2 days ago' },
                { device: 'Firefox on Windows', location: 'Boston, US', current: false, lastActive: '5 days ago' },
              ].map((session, i) => (
                <div key={i} className="flex items-center justify-between p-4 border border-border rounded-lg">
                  <div className="flex items-center gap-4">
                    <div className="w-10 h-10 rounded-lg bg-muted flex items-center justify-center">
                      {session.device.includes('iPhone') ? (
                        <Smartphone className="w-5 h-5" />
                      ) : (
                        <Monitor className="w-5 h-5" />
                      )}
                    </div>
                    <div>
                      <div className="flex items-center gap-2">
                        <p className="font-medium">{session.device}</p>
                        {session.current && (
                          <Badge variant="outline" className="border-primary/30 text-primary">Current session</Badge>
                        )}
                      </div>
                      <p className="text-sm text-muted-foreground">{session.location} • Last active: {session.lastActive}</p>
                    </div>
                  </div>
                  {!session.current && (
                    <Button variant="ghost" size="sm" className="text-error">
                      Revoke
                    </Button>
                  )}
                </div>
              ))}
            </div>
            <Button variant="outline" className="mt-4">Revoke All Other Sessions</Button>
          </Card>

          {/* Login History */}
          <Card className="p-6">
            <h3 className="mb-6">Recent Login Activity</h3>
            <div className="space-y-3">
              {[
                { success: true, browser: 'Chrome, MacOS', date: 'Dec 9, 2024 10:23 AM' },
                { success: true, browser: 'Safari, iPhone', date: 'Dec 7, 2024 3:45 PM' },
                { success: false, browser: 'Unknown browser', date: 'Dec 6, 2024 11:02 PM', note: 'IP: 192.168.x.x (Blocked after 3 attempts)' },
              ].map((activity, i) => (
                <div key={i} className="flex items-start gap-3 p-3 rounded-lg hover:bg-muted/30">
                  <div className={`w-5 h-5 rounded-full flex items-center justify-center flex-shrink-0 ${
                    activity.success ? 'bg-success/20 text-success' : 'bg-error/20 text-error'
                  }`}>
                    {activity.success ? '✓' : '✗'}
                  </div>
                  <div className="flex-1">
                    <p>{activity.success ? 'Successful login' : 'Failed attempt'} • {activity.browser}</p>
                    <p className="text-sm text-muted-foreground">{activity.date}</p>
                    {activity.note && (
                      <p className="text-sm text-muted-foreground mt-1">└─ {activity.note}</p>
                    )}
                  </div>
                </div>
              ))}
            </div>
            <Button variant="ghost" className="mt-4 text-primary">View Full History</Button>
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
                    { label: 'Model Training Complete', email: true, inApp: true, push: false },
                    { label: 'Training Failed', email: true, inApp: true, push: true },
                    { label: 'Dataset Upload Complete', email: false, inApp: true, push: false },
                    { label: 'Weekly Usage Summary', email: true, inApp: false, push: false },
                    { label: 'Security Alerts', email: true, inApp: true, push: true },
                    { label: 'Product Updates', email: false, inApp: false, push: false },
                    { label: 'Tips & Tutorials', email: false, inApp: true, push: false },
                  ].map((pref, i) => (
                    <tr key={i} className="border-b border-border/50">
                      <td className="py-4 px-4">{pref.label}</td>
                      <td className="py-4 px-4 text-center">
                        <Checkbox checked={pref.email} />
                      </td>
                      <td className="py-4 px-4 text-center">
                        <Checkbox checked={pref.inApp} />
                      </td>
                      <td className="py-4 px-4 text-center">
                        <Checkbox checked={pref.push} />
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
            <Button className="mt-6 bg-primary text-primary-foreground hover:bg-primary/90">
              Save Preferences
            </Button>
          </Card>

          <Card className="p-6">
            <h3 className="mb-6">Quiet Hours</h3>
            <p className="text-muted-foreground mb-4">
              Pause non-critical notifications during specific hours.
            </p>
            <div className="space-y-4 max-w-xl">
              <div className="flex items-center justify-between">
                <Label>Enable Quiet Hours</Label>
                <Switch />
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <Label>From</Label>
                  <Select defaultValue="22">
                    <SelectTrigger className="mt-2">
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="22">10:00 PM</SelectItem>
                      <SelectItem value="23">11:00 PM</SelectItem>
                    </SelectContent>
                  </Select>
                </div>
                <div>
                  <Label>To</Label>
                  <Select defaultValue="7">
                    <SelectTrigger className="mt-2">
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="7">7:00 AM</SelectItem>
                      <SelectItem value="8">8:00 AM</SelectItem>
                    </SelectContent>
                  </Select>
                </div>
              </div>
              <div>
                <Label>Timezone</Label>
                <Select defaultValue="et">
                  <SelectTrigger className="mt-2">
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="et">Eastern Time (ET)</SelectItem>
                    <SelectItem value="ct">Central Time (CT)</SelectItem>
                    <SelectItem value="pt">Pacific Time (PT)</SelectItem>
                  </SelectContent>
                </Select>
              </div>
            </div>
          </Card>
        </TabsContent>

        {/* Appearance Tab */}
        <TabsContent value="appearance" className="space-y-6">
          <Card className="p-6">
            <h3 className="mb-6">Accent Color</h3>
            <p className="text-muted-foreground mb-4">
              Choose your preferred accent color for buttons and highlights.
            </p>
            <RadioGroup defaultValue="teal" className="flex gap-4">
              {[
                { id: 'teal', color: '#00d9ff' },
                { id: 'purple', color: '#7c3aed' },
                { id: 'blue', color: '#3b82f6' },
                { id: 'green', color: '#10b981' },
                { id: 'orange', color: '#f59e0b' },
              ].map((color) => (
                <label key={color.id} className="cursor-pointer">
                  <RadioGroupItem value={color.id} className="sr-only" />
                  <div 
                    className="w-12 h-12 rounded-full border-2 border-border hover:scale-110 transition-transform data-[state=checked]:border-white data-[state=checked]:ring-2 data-[state=checked]:ring-offset-2"
                    style={{ backgroundColor: color.color }}
                  ></div>
                </label>
              ))}
            </RadioGroup>
          </Card>

          <Card className="p-6">
            <h3 className="mb-6">Display Density</h3>
            <RadioGroup defaultValue="default" className="space-y-3">
              <div className="flex items-center space-x-2">
                <RadioGroupItem value="comfortable" id="comfortable" />
                <Label htmlFor="comfortable" className="cursor-pointer">
                  <span className="font-medium">Comfortable</span>
                  <span className="text-muted-foreground ml-2">More spacing, larger touch targets</span>
                </Label>
              </div>
              <div className="flex items-center space-x-2">
                <RadioGroupItem value="default" id="default" />
                <Label htmlFor="default" className="cursor-pointer">
                  <span className="font-medium">Default</span>
                  <span className="text-muted-foreground ml-2">Balanced spacing</span>
                </Label>
              </div>
              <div className="flex items-center space-x-2">
                <RadioGroupItem value="compact" id="compact" />
                <Label htmlFor="compact" className="cursor-pointer">
                  <span className="font-medium">Compact</span>
                  <span className="text-muted-foreground ml-2">Denser layout, more content visible</span>
                </Label>
              </div>
            </RadioGroup>
          </Card>

          <Card className="p-6">
            <h3 className="mb-6">Accessibility</h3>
            <div className="space-y-4">
              <div className="flex items-center justify-between">
                <div>
                  <Label>Reduce Motion</Label>
                  <p className="text-sm text-muted-foreground">Minimize animations and transitions</p>
                </div>
                <Switch />
              </div>
              <div className="flex items-center justify-between">
                <div>
                  <Label>High Contrast Mode</Label>
                  <p className="text-sm text-muted-foreground">Increase contrast for better visibility</p>
                </div>
                <Switch />
              </div>
              <div className="flex items-center justify-between">
                <div>
                  <Label>Larger Text</Label>
                  <p className="text-sm text-muted-foreground">Increase default text size</p>
                </div>
                <Switch />
              </div>
            </div>
          </Card>
        </TabsContent>

        {/* API Tab */}
        <TabsContent value="api" className="space-y-6">
          <Card className="p-6">
            <div className="flex items-center justify-between mb-6">
              <div>
                <h3 className="mb-1">API Keys</h3>
                <p className="text-muted-foreground">Manage API keys for programmatic access to XAI-Forge.</p>
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
                  <div className="space-y-4">
                    <div>
                      <Label>Key Name</Label>
                      <Input placeholder="Production API Key" className="mt-2" />
                    </div>
                    <div>
                      <Label>Environment</Label>
                      <RadioGroup defaultValue="production" className="flex gap-4 mt-2">
                        <div className="flex items-center space-x-2">
                          <RadioGroupItem value="production" id="prod" />
                          <Label htmlFor="prod">Production</Label>
                        </div>
                        <div className="flex items-center space-x-2">
                          <RadioGroupItem value="development" id="dev" />
                          <Label htmlFor="dev">Development</Label>
                        </div>
                      </RadioGroup>
                    </div>
                    <div>
                      <Label className="mb-3 block">Permissions</Label>
                      <div className="grid grid-cols-2 gap-3">
                        <div className="flex items-center space-x-2">
                          <Checkbox id="read-datasets" defaultChecked />
                          <Label htmlFor="read-datasets">Read datasets</Label>
                        </div>
                        <div className="flex items-center space-x-2">
                          <Checkbox id="create-datasets" defaultChecked />
                          <Label htmlFor="create-datasets">Create datasets</Label>
                        </div>
                        <div className="flex items-center space-x-2">
                          <Checkbox id="read-models" defaultChecked />
                          <Label htmlFor="read-models">Read models</Label>
                        </div>
                        <div className="flex items-center space-x-2">
                          <Checkbox id="train-models" defaultChecked />
                          <Label htmlFor="train-models">Train models</Label>
                        </div>
                        <div className="flex items-center space-x-2">
                          <Checkbox id="predictions" defaultChecked />
                          <Label htmlFor="predictions">Make predictions</Label>
                        </div>
                        <div className="flex items-center space-x-2">
                          <Checkbox id="delete" />
                          <Label htmlFor="delete">Delete resources</Label>
                        </div>
                      </div>
                    </div>
                    <div className="flex gap-3">
                      <Button variant="outline" onClick={() => setApiKeyModalOpen(false)} className="flex-1">
                        Cancel
                      </Button>
                      <Button className="flex-1 bg-primary text-primary-foreground hover:bg-primary/90">
                        Generate Key
                      </Button>
                    </div>
                  </div>
                </DialogContent>
              </Dialog>
            </div>

            <div className="space-y-3">
              {[
                { name: 'Production Key', key: 'xai_live_sk••••••••••••••••••3f2a', created: 'Dec 1', lastUsed: '2 hours ago' },
                { name: 'Development Key', key: 'xai_test_sk••••••••••••••••••8b1c', created: 'Nov 15', lastUsed: '5 days ago' },
              ].map((apiKey, i) => (
                <div key={i} className="p-4 border border-border rounded-lg">
                  <div className="flex items-center justify-between mb-2">
                    <p className="font-medium">{apiKey.name}</p>
                    <span className="text-sm text-muted-foreground">Created {apiKey.created}</span>
                  </div>
                  <div className="flex items-center justify-between">
                    <code className="text-sm font-mono bg-muted px-2 py-1 rounded">{apiKey.key}</code>
                    <div className="flex gap-2">
                      <Button variant="ghost" size="sm">
                        <Copy className="w-4 h-4 mr-1" />
                        Copy
                      </Button>
                      <Button variant="ghost" size="sm" className="text-error">
                        Revoke
                      </Button>
                    </div>
                  </div>
                  <p className="text-sm text-muted-foreground mt-2">Last used: {apiKey.lastUsed}</p>
                </div>
              ))}
            </div>
          </Card>

          <Card className="p-6">
            <h3 className="mb-6">Integrations</h3>
            <div className="grid grid-cols-3 gap-4">
              {[
                { name: 'Slack', connected: true },
                { name: 'Jupyter', connected: false },
                { name: 'AWS S3', connected: false },
                { name: 'Google Cloud', connected: false },
                { name: 'GitHub', connected: true },
                { name: 'Kaggle', connected: false },
              ].map((integration, i) => (
                <Card key={i} className="p-4 text-center">
                  <div className="w-12 h-12 rounded-lg bg-muted mx-auto mb-3 flex items-center justify-center">
                    <span className="text-xl">{integration.name[0]}</span>
                  </div>
                  <p className="font-medium mb-2">{integration.name}</p>
                  <div className="flex items-center justify-center gap-2 mb-3">
                    <div className={`w-2 h-2 rounded-full ${integration.connected ? 'bg-success' : 'bg-muted'}`}></div>
                    <span className="text-sm text-muted-foreground">
                      {integration.connected ? 'Connected' : 'Not connected'}
                    </span>
                  </div>
                  <Button variant="outline" size="sm" className="w-full">
                    {integration.connected ? 'Configure' : 'Connect'}
                  </Button>
                </Card>
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
            <Button variant="outline" className="mt-6">Upgrade Storage</Button>
          </Card>

          <Card className="p-6">
            <h3 className="mb-6">Data Retention Policies</h3>
            <p className="text-muted-foreground mb-4">Configure automatic cleanup of old data.</p>
            <div className="space-y-4 max-w-xl">
              <div className="flex items-center justify-between">
                <Label>Prediction History Retention</Label>
                <Select defaultValue="90">
                  <SelectTrigger className="w-32">
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="30">30 days</SelectItem>
                    <SelectItem value="90">90 days</SelectItem>
                    <SelectItem value="365">1 year</SelectItem>
                  </SelectContent>
                </Select>
              </div>
              <div className="flex items-center justify-between">
                <Label>Failed Training Logs</Label>
                <Select defaultValue="30">
                  <SelectTrigger className="w-32">
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="7">7 days</SelectItem>
                    <SelectItem value="30">30 days</SelectItem>
                    <SelectItem value="90">90 days</SelectItem>
                  </SelectContent>
                </Select>
              </div>
            </div>
            <Button className="mt-6 bg-primary text-primary-foreground hover:bg-primary/90">
              Save Policies
            </Button>
          </Card>

          <Card className="p-6">
            <h3 className="mb-6">Export Your Data</h3>
            <p className="text-muted-foreground mb-4">Download a copy of all your data from XAI-Forge.</p>
            <div className="space-y-3 mb-4">
              <div className="flex items-center space-x-2">
                <Checkbox id="export-datasets" defaultChecked />
                <Label htmlFor="export-datasets">Datasets (CSV files)</Label>
              </div>
              <div className="flex items-center space-x-2">
                <Checkbox id="export-models" defaultChecked />
                <Label htmlFor="export-models">Trained Models (serialized)</Label>
              </div>
              <div className="flex items-center space-x-2">
                <Checkbox id="export-predictions" defaultChecked />
                <Label htmlFor="export-predictions">Prediction History (JSON)</Label>
              </div>
              <div className="flex items-center space-x-2">
                <Checkbox id="export-reports" defaultChecked />
                <Label htmlFor="export-reports">Explanation Reports (PDF)</Label>
              </div>
            </div>
            <Button className="bg-primary text-primary-foreground hover:bg-primary/90">
              <Download className="w-4 h-4 mr-2" />
              Request Export
            </Button>
            <p className="text-sm text-muted-foreground mt-4">
              Last export: Dec 5, 2024 — <Button variant="link" className="p-0 h-auto text-primary">Download</Button>
            </p>
          </Card>
        </TabsContent>

        {/* Danger Zone Tab */}
        <TabsContent value="danger" className="space-y-6">
          <Card className="p-6 border-error/30 bg-error/5">
            <div className="flex items-start gap-3 mb-6">
              <AlertTriangle className="w-6 h-6 text-error flex-shrink-0" />
              <div>
                <h3 className="text-error mb-1">Danger Zone</h3>
                <p className="text-muted-foreground">These actions are irreversible. Please proceed with caution.</p>
              </div>
            </div>

            <div className="space-y-4">
              <div className="p-4 border border-error/30 rounded-lg bg-background">
                <div className="flex items-start justify-between mb-2">
                  <div>
                    <p className="font-medium mb-1">Delete All Datasets</p>
                    <p className="text-sm text-muted-foreground">
                      Permanently remove all uploaded datasets. Your trained models will become unusable.
                    </p>
                  </div>
                  <Button variant="outline" className="border-error text-error hover:bg-error hover:text-white">
                    Delete Datasets
                  </Button>
                </div>
              </div>

              <div className="p-4 border border-error/30 rounded-lg bg-background">
                <div className="flex items-start justify-between mb-2">
                  <div>
                    <p className="font-medium mb-1">Delete All Models</p>
                    <p className="text-sm text-muted-foreground">
                      Permanently remove all trained models. Prediction history will be preserved.
                    </p>
                  </div>
                  <Button variant="outline" className="border-error text-error hover:bg-error hover:text-white">
                    Delete Models
                  </Button>
                </div>
              </div>

              <div className="p-4 border border-error/30 rounded-lg bg-background">
                <div className="flex items-start justify-between mb-2">
                  <div>
                    <p className="font-medium mb-1">Delete Account</p>
                    <p className="text-sm text-muted-foreground">
                      Permanently delete your account and all associated data. This action cannot be undone.
                    </p>
                  </div>
                  <Dialog open={deleteAccountModalOpen} onOpenChange={setDeleteAccountModalOpen}>
                    <DialogTrigger asChild>
                      <Button variant="outline" className="border-error text-error hover:bg-error hover:text-white">
                        Delete Account
                      </Button>
                    </DialogTrigger>
                    <DialogContent>
                      <DialogHeader>
                        <DialogTitle className="flex items-center gap-2 text-error">
                          <AlertTriangle className="w-5 h-5" />
                          Delete Account
                        </DialogTitle>
                      </DialogHeader>
                      <div className="space-y-4">
                        <div className="p-4 bg-error/10 border border-error/20 rounded-lg">
                          <p className="font-medium mb-2">This will permanently delete:</p>
                          <ul className="space-y-1 text-sm">
                            <li>• 12 datasets</li>
                            <li>• 8 trained models</li>
                            <li>• 156 prediction records</li>
                            <li>• All API keys and integrations</li>
                          </ul>
                        </div>
                        <div>
                          <Label>Type "DELETE" to confirm:</Label>
                          <Input
                            value={deleteConfirmText}
                            onChange={(e) => setDeleteConfirmText(e.target.value)}
                            placeholder="DELETE"
                            className="mt-2"
                          />
                        </div>
                        <div>
                          <Label>Enter your password:</Label>
                          <Input type="password" className="mt-2" />
                        </div>
                        <div className="flex gap-3">
                          <Button variant="outline" onClick={() => setDeleteAccountModalOpen(false)} className="flex-1">
                            Cancel
                          </Button>
                          <Button
                            disabled={deleteConfirmText !== 'DELETE'}
                            className="flex-1 bg-error text-white hover:bg-error/90"
                          >
                            Permanently Delete Account
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
