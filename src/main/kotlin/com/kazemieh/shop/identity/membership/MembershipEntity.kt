package com.kazemieh.shop.identity.membership

import com.kazemieh.shop.identity.persistence.entity.UserEntity
import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.time.OffsetDateTime

enum class MembershipTier { GOLD }

/** عضویتِ ویژه‌ی پرداختی — به‌سبکِ Prime: پرداختِ حقِ‌اشتراک در برابرِ تخفیفِ خودکار روی همه‌ی خریدها. */
@Entity
@Table(name = "memberships", indexes = [Index(name = "idx_memberships_user", columnList = "user_id")])
class MembershipEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    var user: UserEntity? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var tier: MembershipTier = MembershipTier.GOLD,

    @Column(name = "started_at", nullable = false)
    var startedAt: OffsetDateTime = OffsetDateTime.now(),

    @Column(name = "expires_at", nullable = false)
    var expiresAt: OffsetDateTime,

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: OffsetDateTime? = null
)
