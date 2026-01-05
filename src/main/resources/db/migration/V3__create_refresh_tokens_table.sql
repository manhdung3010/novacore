-- Migration: Create refresh_tokens table
-- Description: Creates the refresh_tokens table for session management
-- Author: NovaCore
-- Date: 2024

CREATE TABLE refresh_tokens (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    token VARCHAR(255) NOT NULL UNIQUE,
    device_id VARCHAR(255),
    channel VARCHAR(20) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    revoked_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_refresh_tokens_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT chk_refresh_token_channel CHECK (channel IN ('WEB', 'MOBILE', 'INTERNAL'))
);

-- Create indexes for better query performance
CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens(user_id);
CREATE INDEX idx_refresh_tokens_token ON refresh_tokens(token);
CREATE INDEX idx_refresh_tokens_expires_at ON refresh_tokens(expires_at);
CREATE INDEX idx_refresh_tokens_user_device ON refresh_tokens(user_id, device_id) WHERE device_id IS NOT NULL;

