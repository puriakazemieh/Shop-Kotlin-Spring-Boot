-- Phase U: recurring/subscribe-and-save purchase. Idempotent.

CREATE TABLE IF NOT EXISTS recurring_orders (
    id             BIGSERIAL PRIMARY KEY,
    user_id        BIGINT NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    variant_id     BIGINT NOT NULL,
    qty            INT NOT NULL DEFAULT 1,
    address_id     BIGINT REFERENCES addresses (id),
    interval_days  INT NOT NULL DEFAULT 30,
    next_run_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    is_active      BOOLEAN NOT NULL DEFAULT true,
    last_order_id  BIGINT,
    last_run_at    TIMESTAMPTZ,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_recurring_orders_next_run ON recurring_orders (is_active, next_run_at);
CREATE INDEX IF NOT EXISTS idx_recurring_orders_user ON recurring_orders (user_id);
