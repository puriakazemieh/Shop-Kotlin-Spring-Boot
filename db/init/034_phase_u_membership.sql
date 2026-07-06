-- Phase U: tiered membership (Prime-style: paid membership -> automatic discount). Idempotent.

CREATE TABLE IF NOT EXISTS memberships (
    id           BIGSERIAL PRIMARY KEY,
    user_id      BIGINT NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    tier         VARCHAR(20) NOT NULL DEFAULT 'GOLD',
    started_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    expires_at   TIMESTAMPTZ NOT NULL,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_memberships_user ON memberships (user_id);
