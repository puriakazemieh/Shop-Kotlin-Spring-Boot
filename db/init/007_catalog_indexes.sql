-- ====== Catalog indexes ======
CREATE INDEX IF NOT EXISTS idx_variants_product_price
    ON product_variants(product_id, price)
    WHERE is_active = true;