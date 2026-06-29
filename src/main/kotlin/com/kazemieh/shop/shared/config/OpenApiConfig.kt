package com.kazemieh.shop.shared.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {

    private val bearerScheme = "bearerAuth"

    @Bean
    fun shopOpenAPI(): OpenAPI = OpenAPI()
        .info(
            Info()
                .title("Carmilla Shop API")
                .description("REST API for the Carmilla e-commerce backend.")
                .version("v1")
        )
        .addSecurityItem(SecurityRequirement().addList(bearerScheme))
        .components(
            Components().addSecuritySchemes(
                bearerScheme,
                SecurityScheme()
                    .name(bearerScheme)
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")
            )
        )
}
