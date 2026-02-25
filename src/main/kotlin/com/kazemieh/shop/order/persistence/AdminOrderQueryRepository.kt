package com.kazemieh.shop.order.persistence

import com.kazemieh.shop.order.persistence.entity.OrderEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.Repository
import org.springframework.data.repository.query.Param

interface AdminOrderQueryRepository : Repository<OrderEntity, Long> {

    @Query(
        """
        select o from OrderEntity o
        join fetch o.user u
        where (:status is null or o.status = :status)
          and (:userId is null or u.id = :userId)
        """,
        countQuery = """
        select count(o) from OrderEntity o
        join o.user u
        where (:status is null or o.status = :status)
          and (:userId is null or u.id = :userId)
        """
    )
    fun search(
        @Param("status") status: com.kazemieh.shop.order.persistence.entity.OrderStatus?,
        @Param("userId") userId: Long?,
        pageable: Pageable
    ): Page<OrderEntity>

    @Query(
        """
        select o from OrderEntity o
        join fetch o.user u
        left join fetch o.items i
        where o.id = :id
        """
    )
    fun findDetail(@Param("id") id: Long): OrderEntity?
}