-- Split full_name into first/last name and add profile fields.
-- Idempotent: safe to run on a fresh or already-migrated database.
ALTER TABLE users DROP COLUMN IF EXISTS full_name;

ALTER TABLE users
    ADD COLUMN IF NOT EXISTS first_name  VARCHAR(50),
    ADD COLUMN IF NOT EXISTS last_name   VARCHAR(50),
    ADD COLUMN IF NOT EXISTS city        VARCHAR(50),
    ADD COLUMN IF NOT EXISTS postal_code INTEGER;

ALTER TABLE users
    ALTER COLUMN phone TYPE VARCHAR(30);
