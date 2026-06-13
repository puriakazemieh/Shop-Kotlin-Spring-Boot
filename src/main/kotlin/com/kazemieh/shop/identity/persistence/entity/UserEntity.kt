package com.kazemieh.shop.identity.persistence.entity

import com.kazemieh.shop.identity.domain.UserRole
import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.OffsetDateTime

@Entity
@Table(name = "users")
class UserEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    @Column(unique = true)
    var email: String? = null,

//    @JsonIgnore
    @Column(name = "password_hash", nullable = false)
    var passwordHash: String = "",

    @Column(name = "first_name", length = 50)
    var firstName: String? = null,

    @Column(name = "last_name", length = 50)
    var lastName: String? = null,

    @Column(name = "city", length = 50)
    var city: String? = null,

    @Column(name = "postal_code")
    var postalCode: Int? = null,

    @Column(name = "phone", unique = true)
    var phone: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var role: UserRole = UserRole.ADMIN,

    @Column(name = "is_active", nullable = false)
    var isActive: Boolean = true,

    @Column(name = "reset_password_token")
    var resetPasswordToken: String? = null,

    @Column(name = "reset_password_token_expiry")
    var resetPasswordTokenExpiry: OffsetDateTime? = null,

    @Column(name = "otp_code")
    var otpCode: String? = null,

    @Column(name = "otp_expiry")
    var otpExpiry: OffsetDateTime? = null,

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: OffsetDateTime? = null,

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    var updatedAt: OffsetDateTime? = null,

)