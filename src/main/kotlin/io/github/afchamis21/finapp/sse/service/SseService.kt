package io.github.afchamis21.finapp.sse.service

import io.github.afchamis21.finapp.config.logger
import io.github.afchamis21.finapp.exceptions.HttpException
import io.github.afchamis21.finapp.http.Context
import io.github.afchamis21.finapp.sse.model.SSE_EVENTS
import io.github.afchamis21.finapp.sse.repo.SseEmittersRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Repository
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.time.Duration
import java.time.temporal.ChronoUnit

@Repository
class SseService(
    private val sseEmittersRepository: SseEmittersRepository,
) {
    private val SSE_EMITTER_TIMEOUT = Duration.of(10, ChronoUnit.MINUTES).toMillis()
    private val log = logger()

    fun register(): SseEmitter {
        val userId = Context.userId ?: throw HttpException(HttpStatus.FORBIDDEN)
        log.info("Registering new SSE emitter for user $userId...")

        val sseEmitter = SseEmitter(SSE_EMITTER_TIMEOUT)

        sseEmitter.onCompletion {
            log.info("Emitter ${sseEmitter.hashCode()} completed for user $userId. Reason: Connection closed.")
            unregister(userId, sseEmitter)
        }

        sseEmitter.onTimeout {
            log.warn("Emitter ${sseEmitter.hashCode()} timed out for user $userId. Completing.")
            sseEmitter.complete()
        }

        sseEmitter.onError { ex ->
            log.error("Error on emitter ${sseEmitter.hashCode()} for user $userId. Cause: ${ex.message}")
            unregister(userId, sseEmitter)
        }

        sseEmittersRepository.register(userId, sseEmitter)
        log.info("Emitter ${sseEmitter.hashCode()} registered successfully for user $userId.")

        return sseEmitter
    }

    /**
     * Removes a specific emitter for a user. Usually called by the callbacks.
     */
    fun unregister(userId: Long, emitter: SseEmitter) {
        log.info("Unregistering emitter ${emitter.hashCode()} from repository for user $userId.")
        sseEmittersRepository.unregister(userId, emitter)
    }

    /**
     * Sends an event to all active connections for the target user (defaults to logged-in user)
     */
    suspend fun send(name: SSE_EVENTS, data: Any, userId: Long? = null) {
        val target = userId ?: Context.userId ?: throw HttpException(HttpStatus.FORBIDDEN)

        log.info("Attempting to send event '$name' to user $target...")

        val userEmitters = sseEmittersRepository.fetch(target)

        if (userEmitters == null || userEmitters.getEmitters().isEmpty()) {
            log.warn("Could not send event '$name': No active emitters found for user $target.")
            return
        }

        log.info("Sending event '$name' to ${userEmitters.getEmitters().size} emitter(s) for user $target.")
        userEmitters.emit(
            SseEmitter.event()
                .name(name.name)
                .data(data)
        )
    }
}
