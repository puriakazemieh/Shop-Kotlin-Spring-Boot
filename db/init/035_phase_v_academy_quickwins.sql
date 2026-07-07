-- Phase V: academy quick wins (lesson Q&A, peer review, instructor discount
-- code, course-update flag). Idempotent.

ALTER TABLE courses ADD COLUMN IF NOT EXISTS instructor_discount_code VARCHAR(40);
ALTER TABLE course_enrollments ADD COLUMN IF NOT EXISTS has_unseen_update BOOLEAN NOT NULL DEFAULT false;

CREATE TABLE IF NOT EXISTS lesson_questions (
    id          BIGSERIAL PRIMARY KEY,
    lesson_id   BIGINT NOT NULL REFERENCES course_lessons (id) ON DELETE CASCADE,
    user_id     BIGINT NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    content     TEXT NOT NULL,
    parent_id   BIGINT REFERENCES lesson_questions (id) ON DELETE CASCADE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_lesson_questions_lesson ON lesson_questions (lesson_id);

CREATE TABLE IF NOT EXISTS project_peer_comments (
    id             BIGSERIAL PRIMARY KEY,
    submission_id  BIGINT NOT NULL REFERENCES project_submissions (id) ON DELETE CASCADE,
    user_id        BIGINT NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    comment        TEXT NOT NULL,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_peer_comments_submission ON project_peer_comments (submission_id);
