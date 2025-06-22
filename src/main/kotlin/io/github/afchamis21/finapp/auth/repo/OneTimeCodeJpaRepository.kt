package io.github.afchamis21.finapp.auth.repo

import io.github.afchamis21.finapp.auth.model.OneTimeCode
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface OneTimeCodeJpaRepository : JpaRepository<OneTimeCode, Long> {
    fun existsByCode(code: String): Boolean

    fun findByCode(code: String): OneTimeCode?
}
