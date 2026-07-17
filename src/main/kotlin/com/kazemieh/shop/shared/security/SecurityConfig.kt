package com.kazemieh.shop.shared.security

import com.kazemieh.shop.shared.security.jwt.JwtAuthFilter
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
@EnableMethodSecurity
class SecurityConfig(
    private val jwtAuthFilter: JwtAuthFilter,
    private val entryPoint: RestAuthEntryPoint,
    private val deniedHandler: RestAccessDeniedHandler
) {

    // آدرس‌های مجازِ CORS به‌صورتِ پراپرتی (کامادار). خالی = فهرستِ ثابتِ زیر.
    // ست‌کردنِ `app.cors-origins=*` (یا الگوها) با allowCredentials هم کار می‌کند
    // چون از allowedOriginPatterns استفاده می‌شود — برای تست با URLهای موقتِ تونل.
    @Value("\${app.cors-origins:}")
    private lateinit var corsOrigins: String

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun authenticationManager(cfg: AuthenticationConfiguration): AuthenticationManager =
        cfg.authenticationManager

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val source = UrlBasedCorsConfigurationSource()
        val config = CorsConfiguration()

        config.allowCredentials = true

        val configured = corsOrigins.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        if (configured.isNotEmpty()) {
            // الگوهای پویا (سازگار با allowCredentials) — برای دامنه‌های موقتِ تست.
            config.allowedOriginPatterns = configured
        } else {
            // دامنه‌های مجاز پیش‌فرض شما
            config.allowedOrigins = listOf(
                "http://miaad.puriademo.ir",
                "https://miaad.puriademo.ir",
                "http://milad.puriademo.ir",
                "https://milad.puriademo.ir",
                "http://localhost:8081"
            )
        }

        config.allowedHeaders = listOf("*")
        config.allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
        config.exposedHeaders = listOf("Authorization")

        source.registerCorsConfiguration("/**", config)
        return source
    }

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .cors { it.configurationSource(corsConfigurationSource()) }

            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .headers { it.frameOptions { frame -> frame.disable() } }
            .exceptionHandling {
                it.authenticationEntryPoint(entryPoint)
                it.accessDeniedHandler(deniedHandler)
            }
            .authorizeHttpRequests {
                it.requestMatchers(
                    "/api/products/**",
                    "/api/categories/**",
                    "/api/campaigns/**",
                    "/api/banners",
                    "/api/courses/**",
                    "/api/sizes/**",
                    "/api/colors/**",
                    "/api/stories/**",
                    "/api/blogs/**",
                    "/api/therapists/**",
                    "/api/auth/register",
                    "/api/auth/login",
                    "/api/auth/refresh",
                    "/api/auth/logout",
                    "/api/auth/forgot-password",
                    "/api/auth/reset-password",
                    "/api/swagger-ui.html",
                    "/api/swagger-ui/**",
                    "/api/v3/api-docs/**",
                    "/api/auth/*",
                    "/uploads/**"
                ).permitAll()

                it.anyRequest().authenticated()
            }
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter::class.java)

        return http.build()
    }
}
