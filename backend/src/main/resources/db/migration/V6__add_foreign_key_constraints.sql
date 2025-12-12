-- Migration: Add foreign key constraints
-- This migration ensures referential integrity

-- Note: Most foreign keys are already defined in table creation
-- This migration adds any missing constraints and ensures data integrity

-- Ensure ml_models has user_id constraint
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint 
        WHERE conname = 'ml_models_user_id_fkey'
    ) THEN
        ALTER TABLE ml_models 
        ADD CONSTRAINT ml_models_user_id_fkey 
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;
    END IF;
END $$;

-- Ensure datasets has user_id constraint (if using user_id instead of owner_id)
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint 
        WHERE conname = 'datasets_user_id_fkey'
    ) THEN
        -- Add user_id column if it doesn't exist
        IF NOT EXISTS (
            SELECT 1 FROM information_schema.columns 
            WHERE table_name = 'datasets' AND column_name = 'user_id'
        ) THEN
            ALTER TABLE datasets ADD COLUMN user_id BIGINT;
            -- Migrate from owner_id to user_id
            UPDATE datasets d SET user_id = u.id 
            FROM users u 
            WHERE d.owner_id = u.id;
        END IF;
        
        ALTER TABLE datasets 
        ADD CONSTRAINT datasets_user_id_fkey 
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;
    END IF;
END $$;
