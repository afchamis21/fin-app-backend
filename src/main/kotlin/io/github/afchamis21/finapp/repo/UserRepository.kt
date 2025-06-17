package io.github.afchamis21.finapp.repo

import io.github.afchamis21.finapp.domain.user.User
import io.github.afchamis21.finapp.repo.cache.UserCache
import io.github.afchamis21.finapp.repo.jpa.UserJpaRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository

@Repository
class UserRepository(private val userCache: UserCache, private val userJpaRepository: UserJpaRepository) {
    fun save(user: User): User {
        userJpaRepository.save(user)
        userCache.save(user)

        return user
    }

    fun existsUserByEmail(email: String): Boolean {
        return userJpaRepository.existsUserByEmail(email)
    }

    fun findById(userId: Long): User? {
        return userCache.fetch(userId) ?: userJpaRepository.findByIdOrNull(userId)?.also {
            userCache.save(it)
        }
    }

    fun findByEmail(email: String): User? {
        return userJpaRepository.findByEmail(email)
    }
}