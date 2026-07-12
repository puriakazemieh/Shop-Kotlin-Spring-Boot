-- Clinic session credits: purchased sessions per (user, therapist), consumed on booking.
-- Idempotent: safe to run on an existing database.
CREATE TABLE IF NOT EXISTS session_credits (
    id           BIGSERIAL PRIMARY KEY,
    user_id      BIGINT NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    therapist_id BIGINT NOT NULL REFERENCES therapists (id) ON DELETE CASCADE,
    remaining    INT NOT NULL DEFAULT 0,
    CONSTRAINT ux_credit_user_therapist UNIQUE (user_id, therapist_id)
);
