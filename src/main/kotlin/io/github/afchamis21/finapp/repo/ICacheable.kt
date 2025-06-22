package io.github.afchamis21.finapp.repo

interface ICacheable<K> {
    fun getCacheKey(): K
}