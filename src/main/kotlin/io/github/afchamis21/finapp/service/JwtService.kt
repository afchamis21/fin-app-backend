package io.github.afchamis21.finapp.service

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.DecodedJWT
import io.github.afchamis21.finapp.config.AuthConfigProperties
import io.github.afchamis21.finapp.domain.user.User
import io.github.afchamis21.finapp.http.dto.LoginDTO
import io.github.afchamis21.finapp.http.dto.toDTO
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.*

@Service
class JwtService(
    private val authProperties: AuthConfigProperties,
    private val refreshTokenService: RefreshTokenService
) {
    @Value("\${spring.application.name}")
    lateinit var appName: String

    private val accessTokenAlgorithm: Algorithm = Algorithm.HMAC256(authProperties.user.accessToken.encryptionKey)
    private val refreshTokenAlgorithm: Algorithm = Algorithm.HMAC256(authProperties.user.refreshToken.encryptionKey)
    private val userIdClaimKey = "userId"

    fun generateTokens(user: User, refresh: Boolean): LoginDTO {
        val start = Instant.now()
        val accessTokenExpiration = start.plus(
            authProperties.user.accessToken.duration,
            authProperties.user.accessToken.unit,
        )

        val accessToken = generateToken(user.email, user.id, start, accessTokenExpiration, accessTokenAlgorithm)

        var refreshToken: String? = null
        if (!refresh) {
            val refreshTokenExpiration = start.plus(
                authProperties.user.refreshToken.duration,
                authProperties.user.refreshToken.unit,
            )

            refreshToken = generateToken(user.email, user.id, start, refreshTokenExpiration, refreshTokenAlgorithm)

            refreshTokenService.persist(refreshToken, user, refreshTokenExpiration)
        }

        return LoginDTO(
            accessToken = accessToken,
            refreshToken = refreshToken,
            expiresAt = accessTokenExpiration,
            user = user.toDTO()
        )
    }

    fun generateToken(
        subject: String,
        userId: Long?,
        start: Instant,
        expiration: Instant,
        algorithm: Algorithm
    ): String {
        return JWT.create()
            .withSubject(subject)
            .withIssuer(appName)
            .withIssuedAt(Date.from(start))
            .withExpiresAt(Date.from(expiration))
            .withClaim(userIdClaimKey, userId)
            .sign(algorithm)
    }

    fun validateToken(token: String, type: TokenType): DecodedJWT? {
        val algorithm = when (type) {
            TokenType.ACCESS -> accessTokenAlgorithm
            TokenType.REFRESH -> refreshTokenAlgorithm
        }

        return try {
            val verifier = JWT.require(algorithm)
                .withIssuer(appName)
                .build()
            verifier.verify(token)
        } catch (e: Exception) {
            null
        }
    }

    fun getUserId(claims: DecodedJWT): Long? {
        return claims.getClaim(userIdClaimKey)?.asLong()
    }

    enum class TokenType {
        ACCESS, REFRESH
    }
}
