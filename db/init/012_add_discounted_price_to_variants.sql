-- Add discounted_price to product_variants
ALTER TABLE product_variants ADD COLUMN discounted_price NUMERIC(12,2);

-- Update the price check constraint if necessary, or add a new one
ALTER TABLE product_variants ADD CONSTRAINT chk_variant_discounted_price_nonneg CHECK (discounted_price IS NULL OR discounted_price >= 0);

-- Optional: Update search indexes if they depend on price
CREATE INDEX idx_variants_discounted_price ON product_variants(discounted_price);
