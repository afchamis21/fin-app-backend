package io.github.afchamis21.finapp.repo

import io.github.afchamis21.finapp.domain.category.Category
import io.github.afchamis21.finapp.domain.chat.ChatContent
import io.github.afchamis21.finapp.domain.user.User
import io.github.afchamis21.finapp.repo.cache.ChatCache
import io.github.afchamis21.finapp.repo.jpa.CategoryJpaRepository
import org.springframework.ai.chat.messages.Message
import org.springframework.stereotype.Repository

@Repository
class ChatContentRepository(
    private val chatCache: ChatCache,
    private val categoryJpaRepository: CategoryJpaRepository
) {
    fun findChatByUser(user: User): ChatContent {
        return findChatByUser(user.getCacheKey())
    }

    fun findChatByUser(userId: Long): ChatContent {
        return chatCache.fetch(userId) ?: chatCache.save(ChatContent(userId))
    }

    fun saveMessages(user: User, messages: List<Message>) {
        val content = findChatByUser(user)

        content.addMessage(messages)
    }

    fun findCategoriesByUser(user: User): List<Category> {
        val chat = findChatByUser(user)

        if (chat.getMessages().isEmpty()) {
            loadCategories(user.getCacheKey())
        }

        return chat.getCategories()
    }

    fun loadCategories(userId: Long) {
        val chat = findChatByUser(userId)

        val messages = categoryJpaRepository.findAllByOwnerIdAndActive(userId, true)
        chat.loadCategories(messages)
    }

    fun deleteByUser(user: User) {
        deleteByUser(user.getCacheKey())
    }

    fun deleteByUser(userId: Long) {
        chatCache.delete(userId)
    }

    fun findMessagesByUser(user: User): List<Message> {
        return findChatByUser(user).getMessages()
    }

    fun findMessagesByUser(userId: Long): List<Message> {
        return findChatByUser(userId).getMessages()
    }

}