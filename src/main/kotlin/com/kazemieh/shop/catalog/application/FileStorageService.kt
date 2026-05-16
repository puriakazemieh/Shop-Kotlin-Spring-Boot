package com.kazemieh.shop.catalog.application

import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.nio.file.Files
import java.nio.file.Paths
import java.util.UUID

@Service
class FileStorageService {
    private val uploadDir = Paths.get("uploads")

    init {
        // اگر پوشه وجود نداشت، ساخته می‌شود
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir)
        }
    }

    fun saveFile(file: MultipartFile): String {
        // برای جلوگیری از تداخل اسمی، یک کد یکتا به اسم فایل اضافه می‌کنیم
        val fileName = "${UUID.randomUUID()}_${file.originalFilename}"
        val targetLocation = uploadDir.resolve(fileName)
        
        // ذخیره فایل در سیستم
        Files.copy(file.inputStream, targetLocation)
        
        // برگرداندن مسیر فایل تا در دیتابیس به عنوان url تصویر ذخیره شود
        return "/uploads/$fileName" 
    }
}