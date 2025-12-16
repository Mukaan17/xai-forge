/**
 * @Date:   2025-09-04 16:11:32
 * @Last Modified by:   Mukhil Sundararaj
 * @Last Modified time: 2025-09-04 16:12:21
 */
-- XAI Application Database Setup Script
-- Run this script as a PostgreSQL superuser
-- 
-- SECURITY WARNING: Change the default password before production use!
-- Generate secure password with: openssl rand -base64 32

-- Create database
CREATE DATABASE xai_db;

-- Create user with secure password (CHANGE THIS PASSWORD!)
CREATE USER xai_user WITH PASSWORD 'CHANGE_THIS_PASSWORD_IN_PRODUCTION';

-- Grant privileges
GRANT ALL PRIVILEGES ON DATABASE xai_db TO xai_user;

-- Connect to the database and grant schema privileges
\c xai_db;

-- Grant schema privileges
GRANT ALL ON SCHEMA public TO xai_user;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO xai_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO xai_user;

-- Set default privileges for future objects
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO xai_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO xai_user;

-- Verify setup
SELECT 'Database setup completed successfully!' as status;
