-- Migration: Add profile fields to users table
-- This migration adds new columns to the existing users table for enhanced user profiles

-- Add profile fields
ALTER TABLE users ADD COLUMN IF NOT EXISTS first_name VARCHAR(100);
ALTER TABLE users ADD COLUMN IF NOT EXISTS last_name VARCHAR(100);
ALTER TABLE users ADD COLUMN IF NOT EXISTS organization VARCHAR(200);
ALTER TABLE users ADD COLUMN IF NOT EXISTS role VARCHAR(100);
ALTER TABLE users ADD COLUMN IF NOT EXISTS location VARCHAR(200);
ALTER TABLE users ADD COLUMN IF NOT EXISTS bio TEXT;
ALTER TABLE users ADD COLUMN IF NOT EXISTS profile_image_url VARCHAR(500);

-- Add email verification fields
ALTER TABLE users ADD COLUMN IF NOT EXISTS email_verified BOOLEAN DEFAULT false NOT NULL;
ALTER TABLE users ADD COLUMN IF NOT EXISTS email_verification_token VARCHAR(100);
ALTER TABLE users ADD COLUMN IF NOT EXISTS email_verification_expires TIMESTAMP;

-- Add two-factor authentication fields
ALTER TABLE users ADD COLUMN IF NOT EXISTS two_factor_enabled BOOLEAN DEFAULT false NOT NULL;
ALTER TABLE users ADD COLUMN IF NOT EXISTS two_factor_secret VARCHAR(100);
ALTER TABLE users ADD COLUMN IF NOT EXISTS two_factor_backup_codes VARCHAR(500);

-- Add account status fields
ALTER TABLE users ADD COLUMN IF NOT EXISTS active BOOLEAN DEFAULT true NOT NULL;
ALTER TABLE users ADD COLUMN IF NOT EXISTS account_locked BOOLEAN DEFAULT false NOT NULL;
ALTER TABLE users ADD COLUMN IF NOT EXISTS lock_expires_at TIMESTAMP;
ALTER TABLE users ADD COLUMN IF NOT EXISTS failed_login_attempts INTEGER DEFAULT 0 NOT NULL;

-- Add timestamp fields
ALTER TABLE users ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL;
ALTER TABLE users ADD COLUMN IF NOT EXISTS last_login_at TIMESTAMP;
ALTER TABLE users ADD COLUMN IF NOT EXISTS password_changed_at TIMESTAMP;

-- Create index on email (if not exists)
CREATE INDEX IF NOT EXISTS idx_user_email ON users(email);

-- Update existing records: set username to email if username is null
UPDATE users SET username = email WHERE username IS NULL OR username = '';
