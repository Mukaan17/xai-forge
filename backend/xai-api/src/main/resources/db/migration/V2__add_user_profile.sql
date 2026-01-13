-- Add user profile and preferences tables

CREATE TABLE IF NOT EXISTS user_profiles (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    first_name VARCHAR(255),
    last_name VARCHAR(255),
    organization VARCHAR(255),
    role VARCHAR(255),
    location VARCHAR(255),
    bio TEXT,
    avatar_path VARCHAR(255),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS user_preferences (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    theme VARCHAR(50) DEFAULT 'dark',
    accent_color VARCHAR(50) DEFAULT '#00d9ff',
    notification_prefs TEXT,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

