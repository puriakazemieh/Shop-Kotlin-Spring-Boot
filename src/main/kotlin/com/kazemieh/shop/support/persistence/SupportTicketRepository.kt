package com.kazemieh.shop.support.persistence

import com.kazemieh.shop.support.persistence.entity.SupportTicketEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface SupportTicketRepository : JpaRepository<SupportTicketEntity, Long> {
    fun findAllByUserIdOrderByUpdatedAtDesc(userId: Long): List<SupportTicketEntity>
    fun findAllByOrderByUpdatedAtDesc(pageable: Pageable): Page<SupportTicketEntity>
}
