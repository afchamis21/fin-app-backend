package io.github.afchamis21.finapp.user.repo

import io.github.afchamis21.finapp.config.logger
import io.github.afchamis21.finapp.user.model.User
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository

@Repository
class UserRepository(private val userCache: UserCache, private val userJpaRepository: UserJpaRepository) {
    private val log = logger()

    /**
     * Saves a user to the primary data source (JPA) and then updates the cache.
     */
    fun save(user: User): User {
        log.info("Saving user {} (email: {}) to the database.", user.id, user.email)
        val savedUser = userJpaRepository.save(user)

        log.info("Updating cache for user {}.", savedUser.id)
        userCache.save(savedUser)

        log.info("User {} saved successfully.", savedUser.id)
        return savedUser
    }

    /**
     * Checks for user existence directly in the database.
     */
    fun existsUserByEmail(email: String): Boolean {
        log.debug("Checking database for existence of user with email: {}", email)
        val exists = userJpaRepository.existsUserByEmail(email)
        log.debug("User with email {} exists: {}", email, exists)
        return exists
    }

    /**
     * Finds a user by ID, implementing a cache-aside strategy.
     * It first checks the cache, and only queries the database on a cache miss.
     */
    fun findById(userId: Long): User? {
        log.debug("Attempting to find user by ID: {}", userId)

        val cachedUser = userCache.fetch(userId)
        if (cachedUser != null) {
            log.info("User {} found in cache.", userId)
            return cachedUser
        }

        log.info("User {} not found in cache. Querying database.", userId)
        val dbUser = userJpaRepository.findByIdOrNull(userId)

        if (dbUser != null) {
            log.info("User {} found in database. Populating cache now.", userId)
            userCache.save(dbUser)
        } else {
            log.warn("User {} not found in the database.", userId)
        }

        return dbUser
    }

    /**
     * Finds a user by email directly from the database.
     */
    fun findByEmail(email: String): User? {
        log.debug("Finding user by email '{}' directly from database.", email)
        return userJpaRepository.findByEmail(email)
    }
}