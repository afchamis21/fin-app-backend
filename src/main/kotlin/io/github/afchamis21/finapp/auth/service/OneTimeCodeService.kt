package io.github.afchamis21.finapp.auth.service

import io.github.afchamis21.finapp.auth.dto.LoginDTO
import io.github.afchamis21.finapp.auth.model.OneTimeCode
import io.github.afchamis21.finapp.auth.repo.OneTimeCodeJpaRepository
import io.github.afchamis21.finapp.config.logger
import io.github.afchamis21.finapp.user.dto.toDTO
import io.github.afchamis21.finapp.user.service.UserService
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.*


@Service
class OneTimeCodeService(
    private val userService: UserService,
    private val repository: OneTimeCodeJpaRepository,

    ) {
    private val log = logger()

    fun generateOneTimeCode(): LoginDTO {
        val user = userService.findCurrentUser()
        log.info("Generating one-time code for user ${user.id}...")

        var token = UUID.randomUUID().toString()
        while (repository.existsByCode(token)) {
            log.warn("UUID collision detected for one-time code. Generating a new one.")
            token = UUID.randomUUID().toString()
        }

        val expiresAt = Instant.now().plusSeconds(30)
        val code = repository.save(OneTimeCode(null, token, expiresAt, user))

        log.info("Successfully generated and saved one-time code for user ${user.id}. Code expires at $expiresAt.")

        return LoginDTO(code.code, null, expiresAt, user.toDTO())
    }

    fun findByCode(code: String): OneTimeCode? {
        log.debug("Attempting to find one-time code in repository...")
        val result = repository.findByCode(code)
        if (result == null) {
            log.debug("One-time code not found.")
        } else {
            log.debug("Found one-time code for user ${result.owner.id}.")
        }
        return result
    }

    /**
     * Validates if a one-time code has NOT expired.
     * @return true if the code is still valid, false if it has expired.
     */
    fun validateCode(code: OneTimeCode): Boolean {
        log.info("Validating one-time code ${code.id} for user ${code.owner.id}.")
        val isValid = Instant.now().isBefore(code.expiresAt)

        if (isValid) {
            log.info("Code ${code.id} is valid. Expiration: ${code.expiresAt}.")
        } else {
            log.warn("Code ${code.id} validation failed. The code has expired. Expiration was: ${code.expiresAt}.")
        }

        return isValid
    }

    fun delete(code: OneTimeCode) {
        log.info("Deleting code for user ${code.owner.id}...")
        repository.delete(code)
    }
}