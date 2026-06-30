-- Add discounted_price to product_variants. Idempotent.
ALTER TABLE product_variants ADD COLUMN IF NOT EXISTS discounted_price NUMERIC(12,2);

DO $$
BEGIN
   IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'chk_variant_discounted_price_nonneg') THEN
      ALTER TABLE product_variants
         ADD CONSTRAINT chk_variant_discounted_price_nonneg
         CHECK (discounted_price IS NULL OR discounted_price >= 0);
   END IF;
END;
$$;

CREATE INDEX IF NOT EXISTS idx_variants_discounted_price ON product_variants(discounted_price);
