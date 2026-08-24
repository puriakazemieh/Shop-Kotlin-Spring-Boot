package com.kazemieh.shop.payment.application

import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.web.client.RestTemplate

class ZarinPalServiceTest {

    @Test
    fun `does not call the gateway when payment callback URL is missing`() {
        val restTemplate = mockk<RestTemplate>(relaxed = true)
        val service = ZarinPalService(
            merchantId = "test-merchant",
            isSandbox = true,
            accessToken = "",
            paymentCallbackBaseUrl = "",
            restTemplate = restTemplate
        )

        val paymentUrl = service.createPaymentRequest(amountInToman = 10_000, orderId = "42")

        assertThat(paymentUrl).isNull()
        verify(exactly = 0) { restTemplate.postForObject(any<String>(), any(), Map::class.java) }
    }
}
