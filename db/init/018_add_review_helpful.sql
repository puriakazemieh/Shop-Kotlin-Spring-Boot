-- "Helpful" votes on product reviews.
-- Idempotent: safe to run on an existing database.

-- Denormalized counter on the review for quick display.
ALTER TABLE product_reviews
    ADD COLUMN IF NOT EXISTS helpful_count INTEGER NOT NULL DEFAULT 0;

-- One vote per (review, user).
CREATE TABLE IF NOT EXISTS product_review_helpful (
    id         BIGSERIAL PRIMARY KEY,
    review_id  BIGINT NOT NULL REFERENCES product_reviews (id) ON DELETE CASCADE,
    user_id    BIGINT NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT ux_review_helpful UNIQUE (review_id, user_id)
);

CREATE INDEX IF NOT EXISTS idx_review_helpful_user
    ON product_review_helpful (user_id);
