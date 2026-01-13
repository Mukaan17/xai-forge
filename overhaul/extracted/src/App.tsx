import { useState, useEffect } from 'react';
import { NavigationFixed } from './components/NavigationFixed';
import { Dashboard } from './components/Dashboard';
import { DatasetsEnhanced } from './components/DatasetsEnhanced';
import { ModelTrainingEnhanced } from './components/ModelTrainingEnhanced';
import { ModelDetails } from './components/ModelDetails';
import { Predictions } from './components/Predictions';
import { UserProfile } from './components/UserProfile';
import { SettingsEnhanced } from './components/SettingsEnhanced';
import { NotificationCenter } from './components/NotificationCenter';
import { ModelComparison } from './components/ModelComparison';
import { PredictionHistory } from './components/PredictionHistory';
import { ActivityLog } from './components/ActivityLog';
import { OnboardingFlow } from './components/OnboardingFlow';
import { ErrorPages } from './components/ErrorPages';
import { HelpCenter } from './components/HelpCenter';
import { Toaster } from './components/Toaster';
import { useStore } from './lib/store';

export default function App() {
  const {
    currentPage,
    setCurrentPage,
    showNotifications,
    setShowNotifications,
    showOnboarding,
    setShowOnboarding,
  } = useStore();

  const renderPage = () => {
    switch (currentPage) {
      case 'dashboard':
        return <Dashboard onNavigate={setCurrentPage} />;
      case 'datasets':
      case 'datasets-all':
      case 'datasets-upload':
        return <DatasetsEnhanced onNavigate={setCurrentPage} />;
      case 'models-train':
        return <ModelTrainingEnhanced onNavigate={setCurrentPage} />;
      case 'models-all':
        return <ModelDetails onNavigate={setCurrentPage} />;
      case 'models-compare':
        return <ModelComparison />;
      case 'predictions-new':
      case 'predictions':
        return <Predictions onNavigate={setCurrentPage} />;
      case 'predictions-history':
        return <PredictionHistory onNavigate={setCurrentPage} />;
      case 'profile':
        return <UserProfile />;
      case 'settings':
      case 'settings-security':
        return <SettingsEnhanced />;
      case 'activity-log':
        return <ActivityLog />;
      case 'help':
        return <HelpCenter />;
      case '404':
        return <ErrorPages type="404" onNavigate={setCurrentPage} />;
      case '500':
        return <ErrorPages type="500" onNavigate={setCurrentPage} />;
      default:
        return <Dashboard onNavigate={setCurrentPage} />;
    }
  };

  return (
    <div className="min-h-screen bg-background">
      <Toaster />
      
      {showOnboarding && (
        <OnboardingFlow
          onComplete={() => setShowOnboarding(false)}
          onSkip={() => setShowOnboarding(false)}
        />
      )}

      <NavigationFixed 
        currentPage={currentPage} 
        onNavigate={setCurrentPage}
        onNotificationsClick={() => setShowNotifications(!showNotifications)}
      />
      
      {/* Main Content Area */}
      <div className="pt-16 pl-60">
        {renderPage()}
      </div>

      {/* Notification Center Slide-out */}
      {showNotifications && (
        <>
          <div 
            className="fixed inset-0 bg-black/50 z-40"
            onClick={() => setShowNotifications(false)}
          ></div>
          <NotificationCenter 
            onClose={() => setShowNotifications(false)}
            onNavigate={(page) => {
              setCurrentPage(page);
              setShowNotifications(false);
            }}
          />
        </>
      )}
    </div>
  );
}