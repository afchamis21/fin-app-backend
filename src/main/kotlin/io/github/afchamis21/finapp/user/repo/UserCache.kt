package io.github.afchamis21.finapp.user.repo

import io.github.afchamis21.finapp.repo.InMemoryCache
import io.github.afchamis21.finapp.user.model.User
import org.springframework.stereotype.Repository
import java.util.concurrent.ConcurrentHashMap

@Repository
class UserCache : InMemoryCache<Long, User>(ConcurrentHashMap()) {
}