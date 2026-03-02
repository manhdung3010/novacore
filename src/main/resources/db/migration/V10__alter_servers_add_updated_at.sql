-- Migration: Add updated_at to servers table
-- Description: Aligns servers with BaseEntity (created_at, updated_at)
-- Author: NovaCore

DO $$
BEGIN
    -- Older databases may lack the updated_at column.
    -- New databases created via V6 already have it; we skip to avoid NOTICE.
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'servers'
          AND column_name = 'updated_at'
    ) THEN
        ALTER TABLE servers
            ADD COLUMN updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
    END IF;
END $$;
