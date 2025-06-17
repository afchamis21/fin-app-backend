package io.github.afchamis21.finapp.service

import io.github.afchamis21.finapp.context.Context
import io.github.afchamis21.finapp.exceptions.HttpException
import io.github.afchamis21.finapp.http.dto.LoginDTO
import io.github.afchamis21.finapp.http.request.auth.LoginRequest
import io.github.afchamis21.finapp.http.request.auth.RefreshRequest
import io.github.afchamis21.finapp.repo.jpa.RefreshTokenJpaRepository
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
    fun login(req: LoginRequest): LoginDTO {
        val user = userService.findUserByEmail(req.email)
            ?: throw HttpException(HttpStatus.UNAUTHORIZED, "Credenciais inválidas")

        if (!passwordEncoder.matches(req.password, user.password)) {
            throw HttpException(HttpStatus.UNAUTHORIZED, "Credenciais inválidas")
        }

        return jwtService.generateTokens(user = user, refresh = false)
    }

    fun logout() {
        val userId = Context.userId ?: throw HttpException(HttpStatus.FORBIDDEN)
        repository.deleteAllByOwnerId(userId)
    }

    fun refresh(req: RefreshRequest): LoginDTO {
        val claims = jwtService.validateToken(req.refreshToken, JwtService.TokenType.REFRESH)
            ?: throw HttpException(HttpStatus.UNAUTHORIZED)

        val userId = jwtService.getUserId(claims) ?: throw HttpException(HttpStatus.UNAUTHORIZED)
        if (!repository.existsByTokenAndOwnerId(req.refreshToken, userId)) {
            throw HttpException(HttpStatus.UNAUTHORIZED)
        }

        val email = claims.subject
        val user = userService.findUserByEmail(email) ?: throw HttpException(HttpStatus.UNAUTHORIZED)

        return jwtService.generateTokens(user = user, refresh = true)
    }
}