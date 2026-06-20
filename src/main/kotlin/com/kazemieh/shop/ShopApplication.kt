package com.kazemieh.shop

import com.kazemieh.shop.shared.security.jwt.JwtProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.data.web.config.EnableSpringDataWebSupport
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.web.client.RestTemplate
import java.net.HttpURLConnection
import javax.net.ssl.*

@SpringBootApplication
@EnableScheduling
@EnableAsync
@EnableConfigurationProperties(JwtProperties::class)
@EnableSpringDataWebSupport(pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO)
class ShopApplication {
    @Bean
    fun restTemplate(): RestTemplate {
        val factory = object : SimpleClientHttpRequestFactory() {
            override fun prepareConnection(connection: HttpURLConnection, httpMethod: String) {
                if (connection is HttpsURLConnection) {
                    connection.sslSocketFactory = createUnsafeSslContext().socketFactory
                    connection.hostnameVerifier = HostnameVerifier { _, _ -> true }
                }
                super.prepareConnection(connection, httpMethod)
            }
        }
        return RestTemplate(factory)
    }

    private fun createUnsafeSslContext(): SSLContext {
        val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<java.security.cert.X509Certificate>, authType: String) {}
            override fun checkServerTrusted(chain: Array<java.security.cert.X509Certificate>, authType: String) {}
            override fun getAcceptedIssuers(): Array<java.security.cert.X509Certificate> = arrayOf()
        })
        val sslContext = SSLContext.getInstance("SSL")
        sslContext.init(null, trustAllCerts, java.security.SecureRandom())
        return sslContext
    }
}

fun main(args: Array<String>) {
    runApplication<ShopApplication>(*args)
}
