package io.github.afchamis21.finapp.http.request.auth

import jakarta.validation.constraints.NotNull

data class RefreshRequest(
    @field:NotNull(message = "O refresh token é obrigatório") val refreshToken: String
)
