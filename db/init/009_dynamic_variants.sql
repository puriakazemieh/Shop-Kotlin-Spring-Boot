-- Drop old constraint that prevents unique combination without size and color
DO $$
BEGIN
   IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'uq_variant_unique_combo') THEN
      ALTER TABLE product_variants DROP CONSTRAINT uq_variant_unique_combo;
   END IF;
END;
$$;

-- Add tables for dynamic options, if they don't exist
CREATE TABLE IF NOT EXISTS option_type (
    id   BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS option_value (
    id             BIGSERIAL PRIMARY KEY,
    option_type_id BIGINT NOT NULL REFERENCES option_type(id) ON DELETE CASCADE,
    value          VARCHAR(100) NOT NULL,
    UNIQUE (option_type_id, value)
);

-- Note: we use option_value_id instead of a many-to-many relationship
DO $$
BEGIN
   IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='product_variants' AND column_name='option_value_id') THEN
      ALTER TABLE product_variants ADD COLUMN option_value_id BIGINT REFERENCES option_value(id) ON DELETE SET NULL;
   END IF;
END;
$$;

-- Drop foreign key constraints and columns from product_variants if they exist
DO $$
BEGIN
   IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='product_variants' AND column_name='size_id') THEN
      ALTER TABLE product_variants DROP CONSTRAINT IF EXISTS product_variants_size_id_fkey;
      ALTER TABLE product_variants DROP COLUMN size_id;
   END IF;
   IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='product_variants' AND column_name='color_id') THEN
      ALTER TABLE product_variants DROP CONSTRAINT IF EXISTS product_variants_color_id_fkey;
      ALTER TABLE product_variants DROP COLUMN color_id;
   END IF;
END;
$$;

-- Drop old tables if they exist
DROP TABLE IF EXISTS sizes;
DROP TABLE IF EXISTS colors;

-- Add/alter columns in order_items if they don't exist/exist
DO $$
BEGIN
   IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='order_items' AND column_name='option_type_snapshot') THEN
      ALTER TABLE order_items ADD COLUMN option_type_snapshot VARCHAR(100);
   END IF;
   IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='order_items' AND column_name='option_value_snapshot') THEN
      ALTER TABLE order_items ADD COLUMN option_value_snapshot VARCHAR(100);
   END IF;
   IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='order_items' AND column_name='size_snapshot') THEN
      ALTER TABLE order_items DROP COLUMN size_snapshot;
   END IF;
   IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='order_items' AND column_name='color_snapshot') THEN
      ALTER TABLE order_items DROP COLUMN color_snapshot;
   END IF;
END;
$$;
