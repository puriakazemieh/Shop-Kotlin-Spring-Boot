package com.kazemieh.shop.shared.config

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class JacksonConfig {

    @Bean
    fun objectMapper(): ObjectMapper =
        jacksonObjectMapper()
            .findAndRegisterModules()
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            // نسبت به فیلدهای ناشناخته/اضافیِ کلاینت سخت‌گیر نباش تا تفاوتِ نسخه‌ها
            // به‌جای خطای ۴۰۰ («Malformed JSON»)، بی‌صدا نادیده گرفته شود.
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
}