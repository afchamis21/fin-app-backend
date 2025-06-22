package io.github.afchamis21.finapp.auth.service

import io.github.afchamis21.finapp.auth.model.RefreshToken
import io.github.afchamis21.finapp.auth.repo.RefreshTokenJpaRepository
import io.github.afchamis21.finapp.config.logger
import io.github.afchamis21.finapp.user.model.User
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class RefreshTokenService(private val refreshTokenJpaRepository: RefreshTokenJpaRepository) {
    private val log = logger()

    /**
     * Finds and deletes all refresh tokens that have passed their expiration date.
     * Intended to be called by a scheduled job.
     */
    fun deleteExpiredRefreshTokens() {
        log.info("Running job to delete expired refresh tokens...")

        val expiredTokens = refreshTokenJpaRepository.findAllByExpiresAtIsBefore(Instant.now())

        if (expiredTokens.isNotEmpty()) {
            log.info("Found ${expiredTokens.size} expired refresh token(s). Deleting now...")
            refreshTokenJpaRepository.deleteAllInBatch(expiredTokens) // More efficient for bulk deletes
            log.info("Successfully deleted ${expiredTokens.size} expired refresh token(s).")
        } else {
            log.info("No expired refresh tokens found to delete.")
        }
    }

    /**
     * Persists a new refresh token for a given user.
     */
    fun persist(
        refreshToken: String,
        user: User,
        refreshTokenExpiration: Instant
    ) {
        log.info("Persisting new refresh token for user ${user.id}. Token will expire at $refreshTokenExpiration.")

        refreshTokenJpaRepository.save(
            RefreshToken(
                token = refreshToken,
                owner = user,
                expiresAt = refreshTokenExpiration
            )
        )

        log.info("New refresh token for user ${user.id} saved successfully.")
    }

    /**
     * Deletes all refresh tokens associated with a specific user.
     * This is typically used during a full logout process.
     */
    fun deleteByUser(user: User) {
        log.info("Revoking all refresh tokens for user ${user.id}...")
        refreshTokenJpaRepository.deleteAllByOwner(user)
        log.info("All refresh tokens for user ${user.id} have been deleted.")
    }
}