package com.diploma.ione.auth

import com.diploma.ione.domain.Role
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.Date
import javax.crypto.SecretKey

@Service
class JwtService(
    @Value("\${app.jwt.secret}") secret: String,
    @Value("\${app.jwt.accessTokenMinutes}") private val accessTokenMinutes: Long
) {
    private val key: SecretKey = Keys.hmacShaKeyFor(secret.toByteArray())

    fun generateToken(userId: Long, role: Role): String {
        val now = Date()
        val exp = Date(now.time + accessTokenMinutes * 60_0000)

        return Jwts.builder()
            .setSubject(userId.toString())
            .claim("role", role.name)
            .setIssuedAt(now)
            .setExpiration(exp)
            .signWith(key, SignatureAlgorithm.HS256)
            .compact()
    }

    fun parseUserId(token: String): Long =
        Jwts.parserBuilder().setSigningKey(key).build()
            .parseClaimsJws(token).body.subject.toLong()

    fun parseRole(token: String): Role {
        val role = Jwts.parserBuilder().setSigningKey(key).build()
            .parseClaimsJws(token).body["role"].toString()
        return Role.valueOf(role)
    }
}