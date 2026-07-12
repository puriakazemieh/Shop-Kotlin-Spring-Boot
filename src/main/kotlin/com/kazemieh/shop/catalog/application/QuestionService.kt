package com.kazemieh.shop.catalog.application

import com.kazemieh.shop.catalog.api.dto.*
import com.kazemieh.shop.catalog.persistence.ProductQuestionRepository
import com.kazemieh.shop.catalog.persistence.ProductRepository
import com.kazemieh.shop.catalog.persistence.entity.ProductQuestionEntity
import com.kazemieh.shop.identity.domain.UserRole
import com.kazemieh.shop.identity.persistence.UserRepository
import com.kazemieh.shop.shared.error.ApiException
import com.kazemieh.shop.shared.error.ErrorCodes
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime

@Service
class QuestionService(
    private val questionRepository: ProductQuestionRepository,
    private val productRepository: ProductRepository,
    private val userRepository: UserRepository
) {

    @Transactional(readOnly = true)
    fun listQuestionsAdmin(
        productId: Long?,
        isNew: Boolean?,
        page: Int,
        size: Int
    ): PageResponse<AdminInteractionResponse> {
        val pageable = PageRequest.of(page, size, Sort.by("createdAt").descending())
        val result = questionRepository.findAllByFilters(productId, isNew, pageable)

        return PageResponse(
            items = result.content.map { it.toAdminResponse() },
            page = result.number,
            size = result.size,
            totalElements = result.totalElements,
            totalPages = result.totalPages
        )
    }

    @Transactional(readOnly = true)
    fun getQuestionsByProduct(productId: Long): List<QuestionResponse> {
        val questions = questionRepository.findAllByProductIdAndParentIsNullOrderByCreatedAtDesc(productId)
        return questions.map { it.toResponse() }
    }

    @Transactional
    fun createQuestion(userId: Long, request: CreateQuestionRequest): QuestionResponse {
        val product = productRepository.findById(request.productId)
            .orElseThrow { ApiException(ErrorCodes.PRODUCT_NOT_FOUND, "Product not found", HttpStatus.NOT_FOUND) }
        val user = userRepository.findById(userId)
            .orElseThrow { ApiException(ErrorCodes.USER_NOT_FOUND, "User not found", HttpStatus.NOT_FOUND) }

        val parent = request.parentId?.let {
            questionRepository.findById(it)
                .orElseThrow { ApiException(ErrorCodes.QUESTION_NOT_FOUND, "Parent question not found", HttpStatus.NOT_FOUND) }
        }

        val question = ProductQuestionEntity(
            product = product,
            user = user,
            content = request.content,
            parent = parent,
            isNew = true
        )

        return questionRepository.save(question).toResponse()
    }

    @Transactional
    fun updateQuestion(userId: Long, questionId: Long, request: UpdateQuestionRequest): QuestionResponse {
        val question = questionRepository.findById(questionId)
            .orElseThrow { ApiException(ErrorCodes.QUESTION_NOT_FOUND, "Question not found", HttpStatus.NOT_FOUND) }

        if (question.user.id != userId) {
            throw ApiException(ErrorCodes.ACCESS_DENIED, "You can only edit your own questions", HttpStatus.FORBIDDEN)
        }

        question.content = request.content

        return questionRepository.save(question).toResponse()
    }

    @Transactional
    fun deleteQuestion(userId: Long, questionId: Long) {
        val question = questionRepository.findById(questionId)
            .orElseThrow { ApiException(ErrorCodes.QUESTION_NOT_FOUND, "Question not found", HttpStatus.NOT_FOUND) }

        if (question.user.id != userId) {
            throw ApiException(ErrorCodes.ACCESS_DENIED, "You can only delete your own questions", HttpStatus.FORBIDDEN)
        }

        questionRepository.delete(question)
    }

    private fun ProductQuestionEntity.toResponse(): QuestionResponse {
        val isSupport = this.user.role == UserRole.ADMIN
        val displayName = if (isSupport) "پشتیبانی کارمیلا"
            else "${this.user.firstName ?: ""} ${this.user.lastName ?: ""}".trim()
        return QuestionResponse(
            id = this.id,
            userId = this.user.id,
            userName = displayName,
            content = this.content,
            replies = this.replies.map { it.toResponse() },
            createdAt = this.createdAt ?: OffsetDateTime.now(),
            isSupport = isSupport
        )
    }

    private fun ProductQuestionEntity.toAdminResponse(): AdminInteractionResponse {
        return AdminInteractionResponse(
            id = this.id,
            productId = this.product.id,
            productTitle = this.product.title,
            userId = this.user.id,
            userName = "${this.user.firstName ?: ""} ${this.user.lastName ?: ""}".trim(),
            content = this.content,
            rating = null,
            isNew = this.isNew,
            createdAt = this.createdAt ?: OffsetDateTime.now()
        )
    }
}
