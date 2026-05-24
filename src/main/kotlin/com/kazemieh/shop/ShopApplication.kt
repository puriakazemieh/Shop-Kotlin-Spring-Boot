package com.kazemieh.shop

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling // فعال‌سازی قابلیت زمان‌بندی
class ShopApplication

fun main(args: Array<String>) {
    runApplication<ShopApplication>(*args)
}
