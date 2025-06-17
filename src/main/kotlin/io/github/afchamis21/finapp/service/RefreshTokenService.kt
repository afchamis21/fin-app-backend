package io.github.afchamis21.finapp.service

import io.github.afchamis21.finapp.domain.auth.RefreshToken
import io.github.afchamis21.finapp.domain.user.User
import io.github.afchamis21.finapp.repo.jpa.RefreshTokenJpaRepository
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class RefreshTokenService(private val refreshTokenJpaRepository: RefreshTokenJpaRepository) {

    fun deleteExpiredRefreshTokens() {
        refreshTokenJpaRepository.deleteAllByExpiresAtIsBefore(Instant.now())
    }

    fun persist(
        refreshToken: String,
        user: User,
        refreshTokenExpiration: Instant
    ) {
        refreshTokenJpaRepository.save(
            RefreshToken(
                token = refreshToken,
                owner = user,
                expiresAt = refreshTokenExpiration
            )
        )
    }

    fun deleteByUser(user: User) {
        refreshTokenJpaRepository.deleteAllByOwner(user)
    }

}