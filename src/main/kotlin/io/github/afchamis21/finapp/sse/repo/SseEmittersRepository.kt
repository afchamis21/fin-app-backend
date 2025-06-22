package io.github.afchamis21.finapp.sse.repo

import io.github.afchamis21.finapp.config.logger
import io.github.afchamis21.finapp.sse.model.UserSseEmitters
import org.springframework.stereotype.Repository
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter

@Repository
class SseEmittersRepository(
    private val cache: SseEmitterCache
) {
    private val log = logger()

    fun save(userSseEmitters: UserSseEmitters): UserSseEmitters {
        log.info("Saving new UserSseEmitters container to cache for user ${userSseEmitters.userId}.")
        return cache.save(userSseEmitters)
    }

    fun fetch(userId: Long): UserSseEmitters? {
        log.debug("Fetching UserSseEmitters container for user $userId from cache.")
        val emitters = cache.fetch(userId)
        if (emitters == null) {
            log.debug("No UserSseEmitters container found in cache for user $userId.")
        }
        return emitters
    }

    fun register(userId: Long, emitter: SseEmitter) {
        log.info("Processing registration for emitter ${emitter.hashCode()} for user $userId.")

        var userEmitters = fetch(userId)

        if (userEmitters == null) {
            log.info("No existing emitter container found for user $userId. Creating and saving a new one.")
            userEmitters = save(UserSseEmitters(userId))
        } else {
            log.debug("Found existing emitter container for user $userId.")
        }

        userEmitters.register(emitter)
        log.debug("Emitter ${emitter.hashCode()} passed to container for user $userId.")
    }

    fun unregister(userId: Long, emitter: SseEmitter) {
        log.info("Processing unregistration for emitter ${emitter.hashCode()} for user $userId.")

        val userEmitters = fetch(userId)

        if (userEmitters != null) {
            userEmitters.unregister(emitter)
        } else {
            log.warn("Could not unregister emitter ${emitter.hashCode()}: No container found for user $userId.")
        }
    }
}