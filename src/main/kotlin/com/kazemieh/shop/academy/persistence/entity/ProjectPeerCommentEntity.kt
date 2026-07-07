package com.kazemieh.shop.academy.persistence.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.time.OffsetDateTime

/** نظرِ همتایان روی یک پروژه‌ی تاییدشده — گسترشِ ارزیابیِ پروژه‌محور برایِ یادگیریِ گروهی. */
@Entity
@Table(name = "project_peer_comments", indexes = [Index(name = "idx_peer_comments_submission", columnList = "submission_id")])
class ProjectPeerCommentEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    @Column(name = "submission_id", nullable = false)
    var submissionId: Long,

    @Column(name = "user_id", nullable = false)
    var userId: Long,

    @Column(columnDefinition = "text", nullable = false)
    var comment: String,

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: OffsetDateTime? = null
)
