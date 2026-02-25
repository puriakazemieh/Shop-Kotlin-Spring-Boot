package com.kazemieh.shop.customer.address.persistence

import com.kazemieh.shop.customer.address.persistence.entity.AddressEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface AddressRepository : JpaRepository<AddressEntity, Long> {

    fun findAllByUserIdOrderByIsDefaultDescCreatedAtDesc(userId: Long): List<AddressEntity>

    fun findFirstByUserIdAndIsDefaultTrue(userId: Long): AddressEntity?

    fun findTopByUserIdOrderByCreatedAtDesc(userId: Long): AddressEntity?

    fun countByUserId(userId: Long): Long

    @Modifying
    @Query("update AddressEntity a set a.isDefault = false where a.user.id = :userId and a.isDefault = true")
    fun clearDefaultForUser(@Param("userId") userId: Long): Int

    @Modifying
    @Query(
        "update AddressEntity a set a.isDefault = false " +
        "where a.user.id = :userId and a.isDefault = true and a.id <> :keepId"
    )
    fun clearDefaultExcept(@Param("userId") userId: Long, @Param("keepId") keepId: Long): Int
}