-- Timestamped order status history (fills the tracking timeline with real times).
-- Idempotent: safe to run on an existing database.
CREATE TABLE IF NOT EXISTS order_status_history (
    id       BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES orders (id) ON DELETE CASCADE,
    status   VARCHAR(20) NOT NULL,
    at       TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_order_status_history_order
    ON order_status_history (order_id, at ASC);

-- Backfill: seed an initial "placed" record for existing orders that have none,
-- using each order's created_at so their timelines aren't empty.
INSERT INTO order_status_history (order_id, status, at)
SELECT o.id, o.status, COALESCE(o.created_at, now())
FROM orders o
WHERE NOT EXISTS (
    SELECT 1 FROM order_status_history h WHERE h.order_id = o.id
);
