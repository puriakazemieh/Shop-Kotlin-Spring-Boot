# راهنمای اتصال کلاینت به سیستم وبلاگ

این راهنما برای توسعه‌دهندگان اندروید آماده شده تا به درستی با API وبلاگ تعامل داشته باشند.

## ۱. رفع خطای ۴۰۰ (Malformed JSON)

خطایی که دریافت می‌کردید به دلیل ارسال رشته خالی (`""`) برای فیلد `content` بود.

> [!IMPORTANT]
> **راه حل:**
> - اگر مقاله‌ای محتوا ندارد، فیلد `content` را ارسال نکنید یا یک آرایه خالی `[]` بفرستید.
> - هرگز فیلد `content` را به صورت String خالی (`""`) یا NULL نفرستید.

## ۲. ساختار کلاس‌های کاتلین (Android)

برای راحتی کار، می‌توانید از این کلاس‌ها در پروژه اندروید خود استفاده کنید:

```kotlin
enum class BlogStatus {
    DRAFT, PUBLISHED
}

data class BlogBlock(
    val type: String,    // "header", "paragraph", "image"
    val content: String, // متن یا لینک تصویر
    val level: Int? = null // فقط برای header (1, 2, 3)
)

data class BlogCreateRequest(
    val title: String,
    val content: List<BlogBlock> = emptyList(),
    val summary: String? = null,
    val thumbnailUrl: String? = null,
    val status: BlogStatus = BlogStatus.DRAFT,
    val categoryId: Long? = null,
    val isFeatured: Boolean = false,
    val metaTitle: String? = null,
    val metaDescription: String? = null
)
```

## ۳. نمونه درخواست صحیح (JSON)

در `POST /api/admin/blogs` ساختار بدنه باید به این صورت باشد:

```json
{
  "title": "عنوان مقاله",
  "content": [
    {
      "type": "header",
      "content": "تیتر اول",
      "level": 1
    },
    {
      "type": "paragraph",
      "content": "این یک متن نمونه در کلاینت است."
    }
  ],
  "status": "PUBLISHED",
  "isFeatured": false
}
```

## ۴. مدیریت آپلود تصاویر

برای قرار دادن تصویر در متن بلاگ، ابتدا باید تصویر را آپلود کنید:
1. فراخوانی `POST /api/admin/blogs/media/upload` (بصورت Multipart).
2. دریافت `url` از پاسخ سرور.
3. اضافه کردن یک بلاک از نوع `image` با این `url` به آرایه `content`.
