package io.github.afchamis21.finapp.http.dto

import io.github.afchamis21.finapp.domain.user.User

data class UserDTO(
    val id: Long?,
    val username: String,
    val email: String
)

fun User.toDTO(): UserDTO = UserDTO(
    id = this.id,
    username = this.username,
    email = this.email
)