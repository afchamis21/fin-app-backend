package io.github.afchamis21.finapp.sse.model

import io.github.afchamis21.finapp.config.logger
import io.github.afchamis21.finapp.repo.ICacheable
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.util.concurrent.CopyOnWriteArrayList

class UserSseEmitters(val userId: Long) : ICacheable<Long> {
    private val emitters = CopyOnWriteArrayList<SseEmitter>()
    private val log = logger()

    /**
     * Returns a defensive copy (snapshot) of the list of emitters.
     */
    fun getEmitters(): List<SseEmitter> {
        log.debug("Snapshot of emitters list requested for user $userId. Count: ${emitters.size}.")
        return emitters.toList()
    }

    /**
     * Adds a new emitter to this user's collection.
     */
    fun register(emitter: SseEmitter) {
        emitters.add(emitter)
        log.info("Registered new emitter ${emitter.hashCode()} for user $userId. Total active emitters for this user: ${emitters.size}.")
    }

    /**
     * Removes a specific emitter from this user's collection.
     */
    fun unregister(emitter: SseEmitter) {
        val removed = emitters.remove(emitter)
        if (removed) {
            log.info("Unregistered emitter ${emitter.hashCode()} for user $userId. Total active emitters for this user: ${emitters.size}.")
        } else {
            log.warn("Attempted to unregister emitter ${emitter.hashCode()} for user $userId, but it was not found in the list.")
        }
    }

    /**
     * Sends an event in parallel to all active emitters for this user.
     * If an emitter fails, it is automatically removed from the list.
     */
    suspend fun emit(event: SseEmitter.SseEventBuilder) {
        if (emitters.isEmpty()) {
            log.debug("Skipping emit for user $userId: no active emitters.")
            return
        }

        log.info("Broadcasting event to ${emitters.size} emitter(s) for user $userId.")
        coroutineScope {
            emitters.forEach { emitter ->
                launch {
                    try {
                        emitter.send(event)
                        log.trace("Successfully sent event to emitter ${emitter.hashCode()} for user $userId.")
                    } catch (e: Exception) {
                        log.warn("Failed to send event to emitter ${emitter.hashCode()} for user $userId. Removing it. Error: ${e.message}")
                        unregister(emitter)
                    }
                }
            }
        }
    }

    override fun getCacheKey(): Long {
        return userId
    }

    fun isEmpty(): Boolean {
        return emitters.isEmpty()
    }
}