package io.github.afchamis21.finapp.category.repo

import io.github.afchamis21.finapp.category.model.Category
import jakarta.transaction.Transactional
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface CategoryJpaRepository : JpaRepository<Category, Long> {
    fun findByIdAndOwnerId(id: Long, ownerId: Long): Category?

    fun findAllByOwnerId(ownerId: Long): List<Category>

    fun findAllByOwnerIdAndActive(ownerId: Long, active: Boolean): List<Category>

    fun findAllByIdIn(id: List<Long>): MutableSet<Category>
    fun findAllByIdInAndOwnerId(id: List<Long>, ownerId: Long): MutableSet<Category>

    @Modifying
    @Transactional
    fun deleteByIdAndOwnerId(id: Long, ownerId: Long)

    @Modifying
    @Transactional
    @Query("update Category set active = :active where id = :id and owner.id = :ownerId")
    fun updateByIdAndOwnerIdSetActive(id: Long, ownerId: Long, active: Boolean): Int
}