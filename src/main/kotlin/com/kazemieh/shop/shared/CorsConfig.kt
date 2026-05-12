package com.kazemieh.shop.shared
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import org.springframework.web.filter.CorsFilter

@Configuration
class CorsConfig {

    @Bean
    fun corsFilter(): CorsFilter {
        val config = CorsConfiguration()

        config.allowCredentials = true

        config.allowedOrigins = listOf(
            "http://localhost:8081"
        )

        config.allowedHeaders = listOf("*")

        config.allowedMethods = listOf(
            "GET",
            "POST",
            "PUT",
            "DELETE",
            "OPTIONS"
        )

        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", config)

        return CorsFilter(source)
    }
}