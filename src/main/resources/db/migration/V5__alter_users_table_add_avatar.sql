

-- Migration: Add avatar column to users table
-- Description: Adds avatar URL field for user profile images
-- Author: NovaCore

ALTER TABLE users
    ADD COLUMN avatar VARCHAR(255) NOT NULL DEFAULT '';
















