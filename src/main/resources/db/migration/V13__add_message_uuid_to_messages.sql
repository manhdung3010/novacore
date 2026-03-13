-- Migration: Add message UUID for deduplication
-- Description: Adds a stable message_uuid to support at-least-once Kafka consumption without duplicates
-- Author: NovaCore

CREATE EXTENSION IF NOT EXISTS pgcrypto;

ALTER TABLE messages
    ADD COLUMN IF NOT EXISTS message_uuid UUID;

UPDATE messages
SET message_uuid = gen_random_uuid()
WHERE message_uuid IS NULL;

ALTER TABLE messages
    ALTER COLUMN message_uuid SET NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uq_messages_message_uuid
    ON messages (message_uuid);

