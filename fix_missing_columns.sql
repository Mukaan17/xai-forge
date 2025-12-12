-- Quick fix script to add missing columns to users table
-- Run this in your PostgreSQL database if migrations haven't been applied

-- Add two-factor authentication fields (if missing)
ALTER TABLE users ADD COLUMN IF NOT EXISTS two_factor_enabled BOOLEAN DEFAULT false NOT NULL;
ALTER TABLE users ADD COLUMN IF NOT EXISTS two_factor_secret VARCHAR(100);
ALTER TABLE users ADD COLUMN IF NOT EXISTS two_factor_backup_codes VARCHAR(500);

-- Add other profile fields (if missing)
ALTER TABLE users ADD COLUMN IF NOT EXISTS first_name VARCHAR(100);
ALTER TABLE users ADD COLUMN IF NOT EXISTS last_name VARCHAR(100);
ALTER TABLE users ADD COLUMN IF NOT EXISTS organization VARCHAR(200);
ALTER TABLE users ADD COLUMN IF NOT EXISTS role VARCHAR(100);
ALTER TABLE users ADD COLUMN IF NOT EXISTS location VARCHAR(200);
ALTER TABLE users ADD COLUMN IF NOT EXISTS bio TEXT;
ALTER TABLE users ADD COLUMN IF NOT EXISTS profile_image_url VARCHAR(500);

-- Add email verification fields (if missing)
ALTER TABLE users ADD COLUMN IF NOT EXISTS email_verified BOOLEAN DEFAULT false NOT NULL;
ALTER TABLE users ADD COLUMN IF NOT EXISTS email_verification_token VARCHAR(100);
ALTER TABLE users ADD COLUMN IF NOT EXISTS email_verification_expires TIMESTAMP;

-- Add account status fields (if missing)
ALTER TABLE users ADD COLUMN IF NOT EXISTS active BOOLEAN DEFAULT true NOT NULL;
ALTER TABLE users ADD COLUMN IF NOT EXISTS account_locked BOOLEAN DEFAULT false NOT NULL;
ALTER TABLE users ADD COLUMN IF NOT EXISTS lock_expires_at TIMESTAMP;
ALTER TABLE users ADD COLUMN IF NOT EXISTS failed_login_attempts INTEGER DEFAULT 0 NOT NULL;

-- Add timestamp fields (if missing)
ALTER TABLE users ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL;
ALTER TABLE users ADD COLUMN IF NOT EXISTS last_login_at TIMESTAMP;
ALTER TABLE users ADD COLUMN IF NOT EXISTS password_changed_at TIMESTAMP;

-- Update existing records
UPDATE users SET active = true WHERE active IS NULL;
UPDATE users SET email_verified = false WHERE email_verified IS NULL;
UPDATE users SET two_factor_enabled = false WHERE two_factor_enabled IS NULL;
UPDATE users SET account_locked = false WHERE account_locked IS NULL;
UPDATE users SET failed_login_attempts = 0 WHERE failed_login_attempts IS NULL;
