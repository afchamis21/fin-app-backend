package io.github.afchamis21.finapp.ai.chat.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class ChatMessageRequest(
    @field:NotBlank(message = "A mensagem é obrigatório")
    @field:Size(max = 200, message = "As mensagens ao chat devem ter no máximo 200 caracteres")
    val message: String
)