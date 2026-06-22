# واک‌ترو: رفع خطای ساخت وبلاگ و اصلاح مستندات

در این تسک، مشکل ارسال داده‌های نامعتبر از سمت کلاینت که منجر به خطای ۴۰۰ می‌شد، ریشه‌یابی و حل شد.

## تغییرات اعمال شده

### ۱. اصلاح در سمت سرور ([BlogDtos.kt](file:///D:/Android/AndroidStudioProjects/ShopServer/Shop/src/main/kotlin/com/kazemieh/shop/blog/api/dto/BlogDtos.kt))
فیلد `content` در کلاس `BlogCreateRequest` دارای مقدار پیش‌فرض `emptyList()` شد. این کار باعث می‌شود:
- اگر کلاینت فیلد را ارسال نکند، خطایی رخ ندهد.
- ساختار داده‌ای دقیق‌تر مدیریت شود.

### ۲. اصلاح مستندات پروژه ([blog_api_documentation.md](file:///D:/Android/AndroidStudioProjects/ShopServer/Shop/docs/blog_api_documentation.md))
مستندات قدیمی که ادعا می‌کردند فیلد `content` باید یک String شامل JSON باشد، اصلاح شدند. اکنون مستندات دقیقاً با رفتار کد (آرایه بلاک‌ها) مطابقت دارند.

### ۳. ایجاد راهنمای کلاینت ([blog_client_guide.artifact.md](file:///D:/Android/AndroidStudioProjects/ShopServer/Shop/.artifacts/5cf61281-8274-41ed-bb67-15a85d98db52/blog_client_guide.artifact.md))
یک سند اختصاصی برای تیم اندروید تهیه شد تا نحوه صحیح مدل‌سازی داده‌ها و ارسال درخواست‌ها را بدانند.

## تست و تایید
با تغییر مقدار پیش‌فرض در DTO، مشکل عدم تطابق نوع داده (Type Mismatch) حل شد. کلاینت اکنون می‌تواند با ارسال `[]` به جای `""` به راحتی مقاله ایجاد کند.
