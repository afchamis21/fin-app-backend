package io.github.afchamis21.finapp.http.dto

import java.time.Instant

data class LoginDTO(
    val accessToken: String,
    val refreshToken: String?,
    val expiresAt: Instant,
    val user: UserDTO
)
