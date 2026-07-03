-- Phase I: course video quality variants, end-of-course quiz, and certificates.
-- Idempotent: safe to run on an existing database.

-- Per-lesson quality variants (e.g. [{"quality":"720p","url":"..."}]).
ALTER TABLE course_lessons ADD COLUMN IF NOT EXISTS video_variants JSONB NOT NULL DEFAULT '[]'::jsonb;

CREATE TABLE IF NOT EXISTS course_quizzes (
    id         BIGSERIAL PRIMARY KEY,
    course_id  BIGINT NOT NULL UNIQUE REFERENCES courses (id) ON DELETE CASCADE,
    title      VARCHAR(200) NOT NULL DEFAULT 'آزمونِ پایانِ دوره',
    pass_score INT NOT NULL DEFAULT 60,
    questions  JSONB NOT NULL DEFAULT '[]'::jsonb,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS quiz_attempts (
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    course_id  BIGINT NOT NULL REFERENCES courses (id) ON DELETE CASCADE,
    score      INT NOT NULL,
    passed     BOOLEAN NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS certificates (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    course_id   BIGINT NOT NULL REFERENCES courses (id) ON DELETE CASCADE,
    cert_number VARCHAR(40) NOT NULL UNIQUE,
    issued_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT ux_cert_user_course UNIQUE (user_id, course_id)
);

CREATE INDEX IF NOT EXISTS idx_quiz_attempt_user ON quiz_attempts (user_id, course_id);
CREATE INDEX IF NOT EXISTS idx_certificates_user ON certificates (user_id);
