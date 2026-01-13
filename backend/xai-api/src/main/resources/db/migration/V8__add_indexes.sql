-- Add performance indexes

CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);
CREATE INDEX IF NOT EXISTS idx_datasets_owner_id ON datasets(owner_id);
CREATE INDEX IF NOT EXISTS idx_ml_models_dataset_id ON ml_models(dataset_id);
CREATE INDEX IF NOT EXISTS idx_ml_models_status ON ml_models(status);

