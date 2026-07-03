-- Waitlist for full in-person/offline classes.
-- Idempotent: safe to run on an existing database.

CREATE TABLE IF NOT EXISTS course_waitlist (
    id          BIGSERIAL PRIMARY KEY,
    course_id   BIGINT NOT NULL REFERENCES courses (id) ON DELETE CASCADE,
    user_id     BIGINT NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    notified    BOOLEAN NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    notified_at TIMESTAMPTZ,
    CONSTRAINT ux_course_waitlist UNIQUE (course_id, user_id)
);

CREATE INDEX IF NOT EXISTS idx_course_waitlist_course ON course_waitlist (course_id, notified);
