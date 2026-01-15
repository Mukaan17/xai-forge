-- Create export_jobs table for GDPR-compliant data exports

CREATE TABLE IF NOT EXISTS export_jobs (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    progress INT NOT NULL DEFAULT 0,
    current_step VARCHAR(255),
    file_path VARCHAR(500),
    file_size BIGINT,
    error_message TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP,
    expires_at TIMESTAMP,
    
    CONSTRAINT fk_export_job_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_export_jobs_user_id ON export_jobs(user_id);
CREATE INDEX IF NOT EXISTS idx_export_jobs_status ON export_jobs(status);
CREATE INDEX IF NOT EXISTS idx_export_jobs_expires_at ON export_jobs(expires_at);
