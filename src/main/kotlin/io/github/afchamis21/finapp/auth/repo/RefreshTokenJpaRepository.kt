package io.github.afchamis21.finapp.auth.repo

import io.github.afchamis21.finapp.auth.model.RefreshToken
import io.github.afchamis21.finapp.user.model.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
interface RefreshTokenJpaRepository : JpaRepository<RefreshToken, Long> {
    fun existsByTokenAndOwnerId(token: String, ownerId: Long): Boolean

    fun findAllByExpiresAtIsBefore(expiresAt: Instant): List<RefreshToken>

    @Modifying
    fun deleteAllByOwnerId(ownerId: Long)


    @Modifying
    fun deleteAllByOwner(owner: User)
}