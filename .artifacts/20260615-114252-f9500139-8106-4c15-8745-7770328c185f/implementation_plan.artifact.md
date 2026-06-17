# پیاده‌سازی بخش وبلاگ برای سمت کلاینت (Backend)

برای اینکه کلاینت بتواند لیست مقالات را نمایش دهد و با کلیک روی هر کدام جزییات را ببیند، زیرساخت‌های لازم در سمت سرور (Spring Boot) را تکمیل و بهینه می‌کنیم.

## تغییرات پیشنهادی

### ۱. بخش مدل‌ها و DTOها
افزودن فیلدهای مورد نیاز به پاسخ‌ها مانند نام نویسنده برای نمایش بهتر در کلاینت.

#### [BlogDtos.kt](file:///D:/Android/AndroidStudioProjects/ShopServer/Shop/src/main/kotlin/com/kazemieh/shop/blog/api/dto/BlogDtos.kt)
- افزودن `authorName` به `BlogResponse` و `BlogSummaryResponse`.

---

### ۲. بخش Persistence (مخزن داده)
افزودن قابلیت جستجو در بین مقالات منتشر شده.

#### [BlogRepository.kt](file:///D:/Android/AndroidStudioProjects/ShopServer/Shop/src/main/kotlin/com/kazemieh/shop/blog/persistence/BlogRepository.kt)
- افزودن متد جستجو:
```kotlin
fun findByStatusAndTitleContainingIgnoreCaseOrSummaryContainingIgnoreCase(
    status: BlogStatus,
    titleQuery: String,
    summaryQuery: String,
    pageable: Pageable
): Page<BlogEntity>
```

---

### ۳. بخش Service (منطق کسب و کار)
بهینه‌سازی متدهای دریافت لیست برای پشتیبانی از جستجو و پر کردن اطلاعات نویسنده.

#### [BlogService.kt](file:///D:/Android/AndroidStudioProjects/ShopServer/Shop/src/main/kotlin/com/kazemieh/shop/blog/application/BlogService.kt)
- تغییر متد `getPublishedBlogs` برای پذیرش پارامتر جستجو.
- پیاده‌سازی منطق تبدیل `authorId` به نام نویسنده در پاسخ‌ها.
- افزودن متد `getRelatedBlogs` برای نمایش مقالات پیشنهادی در صفحه جزییات.

---

### ۴. بخش Controller (رابط‌های API)
بروزرسانی اندپوینت‌ها برای استفاده در کلاینت.

#### [BlogController.kt](file:///D:/Android/AndroidStudioProjects/ShopServer/Shop/src/main/kotlin/com/kazemieh/shop/blog/api/BlogController.kt)
- اضافه کردن پارامتر `query` به متد `getPublishedBlogs`.
- افزودن اندپوینت `/api/blogs/{slug}/related` برای دریافت مقالات مرتبط.

## برنامه تایید (Verification Plan)

### تست‌های خودکار
- اجرای تست‌های واحد برای `BlogService` جهت اطمینان از صحت فیلتر مقالات منتشر شده.
- اجرای `gradle_build` برای اطمینان از نبود خطای کامپایل.

### تست دستی
- فراخوانی اندپوینت `GET /api/blogs` با پارامترهای pagination و جستجو.
- فراخوانی اندپوینت `GET /api/blogs/{slug}` و بررسی افزایش `viewCount`.
- بررسی خروجی JSON برای اطمینان از وجود فیلدهای `authorName` و `readingTimeMinutes`.
