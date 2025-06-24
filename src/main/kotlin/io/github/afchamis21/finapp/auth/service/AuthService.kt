package io.github.afchamis21.finapp.auth.service

import io.github.afchamis21.finapp.auth.dto.LoginDTO
import io.github.afchamis21.finapp.auth.repo.RefreshTokenJpaRepository
import io.github.afchamis21.finapp.auth.request.LoginRequest
import io.github.afchamis21.finapp.auth.request.RefreshRequest
import io.github.afchamis21.finapp.config.logger
import io.github.afchamis21.finapp.exceptions.HttpException
import io.github.afchamis21.finapp.http.Context
import io.github.afchamis21.finapp.user.service.UserService
import jakarta.transaction.Transactional
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val userService: UserService,
    private val passwordEncoder: BCryptPasswordEncoder,
    private val jwtService: JwtService,
    private val repository: RefreshTokenJpaRepository
) {
    private val log = logger()

    fun login(req: LoginRequest): LoginDTO {
        log.info("Login attempt for email: ${req.email}")

        val user = userService.findUserByEmail(req.email)
            ?: run {
                log.warn("Login failed for email ${req.email}: User not found.")
                throw HttpException(HttpStatus.UNAUTHORIZED, "Credenciais inválidas")
            }

        if (!passwordEncoder.matches(req.password, user.password)) {
            log.warn("Login failed for user ${user.id} (${req.email}): Password mismatch.")
            throw HttpException(HttpStatus.UNAUTHORIZED, "Credenciais inválidas")
        }

        log.info("User ${user.id} (${req.email}) authenticated successfully. Generating tokens...")
        val tokens = jwtService.generateTokens(user = user, isRefresh = false)
        log.info("Tokens generated for user ${user.id}.")
        return tokens
    }

    @Transactional
    fun logout() {
        val userId = Context.userId ?: run {
            throw HttpException(HttpStatus.FORBIDDEN)
        }

        log.info("Processing logout for user $userId. All refresh tokens will be revoked.")
        repository.deleteAllByOwnerId(userId)
        log.info("All refresh tokens for user $userId have been revoked.")
    }

    fun refresh(req: RefreshRequest): LoginDTO {
        log.info("Token refresh attempt initiated.")

        val claims = jwtService.validateToken(req.refreshToken, JwtService.TokenType.REFRESH)
            ?: run {
                log.warn("Token refresh failed: Provided refresh token is invalid or expired.")
                throw HttpException(HttpStatus.UNAUTHORIZED)
            }

        val userId = jwtService.getUserId(claims) ?: run {
            log.error("Token refresh failed: Valid refresh token is missing the userId claim.")
            throw HttpException(HttpStatus.UNAUTHORIZED)
        }

        log.info("Refresh token is valid for user $userId. Verifying against repository...")
        if (!repository.existsByTokenAndOwnerId(req.refreshToken, userId)) {
            log.warn("SECURITY ALERT: Refresh attempt for user $userId with a valid token that is NOT in the repository. The token might have been already revoked or this could be a replay attack.")
            throw HttpException(HttpStatus.UNAUTHORIZED)
        }

        val email = claims.subject
        val user = userService.findUserByEmail(email) ?: run {
            log.error("Token refresh failed: User '$email' from token claims not found in database for userId $userId.")
            throw HttpException(HttpStatus.UNAUTHORIZED)
        }

        log.info("User ${user.id} re-authenticated via refresh token. Generating new tokens...")
        val tokens = jwtService.generateTokens(user = user, isRefresh = true)
        log.info("New tokens generated successfully for user ${user.id}.")
        return tokens
    }
}