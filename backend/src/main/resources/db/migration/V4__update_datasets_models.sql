-- Migration: Update datasets and ml_models tables with new fields
-- This migration adds comprehensive metadata fields to existing tables

-- Update datasets table
ALTER TABLE datasets ADD COLUMN IF NOT EXISTS name VARCHAR(200);
ALTER TABLE datasets ADD COLUMN IF NOT EXISTS description TEXT;
ALTER TABLE datasets ADD COLUMN IF NOT EXISTS original_filename VARCHAR(255);
ALTER TABLE datasets ADD COLUMN IF NOT EXISTS file_size_bytes BIGINT;
ALTER TABLE datasets ADD COLUMN IF NOT EXISTS mime_type VARCHAR(100) DEFAULT 'text/csv';
ALTER TABLE datasets ADD COLUMN IF NOT EXISTS column_count INTEGER;
ALTER TABLE datasets ADD COLUMN IF NOT EXISTS status VARCHAR(20) DEFAULT 'UPLOADING' NOT NULL;
ALTER TABLE datasets ADD COLUMN IF NOT EXISTS processing_error TEXT;
ALTER TABLE datasets ADD COLUMN IF NOT EXISTS column_metadata JSONB;
ALTER TABLE datasets ADD COLUMN IF NOT EXISTS column_names JSONB;
ALTER TABLE datasets ADD COLUMN IF NOT EXISTS target_column VARCHAR(100);
ALTER TABLE datasets ADD COLUMN IF NOT EXISTS recommended_target VARCHAR(100);
ALTER TABLE datasets ADD COLUMN IF NOT EXISTS inferred_task_type VARCHAR(20);
ALTER TABLE datasets ADD COLUMN IF NOT EXISTS quality_score INTEGER;
ALTER TABLE datasets ADD COLUMN IF NOT EXISTS quality_issues JSONB;
ALTER TABLE datasets ADD COLUMN IF NOT EXISTS sample_rows JSONB;
ALTER TABLE datasets ADD COLUMN IF NOT EXISTS deleted BOOLEAN DEFAULT false NOT NULL;
ALTER TABLE datasets ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE datasets ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL;
ALTER TABLE datasets ADD COLUMN IF NOT EXISTS processed_at TIMESTAMP;

-- Migrate existing data: set name from fileName if name is null
UPDATE datasets SET name = COALESCE(file_name, 'Untitled Dataset') WHERE name IS NULL;

-- Migrate existing data: set original_filename from fileName
UPDATE datasets SET original_filename = file_name WHERE original_filename IS NULL AND file_name IS NOT NULL;

-- Migrate existing data: set column_names from headers (if headers table exists)
-- Note: This assumes headers were stored in a separate table. Adjust based on actual schema.
-- If headers are in a JSONB column, migrate them directly

-- Update ml_models table
ALTER TABLE ml_models ADD COLUMN IF NOT EXISTS user_id BIGINT REFERENCES users(id) ON DELETE CASCADE;
ALTER TABLE ml_models ADD COLUMN IF NOT EXISTS name VARCHAR(200);
ALTER TABLE ml_models ADD COLUMN IF NOT EXISTS description TEXT;
ALTER TABLE ml_models ADD COLUMN IF NOT EXISTS version INTEGER DEFAULT 1 NOT NULL;
ALTER TABLE ml_models ADD COLUMN IF NOT EXISTS base_name VARCHAR(200);
ALTER TABLE ml_models ADD COLUMN IF NOT EXISTS algorithm VARCHAR(50) NOT NULL DEFAULT 'LOGISTIC_REGRESSION';
ALTER TABLE ml_models ADD COLUMN IF NOT EXISTS target_column VARCHAR(100) NOT NULL DEFAULT '';
ALTER TABLE ml_models ADD COLUMN IF NOT EXISTS feature_columns JSONB NOT NULL DEFAULT '[]';
ALTER TABLE ml_models ADD COLUMN IF NOT EXISTS status VARCHAR(20) DEFAULT 'TRAINING' NOT NULL;
ALTER TABLE ml_models ADD COLUMN IF NOT EXISTS model_path VARCHAR(500);
ALTER TABLE ml_models ADD COLUMN IF NOT EXISTS model_size_bytes BIGINT;
ALTER TABLE ml_models ADD COLUMN IF NOT EXISTS precision_score DOUBLE PRECISION;
ALTER TABLE ml_models ADD COLUMN IF NOT EXISTS recall_score DOUBLE PRECISION;
ALTER TABLE ml_models ADD COLUMN IF NOT EXISTS f1_score DOUBLE PRECISION;
ALTER TABLE ml_models ADD COLUMN IF NOT EXISTS mse DOUBLE PRECISION;
ALTER TABLE ml_models ADD COLUMN IF NOT EXISTS rmse DOUBLE PRECISION;
ALTER TABLE ml_models ADD COLUMN IF NOT EXISTS mae DOUBLE PRECISION;
ALTER TABLE ml_models ADD COLUMN IF NOT EXISTS r2_score DOUBLE PRECISION;
ALTER TABLE ml_models ADD COLUMN IF NOT EXISTS confusion_matrix JSONB;
ALTER TABLE ml_models ADD COLUMN IF NOT EXISTS class_labels JSONB;
ALTER TABLE ml_models ADD COLUMN IF NOT EXISTS feature_importance JSONB;
ALTER TABLE ml_models ADD COLUMN IF NOT EXISTS training_history JSONB;
ALTER TABLE ml_models ADD COLUMN IF NOT EXISTS training_duration_ms BIGINT;
ALTER TABLE ml_models ADD COLUMN IF NOT EXISTS training_samples INTEGER;
ALTER TABLE ml_models ADD COLUMN IF NOT EXISTS test_samples INTEGER;
ALTER TABLE ml_models ADD COLUMN IF NOT EXISTS train_test_split DOUBLE PRECISION DEFAULT 0.8;
ALTER TABLE ml_models ADD COLUMN IF NOT EXISTS hyperparameters JSONB;
ALTER TABLE ml_models ADD COLUMN IF NOT EXISTS training_error TEXT;
ALTER TABLE ml_models ADD COLUMN IF NOT EXISTS training_progress INTEGER DEFAULT 0;
ALTER TABLE ml_models ADD COLUMN IF NOT EXISTS training_step VARCHAR(200);
ALTER TABLE ml_models ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL;
ALTER TABLE ml_models ADD COLUMN IF NOT EXISTS trained_at TIMESTAMP;
ALTER TABLE ml_models ADD COLUMN IF NOT EXISTS last_used_at TIMESTAMP;
ALTER TABLE ml_models ADD COLUMN IF NOT EXISTS archived_at TIMESTAMP;
ALTER TABLE ml_models ADD COLUMN IF NOT EXISTS prediction_count BIGINT DEFAULT 0 NOT NULL;

-- Migrate existing data: set name from modelName
UPDATE ml_models SET name = COALESCE(model_name, 'Untitled Model') WHERE name IS NULL;

-- Migrate existing data: set model_path from serializedModelPath
UPDATE ml_models SET model_path = serialized_model_path WHERE model_path IS NULL AND serialized_model_path IS NOT NULL;

-- Migrate existing data: set target_column from targetVariable
UPDATE ml_models SET target_column = target_variable WHERE target_column IS NULL OR target_column = '' AND target_variable IS NOT NULL;

-- Migrate existing data: set feature_columns from featureNames (if stored separately)
-- Note: Adjust based on actual schema. If featureNames is in a separate table, migrate accordingly.

-- Set user_id from dataset owner if not set
UPDATE ml_models m SET user_id = d.user_id 
FROM datasets d 
WHERE m.dataset_id = d.id AND m.user_id IS NULL;

-- Change dataset relationship: allow multiple models per dataset
-- Remove unique constraint if exists (PostgreSQL doesn't enforce OneToOne at DB level, but check anyway)
-- The relationship is already ManyToOne in the entity, so no DB constraint change needed
