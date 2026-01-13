import { Edit2, Camera, CheckCircle2, Link2 } from 'lucide-react';
import { Button } from './ui/button';
import { Input } from './ui/input';
import { Label } from './ui/label';
import { Card } from './ui/card';
import { Textarea } from './ui/textarea';
import { Badge } from './ui/badge';

export function UserProfile() {
  return (
    <div className="p-8 flex justify-center">
      <div className="w-full max-w-3xl space-y-6">
        {/* Profile Header Section */}
        <Card className="p-8">
          <div className="flex items-start gap-6">
            <div className="relative group">
              <div className="w-32 h-32 rounded-full bg-gradient-to-br from-primary/20 to-secondary/20 flex items-center justify-center">
                <span className="text-4xl font-semibold">MC</span>
              </div>
              <button className="absolute inset-0 rounded-full bg-black/50 opacity-0 group-hover:opacity-100 transition-opacity flex items-center justify-center">
                <Camera className="w-6 h-6 text-white" />
              </button>
            </div>
            <div className="flex-1">
              <div className="flex items-center gap-3 mb-2">
                <h1>Mukaan Chaturvedi</h1>
                <Button variant="ghost" size="icon" className="w-8 h-8">
                  <Edit2 className="w-4 h-4" />
                </Button>
              </div>
              <div className="space-y-1 text-muted-foreground">
                <div className="flex items-center gap-2">
                  <span>mukaan@example.com</span>
                  <Badge variant="outline" className="border-success/30 text-success">
                    <CheckCircle2 className="w-3 h-3 mr-1" />
                    Verified
                  </Badge>
                </div>
                <p>Member since Dec 2024</p>
              </div>
              <Button variant="outline" className="mt-4" size="sm">
                <Camera className="w-4 h-4 mr-2" />
                Change Photo
              </Button>
            </div>
          </div>
        </Card>

        {/* Profile Information Card */}
        <Card className="p-6">
          <div className="flex items-center justify-between mb-6">
            <h3>Personal Information</h3>
            <Button variant="ghost" size="sm" className="text-primary">
              <Edit2 className="w-4 h-4 mr-2" />
              Edit
            </Button>
          </div>
          
          <div className="grid grid-cols-2 gap-6">
            <div>
              <Label>First Name</Label>
              <Input value="Mukaan" readOnly className="mt-2" />
            </div>
            <div>
              <Label>Last Name</Label>
              <Input value="Chaturvedi" readOnly className="mt-2" />
            </div>
            <div className="col-span-2">
              <Label>Email</Label>
              <div className="flex items-center gap-2 mt-2">
                <Input value="mukaan@example.com" readOnly className="flex-1" />
                <Badge variant="outline" className="border-success/30 text-success">
                  <CheckCircle2 className="w-3 h-3 mr-1" />
                  Verified
                </Badge>
              </div>
            </div>
            <div>
              <Label>Organization</Label>
              <Input value="New York University" readOnly className="mt-2" />
            </div>
            <div>
              <Label>Role</Label>
              <Input value="Data Scientist" readOnly className="mt-2" />
            </div>
            <div>
              <Label>Location</Label>
              <Input value="New York, NY" readOnly className="mt-2" />
            </div>
            <div className="col-span-2">
              <Label>Bio</Label>
              <Textarea
                value="ML enthusiast focused on explainable AI and responsible machine learning. Currently researching fairness in predictive models."
                readOnly
                className="mt-2"
                rows={3}
              />
            </div>
          </div>
        </Card>

        {/* Account Statistics Card */}
        <Card className="p-6">
          <h3 className="mb-6">Your Activity</h3>
          
          <div className="grid grid-cols-4 gap-6 mb-6">
            {[
              { value: '12', label: 'Datasets Uploaded' },
              { value: '8', label: 'Models Trained' },
              { value: '156', label: 'Predictions Made' },
              { value: '87.3%', label: 'Avg Acc.' },
            ].map((stat, i) => (
              <div key={i} className="text-center p-4 rounded-lg bg-muted/30">
                <p className="text-3xl font-semibold mb-1">{stat.value}</p>
                <p className="text-sm text-muted-foreground">{stat.label}</p>
              </div>
            ))}
          </div>

          <div className="flex items-center justify-center gap-2 text-sm text-muted-foreground">
            <span>Member for 45 days</span>
            <span>•</span>
            <span>Last active 2 hours ago</span>
          </div>
        </Card>

        {/* Linked Accounts Section */}
        <Card className="p-6">
          <h3 className="mb-6">Connected Accounts</h3>
          
          <div className="space-y-4">
            <div className="flex items-center justify-between p-4 border border-border rounded-lg">
              <div className="flex items-center gap-4">
                <div className="w-10 h-10 rounded-lg bg-muted flex items-center justify-center">
                  <span className="text-xl">G</span>
                </div>
                <div>
                  <p className="font-medium">Google</p>
                  <p className="text-sm text-muted-foreground">mukaan@example.com</p>
                </div>
              </div>
              <div className="flex items-center gap-3">
                <Badge variant="outline" className="border-success/30 text-success">Connected</Badge>
                <Button variant="ghost" size="sm" className="text-error">
                  Disconnect
                </Button>
              </div>
            </div>

            <div className="flex items-center justify-between p-4 border border-border rounded-lg">
              <div className="flex items-center gap-4">
                <div className="w-10 h-10 rounded-lg bg-muted flex items-center justify-center">
                  <span className="text-xl">GH</span>
                </div>
                <div>
                  <p className="font-medium">GitHub</p>
                  <p className="text-sm text-muted-foreground">Not connected</p>
                </div>
              </div>
              <Button variant="outline" size="sm">
                <Link2 className="w-4 h-4 mr-2" />
                Connect
              </Button>
            </div>
          </div>
        </Card>
      </div>
    </div>
  );
}
