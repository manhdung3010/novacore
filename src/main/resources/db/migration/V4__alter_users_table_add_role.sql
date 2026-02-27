-- Migration: Add role column to users table
-- Description: Adds role column for user authorization and access control
-- Author: NovaCore
-- Date: 2024

ALTER TABLE users ADD COLUMN role VARCHAR(20) NOT NULL DEFAULT 'USER';

-- Add constraint to ensure valid role values
ALTER TABLE users ADD CONSTRAINT chk_user_role CHECK (role IN ('USER', 'ADMIN', 'MODERATOR'));

-- Create index for role-based queries
CREATE INDEX idx_users_role ON users(role);














