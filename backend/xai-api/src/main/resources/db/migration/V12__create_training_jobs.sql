-- Create training_jobs table for tracking async training progress
CREATE TABLE IF NOT EXISTS training_jobs (
    id BIGSERIAL PRIMARY KEY,
    model_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    progress INTEGER NOT NULL DEFAULT 0,
    current_step VARCHAR(500),
    error_message TEXT,
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    estimated_completion_seconds BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_training_job_model FOREIGN KEY (model_id) REFERENCES ml_models(id) ON DELETE CASCADE,
    CONSTRAINT fk_training_job_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Create index for faster lookups
CREATE INDEX IF NOT EXISTS idx_training_jobs_model_id ON training_jobs(model_id);
CREATE INDEX IF NOT EXISTS idx_training_jobs_user_id ON training_jobs(user_id);
CREATE INDEX IF NOT EXISTS idx_training_jobs_status ON training_jobs(status);
