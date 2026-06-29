package com.kazemieh.shop.shared

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class SmsService(
    private val restTemplate: RestTemplate
) {

    private val log = LoggerFactory.getLogger(SmsService::class.java)

    // Provide these via environment variables (e.g. an Iranian gateway: Kavenegar / SMS.ir / Qasedak).
    @Value("\${sms.provider.url:}")
    private lateinit var apiUrl: String

    @Value("\${sms.provider.api-key:}")
    private lateinit var apiKey: String

    @Value("\${sms.provider.sender:}")
    private lateinit var senderNumber: String

    private val isConfigured: Boolean
        get() = apiUrl.isNotBlank() && apiKey.isNotBlank()

    fun sendSms(mobile: String, message: String) {
        // Dev fallback: when no provider is configured, log instead of failing the auth flow.
        if (!isConfigured) {
            log.warn("SMS provider not configured; skipping real send to {}. Message: {}", mobile, message)
            return
        }
        try {
            val headers = HttpHeaders()
            headers.contentType = MediaType.APPLICATION_JSON
            headers.set("Authorization", "Bearer $apiKey")

            val requestBody = mapOf(
                "sender" to senderNumber,
                "receptor" to mobile,
                "message" to message
            )

            val requestEntity = HttpEntity(requestBody, headers)
            restTemplate.postForEntity(apiUrl, requestEntity, String::class.java)
            log.info("SMS sent to {}", mobile)
        } catch (e: Exception) {
            // Do not leak OTP/message contents on failure.
            log.error("Failed to send SMS to {}: {}", mobile, e.message)
        }
    }
}
