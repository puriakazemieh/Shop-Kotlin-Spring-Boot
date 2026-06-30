-- Add brand and attributes (specs) to products. Idempotent.
ALTER TABLE products ADD COLUMN IF NOT EXISTS brand VARCHAR(120);
ALTER TABLE products ADD COLUMN IF NOT EXISTS attributes JSONB;
