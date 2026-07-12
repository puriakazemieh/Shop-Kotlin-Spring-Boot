-- Phase H: course type/format split (seminar/workshop, online vs in-person).
-- Adds activity type, delivery format, level, and in-person location/capacity fields.
-- Idempotent: safe to run on an existing database.

ALTER TABLE courses ADD COLUMN IF NOT EXISTS course_type       VARCHAR(20)  NOT NULL DEFAULT 'COURSE';
ALTER TABLE courses ADD COLUMN IF NOT EXISTS format            VARCHAR(20)  NOT NULL DEFAULT 'ONLINE_RECORDED';
ALTER TABLE courses ADD COLUMN IF NOT EXISTS level             VARCHAR(20);
ALTER TABLE courses ADD COLUMN IF NOT EXISTS location          VARCHAR(300);
ALTER TABLE courses ADD COLUMN IF NOT EXISTS capacity          INT;
ALTER TABLE courses ADD COLUMN IF NOT EXISTS seats_taken       INT          NOT NULL DEFAULT 0;
ALTER TABLE courses ADD COLUMN IF NOT EXISTS job_market_badge  BOOLEAN      NOT NULL DEFAULT FALSE;
ALTER TABLE courses ADD COLUMN IF NOT EXISTS free_update_badge BOOLEAN      NOT NULL DEFAULT FALSE;
ALTER TABLE courses ADD COLUMN IF NOT EXISTS instructor_bio    TEXT;
ALTER TABLE courses ADD COLUMN IF NOT EXISTS instructor_skills VARCHAR(500);
