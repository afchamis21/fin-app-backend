package io.github.afchamis21.finapp.repo.cache

import io.github.afchamis21.finapp.domain.user.User
import io.github.afchamis21.finapp.repo.cache.contracts.InMemoryCache
import org.springframework.stereotype.Repository
import java.util.concurrent.ConcurrentHashMap

@Repository
class UserCache : InMemoryCache<Long, User>(ConcurrentHashMap()) {
}