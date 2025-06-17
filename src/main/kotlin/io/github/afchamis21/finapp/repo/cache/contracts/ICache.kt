package io.github.afchamis21.finapp.repo.cache.contracts

interface ICache<K, V : ICacheable<K>> {
    fun save(value: V): V
    fun fetch(key: K): V?
    fun delete(key: K)
    fun load(values: List<V>)
}