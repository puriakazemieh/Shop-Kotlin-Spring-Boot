-- ====== Search Improvements ======

-- 1) Trigram for fuzzy search (typos)
CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- 2) Full-text search vector (generated column)
-- NOTE: For Persian text, built-in stemming isn't available; 'simple' works as token-based search.
ALTER TABLE products
    ADD COLUMN IF NOT EXISTS search_vector tsvector
    GENERATED ALWAYS AS (
    to_tsvector('simple', coalesce(title,'') || ' ' || coalesce(description,''))
    ) STORED;

-- 3) GIN index on tsvector
CREATE INDEX IF NOT EXISTS idx_products_search_vector
    ON products USING GIN (search_vector);

-- 4) Trigram index on title for fuzzy matching
CREATE INDEX IF NOT EXISTS idx_products_title_trgm
    ON products USING GIN (title gin_trgm_ops);