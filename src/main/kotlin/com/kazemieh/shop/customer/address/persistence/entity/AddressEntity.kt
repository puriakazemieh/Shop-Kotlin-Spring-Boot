package com.kazemieh.shop.customer.address.persistence.entity

import com.kazemieh.shop.identity.persistence.entity.UserEntity
import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.time.OffsetDateTime

@Entity
@Table(name = "addresses")
class AddressEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    var user: UserEntity? = null,

    @Column(name = "receiver_name", nullable = false, length = 255)
    var receiverName: String = "",

    @Column(name = "receiver_phone", nullable = false, length = 30)
    var receiverPhone: String = "",

    @Column(name = "country", nullable = false, length = 80)
    var country: String = "IR",

    @Column(name = "province", nullable = false, length = 120)
    var province: String = "",

    @Column(name = "city", nullable = false, length = 120)
    var city: String = "",

    @Column(name = "address_line1", nullable = false, length = 255)
    var addressLine1: String = "",

    @Column(name = "address_line2", length = 255)
    var addressLine2: String? = null,

    @Column(name = "postal_code", length = 20)
    var postalCode: String? = null,

    @Column(name = "is_default", nullable = false)
    var isDefault: Boolean = false,

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: OffsetDateTime? = null,
)