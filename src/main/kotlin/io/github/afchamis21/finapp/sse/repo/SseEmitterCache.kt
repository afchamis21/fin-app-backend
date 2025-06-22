package io.github.afchamis21.finapp.sse.repo

import io.github.afchamis21.finapp.repo.InMemoryCache
import io.github.afchamis21.finapp.sse.model.UserSseEmitters
import org.springframework.stereotype.Repository
import java.util.concurrent.ConcurrentHashMap

@Repository
class SseEmitterCache : InMemoryCache<Long, UserSseEmitters>(ConcurrentHashMap()) {
}