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
  "content": "{\"blocks\": [...]}", 
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
  "content": "{\"blocks\": [{\"type\": \"paragraph\", \"text\": \"سلام\"}]}",
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

## ۴. ساختار پیشنهادی Content (JSON Blocks)

برای اینکه سمت اندروید به راحتی رندر کنید، محتوا را به این صورت در فیلد `content` ارسال و دریافت کنید:

```json
{
  "blocks": [
    {
      "type": "header",
      "data": { "text": "معرفی بخش اول", "level": 2 }
    },
    {
      "type": "paragraph",
      "data": { "text": "این یک متن نمونه است." }
    },
    {
      "type": "image",
      "data": {
        "url": "/uploads/abc.jpg",
        "caption": "توضیح تصویر"
      }
    }
  ]
}
```
*نکته: در سمت سرور، این کل ساختار به صورت یک String در دیتابیس ذخیره می‌شود.*
