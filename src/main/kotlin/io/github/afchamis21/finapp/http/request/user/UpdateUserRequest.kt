package io.github.afchamis21.finapp.http.request.user

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class UpdateUserRequest(
    @field:Email(message = "Email inválido")
    val email: String?,

    @field:Size(min = 5, max = 20, message = "O nome de usuário deve ter de 5 à 20 caracteres")
    @field:Pattern(
        message = "O nome de usuário só pode conter letras, números e espaço",
        regexp = "^[A-Za-zÀ-ÖØ-öø-ÿ0-9 ]+$"
    )
    val username: String?,

    @field:Size(min = 5, max = 20, message = "A senha deve ter de 5 a 20 caracteres")
    @field:Pattern(
        message = "A senha deve conter pelo menos uma letra maiúscula, uma letra minúscula, um número e um caractere especial, sem espaços ou acentos",
        regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@#$%^&+=!])[A-Za-z\\d@#$%^&+=!]+$"
    )
    val password: String?,

    val confirmPassword: String?
)
