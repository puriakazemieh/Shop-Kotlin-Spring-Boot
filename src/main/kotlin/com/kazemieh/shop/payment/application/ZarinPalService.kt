package com.kazemieh.shop.payment.application

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI

@Service
class ZarinPalService(
    @Value("\${zarinpal.merchant-id}") private val merchantId: String,
    @Value("\${zarinpal.sandbox:false}") private val isSandbox: Boolean,
    @Value("\${zarinpal.access-token:}") private val accessToken: String,
    @Value("\${app.payment.callback-base-url:}") private val paymentCallbackBaseUrl: String,
    private val restTemplate: RestTemplate
) {
    private val logger = LoggerFactory.getLogger(ZarinPalService::class.java)

    private val baseUrl =
        if (isSandbox) "https://sandbox.zarinpal.com/pg/v4/payment" else "https://api.zarinpal.com/pg/v4/payment"
    private val startPayUrl =
        if (isSandbox) "https://sandbox.zarinpal.com/pg/StartPay/" else "https://www.zarinpal.com/pg/StartPay/"


    private fun createHeaders(includeAuth: Boolean = false): HttpHeaders {
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        headers.accept = listOf(MediaType.APPLICATION_JSON)
        // توکن فقط زمانی که صریحاً خواسته شده و مقدار معتبری دارد اضافه می‌شود
        if (includeAuth && accessToken.isNotBlank() && accessToken != "YOUR_ZARINPAL_ACCESS_TOKEN") {
            headers.setBearerAuth(accessToken)
        }
        return headers
    }

    fun createPaymentRequest(amountInToman: Long, orderId: String): String? {
        val url = "$baseUrl/request.json"
        val callbackUrl = buildCallbackUrl(orderId) ?: return null

        val requestBody = mapOf(
            "merchant_id" to merchantId,
            "amount" to amountInToman * 10,
            "callback_url" to callbackUrl,
            "description" to "تراکنش سفارش شماره $orderId"
        )

        val entity = HttpEntity(requestBody, createHeaders(includeAuth = false))

        return try {
            val response = restTemplate.postForObject(url, entity, Map::class.java)
            val data = response?.get("data") as? Map<*, *>
            val authority = data?.get("authority")?.toString()

            if (authority != null && authority.isNotEmpty()) {
                startPayUrl + authority
            } else {
                logger.error("Failed to get authority from Zarinpal. Response: $response")
                null
            }
        } catch (e: Exception) {
            logger.error(
                "Error creating Zarinpal payment request. Merchant ID used: $merchantId, IsSandbox: $isSandbox",
                e
            )
            null
        }
    }

    private fun buildCallbackUrl(orderId: String): String? {
        val callbackBaseUri = try {
            URI(paymentCallbackBaseUrl.trim())
        } catch (_: IllegalArgumentException) {
            null
        }

        if (
            callbackBaseUri == null ||
            callbackBaseUri.scheme !in setOf("http", "https") ||
            callbackBaseUri.host.isNullOrBlank() ||
            callbackBaseUri.userInfo != null ||
            callbackBaseUri.query != null ||
            callbackBaseUri.fragment != null ||
            (!isSandbox && callbackBaseUri.scheme != "https")
        ) {
            logger.error("Payment callback base URL is missing or invalid.")
            return null
        }

        return UriComponentsBuilder.fromUri(callbackBaseUri)
            .path("/api/payment/callback")
            .queryParam("order_id", orderId)
            .build()
            .toUriString()
    }

    fun verifyPayment(authority: String, amountInToman: Long): ZarinPalVerificationResponse {
        val url = "$baseUrl/verify.json"
        val verifyBody = mapOf("merchant_id" to merchantId, "amount" to amountInToman * 10, "authority" to authority)

        val entity = HttpEntity(verifyBody, createHeaders(includeAuth = false))

        return try {
            val response = restTemplate.postForObject(url, entity, Map::class.java)
            val data = response?.get("data") as? Map<*, *>
            val code = data?.get("code")?.toString()

            if (code == "100" || code == "101") {
                val refId = (data?.get("ref_id") as? Number)?.toString()
                ZarinPalVerificationResponse(isSuccess = true, refId = refId)
            } else {
                logger.warn("Zarinpal verification failed for authority $authority. Code: $code, Response: $response")
                ZarinPalVerificationResponse(isSuccess = false)
            }
        } catch (e: Exception) {
            logger.error("Error verifying Zarinpal payment for authority $authority", e)
            ZarinPalVerificationResponse(isSuccess = false)
        }
    }

    fun getUnverifiedTransactions(): List<String> {
        val url = "$baseUrl/unVerified.json"
        val requestBody = mapOf("merchant_id" to merchantId)

        val entity = HttpEntity(requestBody, createHeaders(includeAuth = true))

        return try {
            val response = restTemplate.postForObject(url, entity, Map::class.java)
            val data = response?.get("data") as? Map<*, *>
            val code = data?.get("code")?.toString()

            if (code == "100") {
                val authoritiesList = data?.get("authorities") as? List<Map<*, *>>
                authoritiesList?.mapNotNull { it["authority"]?.toString() } ?: emptyList()
            } else {
                logger.warn("Zarinpal getUnverifiedTransactions failed. Code: $code, Response: $response")
                emptyList()
            }
        } catch (e: Exception) {
            logger.error("Error in Zarinpal getUnverifiedTransactions", e)
            emptyList()
        }
    }
}
