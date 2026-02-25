package com.kazemieh.shop.catalog.persistence

import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.Repository
import org.springframework.data.repository.query.Param

interface VariantQueryRepository : Repository<Any, Long> {

    @Query(
        value = """
        select 
          pv.id as variantId,
          pv.price as price,
          p.title as title,
          s.name as sizeName,
          c.name as colorName,
          pv.is_active as isActive
        from product_variants pv
        join products p on p.id = pv.product_id
        join sizes s on s.id = pv.size_id
        join colors c on c.id = pv.color_id
        where pv.id in (:ids)
        """,
        nativeQuery = true
    )
    fun findSnapshots(@Param("ids") ids: List<Long>): List<VariantSnapshotProjection>
}