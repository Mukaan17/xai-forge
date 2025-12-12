-- Migration: Create new tables for enhanced features
-- This migration creates all new entity tables

-- Create predictions table
CREATE TABLE IF NOT EXISTS predictions (
    id BIGSERIAL PRIMARY KEY,
    model_id BIGINT NOT NULL REFERENCES ml_models(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    input_data JSONB NOT NULL,
    prediction_result VARCHAR(500) NOT NULL,
    confidence DOUBLE PRECISION NOT NULL,
    explanation JSONB,
    explanation_summary TEXT,
    prediction_time_ms BIGINT,
    explanation_time_ms BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create api_keys table
CREATE TABLE IF NOT EXISTS api_keys (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    key_hash VARCHAR(64) NOT NULL UNIQUE,
    key_prefix VARCHAR(20) NOT NULL,
    key_suffix VARCHAR(4) NOT NULL,
    environment VARCHAR(20) NOT NULL,
    permissions JSONB NOT NULL,
    active BOOLEAN NOT NULL DEFAULT true,
    expires_at TIMESTAMP,
    last_used_at TIMESTAMP,
    last_used_ip VARCHAR(45),
    usage_count BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    description VARCHAR(500)
);

-- Create user_sessions table
CREATE TABLE IF NOT EXISTS user_sessions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    session_token VARCHAR(100) NOT NULL UNIQUE,
    refresh_token_hash VARCHAR(64),
    device_info VARCHAR(200),
    user_agent VARCHAR(500),
    ip_address VARCHAR(45) NOT NULL,
    location VARCHAR(200),
    country_code VARCHAR(2),
    is_active BOOLEAN NOT NULL DEFAULT true,
    last_active_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    revoked_at TIMESTAMP,
    revocation_reason VARCHAR(100)
);

-- Create notifications table
CREATE TABLE IF NOT EXISTS notifications (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    type VARCHAR(30) NOT NULL,
    title VARCHAR(200) NOT NULL,
    message TEXT NOT NULL,
    metadata JSONB,
    is_read BOOLEAN NOT NULL DEFAULT false,
    read_at TIMESTAMP,
    priority VARCHAR(10) NOT NULL DEFAULT 'NORMAL',
    action_url VARCHAR(500),
    action_label VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP
);

-- Create user_preferences table
CREATE TABLE IF NOT EXISTS user_preferences (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    theme VARCHAR(10) NOT NULL DEFAULT 'DARK',
    accent_color VARCHAR(6) NOT NULL DEFAULT '00d9ff',
    display_density VARCHAR(15) NOT NULL DEFAULT 'DEFAULT',
    reduce_motion BOOLEAN NOT NULL DEFAULT false,
    high_contrast BOOLEAN NOT NULL DEFAULT false,
    font_size_multiplier DOUBLE PRECISION NOT NULL DEFAULT 1.0,
    email_notifications JSONB NOT NULL DEFAULT '{}',
    in_app_notifications JSONB NOT NULL DEFAULT '{}',
    push_notifications JSONB NOT NULL DEFAULT '{}',
    quiet_hours_enabled BOOLEAN NOT NULL DEFAULT false,
    quiet_hours_start TIME,
    quiet_hours_end TIME,
    timezone VARCHAR(50) NOT NULL DEFAULT 'America/New_York',
    default_classification_algorithm VARCHAR(50) NOT NULL DEFAULT 'LOGISTIC_REGRESSION',
    default_regression_algorithm VARCHAR(50) NOT NULL DEFAULT 'LINEAR_REGRESSION',
    auto_detect_column_types BOOLEAN NOT NULL DEFAULT true,
    auto_exclude_id_columns BOOLEAN NOT NULL DEFAULT true,
    default_preview_rows INTEGER NOT NULL DEFAULT 5,
    prediction_retention_days INTEGER NOT NULL DEFAULT 90,
    failed_training_retention_days INTEGER NOT NULL DEFAULT 30,
    deleted_dataset_retention_days INTEGER NOT NULL DEFAULT 7,
    sidebar_collapsed BOOLEAN NOT NULL DEFAULT false,
    dataset_view VARCHAR(10) NOT NULL DEFAULT 'GRID',
    items_per_page INTEGER NOT NULL DEFAULT 20,
    show_onboarding BOOLEAN NOT NULL DEFAULT true,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create activity_logs table
CREATE TABLE IF NOT EXISTS activity_logs (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id) ON DELETE SET NULL,
    action VARCHAR(30) NOT NULL,
    resource_type VARCHAR(30),
    resource_id BIGINT,
    resource_name VARCHAR(200),
    description VARCHAR(500),
    metadata JSONB,
    success BOOLEAN NOT NULL DEFAULT true,
    error_message VARCHAR(1000),
    ip_address VARCHAR(45) NOT NULL,
    user_agent VARCHAR(500),
    device_info VARCHAR(200),
    location VARCHAR(200),
    session_id VARCHAR(100),
    api_key_id BIGINT,
    duration_ms BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create webhooks table
CREATE TABLE IF NOT EXISTS webhooks (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    url VARCHAR(500) NOT NULL,
    secret VARCHAR(100) NOT NULL,
    events JSONB NOT NULL,
    active BOOLEAN NOT NULL DEFAULT true,
    description VARCHAR(500),
    last_triggered_at TIMESTAMP,
    last_response_code INTEGER,
    last_response_body VARCHAR(1000),
    failure_count INTEGER NOT NULL DEFAULT 0,
    success_count BIGINT NOT NULL DEFAULT 0,
    auto_disabled BOOLEAN NOT NULL DEFAULT false,
    auto_disabled_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create export_jobs table
CREATE TABLE IF NOT EXISTS export_jobs (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    export_type VARCHAR(20) NOT NULL,
    include_items JSONB NOT NULL,
    format VARCHAR(10) NOT NULL DEFAULT 'ZIP',
    progress INTEGER NOT NULL DEFAULT 0,
    current_step VARCHAR(200),
    file_path VARCHAR(500),
    file_size_bytes BIGINT,
    error_message TEXT,
    metadata JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    expires_at TIMESTAMP,
    download_count INTEGER NOT NULL DEFAULT 0
);
