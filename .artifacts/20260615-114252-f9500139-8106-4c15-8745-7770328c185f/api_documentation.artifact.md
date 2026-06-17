# مستندات جامع API سیستم وبلاگ (Blog System)

این مستندات شامل تمام اندپوینت‌های مورد نیاز برای پیاده‌سازی بخش وبلاگ در اپلیکیشن کلاینت و پنل مدیریت است.

---

## ۱. بخش کلاینت (Public/Client API)
این اندپوینت‌ها برای کاربران عادی در دسترس هستند و فقط مقالات **منتشر شده (PUBLISHED)** را برمی‌گردانند.

### ۱.۱. دریافت لیست مقالات (با جستجو و فیلتر)
- **URL**: `GET /api/blogs`
- **پارامترها**:
    - `query` (اختیاری): جستجو در عنوان و خلاصه.
    - `categoryId` (اختیاری): فیلتر بر اساس شناسه دسته‌بندی.
    - `page`, `size`, `sort`: پارامترهای صفحه‌بندی (مثلاً `?page=0&size=10`).
- **خروجی**: صفحه‌ای از `BlogSummaryResponse`.

### ۱.۲. دریافت مقالات برگزیده (Featured)
- **URL**: `GET /api/blogs/featured`
- **کاربرد**: نمایش در اسلایدر یا بخش پیشنهادات ویژه.

### ۱.۳. جزییات کامل مقاله
- **URL**: `GET /api/blogs/{slug}`
- **نکته**: فراخوانی این متد به صورت خودکار `viewCount` را یک واحد افزایش می‌دهد.

### ۱.۴. مقالات مرتبط
- **URL**: `GET /api/blogs/{slug}/related`
- **منطق**: ۳ مقاله پیشنهادی (اولویت با دسته‌بندی مشترک).

### ۱.۵. لیست دسته‌بندی‌ها
- **URL**: `GET /api/blogs/categories`
- **کاربرد**: نمایش در منو یا سایدبار برای فیلترینگ.

---

## ۲. بخش مدیریت (Admin API)
تمام این اندپوینت‌ها نیاز به نقش **ADMIN** دارند و آدرس آن‌ها با `/api/admin/blogs` شروع می‌شود.

### ۲.۱. لیست جامع مقالات ادمین
- **URL**: `GET /api/admin/blogs`
- **تفاوت**: شامل مقالات `DRAFT` و نمایش آمار دقیق بازدید است.

### ۲.۲. مدیریت مقالات (CRUD)
- **ساخت**: `POST /api/admin/blogs`
- **ویرایش**: `PUT /api/admin/blogs/{id}`
- **حذف**: `DELETE /api/admin/blogs/{id}`
- **مشاهده (شامل Draft)**: `GET /api/admin/blogs/{slug}`

### ۲.۳. مدیریت دسته‌بندی‌ها (Category CRUD)
- **ساخت**: `POST /api/admin/blogs/categories`
- **ویرایش**: `PUT /api/admin/blogs/categories/{id}`
- **حذف**: `DELETE /api/admin/blogs/categories/{id}`

### ۲.۴. آپلود مدیا (تصاویر)
- **URL**: `POST /api/admin/blogs/media/upload`
- **نوع**: `multipart/form-data`
- **فیلد**: `file`

---

## ۳. ساختار داده‌های مهم (DTOs)

### BlogSummaryResponse (نمای لیست)
```json
{
  "id": 1,
  "title": "عنوان مقاله",
  "slug": "article-slug",
  "summary": "خلاصه کوتاه...",
  "thumbnailUrl": "https://...",
  "viewCount": 150,
  "readingTimeMinutes": 5,
  "authorName": "نام نویسنده",
  "categoryName": "نام دسته‌بندی",
  "isFeatured": true,
  "createdAt": "2024-06-15T10:00:00"
}
```

### BlogResponse (نمای جزییات)
شامل تمام فیلدهای بالا به علاوه:
- `content`: محتوای کامل (ساختار JSON برای ادیتور).
- `author`: شامل `id` و `name`.
- `category`: شامل `id`, `name`, `slug`.
- `metaTitle` و `metaDescription`: برای سئو.

---

> [!TIP]
> برای نمایش بهتر در کلاینت، همیشه از `slug` برای جابجایی بین صفحات استفاده کنید تا سئوی اپلیکیشن/وب‌سایت شما بهبود یابد.
