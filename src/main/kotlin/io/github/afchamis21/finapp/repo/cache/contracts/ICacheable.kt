package io.github.afchamis21.finapp.repo.cache.contracts

interface ICacheable<K> {
    fun getCacheKey(): K
}