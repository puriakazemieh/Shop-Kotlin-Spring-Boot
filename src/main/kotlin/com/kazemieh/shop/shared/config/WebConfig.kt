package com.kazemieh.shop.shared.config

import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebConfig : WebMvcConfigurer {
    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        // هر درخواستی که با uploads/ شروع شود رو به پوشه لوکال هدایت می‌کند
        registry.addResourceHandler("/uploads/**")
            .addResourceLocations("file:uploads/")
    }
}