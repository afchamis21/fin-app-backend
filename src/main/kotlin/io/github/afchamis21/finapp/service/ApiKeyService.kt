package io.github.afchamis21.finapp.service

import io.github.afchamis21.finapp.repo.jpa.ApiKeyJpaRepository
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class ApiKeyService(private val repository: ApiKeyJpaRepository) {

    fun validateKey(key: String): Boolean {
        return repository.existsByTokenAndExpiresAtIsAfter(key, Instant.now())
    }
}