package io.github.afchamis21.finapp.auth.dto

import io.github.afchamis21.finapp.user.dto.UserDTO
import java.time.Instant

data class LoginDTO(
    val accessToken: String,
    val refreshToken: String?,
    val expiresAt: Instant,
    val user: UserDTO
)
