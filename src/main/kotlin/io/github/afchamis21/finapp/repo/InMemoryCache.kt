package io.github.afchamis21.finapp.repo

import io.github.afchamis21.finapp.config.logger
import java.util.concurrent.ConcurrentMap

abstract class InMemoryCache<K, V : ICacheable<K>>(private val cache: ConcurrentMap<K, V>) : ICache<K, V> {
    protected val log = logger()

    override fun save(value: V): V {
        val key = value.getCacheKey()
        log.debug("Saving/updating value of type '{}' in cache with key '{}'.", value::class.simpleName, key)
        cache[key] = value
        log.debug("Value successfully saved for key '{}'. Current cache size: {}.", key, cache.size)
        return value
    }

    override fun fetch(key: K): V? {
        log.trace("Attempting to fetch from cache with key '{}'.", key)
        val value = cache[key]
        if (value != null) {
            log.debug("Cache HIT for key '{}'.", key)
        } else {
            log.debug("Cache MISS for key '{}'.", key)
        }
        return value
    }

    override fun delete(key: K) {
        log.debug("Attempting to delete from cache with key '{}'.", key)
        val removedValue = cache.remove(key)
        if (removedValue != null) {
            log.info("Successfully deleted item from cache with key '{}'. Current cache size: {}.", key, cache.size)
        } else {
            log.warn("Attempted to delete item with key '{}', but it was not found in the cache.", key)
        }
    }

    override fun load(values: List<V>) {
        if (values.isEmpty()) {
            log.info("Bulk load called with an empty list. No changes made to the cache.")
            return
        }
        log.info(
            "Performing bulk load of {} items of type '{}' into the cache.",
            values.size,
            values.first()::class.simpleName
        )
        val newEntries = values.associateBy { it.getCacheKey() }
        cache.putAll(newEntries)
        log.info("Bulk load complete. Cache now contains {} items.", cache.size)
        log.debug("Keys loaded: {}", newEntries.keys)
    }
}