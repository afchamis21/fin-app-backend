package io.github.afchamis21.finapp.ai.chat.model

import io.github.afchamis21.finapp.repo.ICacheable
import org.springframework.ai.chat.messages.Message

class ChatContent(
    private val userId: Long
) : ICacheable<Long> {
    private val MAX_SIZE = 50
    private val messages: ArrayDeque<Message> = ArrayDeque()

    override fun getCacheKey(): Long {
        return userId
    }

    fun addMessage(message: Message) {
        if (messages.size >= MAX_SIZE) {
            messages.removeFirst()
        }

        messages.add(message)
    }

    fun addMessage(messages: List<Message>) {
        messages.forEach { addMessage(it) }
    }

    fun getMessages(): List<Message> {
        return messages.toList()
    }
}