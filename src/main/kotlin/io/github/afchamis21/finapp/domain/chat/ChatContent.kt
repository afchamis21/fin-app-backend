package io.github.afchamis21.finapp.domain.chat

import io.github.afchamis21.finapp.domain.category.Category
import io.github.afchamis21.finapp.repo.cache.contracts.ICacheable
import org.springframework.ai.chat.messages.Message

class ChatContent(
    private val userId: Long
) : ICacheable<Long> {
    private val MAX_SIZE = 50
    private val messages: ArrayDeque<Message> = ArrayDeque()
    private val categories: MutableList<Category> = mutableListOf()

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

    fun addCategory(category: Category) {
        categories.add(category)
    }

    fun addCategory(categories: List<Category>) {
        this.categories.addAll(categories)
    }

    fun getCategories(): List<Category> {
        return categories.toList()
    }

    fun loadCategories(messages: List<Category>) {
        this.categories.clear()
        addCategory(messages)
    }
}