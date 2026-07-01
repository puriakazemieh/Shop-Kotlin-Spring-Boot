package com.kazemieh.shop.catalog.persistence

import com.kazemieh.shop.catalog.persistence.entity.ProductEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.Repository
import org.springframework.data.repository.query.Param
import java.math.BigDecimal

interface ProductSearchRepository : Repository<ProductEntity, Long> {

    // ---------- Relevance (Full-text rank) ----------
    @Query(
        value = """
        WITH RECURSIVE cat(id) AS (
          SELECT id FROM categories WHERE id = :categoryId
          UNION ALL
          SELECT c.id FROM categories c JOIN cat ON c.parent_id = cat.id
        ),
        q AS (
          SELECT CASE
            WHEN coalesce(nullif(:q, ''), '') = '' THEN NULL::tsquery
            ELSE websearch_to_tsquery('simple', :q)
          END AS query
        )
        SELECT
          p.id,
          p.category_id,
          p.title,
          p.slug,
          p.description,
          p.brand,
          p.attributes,
          p.base_price,
          p.discounted_price,
          p.is_active,
          p.created_at,
          p.updated_at
        FROM products p
        CROSS JOIN q
        WHERE p.is_active = true
          AND (q.query IS NULL OR p.search_vector @@ q.query)
          AND (:categoryId IS NULL OR p.category_id IN (SELECT id FROM cat))
          AND (:discountedOnly = false OR EXISTS (
            SELECT 1 FROM product_variants pvd
            WHERE pvd.product_id = p.id AND pvd.is_active = true AND pvd.discounted_price IS NOT NULL
          ))
          AND (
            :needVariantFilter = false
            OR EXISTS (
              SELECT 1
              FROM product_variants pv
              LEFT JOIN inventory i ON i.variant_id = pv.id
              WHERE pv.product_id = p.id
                AND pv.is_active = true
                AND (:minPrice IS NULL OR COALESCE(pv.discounted_price, pv.price) >= :minPrice)
                AND (:maxPrice IS NULL OR COALESCE(pv.discounted_price, pv.price) <= :maxPrice)
                AND (:inStock IS NULL OR :inStock = false OR (COALESCE(i.on_hand,0) - COALESCE(i.reserved,0)) > 0)
            )
          )
        ORDER BY
          COALESCE(ts_rank_cd(p.search_vector, q.query), 0) DESC,
          p.created_at DESC
        """,
        countQuery = """
        WITH RECURSIVE cat(id) AS (
          SELECT id FROM categories WHERE id = :categoryId
          UNION ALL
          SELECT c.id FROM categories c JOIN cat ON c.parent_id = cat.id
        ),
        q AS (
          SELECT CASE
            WHEN coalesce(nullif(:q, ''), '') = '' THEN NULL::tsquery
            ELSE websearch_to_tsquery('simple', :q)
          END AS query
        )
        SELECT count(*)
        FROM products p
        CROSS JOIN q
        WHERE p.is_active = true
          AND (q.query IS NULL OR p.search_vector @@ q.query)
          AND (:categoryId IS NULL OR p.category_id IN (SELECT id FROM cat))
          AND (:discountedOnly = false OR EXISTS (
            SELECT 1 FROM product_variants pvd
            WHERE pvd.product_id = p.id AND pvd.is_active = true AND pvd.discounted_price IS NOT NULL
          ))
          AND (
            :needVariantFilter = false
            OR EXISTS (
              SELECT 1
              FROM product_variants pv
              LEFT JOIN inventory i ON i.variant_id = pv.id
              WHERE pv.product_id = p.id
                AND pv.is_active = true
                AND (:minPrice IS NULL OR COALESCE(pv.discounted_price, pv.price) >= :minPrice)
                AND (:maxPrice IS NULL OR COALESCE(pv.discounted_price, pv.price) <= :maxPrice)
                AND (:inStock IS NULL OR :inStock = false OR (COALESCE(i.on_hand,0) - COALESCE(i.reserved,0)) > 0)
            )
          )
        """,
        nativeQuery = true
    )
    fun searchRelevance(
        @Param("q") q: String?,
        @Param("categoryId") categoryId: Long?,
        @Param("minPrice") minPrice: BigDecimal?,
        @Param("maxPrice") maxPrice: BigDecimal?,
        @Param("inStock") inStock: Boolean?,
        @Param("needVariantFilter") needVariantFilter: Boolean,
        @Param("discountedOnly") discountedOnly: Boolean,
        pageable: Pageable
    ): Page<ProductEntity>

    // ---------- Newest (ignore rank) ----------
    @Query(
        value = """
        WITH RECURSIVE cat(id) AS (
          SELECT id FROM categories WHERE id = :categoryId
          UNION ALL
          SELECT c.id FROM categories c JOIN cat ON c.parent_id = cat.id
        )
        SELECT
          p.id, p.category_id, p.title, p.slug, p.description, p.brand, p.attributes, p.base_price, p.discounted_price, p.is_active, p.created_at, p.updated_at
        FROM products p
        WHERE p.is_active = true
          AND (:categoryId IS NULL OR p.category_id IN (SELECT id FROM cat))
          AND (:discountedOnly = false OR EXISTS (
            SELECT 1 FROM product_variants pvd
            WHERE pvd.product_id = p.id AND pvd.is_active = true AND pvd.discounted_price IS NOT NULL
          ))
          AND (
            :needVariantFilter = false
            OR EXISTS (
              SELECT 1
              FROM product_variants pv
              LEFT JOIN inventory i ON i.variant_id = pv.id
              WHERE pv.product_id = p.id
                AND pv.is_active = true
                AND (:minPrice IS NULL OR COALESCE(pv.discounted_price, pv.price) >= :minPrice)
                AND (:maxPrice IS NULL OR COALESCE(pv.discounted_price, pv.price) <= :maxPrice)
                AND (:inStock IS NULL OR :inStock = false OR (COALESCE(i.on_hand,0) - COALESCE(i.reserved,0)) > 0)
            )
          )
        ORDER BY p.created_at DESC
        """,
        countQuery = """
        WITH RECURSIVE cat(id) AS (
          SELECT id FROM categories WHERE id = :categoryId
          UNION ALL
          SELECT c.id FROM categories c JOIN cat ON c.parent_id = cat.id
        )
        SELECT count(*)
        FROM products p
        WHERE p.is_active = true
          AND (:categoryId IS NULL OR p.category_id IN (SELECT id FROM cat))
          AND (:discountedOnly = false OR EXISTS (
            SELECT 1 FROM product_variants pvd
            WHERE pvd.product_id = p.id AND pvd.is_active = true AND pvd.discounted_price IS NOT NULL
          ))
          AND (
            :needVariantFilter = false
            OR EXISTS (
              SELECT 1
              FROM product_variants pv
              LEFT JOIN inventory i ON i.variant_id = pv.id
              WHERE pv.product_id = p.id
                AND pv.is_active = true
                AND (:minPrice IS NULL OR COALESCE(pv.discounted_price, pv.price) >= :minPrice)
                AND (:maxPrice IS NULL OR COALESCE(pv.discounted_price, pv.price) <= :maxPrice)
                AND (:inStock IS NULL OR :inStock = false OR (COALESCE(i.on_hand,0) - COALESCE(i.reserved,0)) > 0)
            )
          )
        """,
        nativeQuery = true
    )
    fun searchNewest(
        @Param("categoryId") categoryId: Long?,
        @Param("minPrice") minPrice: BigDecimal?,
        @Param("maxPrice") maxPrice: BigDecimal?,
        @Param("inStock") inStock: Boolean?,
        @Param("needVariantFilter") needVariantFilter: Boolean,
        @Param("discountedOnly") discountedOnly: Boolean,
        pageable: Pageable
    ): Page<ProductEntity>

    // ---------- Fuzzy fallback (pg_trgm) ----------
    @Query(
        value = """
        WITH RECURSIVE cat(id) AS (
          SELECT id FROM categories WHERE id = :categoryId
          UNION ALL
          SELECT c.id FROM categories c JOIN cat ON c.parent_id = cat.id
        )
        SELECT
          p.id, p.category_id, p.title, p.slug, p.description, p.brand, p.attributes, p.base_price, p.discounted_price, p.is_active, p.created_at, p.updated_at
        FROM products p
        WHERE p.is_active = true
          AND (:categoryId IS NULL OR p.category_id IN (SELECT id FROM cat))
          AND (:discountedOnly = false OR EXISTS (
            SELECT 1 FROM product_variants pvd
            WHERE pvd.product_id = p.id AND pvd.is_active = true AND pvd.discounted_price IS NOT NULL
          ))
          AND (p.title % :q)  -- trigram match
        ORDER BY similarity(p.title, :q) DESC, p.created_at DESC
        """,
        countQuery = """
        WITH RECURSIVE cat(id) AS (
          SELECT id FROM categories WHERE id = :categoryId
          UNION ALL
          SELECT c.id FROM categories c JOIN cat ON c.parent_id = cat.id
        )
        SELECT count(*)
        FROM products p
        WHERE p.is_active = true
          AND (:categoryId IS NULL OR p.category_id IN (SELECT id FROM cat))
          AND (:discountedOnly = false OR EXISTS (
            SELECT 1 FROM product_variants pvd
            WHERE pvd.product_id = p.id AND pvd.is_active = true AND pvd.discounted_price IS NOT NULL
          ))
          AND (p.title % :q)
        """,
        nativeQuery = true
    )
    fun searchFuzzyTitle(
        @Param("q") q: String,
        @Param("categoryId") categoryId: Long?,
        @Param("discountedOnly") discountedOnly: Boolean,
        pageable: Pageable
    ): Page<ProductEntity>


    @Query(
        value = """
      WITH RECURSIVE cat(id) AS (
        SELECT id FROM categories WHERE id = :categoryId
        UNION ALL
        SELECT c.id FROM categories c JOIN cat ON c.parent_id = cat.id
      )
      SELECT p.id, p.category_id, p.title, p.slug, p.description, p.brand, p.attributes, p.base_price, p.discounted_price, p.is_active, p.created_at, p.updated_at
      FROM products p
      WHERE p.is_active = true
        AND (:categoryId IS NULL OR p.category_id IN (SELECT id FROM cat))
        AND (:discountedOnly = false OR EXISTS (
          SELECT 1 FROM product_variants pvd
          WHERE pvd.product_id = p.id AND pvd.is_active = true AND pvd.discounted_price IS NOT NULL
        ))
        AND (
          :needVariantFilter = false
          OR EXISTS (
            SELECT 1
            FROM product_variants pv
            LEFT JOIN inventory i ON i.variant_id = pv.id
            WHERE pv.product_id = p.id
              AND pv.is_active = true
              AND (:minPrice IS NULL OR pv.price >= :minPrice)
              AND (:maxPrice IS NULL OR pv.price <= :maxPrice)
              AND (:inStock IS NULL OR :inStock = false OR (COALESCE(i.on_hand,0) - COALESCE(i.reserved,0)) > 0)
          )
        )
      ORDER BY
        (SELECT min(COALESCE(pv.discounted_price, pv.price)) FROM product_variants pv WHERE pv.product_id = p.id AND pv.is_active = true) ASC NULLS LAST,
        p.created_at DESC
      """,
        countQuery = """
      WITH RECURSIVE cat(id) AS (
        SELECT id FROM categories WHERE id = :categoryId
        UNION ALL
        SELECT c.id FROM categories c JOIN cat ON c.parent_id = cat.id
      )
      SELECT count(*)
      FROM products p
      WHERE p.is_active = true
        AND (:categoryId IS NULL OR p.category_id IN (SELECT id FROM cat))
        AND (:discountedOnly = false OR EXISTS (
          SELECT 1 FROM product_variants pvd
          WHERE pvd.product_id = p.id AND pvd.is_active = true AND pvd.discounted_price IS NOT NULL
        ))
        AND (
          :needVariantFilter = false
          OR EXISTS (
            SELECT 1
            FROM product_variants pv
            LEFT JOIN inventory i ON i.variant_id = pv.id
            WHERE pv.product_id = p.id
              AND pv.is_active = true
              AND (:minPrice IS NULL OR pv.price >= :minPrice)
              AND (:maxPrice IS NULL OR pv.price <= :maxPrice)
              AND (:inStock IS NULL OR :inStock = false OR (COALESCE(i.on_hand,0) - COALESCE(i.reserved,0)) > 0)
          )
        )
      """,
        nativeQuery = true
    )
    fun searchPriceAsc(
        @Param("categoryId") categoryId: Long?,
        @Param("minPrice") minPrice: BigDecimal?,
        @Param("maxPrice") maxPrice: BigDecimal?,
        @Param("inStock") inStock: Boolean?,
        @Param("needVariantFilter") needVariantFilter: Boolean,
        @Param("discountedOnly") discountedOnly: Boolean,
        pageable: Pageable
    ): Page<ProductEntity>

    @Query(
        value = """
      WITH RECURSIVE cat(id) AS (
        SELECT id FROM categories WHERE id = :categoryId
        UNION ALL
        SELECT c.id FROM categories c JOIN cat ON c.parent_id = cat.id
      )
      SELECT p.id, p.category_id, p.title, p.slug, p.description, p.brand, p.attributes, p.base_price, p.discounted_price, p.is_active, p.created_at, p.updated_at
      FROM products p
      WHERE p.is_active = true
        AND (:categoryId IS NULL OR p.category_id IN (SELECT id FROM cat))
        AND (:discountedOnly = false OR EXISTS (
          SELECT 1 FROM product_variants pvd
          WHERE pvd.product_id = p.id AND pvd.is_active = true AND pvd.discounted_price IS NOT NULL
        ))
        AND (
          :needVariantFilter = false
          OR EXISTS (
            SELECT 1
            FROM product_variants pv
            LEFT JOIN inventory i ON i.variant_id = pv.id
            WHERE pv.product_id = p.id
              AND pv.is_active = true
              AND (:minPrice IS NULL OR pv.price >= :minPrice)
              AND (:maxPrice IS NULL OR pv.price <= :maxPrice)
              AND (:inStock IS NULL OR :inStock = false OR (COALESCE(i.on_hand,0) - COALESCE(i.reserved,0)) > 0)
          )
        )
      ORDER BY
        (SELECT min(COALESCE(pv.discounted_price, pv.price)) FROM product_variants pv WHERE pv.product_id = p.id AND pv.is_active = true) DESC NULLS LAST,
        p.created_at DESC
      """,
        countQuery = """
      WITH RECURSIVE cat(id) AS (
        SELECT id FROM categories WHERE id = :categoryId
        UNION ALL
        SELECT c.id FROM categories c JOIN cat ON c.parent_id = cat.id
      )
      SELECT count(*)
      FROM products p
      WHERE p.is_active = true
        AND (:categoryId IS NULL OR p.category_id IN (SELECT id FROM cat))
        AND (:discountedOnly = false OR EXISTS (
          SELECT 1 FROM product_variants pvd
          WHERE pvd.product_id = p.id AND pvd.is_active = true AND pvd.discounted_price IS NOT NULL
        ))
        AND (
          :needVariantFilter = false
          OR EXISTS (
            SELECT 1
            FROM product_variants pv
            LEFT JOIN inventory i ON i.variant_id = pv.id
            WHERE pv.product_id = p.id
              AND pv.is_active = true
              AND (:minPrice IS NULL OR pv.price >= :minPrice)
              AND (:maxPrice IS NULL OR pv.price <= :maxPrice)
              AND (:inStock IS NULL OR :inStock = false OR (COALESCE(i.on_hand,0) - COALESCE(i.reserved,0)) > 0)
          )
        )
      """,
        nativeQuery = true
    )
    fun searchPriceDesc(
        @Param("categoryId") categoryId: Long?,
        @Param("minPrice") minPrice: BigDecimal?,
        @Param("maxPrice") maxPrice: BigDecimal?,
        @Param("inStock") inStock: Boolean?,
        @Param("needVariantFilter") needVariantFilter: Boolean,
        @Param("discountedOnly") discountedOnly: Boolean,
        pageable: Pageable
    ): Page<ProductEntity>
}
