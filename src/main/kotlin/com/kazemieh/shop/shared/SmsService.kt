package com.kazemieh.shop.shared

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

    // These values should ideally come from your application.yml or environment variables
    @Value("\${sms.provider.url:https://api.example-sms-provider.com/v1/send}")
    private lateinit var apiUrl: String

    @Value("\${sms.provider.api-key:your_api_key_here}")
    private lateinit var apiKey: String

    @Value("\${sms.provider.sender:your_sender_number}")
    private lateinit var senderNumber: String

    fun sendSms(mobile: String, message: String) {
        // Example implementation using a hypothetical REST API
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

            // Send the request
            // val response = restTemplate.postForEntity(apiUrl, requestEntity, String::class.java)
            // println("SMS sent successfully. Response: \${response.body}")
            
            // For now we just log it since we don't have a real API
            println("Mock sending SMS via HTTP to $mobile: $message")

        } catch (e: Exception) {
            println("Failed to send SMS to $mobile: \${e.message}")
            // Consider logging the error properly or throwing a custom exception
        }
    }
}
