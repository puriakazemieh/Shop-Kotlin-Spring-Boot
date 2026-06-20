-- ====== Orders shipping/tracking fields ======
ALTER TABLE orders
    ADD COLUMN IF NOT EXISTS shipping_carrier VARCHAR(80),
    ADD COLUMN IF NOT EXISTS tracking_code VARCHAR(120),
    ADD COLUMN IF NOT EXISTS shipped_at TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS delivered_at TIMESTAMPTZ;