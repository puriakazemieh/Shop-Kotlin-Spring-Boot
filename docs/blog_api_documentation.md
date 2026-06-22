# مستندات کامل API سیستم وبلاگ

این مستند شامل تمامی نقاط دسترسی (Endpoints)، ساختار درخواست‌ها، پاسخ‌ها و کدهای خطای مربوط به سیستم وبلاگ است.

---

## ۱. کدهای خطا (Error Codes)

در صورت بروز خطا، پاسخ با وضعیت (Status Code) متناسب و بدنه زیر برگردانده می‌شود:
```json
{
  "errorCode": "BLOG_NOT_FOUND",
  "message": "Blog not found with slug: my-post"
}
```

| کد خطا | وضعیت HTTP | توضیح |
| :--- | :--- | :--- |
| `BLOG_NOT_FOUND` | 404 Not Found | وبلاگ با شناسه یا اسلاگ مورد نظر یافت نشد. |
| `BLOG_SLUG_EXISTS` | 409 Conflict | اسلاگ وارد شده تکراری است. |
| `ACCESS_DENIED` | 403 Forbidden | کاربر دسترسی ادمین ندارد. |

---

## ۲. متدهای عمومی (Public APIs)

### لیست مقالات (منتشر شده)
`GET /api/blogs`

**پارامترهای کوئری (Optional):**
- `page`: شماره صفحه (پیش‌فرض 0)
- `size`: تعداد در هر صفحه (پیش‌فرض 20)

**پاسخ (200 OK):**
```json
{
  "content": [
    {
      "id": 1,
      "title": "آموزش کاتلین",
      "slug": "kotlin-tutorial",
      "summary": "خلاصه‌ای از آموزش کاتلین...",
      "thumbnailUrl": "/uploads/image1.jpg",
      "viewCount": 150,
      "readingTimeMinutes": 5,
      "createdAt": "2026-06-15T11:00:00"
    }
  ],
  "totalPages": 1,
  "totalElements": 1
}
```

### جزئیات یک مقاله
`GET /api/blogs/{slug}`

**پاسخ (200 OK):**
```json
{
  "id": 1,
  "title": "آموزش کاتلین",
  "slug": "kotlin-tutorial",
  "content": [
    {
      "type": "paragraph",
      "content": "این متن مقاله است."
    }
  ], 
  "summary": "خلاصه‌ای از آموزش کاتلین...",
  "thumbnailUrl": "/uploads/image1.jpg",
  "viewCount": 151,
  "readingTimeMinutes": 5,
  "status": "PUBLISHED",
  "createdAt": "2026-06-15T11:00:00",
  "updatedAt": "2026-06-15T11:10:00"
}
```


---

## ۳. متدهای مدیریت (Admin APIs)
*نیاز به توکن ادمین در Header دارد.*

### ایجاد مقاله جدید
`POST /api/admin/blogs`

**درخواست:**
```json
{
  "title": "عنوان مقاله",
  "content": [
    {
      "type": "paragraph", 
      "content": "سلام، این یک مقاله جدید است."
    }
  ],
  "summary": "خلاصه اختیاری",
  "thumbnailUrl": "/uploads/cover.jpg",
  "status": "PUBLISHED" 
}
```


### ویرایش مقاله
`PUT /api/admin/blogs/{id}`

**درخواست:** (همه فیلدها اختیاری هستند)
```json
{
  "title": "عنوان جدید",
  "status": "DRAFT"
}
```

### حذف مقاله
`DELETE /api/admin/blogs/{id}`
**پاسخ:** `204 No Content`

### آپلود تصویر برای ادیتور
`POST /api/admin/blogs/media/upload`

**درخواست:** `Multipart/Form-Data`
- `file`: فایل تصویر

**پاسخ (200 OK):**
```json
{
  "url": "/uploads/uuid_filename.png"
}
```

---

## ۴. ساختار محتوا (Content Structure)

محتوای مقاله به صورت یک آرایه از اشیاء (Blocks) ارسال و دریافت می‌شود. هر بلاک دارای فیلدهای زیر است:

| فیلد | نوع | توضیح |
| :--- | :--- | :--- |
| `type` | String | نوع بلاک: `paragraph`, `header`, `image` |
| `content` | String | متن (برای پاراگراف و تیتر) یا لینک تصویر |
| `level` | Integer | (اختیاری) سطح تیتر (1, 2, 3) |

**نمونه محتوا:**
```json
[
  {
    "type": "header",
    "content": "معرفی بخش اول",
    "level": 2
  },
  {
    "type": "paragraph",
    "content": "این یک متن نمونه است."
  },
  {
    "type": "image",
    "content": "https://example.com/uploads/abc.jpg"
  }
]
```

