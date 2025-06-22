package io.github.afchamis21.finapp.entry.repo

import io.github.afchamis21.finapp.entry.model.Entry
import io.github.afchamis21.finapp.user.model.User
import jakarta.transaction.Transactional
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
interface EntryJpaRepository : JpaRepository<Entry, Long> {
    fun findAllByOwnerAndDateBetween(
        owner: User,
        start: LocalDate,
        end: LocalDate
    ): List<Entry>

    fun findByIdAndOwnerId(id: Long, userId: Long): Entry?

    fun findAllByOwnerIdOrderByDateAsc(ownerId: Long): List<Entry>

    @Modifying
    @Transactional
    fun deleteAllByIdAndOwnerId(id: Long, ownerId: Long)
}