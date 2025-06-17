package io.github.afchamis21.finapp.repo.jpa

import io.github.afchamis21.finapp.domain.auth.ApiKey
import org.springframework.data.jpa.repository.JpaRepository
import java.time.Instant

interface ApiKeyJpaRepository : JpaRepository<ApiKey, Long> {
    fun existsByTokenAndExpiresAtIsAfter(token: String, expiresAt: Instant): Boolean
}