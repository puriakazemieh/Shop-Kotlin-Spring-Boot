-- Phase W: academy bigger features — cohort scheduling, subtitles, organizational seats, refund guarantee.

ALTER TABLE courses ADD COLUMN IF NOT EXISTS cohort_start_date TIMESTAMPTZ;

ALTER TABLE course_lessons ADD COLUMN IF NOT EXISTS subtitles JSONB NOT NULL DEFAULT '[]';

CREATE TABLE IF NOT EXISTS organizations (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    contact_email VARCHAR(200),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS organization_seats (
    id BIGSERIAL PRIMARY KEY,
    organization_id BIGINT NOT NULL REFERENCES organizations(id),
    course_id BIGINT NOT NULL REFERENCES courses(id),
    assigned_user_id BIGINT REFERENCES users(id),
    assigned_email VARCHAR(200),
    assigned_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_org_seats_org ON organization_seats(organization_id);
CREATE INDEX IF NOT EXISTS idx_org_seats_course ON organization_seats(course_id);

CREATE TABLE IF NOT EXISTS course_refund_requests (
    id BIGSERIAL PRIMARY KEY,
    course_id BIGINT NOT NULL REFERENCES courses(id),
    user_id BIGINT NOT NULL REFERENCES users(id),
    amount NUMERIC(12, 2) NOT NULL DEFAULT 0,
    reason TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    admin_note TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    resolved_at TIMESTAMPTZ
);
CREATE INDEX IF NOT EXISTS idx_course_refund_requests_user ON course_refund_requests(user_id);
CREATE INDEX IF NOT EXISTS idx_course_refund_requests_status ON course_refund_requests(status);
