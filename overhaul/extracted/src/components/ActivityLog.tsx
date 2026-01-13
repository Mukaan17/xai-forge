import { Download, Filter, Calendar, Lock, BrainCircuit, Database, Target, Key } from 'lucide-react';
import { Button } from './ui/button';
import { Card } from './ui/card';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from './ui/select';

export function ActivityLog() {
  const activities = [
    {
      date: 'December 9, 2024',
      events: [
        { time: '10:45 AM', icon: Target, color: 'text-success', type: 'Prediction Made', details: 'Model: Churn Predictor v3\nResult: Will Churn (87% confidence)\nIP: 192.168.1.x' },
        { time: '10:23 AM', icon: Lock, color: 'text-primary', type: 'Login Successful', details: 'Browser: Chrome on MacOS\nLocation: New York, US\nIP: 192.168.1.x' },
      ],
    },
    {
      date: 'December 8, 2024',
      events: [
        { time: '4:30 PM', icon: BrainCircuit, color: 'text-secondary', type: 'Model Trained', details: 'Model: Churn Predictor v3\nDataset: customer_data_q4.csv\nDuration: 18 seconds\nResult: Success (89.2% accuracy)' },
        { time: '3:15 PM', icon: Database, color: 'text-primary', type: 'Dataset Uploaded', details: 'File: customer_data_q4.csv\nSize: 2.4 MB (15,420 rows × 12 columns)' },
        { time: '2:00 PM', icon: Key, color: 'text-warning', type: 'API Key Generated', details: 'Name: Production Key\nPermissions: Read, Write, Predict' },
      ],
    },
    {
      date: 'December 7, 2024',
      events: [
        { time: '5:45 PM', icon: Target, color: 'text-success', type: 'Prediction Made', details: 'Model: Revenue Forecaster\nResult: $125,420 (92% confidence)\nIP: 192.168.1.x' },
        { time: '2:30 PM', icon: BrainCircuit, color: 'text-secondary', type: 'Model Trained', details: 'Model: Revenue Forecaster\nDataset: sales_history.csv\nDuration: 24 seconds\nResult: Success (92.1% accuracy)' },
        { time: '9:15 AM', icon: Lock, color: 'text-primary', type: 'Login Successful', details: 'Browser: Safari on iPhone\nLocation: New York, US\nIP: 192.168.1.y' },
      ],
    },
    {
      date: 'December 6, 2024',
      events: [
        { time: '11:02 PM', icon: Lock, color: 'text-error', type: 'Failed Login Attempt', details: 'Browser: Unknown\nLocation: Unknown\nIP: 192.168.x.x (Blocked after 3 attempts)' },
        { time: '3:20 PM', icon: Database, color: 'text-primary', type: 'Dataset Updated', details: 'File: customer_data_2024.csv\nChanges: 145 new rows added' },
      ],
    },
  ];

  return (
    <div className="p-8 space-y-6">
      {/* Header */}
      <div className="flex items-start justify-between">
        <div>
          <h1>Activity Log</h1>
          <p className="text-muted-foreground mt-1">Complete audit trail of all actions in your account</p>
        </div>
        <div className="flex gap-3">
          <Button variant="outline">
            <Filter className="w-4 h-4 mr-2" />
            Filter
          </Button>
          <Select defaultValue="30">
            <SelectTrigger className="w-40">
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="7">Last 7 days</SelectItem>
              <SelectItem value="30">Last 30 days</SelectItem>
              <SelectItem value="90">Last 90 days</SelectItem>
            </SelectContent>
          </Select>
          <Button className="bg-primary text-primary-foreground hover:bg-primary/90">
            <Download className="w-4 h-4 mr-2" />
            Export
          </Button>
        </div>
      </div>

      {/* Activity Timeline */}
      <div className="space-y-8">
        {activities.map((day, dayIndex) => (
          <div key={dayIndex}>
            <div className="flex items-center gap-3 mb-6">
              <Calendar className="w-5 h-5 text-muted-foreground" />
              <h3>{day.date}</h3>
              <div className="flex-1 h-px bg-border"></div>
            </div>

            <div className="space-y-4 ml-8">
              {day.events.map((event, eventIndex) => (
                <Card key={eventIndex} className="p-4 hover:border-primary/30 transition-colors">
                  <div className="flex gap-4">
                    <div className="flex flex-col items-center">
                      <div className={`w-10 h-10 rounded-lg bg-gradient-to-br ${event.color} from-current/10 to-current/5 flex items-center justify-center flex-shrink-0`}>
                        <event.icon className={`w-5 h-5 ${event.color}`} />
                      </div>
                      {eventIndex < day.events.length - 1 && (
                        <div className="w-px h-full bg-border mt-2"></div>
                      )}
                    </div>
                    <div className="flex-1">
                      <div className="flex items-center justify-between mb-1">
                        <p className="font-medium">{event.type}</p>
                        <span className="text-sm text-muted-foreground">{event.time}</span>
                      </div>
                      <pre className="text-sm text-muted-foreground whitespace-pre-line font-sans">
                        {event.details}
                      </pre>
                    </div>
                  </div>
                </Card>
              ))}
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
