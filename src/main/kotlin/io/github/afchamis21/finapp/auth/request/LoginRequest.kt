package io.github.afchamis21.finapp.auth.request

import jakarta.validation.constraints.NotBlank

data class LoginRequest(
    @field:NotBlank(message = "O email é obrigatório") val email: String,
    @field:NotBlank(message = "A senha é obrigatória") val password: String
)
