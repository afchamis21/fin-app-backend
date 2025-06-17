package io.github.afchamis21.finapp.http.dto

import org.springframework.ai.chat.messages.MessageType

data class ChatMessageDTO(val content: String, val kind: MessageType)
