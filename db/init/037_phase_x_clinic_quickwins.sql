-- Phase X: clinic quick wins — mood check-ins, therapist-switch requests.

CREATE TABLE IF NOT EXISTS mood_checkins (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    mood_score INT NOT NULL,
    note TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_mood_checkins_user ON mood_checkins(user_id);

CREATE TABLE IF NOT EXISTS therapist_switch_requests (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    from_therapist_id BIGINT NOT NULL REFERENCES therapists(id),
    to_therapist_id BIGINT REFERENCES therapists(id),
    reason TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    admin_note TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    resolved_at TIMESTAMPTZ
);
CREATE INDEX IF NOT EXISTS idx_switch_requests_user ON therapist_switch_requests(user_id);
