-- ====== Core tables ======

CREATE TABLE users (
    id              BIGSERIAL PRIMARY KEY,
    email           VARCHAR(255) NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    full_name       VARCHAR(255),
    phone           VARCHAR(30),
    role            VARCHAR(20) NOT NULL DEFAULT 'ADMIN', -- CUSTOMER / ADMIN
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE categories (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(120) NOT NULL,
    slug        VARCHAR(140) NOT NULL UNIQUE,
    parent_id   BIGINT REFERENCES categories(id) ON DELETE SET NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE products (
    id          BIGSERIAL PRIMARY KEY,
    category_id BIGINT REFERENCES categories(id) ON DELETE SET NULL,
    title       VARCHAR(255) NOT NULL,
    slug        VARCHAR(280) NOT NULL UNIQUE,
    description TEXT,
    base_price  NUMERIC(12,2), -- optional (if price is variant-based you can keep NULL)
    is_active   BOOLEAN NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_products_category ON products(category_id);
CREATE INDEX idx_products_active ON products(is_active);

-- Optional: simple search index (not full-text yet)
CREATE INDEX idx_products_title ON products(title);

CREATE TABLE sizes (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(40) NOT NULL UNIQUE, -- S, M, L, XL ...
    sort_order  INT NOT NULL DEFAULT 0
);

CREATE TABLE colors (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(60) NOT NULL UNIQUE, -- Black, White ...
    hex         VARCHAR(7)  -- optional: #000000
);

CREATE TABLE product_images (
    id          BIGSERIAL PRIMARY KEY,
    product_id  BIGINT NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    url         TEXT NOT NULL,
    sort_order  INT NOT NULL DEFAULT 0
);

CREATE INDEX idx_product_images_product ON product_images(product_id);

-- ====== Variants + Inventory ======

CREATE TABLE product_variants (
    id              BIGSERIAL PRIMARY KEY,
    product_id      BIGINT NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    size_id         BIGINT NOT NULL REFERENCES sizes(id),
    color_id        BIGINT NOT NULL REFERENCES colors(id),
    sku             VARCHAR(80) NOT NULL UNIQUE,
    price           NUMERIC(12,2) NOT NULL,
    compare_at_price NUMERIC(12,2),
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_variant_unique_combo UNIQUE (product_id, size_id, color_id),
    CONSTRAINT chk_prices_nonneg CHECK (price >= 0 AND (compare_at_price IS NULL OR compare_at_price >= 0))
);

CREATE INDEX idx_variants_product ON product_variants(product_id);
CREATE INDEX idx_variants_size ON product_variants(size_id);
CREATE INDEX idx_variants_color ON product_variants(color_id);

CREATE TABLE inventory (
    variant_id  BIGINT PRIMARY KEY REFERENCES product_variants(id) ON DELETE CASCADE,
    on_hand     INT NOT NULL DEFAULT 0,
    reserved    INT NOT NULL DEFAULT 0,
    version     INT NOT NULL DEFAULT 0, -- optimistic locking helper
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_inventory_nonneg CHECK (on_hand >= 0 AND reserved >= 0),
    CONSTRAINT chk_reserved_le_onhand CHECK (reserved <= on_hand)
);

-- ====== Addresses + Orders ======

CREATE TABLE addresses (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    receiver_name   VARCHAR(255) NOT NULL,
    receiver_phone  VARCHAR(30) NOT NULL,
    country         VARCHAR(80) NOT NULL DEFAULT 'IR',
    province        VARCHAR(120) NOT NULL,
    city            VARCHAR(120) NOT NULL,
    address_line1   VARCHAR(255) NOT NULL,
    address_line2   VARCHAR(255),
    postal_code     VARCHAR(20),
    is_default      BOOLEAN NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_addresses_user ON addresses(user_id);

CREATE TABLE orders (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    status          VARCHAR(20) NOT NULL DEFAULT 'PENDING', -- PENDING/CONFIRMED/SHIPPED/DELIVERED/CANCELED
    subtotal_price  NUMERIC(12,2) NOT NULL DEFAULT 0,
    shipping_price  NUMERIC(12,2) NOT NULL DEFAULT 0,
    total_price     NUMERIC(12,2) NOT NULL DEFAULT 0,
    address_snapshot JSONB NOT NULL, -- store address at order time
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_order_prices_nonneg CHECK (subtotal_price >= 0 AND shipping_price >= 0 AND total_price >= 0)
);

CREATE INDEX idx_orders_user ON orders(user_id);
CREATE INDEX idx_orders_status ON orders(status);

CREATE TABLE order_items (
    id                  BIGSERIAL PRIMARY KEY,
    order_id            BIGINT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    variant_id          BIGINT NOT NULL REFERENCES product_variants(id) ON DELETE RESTRICT,
    qty                 INT NOT NULL,
    unit_price_snapshot NUMERIC(12,2) NOT NULL,
    title_snapshot      VARCHAR(255) NOT NULL,
    size_snapshot       VARCHAR(40) NOT NULL,
    color_snapshot      VARCHAR(60) NOT NULL,
    CONSTRAINT chk_order_item_qty CHECK (qty > 0),
    CONSTRAINT chk_order_item_price CHECK (unit_price_snapshot >= 0)
);

CREATE INDEX idx_order_items_order ON order_items(order_id);