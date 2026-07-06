-- Phase U: referral/affiliate program. Idempotent.

ALTER TABLE users ADD COLUMN IF NOT EXISTS referral_code VARCHAR(16);
ALTER TABLE users ADD COLUMN IF NOT EXISTS referred_by_user_id BIGINT REFERENCES users (id);
CREATE UNIQUE INDEX IF NOT EXISTS ux_users_referral_code ON users (referral_code) WHERE referral_code IS NOT NULL;

CREATE TABLE IF NOT EXISTS referral_rewards (
    id            BIGSERIAL PRIMARY KEY,
    referrer_id   BIGINT NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    referee_id    BIGINT NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    order_id      BIGINT NOT NULL REFERENCES orders (id) ON DELETE CASCADE,
    amount        NUMERIC(12,2) NOT NULL,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT ux_referral_reward_order UNIQUE (order_id)
);
CREATE INDEX IF NOT EXISTS idx_referral_rewards_referrer ON referral_rewards (referrer_id);
