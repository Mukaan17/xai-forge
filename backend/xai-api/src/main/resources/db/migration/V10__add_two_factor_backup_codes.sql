-- Add two_factor_backup_codes column to users table
ALTER TABLE users ADD COLUMN IF NOT EXISTS two_factor_backup_codes VARCHAR(500);
