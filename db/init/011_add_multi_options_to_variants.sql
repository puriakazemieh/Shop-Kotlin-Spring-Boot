-- Many-to-many between product_variants and option_value (ProductVariantEntity.optionValues).
-- Idempotent and self-consistent: the join table references option_value(id)
-- (the actual table name created in 009), not the non-existent "option_values".

-- Step 1: create the join table for the Many-to-Many relationship
CREATE TABLE IF NOT EXISTS product_variant_option_values (
    product_variant_id BIGINT NOT NULL,
    option_value_id    BIGINT NOT NULL,
    PRIMARY KEY (product_variant_id, option_value_id),
    CONSTRAINT fk_variant      FOREIGN KEY (product_variant_id) REFERENCES product_variants(id) ON DELETE CASCADE,
    CONSTRAINT fk_option_value FOREIGN KEY (option_value_id)    REFERENCES option_value(id)     ON DELETE CASCADE
);
CREATE INDEX IF NOT EXISTS idx_variant_options_variant_id ON product_variant_option_values(product_variant_id);

-- Step 2: migrate the legacy single-value column into the join table, then drop it.
DO $$
BEGIN
   IF EXISTS (SELECT 1 FROM information_schema.columns
              WHERE table_name = 'product_variants' AND column_name = 'option_value_id') THEN
      INSERT INTO product_variant_option_values (product_variant_id, option_value_id)
      SELECT id, option_value_id
      FROM product_variants
      WHERE option_value_id IS NOT NULL
      ON CONFLICT DO NOTHING;

      ALTER TABLE product_variants DROP COLUMN option_value_id;
   END IF;
END;
$$;
