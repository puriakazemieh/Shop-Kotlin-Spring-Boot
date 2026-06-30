-- Add story link targeting: stories can link to nothing, a product, a category, or a blog post.
-- Idempotent so it is safe to re-run and safe to apply to databases with existing story rows.

ALTER TABLE stories ADD COLUMN IF NOT EXISTS link_type VARCHAR(20);
ALTER TABLE stories ADD COLUMN IF NOT EXISTS category_id BIGINT;
ALTER TABLE stories ADD COLUMN IF NOT EXISTS blog_slug VARCHAR(200);

-- Backfill existing rows: anything already pointing at a product is a PRODUCT link, the rest are NONE.
UPDATE stories SET link_type = 'PRODUCT' WHERE link_type IS NULL AND product_id IS NOT NULL;
UPDATE stories SET link_type = 'NONE' WHERE link_type IS NULL;

ALTER TABLE stories ALTER COLUMN link_type SET DEFAULT 'NONE';
ALTER TABLE stories ALTER COLUMN link_type SET NOT NULL;
