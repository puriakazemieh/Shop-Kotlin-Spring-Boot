package com.kazemieh.shop.catalog.persistence.entity

import jakarta.persistence.*

@Entity
@Table(name = "colors")
class ColorEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    @Column(nullable = false, unique = true, length = 60)
    var name: String = "",

    @Column(length = 7)
    var hex: String? = null,
)