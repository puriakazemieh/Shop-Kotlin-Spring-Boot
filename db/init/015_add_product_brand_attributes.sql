-- Add brand and attributes (specs) to products. Idempotent.
ALTER TABLE products ADD COLUMN IF NOT EXISTS brand VARCHAR(120);
ALTER TABLE products ADD COLUMN IF NOT EXISTS attributes JSONB NOT NULL DEFAULT '[]'::jsonb;
-- Backfill any pre-existing NULLs (in case the column existed without a default).
UPDATE products SET attributes = '[]'::jsonb WHERE attributes IS NULL;
