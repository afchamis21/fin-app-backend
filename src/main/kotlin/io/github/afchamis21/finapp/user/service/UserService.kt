package io.github.afchamis21.finapp.user.service

import io.github.afchamis21.finapp.auth.service.RefreshTokenService
import io.github.afchamis21.finapp.config.logger
import io.github.afchamis21.finapp.exceptions.HttpException
import io.github.afchamis21.finapp.http.Context
import io.github.afchamis21.finapp.user.dto.UserDTO
import io.github.afchamis21.finapp.user.dto.toDTO
import io.github.afchamis21.finapp.user.model.User
import io.github.afchamis21.finapp.user.repo.UserRepository
import io.github.afchamis21.finapp.user.request.RegisterUserRequest
import io.github.afchamis21.finapp.user.request.UpdateUserRequest
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service

@Service
class UserService(
    private val repository: UserRepository,
    private val passwordEncoder: BCryptPasswordEncoder,
    private val refreshTokenService: RefreshTokenService
) {
    private val log = logger()

    fun register(req: RegisterUserRequest): UserDTO {
        log.info("Attempting to register new user with email: {} and username: {}", req.email, req.username)

        if (req.password != req.confirmPassword) {
            log.warn("User registration failed for email {}: Passwords do not match.", req.email)
            throw HttpException(HttpStatus.BAD_REQUEST, "As senhas não são iguais")
        }

        if (repository.existsUserByEmail(req.email)) {
            log.warn("User registration failed: Email {} is already in use.", req.email)
            throw HttpException(HttpStatus.BAD_REQUEST, "Usuário já cadastrado com esse email")
        }

        val user = User(
            email = req.email,
            username = req.username,
            password = hash(req.password)
        )

        val savedUser = repository.save(user)
        log.info("User {} with email {} registered successfully.", savedUser.id, savedUser.email)

        return savedUser.toDTO()
    }

    fun findCurrentUser(): User {
        val userId = Context.userId
        log.debug("Attempting to find current user with ID from context: {}", userId)

        if (userId == null) {
            log.warn("Failed to find current user: No userId found in context.")
            throw HttpException(HttpStatus.FORBIDDEN)
        }

        return repository.findById(userId) ?: run {
            log.error(
                "CRITICAL: Failed to find current user: User with ID {} from context not found in database.",
                userId
            )
            throw HttpException(HttpStatus.FORBIDDEN)
        }
    }

    fun getCurrentUser(): UserDTO {
        log.trace("Mapping current user to DTO.")
        return findCurrentUser().toDTO()
    }

    fun findUserByEmail(email: String): User? {
        log.debug("Searching for user with email: {}", email)
        val user = repository.findByEmail(email)
        log.debug("Search for user with email {} completed. Found: {}", email, user != null)
        return user
    }

    fun update(req: UpdateUserRequest): UserDTO {
        val currentUser = findCurrentUser()
        log.info("Attempting to update profile for user {}.", currentUser.id)

        var user = currentUser
        var reAuth = false
        var updated = false

        req.email?.let {
            if (user.email != it) {
                log.debug("User {} is attempting to update email to {}.", user.id, it)
                if (repository.existsUserByEmail(it)) {
                    log.warn("Update failed for user {}: New email {} already exists.", user.id, it)
                    throw HttpException(HttpStatus.BAD_REQUEST, "Usuário já cadastrado com esse email")
                }
                user.email = it
                reAuth = true
                updated = true
                log.info("User {} email updated.", user.id)
            }
        }

        req.username?.let {
            if (user.username != it) {
                log.debug("User {} is updating username to {}.", user.id, it)
                user.username = it
                updated = true
                log.info("User {} username updated.", user.id)
            }
        }

        req.password?.let {
            log.debug("User {} is updating their password.", user.id)
            if (req.password != req.confirmPassword) {
                log.warn("Password update for user {} failed: Passwords do not match.", user.id)
                throw HttpException(HttpStatus.BAD_REQUEST, "As senhas não são iguais")
            }
            user.password = hash(req.password)
            reAuth = true
            updated = true
            log.info("User {} password updated.", user.id)
        }

        if (!updated) {
            log.info(
                "No updatable fields were provided or values were unchanged for user {}. No update performed.",
                user.id
            )
            return user.toDTO()
        }

        if (reAuth) {
            log.info(
                "Sensitive field (email/password) updated for user {}. All refresh tokens will be revoked.",
                user.id
            )
            refreshTokenService.deleteByUser(user)
        }

        log.info("Saving updated profile for user {}.", user.id)
        user = repository.save(user)

        log.info("User profile for {} updated successfully.", user.id)
        return user.toDTO()
    }

    private fun hash(s: String): String {
        log.trace("Encoding a password string.")
        return passwordEncoder.encode(s)
    }
}