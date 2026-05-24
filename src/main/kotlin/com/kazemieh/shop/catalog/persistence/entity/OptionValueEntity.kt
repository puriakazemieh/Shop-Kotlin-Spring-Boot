package com.kazemieh.shop.catalog.persistence.entity

import com.fasterxml.jackson.annotation.JsonIgnore
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
    var value: String,

    @JsonIgnore
    @ManyToMany(mappedBy = "optionValues")
    var variants: MutableSet<ProductVariantEntity> = mutableSetOf()
)