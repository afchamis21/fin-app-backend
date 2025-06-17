package io.github.afchamis21.finapp.repo.cache.contracts

import java.util.concurrent.ConcurrentMap

abstract class InMemoryCache<K, V : ICacheable<K>>(private val cache: ConcurrentMap<K, V>) : ICache<K, V> {
    override fun save(value: V): V {
        cache[value.getCacheKey()] = value
        return value
    }

    override fun fetch(key: K): V? {
        return cache.getOrDefault(key, null)
    }

    override fun delete(key: K) {
        cache.remove(key)
    }

    override fun load(values: List<V>) {
        cache.putAll(values.associateBy { it.getCacheKey() })
    }
}