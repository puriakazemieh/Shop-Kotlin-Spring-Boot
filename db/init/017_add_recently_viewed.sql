-- Recently viewed products (per-user browsing history).
-- Idempotent: safe to run on an existing database.
CREATE TABLE IF NOT EXISTS recently_viewed (
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT NOT NULL REFERENCES users (id),
    product_id BIGINT NOT NULL REFERENCES products (id),
    viewed_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uc_recently_viewed_user_product UNIQUE (user_id, product_id)
);

CREATE INDEX IF NOT EXISTS idx_recently_viewed_user_viewed_at
    ON recently_viewed (user_id, viewed_at DESC);
