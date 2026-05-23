package com.kazemieh.shop.catalog.persistence.entity

import jakarta.persistence.*

@Entity
@Table(name = "option_type")
class OptionTypeEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false, unique = true, length = 100)
    var name: String
)
