package com.kazemieh.shop.blog.persistence.entity

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.kazemieh.shop.blog.api.dto.BlogBlock
import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime

@Entity
@Table(name = "blogs")
class BlogEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    @Column(nullable = false)
    var title: String = "",

    @Column(nullable = false, unique = true)
    var slug: String = "",

    @Convert(converter = BlogContentConverter::class)
    @Column(nullable = false, columnDefinition = "TEXT")
    var content: List<BlogBlock> = emptyList(),

    @Column(columnDefinition = "TEXT")
    var summary: String? = null,

    @Column(name = "thumbnail_url")
    var thumbnailUrl: String? = null,

    @Column(name = "view_count")
    var viewCount: Long = 0,

    @Column(name = "reading_time_minutes")
    var readingTimeMinutes: Int = 0,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: BlogStatus = BlogStatus.DRAFT,

    @Column(name = "author_id")
    var authorId: Long? = null,

    @Column(name = "category_id")
    var categoryId: Long? = null,

    @Column(name = "is_featured")
    var isFeatured: Boolean = false,

    @Column(name = "meta_title")
    var metaTitle: String? = null,

    @Column(name = "meta_description", columnDefinition = "TEXT")
    var metaDescription: String? = null,

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    var createdAt: LocalDateTime? = null,

    @UpdateTimestamp
    @Column(name = "updated_at")
    var updatedAt: LocalDateTime? = null
)

enum class BlogStatus {
    DRAFT, PUBLISHED
}

@Converter
class BlogContentConverter : AttributeConverter<List<BlogBlock>, String> {
    private val objectMapper: ObjectMapper = jacksonObjectMapper()

    override fun convertToDatabaseColumn(attribute: List<BlogBlock>?): String {
        return objectMapper.writeValueAsString(attribute ?: emptyList<BlogBlock>())
    }

    override fun convertToEntityAttribute(dbData: String?): List<BlogBlock> {
        if (dbData.isNullOrBlank()) return emptyList()
        return objectMapper.readValue(dbData, object : TypeReference<List<BlogBlock>>() {})
    }
}
