-- Add prediction history table

CREATE TABLE IF NOT EXISTS prediction_records (
    id BIGSERIAL PRIMARY KEY,
    model_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    input_data TEXT,
    prediction TEXT,
    confidence DOUBLE PRECISION,
    explanation TEXT,
    created_at TIMESTAMP,
    FOREIGN KEY (model_id) REFERENCES ml_models(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_prediction_records_user_id ON prediction_records(user_id);
CREATE INDEX idx_prediction_records_model_id ON prediction_records(model_id);
CREATE INDEX idx_prediction_records_created_at ON prediction_records(created_at);

