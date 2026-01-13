-- Initial schema migration
-- This migration creates the base tables that already exist
-- It's kept for reference and to ensure Flyway baseline is set correctly

-- Users table (already exists)
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP,
    two_factor_enabled BOOLEAN DEFAULT FALSE,
    two_factor_secret VARCHAR(255)
);

-- Datasets table (already exists)
CREATE TABLE IF NOT EXISTS datasets (
    id BIGSERIAL PRIMARY KEY,
    file_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(255) NOT NULL,
    upload_date TIMESTAMP,
    row_count BIGINT,
    owner_id BIGINT NOT NULL,
    FOREIGN KEY (owner_id) REFERENCES users(id)
);

-- Dataset headers table (already exists)
CREATE TABLE IF NOT EXISTS dataset_headers (
    dataset_id BIGINT NOT NULL,
    header VARCHAR(255),
    FOREIGN KEY (dataset_id) REFERENCES datasets(id)
);

-- ML Models table (already exists)
CREATE TABLE IF NOT EXISTS ml_models (
    id BIGSERIAL PRIMARY KEY,
    model_name VARCHAR(255) NOT NULL,
    model_type VARCHAR(50) NOT NULL,
    serialized_model_path VARCHAR(255) NOT NULL,
    training_date TIMESTAMP,
    target_variable VARCHAR(255) NOT NULL,
    dataset_id BIGINT NOT NULL,
    accuracy DOUBLE PRECISION,
    model_metadata TEXT,
    FOREIGN KEY (dataset_id) REFERENCES datasets(id)
);

-- Model features table (already exists)
CREATE TABLE IF NOT EXISTS model_features (
    model_id BIGINT NOT NULL,
    feature_name VARCHAR(255),
    FOREIGN KEY (model_id) REFERENCES ml_models(id)
);

