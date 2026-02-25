package com.kazemieh.shop.catalog.persistence.entity

import jakarta.persistence.*

@Entity
@Table(name = "sizes")
class SizeEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    @Column(nullable = false, unique = true, length = 40)
    var name: String = "",

    @Column(name = "sort_order", nullable = false)
    var sortOrder: Int = 0,
)