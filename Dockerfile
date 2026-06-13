# استفاده از جاوا نسخه ۱۷ (یا هر نسخه‌ای که پروژه‌ات باهاش نوشته شده)
#FROM eclipse-temurin:17-jre-alpine
# استفاده از سرور جایگزین برای دور زدن تحریم داکر‌هاب
FROM docker.iranserver.com/eclipse-temurin:17-jre-alpine

# کپی کردن فایل تک و اجرایی پروژه به داخل کانتینر
COPY build/libs/*.jar app.jar
# کپی کردن فایل از همین پوشه فعلی به داخل سرور
#COPY *.jar app.jar

# دستور اجرای برنامه اسپرینگ‌بوت
ENTRYPOINT ["java", "-jar", "/app.jar"]