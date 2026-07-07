package com.kazemieh.shop.academy.persistence

import com.kazemieh.shop.academy.persistence.entity.LessonQuestionEntity
import org.springframework.data.jpa.repository.JpaRepository

interface LessonQuestionRepository : JpaRepository<LessonQuestionEntity, Long> {
    fun findAllByLessonIdOrderByCreatedAtAsc(lessonId: Long): List<LessonQuestionEntity>
}
