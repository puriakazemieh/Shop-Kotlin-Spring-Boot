-- Drop old constraint that prevents unique combination without size and color
ALTER TABLE product_variants DROP CONSTRAINT uq_variant_unique_combo;

-- Add tables for dynamic options
CREATE TABLE option_type (
    id   BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE option_value (
    id             BIGSERIAL PRIMARY KEY,
    option_type_id BIGINT NOT NULL REFERENCES option_type(id) ON DELETE CASCADE,
    value          VARCHAR(100) NOT NULL,
    UNIQUE (option_type_id, value)
);

CREATE TABLE product_variant_option_values (
    product_variant_id BIGINT NOT NULL REFERENCES product_variants(id) ON DELETE CASCADE,
    option_value_id    BIGINT NOT NULL REFERENCES option_value(id) ON DELETE CASCADE,
    PRIMARY KEY (product_variant_id, option_value_id)
);

-- Note: We should probably drop size_id and color_id from product_variants
-- However, since this is a migration, there might be existing data.
-- To make this safe, we first allow them to be NULL.
ALTER TABLE product_variants ALTER COLUMN size_id DROP NOT NULL;
ALTER TABLE product_variants ALTER COLUMN color_id DROP NOT NULL;

-- Drop old size and color tables
-- Be careful: this will drop existing sizes and colors.
-- In a real production migration, you would first migrate the data to option_type/option_value.
-- For this development environment, we will drop them.
-- First, drop foreign keys from product_variants
ALTER TABLE product_variants DROP CONSTRAINT product_variants_size_id_fkey;
ALTER TABLE product_variants DROP CONSTRAINT product_variants_color_id_fkey;

-- Then drop the columns
ALTER TABLE product_variants DROP COLUMN size_id;
ALTER TABLE product_variants DROP COLUMN color_id;

-- Now drop the tables
DROP TABLE sizes;
DROP TABLE colors;

-- Update order_items to use options_snapshot instead of size_snapshot and color_snapshot
ALTER TABLE order_items ADD COLUMN options_snapshot JSONB;

-- In a real migration, you would populate options_snapshot from size_snapshot/color_snapshot here
-- For now, we just drop the old columns
ALTER TABLE order_items DROP COLUMN size_snapshot;
ALTER TABLE order_items DROP COLUMN color_snapshot;
