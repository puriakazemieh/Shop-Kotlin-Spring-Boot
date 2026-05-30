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

    @Column(nullable = false, unique = true)
    var email: String = "",

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

    @Column(name = "phone")
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

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: OffsetDateTime? = null,

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    var updatedAt: OffsetDateTime? = null,

//    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
//    var addresses: MutableList<Address> = mutableListOf(),
//
//    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
//    var orders: MutableList<Order> = mutableListOf()

) /*: UserDetails {

    override fun getAuthorities(): Collection<GrantedAuthority> =
        listOf(SimpleGrantedAuthority("ROLE_${role.name}"))

    override fun getPassword(): String = passwordHash
    override fun getUsername(): String = email
    override fun isAccountNonExpired(): Boolean = true
    override fun isAccountNonLocked(): Boolean = true
    override fun isCredentialsNonExpired(): Boolean = true
    override fun isEnabled(): Boolean = isActive
}*/
