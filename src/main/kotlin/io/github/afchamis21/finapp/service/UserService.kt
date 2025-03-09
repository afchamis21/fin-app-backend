package io.github.afchamis21.finapp.service

import io.github.afchamis21.finapp.config.logger
import io.github.afchamis21.finapp.context.Context
import io.github.afchamis21.finapp.domain.user.User
import io.github.afchamis21.finapp.exceptions.HttpException
import io.github.afchamis21.finapp.http.dto.UserDTO
import io.github.afchamis21.finapp.http.dto.toDTO
import io.github.afchamis21.finapp.http.request.user.RegisterUserRequest
import io.github.afchamis21.finapp.http.request.user.UpdateUserRequest
import io.github.afchamis21.finapp.repo.UserJpaRepository
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service

@Service
class UserService(
    private val repository: UserJpaRepository,
    private val passwordEncoder: BCryptPasswordEncoder,
    private val refreshTokenService: RefreshTokenService
) {
    private val log = logger()

    fun register(req: RegisterUserRequest): UserDTO {
        log.info("Registering user with payload [{}]", req)

        if (req.password != req.confirmPassword) {
            throw HttpException(HttpStatus.BAD_REQUEST, "As senhas não são iguais")
        }

        if (repository.existsUserByEmail(req.email)) {
            throw HttpException(HttpStatus.BAD_REQUEST, "Usuário já cadastrado com esse email")
        }

        val user = User(
            email = req.email,
            username = req.username,
            password = hash(req.password)
        )

        log.info("User registered successfully!")

        return repository.save(user).toDTO()
    }

    fun findCurrentUser(): User {
        log.info("Searching for current user (ID: [{}])", Context.userId)
        val forbiddenEx = HttpException(HttpStatus.FORBIDDEN)
        val userId = Context.userId ?: throw forbiddenEx
        return repository.findById(userId).orElseThrow { forbiddenEx }
    }

    fun getCurrentUser(): UserDTO {
        return findCurrentUser().toDTO()
    }

    fun findUserByEmail(email: String): User? {
        log.info("Searching for user with email [{}]", email)
        return repository.findByEmail(email)
    }

    fun update(req: UpdateUserRequest): UserDTO {
        log.info("Updating current user with payload [{}]", req)
        var user = findCurrentUser()

        var reAuth = false;
        var updated = false;
        req.email?.let {
            if (user.email != it && repository.existsUserByEmail(it)) {
                throw HttpException(HttpStatus.BAD_REQUEST, "Usuário já cadastrado com esse email")
            }

            log.info("Email [{}] is not null! Updating...", it)
            user.email = it

            reAuth = true;
            updated = true;
        }

        req.username?.let {
            log.info("Username [{}] is not null! Updating...", it)
            user.username = it

            updated = true;
        }

        req.password?.let {
            log.info(
                "Password is not null! (Is confirm password null [{}]) Updating...",
                req.confirmPassword == null
            )

            if (req.password != req.confirmPassword) {
                throw HttpException(HttpStatus.BAD_REQUEST, "As senhas não são iguais")
            }

            user.password = hash(req.password)
            reAuth = true;
            updated = true;
        }

        if (!updated) {
            return user.toDTO()
        }

        if (reAuth) {
            refreshTokenService.deleteByUser(user)
        }

        user = repository.save(user)

        return repository.save(user).toDTO()
    }

    private fun hash(s: String): String {
        return passwordEncoder.encode(s)
    }
}