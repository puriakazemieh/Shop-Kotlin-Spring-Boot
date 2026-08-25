package com.kazemieh.shop.identity.api

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class WebSessionCookieFactoryTest {

    private val factory = WebSessionCookieFactory(refreshTtlDays = 30, secure = true)

    @Test
    fun `refresh cookie is host-bound and inaccessible to JavaScript`() {
        val cookie = factory.refreshToken("opaque-token").toString()

        assertThat(cookie).contains("__Host-shop_refresh=opaque-token")
        assertThat(cookie).contains("Path=/")
        assertThat(cookie).contains("Max-Age=2592000")
        assertThat(cookie).contains("Secure")
        assertThat(cookie).contains("HttpOnly")
        assertThat(cookie).contains("SameSite=Strict")
        assertThat(cookie).doesNotContain("Domain=")
    }

    @Test
    fun `logout cookie expires immediately with identical security attributes`() {
        val cookie = factory.clear().toString()

        assertThat(cookie).contains("__Host-shop_refresh=")
        assertThat(cookie).contains("Max-Age=0")
        assertThat(cookie).contains("Secure")
        assertThat(cookie).contains("HttpOnly")
        assertThat(cookie).contains("SameSite=Strict")
    }
}
