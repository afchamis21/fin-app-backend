package io.github.afchamis21.finapp.repo

import io.github.afchamis21.finapp.domain.user.User
import org.springframework.data.jpa.repository.JpaRepository

interface UserJpaRepository : JpaRepository<User, Long> {
    fun existsUserByEmail(email: String): Boolean
    fun findByEmail(email: String): User?
}