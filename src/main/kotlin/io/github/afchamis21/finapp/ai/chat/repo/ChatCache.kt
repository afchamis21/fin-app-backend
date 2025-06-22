package io.github.afchamis21.finapp.ai.chat.repo

import io.github.afchamis21.finapp.ai.chat.model.ChatContent
import io.github.afchamis21.finapp.repo.InMemoryCache
import org.springframework.stereotype.Repository
import java.util.concurrent.ConcurrentHashMap

@Repository
class ChatCache : InMemoryCache<Long, ChatContent>(ConcurrentHashMap()) {
}