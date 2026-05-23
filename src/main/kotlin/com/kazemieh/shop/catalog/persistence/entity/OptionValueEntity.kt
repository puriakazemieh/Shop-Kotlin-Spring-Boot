package com.kazemieh.shop.catalog.persistence.entity

import jakarta.persistence.*

@Entity
@Table(name = "option_value")
class OptionValueEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "option_type_id", nullable = false)
    var optionType: OptionTypeEntity,

    @Column(nullable = false, length = 100)
    var value: String
)
