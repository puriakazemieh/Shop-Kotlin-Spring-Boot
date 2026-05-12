-- V{version}__update_user_profile_fields.sql
ALTER TABLE users
DROP COLUMN IF EXISTS full_name;

ALTER TABLE users
    ADD COLUMN first_name VARCHAR(50),
    ADD COLUMN last_name VARCHAR(50),
    ADD COLUMN city VARCHAR(50),
    ADD COLUMN postal_code INTEGER,

ALTER TABLE users
ALTER COLUMN phone TYPE VARCHAR(30);
