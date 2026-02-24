package com.kazemieh.shop.shared.security.jwt

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Service
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.util.*

@Service
class JwtService(private val props: JwtProperties) {

    private val key = Keys.hmacShaKeyFor(props.secretKey.toByteArray(StandardCharsets.UTF_8))

    fun generateAccessToken(userId: Long, email: String, role: String): String {
        val now = Instant.now()
        val exp = now.plusSeconds(props.accessTtlMinutes * 60)

        return Jwts.builder()
            .issuer(props.issuer)
            .subject(email)
            .claim("uid", userId)
            .claim("role", role)
            .claim("typ", "access")
            .issuedAt(Date.from(now))
            .expiration(Date.from(exp))
            .signWith(key)
            .compact()
    }

    fun parseClaims(token: String): Claims =
        Jwts.parser().verifyWith(key).build().parseSignedClaims(token).payload
}