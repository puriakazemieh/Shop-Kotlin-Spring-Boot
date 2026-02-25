package com.kazemieh.shop.catalog.persistence.entity

import jakarta.persistence.*
import org.hibernate.annotations.UpdateTimestamp
import java.time.OffsetDateTime

@Entity
@Table(name = "inventory")
class InventoryEntity(

    @Id
    @Column(name = "variant_id")
    var variantId: Long = 0,

    @Column(name = "on_hand", nullable = false)
    var onHand: Int = 0,

    @Column(name = "reserved", nullable = false)
    var reserved: Int = 0,

    @Version
    @Column(name = "version", nullable = false)
    var version: Int = 0,

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    var updatedAt: OffsetDateTime? = null,
)