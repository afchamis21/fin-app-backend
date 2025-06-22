package io.github.afchamis21.finapp.auth.service

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTVerificationException
import com.auth0.jwt.exceptions.SignatureVerificationException
import com.auth0.jwt.exceptions.TokenExpiredException
import com.auth0.jwt.interfaces.DecodedJWT
import io.github.afchamis21.finapp.auth.dto.LoginDTO
import io.github.afchamis21.finapp.config.AuthConfigProperties
import io.github.afchamis21.finapp.config.logger
import io.github.afchamis21.finapp.user.dto.toDTO
import io.github.afchamis21.finapp.user.model.User
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.*


@Service
class JwtService(
    private val authProperties: AuthConfigProperties,
    private val refreshTokenService: RefreshTokenService,
) {
    private val log = logger()

    @Value("\${spring.application.name}")
    private lateinit var appName: String

    private val accessTokenAlgorithm: Algorithm by lazy {
        log.debug("Initializing access token algorithm.")
        Algorithm.HMAC256(authProperties.user.accessToken.encryptionKey)
    }

    private val refreshTokenAlgorithm: Algorithm by lazy {
        log.debug("Initializing refresh token algorithm.")
        Algorithm.HMAC256(authProperties.user.refreshToken.encryptionKey)
    }

    private val userIdClaimKey = "userId"

    enum class TokenType { ACCESS, REFRESH }

    fun generateTokens(user: User, isRefresh: Boolean): LoginDTO {
        log.info("Generating tokens for user ${user.id}. Is refresh flow: $isRefresh")
        val start = Instant.now()
        val accessTokenExpiration = start.plus(
            authProperties.user.accessToken.duration,
            authProperties.user.accessToken.unit,
        )

        val accessToken = generateToken(user.email, user.id, start, accessTokenExpiration, accessTokenAlgorithm)
        log.info("Generated new access token for user ${user.id} expiring at $accessTokenExpiration.")

        var refreshToken: String? = null
        if (!isRefresh) {
            log.info("Generating new refresh token for user ${user.id} as part of initial login.")
            val refreshTokenExpiration = start.plus(
                authProperties.user.refreshToken.duration,
                authProperties.user.refreshToken.unit,
            )

            refreshToken = generateToken(user.email, user.id, start, refreshTokenExpiration, refreshTokenAlgorithm)
            log.info("New refresh token expires at $refreshTokenExpiration.")

            refreshTokenService.persist(refreshToken, user, refreshTokenExpiration)
        }

        log.info("Token generation process completed for user ${user.id}.")
        return LoginDTO(
            accessToken = accessToken,
            refreshToken = refreshToken,
            expiresAt = accessTokenExpiration,
            user = user.toDTO()
        )
    }

    private fun generateToken(
        subject: String,
        userId: Long?,
        start: Instant,
        expiration: Instant,
        algorithm: Algorithm
    ): String {
        log.debug("Building JWT for subject '$subject', userId: $userId, issuer: $appName.")
        return JWT.create()
            .withSubject(subject)
            .withIssuer(appName)
            .withIssuedAt(Date.from(start))
            .withExpiresAt(Date.from(expiration))
            .withClaim(userIdClaimKey, userId)
            .sign(algorithm)
    }

    fun validateToken(token: String, type: TokenType): DecodedJWT? {
        log.info("Attempting to validate a $type token...")
        val algorithm = when (type) {
            TokenType.ACCESS -> accessTokenAlgorithm
            TokenType.REFRESH -> refreshTokenAlgorithm
        }

        return try {
            val verifier = JWT.require(algorithm)
                .withIssuer(appName)
                .build()

            val decodedJWT = verifier.verify(token)
            log.info(
                "Token validation successful for subject '${decodedJWT.subject}', userId: ${
                    decodedJWT.getClaim(
                        userIdClaimKey
                    ).asLong()
                }."
            )
            decodedJWT
        } catch (e: TokenExpiredException) {
            log.warn("Token validation failed: Token has expired. ${e.message}")
            null
        } catch (e: SignatureVerificationException) {
            log.warn("Token validation failed: Signature is invalid. ${e.message}")
            null
        } catch (e: JWTVerificationException) {
            log.warn("Token validation failed: JWT verification failed. ${e.message}")
            null
        } catch (e: Exception) {
            log.error("An unexpected error occurred during token validation.", e)
            null
        }
    }

    fun getUserId(claims: DecodedJWT): Long? {
        val userId = claims.getClaim(userIdClaimKey)?.asLong()
        log.debug("Extracting userId from claims. Found: $userId")
        return userId
    }
}