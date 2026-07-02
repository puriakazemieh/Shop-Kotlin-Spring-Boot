-- Academy vertical: courses, sections, lessons, enrollments, lesson progress.
-- Idempotent: safe to run on an existing database.
CREATE TABLE IF NOT EXISTS courses (
    id               BIGSERIAL PRIMARY KEY,
    title            VARCHAR(200) NOT NULL,
    slug             VARCHAR(220) NOT NULL UNIQUE,
    description      TEXT,
    thumbnail_url    VARCHAR(500),
    instructor       VARCHAR(120),
    price            NUMERIC(12,2) NOT NULL DEFAULT 0,
    discounted_price NUMERIC(12,2),
    product_id       BIGINT,
    is_published     BOOLEAN NOT NULL DEFAULT TRUE,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at       TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS course_sections (
    id         BIGSERIAL PRIMARY KEY,
    course_id  BIGINT NOT NULL REFERENCES courses (id) ON DELETE CASCADE,
    title      VARCHAR(200) NOT NULL,
    sort_order INT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS course_lessons (
    id               BIGSERIAL PRIMARY KEY,
    section_id       BIGINT NOT NULL REFERENCES course_sections (id) ON DELETE CASCADE,
    title            VARCHAR(200) NOT NULL,
    video_url        VARCHAR(500),
    duration_seconds INT NOT NULL DEFAULT 0,
    sort_order       INT NOT NULL DEFAULT 0,
    is_free_preview  BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS course_enrollments (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    course_id   BIGINT NOT NULL REFERENCES courses (id) ON DELETE CASCADE,
    enrolled_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT ux_enrollment_user_course UNIQUE (user_id, course_id)
);

CREATE TABLE IF NOT EXISTS lesson_progress (
    id                    BIGSERIAL PRIMARY KEY,
    user_id               BIGINT NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    lesson_id             BIGINT NOT NULL REFERENCES course_lessons (id) ON DELETE CASCADE,
    course_id             BIGINT NOT NULL REFERENCES courses (id) ON DELETE CASCADE,
    completed             BOOLEAN NOT NULL DEFAULT FALSE,
    last_position_seconds INT NOT NULL DEFAULT 0,
    updated_at            TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT ux_progress_user_lesson UNIQUE (user_id, lesson_id)
);

CREATE INDEX IF NOT EXISTS idx_sections_course ON course_sections (course_id, sort_order);
CREATE INDEX IF NOT EXISTS idx_lessons_section ON course_lessons (section_id, sort_order);
CREATE INDEX IF NOT EXISTS idx_enrollments_user ON course_enrollments (user_id);
CREATE INDEX IF NOT EXISTS idx_progress_user_course ON lesson_progress (user_id, course_id);
