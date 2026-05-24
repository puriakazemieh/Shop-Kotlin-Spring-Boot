package com.kazemieh.shop.payment.application

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class ZarinPalService(
    @Value("\${zarinpal.merchant-id}") private val merchantId: String,
    private val restTemplate: RestTemplate
) {

    fun createPaymentRequest(amountInToman: Long, orderId: String): String? {
        val url = "https://api.zarinpal.com/pg/v4/payment/request.json"
        
        val callbackUrl = "http://localhost:8080/api/payment/callback?order_id=$orderId"

        val requestBody = mapOf(
            "merchant_id" to merchantId,
            "amount" to amountInToman * 10,
            "callback_url" to callbackUrl,
            "description" to "تراکنش سفارش شماره $orderId"
        )

        return try {
            val response = restTemplate.postForObject(url, requestBody, Map::class.java)
            val data = response?.get("data") as? Map<*, *>
            val authority = data?.get("authority")?.toString()

            if (authority != null && authority.isNotEmpty()) {
                "https://www.zarinpal.com/pg/StartPay/$authority"
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun verifyPayment(authority: String, amountInToman: Long): ZarinPalVerificationResponse {
        val url = "https://api.zarinpal.com/pg/v4/payment/verify.json"
        
        val verifyBody = mapOf(
            "merchant_id" to merchantId,
            "amount" to amountInToman * 10,
            "authority" to authority
        )

        return try {
            val response = restTemplate.postForObject(url, verifyBody, Map::class.java)
            val data = response?.get("data") as? Map<*, *>
            val code = data?.get("code")?.toString()

            if (code == "100" || code == "101") {
                val refId = (data?.get("ref_id") as? Number)?.toString()
                ZarinPalVerificationResponse(isSuccess = true, refId = refId)
            } else {
                ZarinPalVerificationResponse(isSuccess = false)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            ZarinPalVerificationResponse(isSuccess = false)
        }
    }
}