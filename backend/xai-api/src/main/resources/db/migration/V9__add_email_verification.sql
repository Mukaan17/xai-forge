-- Add email verification fields to users table
ALTER TABLE users ADD COLUMN IF NOT EXISTS email_verified BOOLEAN DEFAULT FALSE NOT NULL;
ALTER TABLE users ADD COLUMN IF NOT EXISTS email_verification_token VARCHAR(100);
ALTER TABLE users ADD COLUMN IF NOT EXISTS email_verification_expires TIMESTAMP;

-- Create index on email_verification_token for faster lookups
CREATE INDEX IF NOT EXISTS idx_users_email_verification_token ON users(email_verification_token);
