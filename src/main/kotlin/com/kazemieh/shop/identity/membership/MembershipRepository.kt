package com.kazemieh.shop.identity.membership

import org.springframework.data.jpa.repository.JpaRepository

interface MembershipRepository : JpaRepository<MembershipEntity, Long> {
    fun findFirstByUserIdOrderByExpiresAtDesc(userId: Long): MembershipEntity?
}
