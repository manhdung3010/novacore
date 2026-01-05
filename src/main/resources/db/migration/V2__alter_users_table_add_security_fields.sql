-- Migration: Add security fields to users table
-- Description: Adds password, failed_login_attempts, account_locked_until, last_login_at fields and updates status constraint
-- Author: NovaCore
-- Date: 2024

ALTER TABLE users ADD COLUMN password VARCHAR(60) NOT NULL DEFAULT '';
ALTER TABLE users ADD COLUMN failed_login_attempts INTEGER NOT NULL DEFAULT 0;
ALTER TABLE users ADD COLUMN account_locked_until TIMESTAMP;
ALTER TABLE users ADD COLUMN last_login_at TIMESTAMP;

ALTER TABLE users DROP CONSTRAINT IF EXISTS chk_user_status;
ALTER TABLE users ADD CONSTRAINT chk_user_status CHECK (status IN ('ACTIVE', 'SUSPENDED', 'DELETED'));

