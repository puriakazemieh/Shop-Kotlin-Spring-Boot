package com.kazemieh.shop.academy.persistence.entity

import jakarta.persistence.*
import java.time.OffsetDateTime

/**
 * گواهیِ پایانِ دوره — پس از قبولی در آزمون صادر می‌شود. idempotent per (user, course).
 * شماره‌ی گواهی یکتاست و می‌تواند برای استعلام/رندرِ PDF استفاده شود.
 */
@Entity
@Table(
    name = "certificates",
    uniqueConstraints = [UniqueConstraint(name = "ux_cert_user_course", columnNames = ["user_id", "course_id"])]
)
class CertificateEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    @Column(name = "user_id", nullable = false)
    var userId: Long,

    @Column(name = "course_id", nullable = false)
    var courseId: Long,

    @Column(name = "cert_number", nullable = false, unique = true, length = 40)
    var certNumber: String,

    @Column(name = "issued_at", nullable = false)
    var issuedAt: OffsetDateTime = OffsetDateTime.now()
)
