package io.github.afchamis21.finapp.ai.chat.repo

import io.github.afchamis21.finapp.ai.chat.model.ChatContent
import io.github.afchamis21.finapp.category.repo.CategoryJpaRepository
import io.github.afchamis21.finapp.config.logger
import io.github.afchamis21.finapp.user.model.User
import org.springframework.ai.chat.messages.Message
import org.springframework.stereotype.Repository

@Repository
class ChatContentRepository(
    private val chatCache: ChatCache,
    private val categoryJpaRepository: CategoryJpaRepository
) {
    private val log = logger()

    fun findChatByUser(user: User): ChatContent {
        return findChatByUser(user.getCacheKey())
    }

    fun existsByUser(user: User): Boolean {
        return existsByUser(user.getCacheKey())
    }

    private fun existsByUser(userId: Long): Boolean {
        return chatCache.fetch(userId) != null

    }

    /**
     * Core method to fetch ChatContent from the cache.
     * Creates and saves a new instance if a cache miss occurs.
     */
    fun findChatByUser(userId: Long): ChatContent {
        log.debug("Attempting to fetch ChatContent from cache for user {}.", userId)
        val cachedContent = chatCache.fetch(userId)
        if (cachedContent != null) {
            log.info("Cache HIT for ChatContent for user {}.", userId)
            return cachedContent
        }

        log.info("Cache MISS for ChatContent for user {}. Creating and saving a new instance.", userId)
        return chatCache.save(ChatContent(userId))
    }

    fun saveMessages(user: User, messages: List<Message>) {
        val userId = user.getCacheKey()
        log.info("Saving {} new message(s) to cached ChatContent for user {}.", messages.size, userId)
        val content = findChatByUser(userId)

        content.addMessage(messages)
        log.info("Messages successfully added to in-memory chat history for user {}.", userId)
    }

    fun deleteByUser(user: User) {
        deleteByUser(user.getCacheKey())
    }

    /**
     * Deletes a user's entire chat content (history, categories) from the cache.
     */
    fun deleteByUser(userId: Long) {
        log.info("Deleting entire ChatContent from cache for user {}.", userId)
        chatCache.delete(userId)
        log.info("ChatContent for user {} has been successfully deleted from cache.", userId)
    }

    fun findMessagesByUser(user: User): List<Message> {
        return findMessagesByUser(user.getCacheKey())
    }

    fun findMessagesByUser(userId: Long): List<Message> {
        log.debug("Fetching messages from cached ChatContent for user {}.", userId)
        return findChatByUser(userId).getMessages()
    }
}