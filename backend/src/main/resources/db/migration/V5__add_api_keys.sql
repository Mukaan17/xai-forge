-- Add API keys table

CREATE TABLE IF NOT EXISTS api_keys (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    hashed_key VARCHAR(255) NOT NULL UNIQUE,
    permissions TEXT,
    created_at TIMESTAMP NOT NULL,
    last_used TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_api_keys_user_id ON api_keys(user_id);
CREATE INDEX idx_api_keys_hashed_key ON api_keys(hashed_key);

