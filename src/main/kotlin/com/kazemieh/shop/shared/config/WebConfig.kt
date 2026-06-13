package com.kazemieh.shop.shared.config

import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import java.nio.file.Paths

@Configuration
class WebConfig : WebMvcConfigurer {
    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        val uploadPath = Paths.get("uploads").toAbsolutePath().toUri().toString()
        // هر درخواستی که با uploads/ شروع شود رو به پوشه لوکال هدایت می‌کند
        registry.addResourceHandler("/uploads/**")
            .addResourceLocations(uploadPath)
    }

}
