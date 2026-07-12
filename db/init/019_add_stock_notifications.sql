-- "Notify me when back in stock" subscriptions.
-- Idempotent: safe to run on an existing database.
CREATE TABLE IF NOT EXISTS stock_notifications (
    id          BIGSERIAL PRIMARY KEY,
    product_id  BIGINT NOT NULL REFERENCES products (id) ON DELETE CASCADE,
    variant_id  BIGINT NOT NULL REFERENCES product_variants (id) ON DELETE CASCADE,
    user_id     BIGINT NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    notified    BOOLEAN NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    notified_at TIMESTAMPTZ,
    CONSTRAINT ux_stock_notification UNIQUE (variant_id, user_id)
);

CREATE INDEX IF NOT EXISTS idx_stock_notification_variant_pending
    ON stock_notifications (variant_id) WHERE notified = FALSE;
