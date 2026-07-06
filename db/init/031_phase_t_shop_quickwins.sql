-- Phase T: shop quick-win features (price-drop alerts, return/exchange
-- requests, gift checkout, photo reviews). Idempotent.

CREATE TABLE IF NOT EXISTS price_alerts (
    id            BIGSERIAL PRIMARY KEY,
    product_id    BIGINT NOT NULL,
    variant_id    BIGINT NOT NULL,
    user_id       BIGINT NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    target_price  NUMERIC(12,2) NOT NULL,
    notified      BOOLEAN NOT NULL DEFAULT false,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    notified_at   TIMESTAMPTZ,
    CONSTRAINT ux_price_alert UNIQUE (variant_id, user_id)
);

CREATE TABLE IF NOT EXISTS return_requests (
    id             BIGSERIAL PRIMARY KEY,
    order_id       BIGINT NOT NULL REFERENCES orders (id) ON DELETE CASCADE,
    order_item_id  BIGINT NOT NULL REFERENCES order_items (id) ON DELETE CASCADE,
    user_id        BIGINT NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    type           VARCHAR(20) NOT NULL DEFAULT 'RETURN',
    reason         TEXT NOT NULL,
    status         VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    admin_note     TEXT,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
    resolved_at    TIMESTAMPTZ
);
CREATE INDEX IF NOT EXISTS idx_return_requests_user ON return_requests (user_id);
CREATE INDEX IF NOT EXISTS idx_return_requests_status ON return_requests (status);

ALTER TABLE orders ADD COLUMN IF NOT EXISTS is_gift BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE orders ADD COLUMN IF NOT EXISTS gift_message TEXT;

ALTER TABLE product_reviews ADD COLUMN IF NOT EXISTS images JSONB NOT NULL DEFAULT '[]'::jsonb;
