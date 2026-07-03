-- Phase J: purchasable psychology-test vertical.
-- Idempotent: safe to run on an existing database.

CREATE TABLE IF NOT EXISTS psych_tests (
    id               BIGSERIAL PRIMARY KEY,
    title            VARCHAR(200) NOT NULL,
    slug             VARCHAR(220) NOT NULL UNIQUE,
    description      TEXT,
    price            NUMERIC(12,2) NOT NULL DEFAULT 0,
    discounted_price NUMERIC(12,2),
    product_id       BIGINT,
    result_mode      VARCHAR(20) NOT NULL DEFAULT 'AUTO',
    questions        JSONB NOT NULL DEFAULT '[]'::jsonb,
    ranges           JSONB NOT NULL DEFAULT '[]'::jsonb,
    is_published     BOOLEAN NOT NULL DEFAULT TRUE,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS user_psych_tests (
    id                          BIGSERIAL PRIMARY KEY,
    user_id                     BIGINT NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    test_id                     BIGINT NOT NULL REFERENCES psych_tests (id) ON DELETE CASCADE,
    status                      VARCHAR(30) NOT NULL DEFAULT 'PURCHASED',
    total_score                 INT,
    interpretation              TEXT,
    interpreted_by_counselor_id BIGINT,
    completed_at                TIMESTAMPTZ,
    created_at                  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_user_psych_test_user ON user_psych_tests (user_id);
CREATE INDEX IF NOT EXISTS idx_user_psych_test_status ON user_psych_tests (status);
