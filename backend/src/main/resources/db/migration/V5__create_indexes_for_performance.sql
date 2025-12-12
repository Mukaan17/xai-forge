-- Migration: Create indexes for performance optimization
-- This migration creates all necessary indexes for efficient queries

-- Indexes for predictions table
CREATE INDEX IF NOT EXISTS idx_prediction_user_id ON predictions(user_id);
CREATE INDEX IF NOT EXISTS idx_prediction_model_id ON predictions(model_id);
CREATE INDEX IF NOT EXISTS idx_prediction_created_at ON predictions(created_at);
CREATE INDEX IF NOT EXISTS idx_prediction_user_created ON predictions(user_id, created_at DESC);

-- Indexes for api_keys table
CREATE INDEX IF NOT EXISTS idx_api_key_user_id ON api_keys(user_id);
CREATE INDEX IF NOT EXISTS idx_api_key_hash ON api_keys(key_hash);
CREATE INDEX IF NOT EXISTS idx_api_key_active ON api_keys(user_id, active);

-- Indexes for user_sessions table
CREATE INDEX IF NOT EXISTS idx_session_user_id ON user_sessions(user_id);
CREATE INDEX IF NOT EXISTS idx_session_token ON user_sessions(session_token);
CREATE INDEX IF NOT EXISTS idx_session_active ON user_sessions(user_id, is_active);

-- Indexes for notifications table
CREATE INDEX IF NOT EXISTS idx_notification_user_id ON notifications(user_id);
CREATE INDEX IF NOT EXISTS idx_notification_read ON notifications(user_id, is_read);
CREATE INDEX IF NOT EXISTS idx_notification_created ON notifications(user_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_notification_type ON notifications(user_id, type);

-- Indexes for activity_logs table
CREATE INDEX IF NOT EXISTS idx_activity_user_id ON activity_logs(user_id);
CREATE INDEX IF NOT EXISTS idx_activity_created ON activity_logs(user_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_activity_action ON activity_logs(user_id, action);
CREATE INDEX IF NOT EXISTS idx_activity_resource ON activity_logs(resource_type, resource_id);
CREATE INDEX IF NOT EXISTS idx_activity_date_range ON activity_logs(created_at);

-- Indexes for webhooks table
CREATE INDEX IF NOT EXISTS idx_webhook_user_id ON webhooks(user_id);
CREATE INDEX IF NOT EXISTS idx_webhook_active ON webhooks(user_id, active);

-- Indexes for export_jobs table
CREATE INDEX IF NOT EXISTS idx_export_user_id ON export_jobs(user_id);
CREATE INDEX IF NOT EXISTS idx_export_status ON export_jobs(user_id, status);

-- Indexes for datasets table
CREATE INDEX IF NOT EXISTS idx_dataset_user_id ON datasets(user_id);
CREATE INDEX IF NOT EXISTS idx_dataset_status ON datasets(user_id, status);
CREATE INDEX IF NOT EXISTS idx_dataset_created ON datasets(user_id, created_at DESC);

-- Indexes for ml_models table
CREATE INDEX IF NOT EXISTS idx_model_user_id ON ml_models(user_id);
CREATE INDEX IF NOT EXISTS idx_model_dataset_id ON ml_models(dataset_id);
CREATE INDEX IF NOT EXISTS idx_model_status ON ml_models(user_id, status);
CREATE INDEX IF NOT EXISTS idx_model_created ON ml_models(user_id, created_at DESC);
