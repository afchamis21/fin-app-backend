package io.github.afchamis21.finapp.auth.request

import jakarta.validation.constraints.NotNull

data class RefreshRequest(
    @field:NotNull(message = "O refresh token é obrigatório") val refreshToken: String
)
