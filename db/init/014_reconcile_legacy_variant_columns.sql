-- 014: Reconcile product_variants and order_items with the current JPA entities.
--
-- Databases created from the original 001_schema.sql (before the dynamic-options
-- refactor) still carry legacy NOT NULL columns that the current entities no
-- longer map:
--   * product_variants.size_id  / color_id        -> replaced by the
--     product_variant_option_values join table (ProductVariantEntity.optionValues)
--   * product_variants.option_value_id            -> superseded by the join table (011)
--   * order_items.size_snapshot / color_snapshot  -> replaced by options_snapshot (jsonb)
--   * order_items.option_type_snapshot / option_value_snapshot -> superseded by options_snapshot
--
-- Hibernate's ddl-auto=update only ADDS missing columns/tables; it never drops a
-- column nor relaxes a legacy NOT NULL. So on such databases every INSERT into
-- product_variants / order_items (e.g. the DataSeeder at startup) fails with
-- "null value in column ... violates not-null constraint".
--
-- This migration removes those legacy columns and the lookup tables they pointed
-- at. Dropping a column automatically drops the indexes and constraints that
-- depend on it (FKs, the uq_variant_unique_combo composite unique, etc.), so no
-- explicit constraint/index drops are needed. Every statement is guarded with
-- IF EXISTS, making the whole script idempotent and safe to run on any DB state.

-- ---- product_variants ----
ALTER TABLE product_variants DROP COLUMN IF EXISTS size_id;
ALTER TABLE product_variants DROP COLUMN IF EXISTS color_id;
ALTER TABLE product_variants DROP COLUMN IF EXISTS option_value_id;

-- ---- order_items ----
ALTER TABLE order_items DROP COLUMN IF EXISTS size_snapshot;
ALTER TABLE order_items DROP COLUMN IF EXISTS color_snapshot;
ALTER TABLE order_items DROP COLUMN IF EXISTS option_type_snapshot;
ALTER TABLE order_items DROP COLUMN IF EXISTS option_value_snapshot;

-- Ensure the current JSONB snapshot column exists.
ALTER TABLE order_items ADD COLUMN IF NOT EXISTS options_snapshot JSONB;

-- ---- legacy lookup tables no longer mapped by any entity ----
DROP TABLE IF EXISTS sizes;
DROP TABLE IF EXISTS colors;
