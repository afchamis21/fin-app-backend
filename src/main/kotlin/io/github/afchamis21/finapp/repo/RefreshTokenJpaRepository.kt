package io.github.afchamis21.finapp.repo

import io.github.afchamis21.finapp.domain.auth.RefreshToken
import io.github.afchamis21.finapp.domain.user.User
import jakarta.transaction.Transactional
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
interface RefreshTokenJpaRepository : JpaRepository<RefreshToken, Long> {
    fun existsByTokenAndOwnerId(token: String, ownerId: Long): Boolean

    @Modifying
    @Transactional
    fun deleteAllByExpiresAtIsBefore(expiresAt: Instant)

    @Modifying
    @Transactional
    fun deleteAllByOwnerId(ownerId: Long)


    @Modifying
    @Transactional
    fun deleteAllByOwner(owner: User)
}