-- Reconcile order_items snapshot columns with the current OrderItemEntity.
-- Older databases created order_items with NOT NULL size_snapshot / color_snapshot
-- columns (001_schema.sql) that the current entity no longer maps; it now stores
-- the selected options in a single JSONB column (options_snapshot). This migration
-- is idempotent and safe to run on any existing database.
DO $$
BEGIN
   -- Drop the legacy NOT NULL columns if they still exist.
   IF EXISTS (SELECT 1 FROM information_schema.columns
              WHERE table_name = 'order_items' AND column_name = 'size_snapshot') THEN
      ALTER TABLE order_items DROP COLUMN size_snapshot;
   END IF;
   IF EXISTS (SELECT 1 FROM information_schema.columns
              WHERE table_name = 'order_items' AND column_name = 'color_snapshot') THEN
      ALTER TABLE order_items DROP COLUMN color_snapshot;
   END IF;

   -- Ensure the current JSONB snapshot column exists.
   IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                  WHERE table_name = 'order_items' AND column_name = 'options_snapshot') THEN
      ALTER TABLE order_items ADD COLUMN options_snapshot JSONB;
   END IF;
END;
$$;
