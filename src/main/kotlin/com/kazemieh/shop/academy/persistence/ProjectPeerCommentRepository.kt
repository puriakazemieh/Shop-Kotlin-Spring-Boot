package com.kazemieh.shop.academy.persistence

import com.kazemieh.shop.academy.persistence.entity.ProjectPeerCommentEntity
import org.springframework.data.jpa.repository.JpaRepository

interface ProjectPeerCommentRepository : JpaRepository<ProjectPeerCommentEntity, Long> {
    fun findAllBySubmissionIdOrderByCreatedAtAsc(submissionId: Long): List<ProjectPeerCommentEntity>
}
