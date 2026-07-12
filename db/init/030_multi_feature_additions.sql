-- Foundation + Phases P/Q/R/S: lesson resource files, per-lesson quizzes,
-- project-based assessment, product bundles, patient CRM/tags.
-- Idempotent: safe to run on an existing database.

-- ---- Lesson resource files (files alongside video, e.g. [{"name":"جزوه","url":"...","sizeLabel":"2MB"}]) ----
ALTER TABLE course_lessons ADD COLUMN IF NOT EXISTS resource_files JSONB NOT NULL DEFAULT '[]'::jsonb;

-- ---- Per-lesson checkpoint quiz (separate from the course-final quiz in course_quizzes) ----
CREATE TABLE IF NOT EXISTS lesson_quizzes (
    id         BIGSERIAL PRIMARY KEY,
    lesson_id  BIGINT NOT NULL UNIQUE REFERENCES course_lessons (id) ON DELETE CASCADE,
    title      VARCHAR(200) NOT NULL DEFAULT 'آزمونِ این درس',
    pass_score INT NOT NULL DEFAULT 60,
    questions  JSONB NOT NULL DEFAULT '[]'::jsonb,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS lesson_quiz_attempts (
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    lesson_id  BIGINT NOT NULL REFERENCES course_lessons (id) ON DELETE CASCADE,
    score      INT NOT NULL,
    passed     BOOLEAN NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_lesson_quiz_attempt_user ON lesson_quiz_attempts (user_id, lesson_id);

-- ---- Project-based assessment (alternative/additional to the multiple-choice quiz) ----
ALTER TABLE courses ADD COLUMN IF NOT EXISTS requires_project_submission BOOLEAN NOT NULL DEFAULT false;

CREATE TABLE IF NOT EXISTS project_submissions (
    id               BIGSERIAL PRIMARY KEY,
    course_id        BIGINT NOT NULL REFERENCES courses (id) ON DELETE CASCADE,
    user_id          BIGINT NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    file_url         VARCHAR(500) NOT NULL,
    note             TEXT,
    status           VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    mentor_feedback  TEXT,
    submitted_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
    reviewed_at      TIMESTAMPTZ,
    CONSTRAINT ux_project_submission_course_user UNIQUE (course_id, user_id)
);

CREATE INDEX IF NOT EXISTS idx_project_submissions_course ON project_submissions (course_id);

-- ---- Product bundles (a bundle is itself a purchasable product; membership is informational) ----
CREATE TABLE IF NOT EXISTS product_bundles (
    id                BIGSERIAL PRIMARY KEY,
    title             VARCHAR(200) NOT NULL,
    slug              VARCHAR(220) NOT NULL UNIQUE,
    description       TEXT,
    product_id        BIGINT NOT NULL REFERENCES products (id) ON DELETE CASCADE,
    member_product_ids JSONB NOT NULL DEFAULT '[]'::jsonb,
    is_active         BOOLEAN NOT NULL DEFAULT true,
    created_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at        TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_product_bundles_active ON product_bundles (is_active);

-- ---- Lightweight patient CRM: per-therapist tags/relationship on a patient ----
CREATE TABLE IF NOT EXISTS patient_relations (
    id           BIGSERIAL PRIMARY KEY,
    therapist_id BIGINT NOT NULL REFERENCES therapists (id) ON DELETE CASCADE,
    user_id      BIGINT NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    tags         JSONB NOT NULL DEFAULT '[]'::jsonb,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT ux_patient_relation_therapist_user UNIQUE (therapist_id, user_id)
);
