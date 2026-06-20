-- Step 1: Create the new join table for the Many-to-Many relationship
CREATE TABLE product_variant_option_values (
    product_variant_id BIGINT NOT NULL, -- Renamed from variant_id to product_variant_id for consistency
    option_value_id BIGINT NOT NULL,
    PRIMARY KEY (product_variant_id, option_value_id),
    CONSTRAINT fk_variant FOREIGN KEY (product_variant_id) REFERENCES product_variants(id) ON DELETE CASCADE,
    CONSTRAINT fk_option_value FOREIGN KEY (option_value_id) REFERENCES option_values(id) ON DELETE CASCADE
);
CREATE INDEX idx_variant_options_variant_id ON product_variant_option_values(product_variant_id);


-- Step 2: Migrate existing data from the old column to the new join table
-- This query assumes the old column was named 'option_value_id' in 'product_variants' table
-- Note: This part might not be relevant if you are starting with a clean DB, but it's good practice for migration.
INSERT INTO product_variant_option_values (product_variant_id, option_value_id)
SELECT id, option_value_id
FROM product_variants
WHERE option_value_id IS NOT NULL;


-- Step 3: Drop the old, now redundant, column from the product_variants table
ALTER TABLE product_variants
    DROP COLUMN IF EXISTS option_value_id;
