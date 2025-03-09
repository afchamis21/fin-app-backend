package io.github.afchamis21.finapp.service

import io.github.afchamis21.finapp.context.Context
import io.github.afchamis21.finapp.domain.entry.Entry
import io.github.afchamis21.finapp.exceptions.HttpException
import io.github.afchamis21.finapp.http.dto.EntryDTO
import io.github.afchamis21.finapp.http.dto.toDTO
import io.github.afchamis21.finapp.http.request.entry.CreateEntryRequest
import io.github.afchamis21.finapp.http.request.entry.UpdateEntryRequest
import io.github.afchamis21.finapp.repo.EntryJpaRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class EntryService(
    private val userService: UserService,
    private val categoryService: CategoryService,
    private val entryJpaRepository: EntryJpaRepository,
) {
    fun create(req: CreateEntryRequest): List<EntryDTO> {
        val user = userService.findCurrentUser()
        val entries = req.data.map { data ->
            val categories = when (data.categories.size) {
                0 -> mutableSetOf()
                else -> categoryService.findCategoriesByIds(data.categories)
            }

            Entry(
                label = data.label,
                value = data.value,
                date = data.date,
                categories = categories,
                owner = user
            )
        }

        return entryJpaRepository.saveAll(entries).map { it.toDTO() }
    }

    fun search(startDt: LocalDate, endDt: LocalDate): List<EntryDTO> {
        val user = userService.findCurrentUser()

        return entryJpaRepository.findAllByOwnerAndDateBetween(
            user, startDt, endDt
        ).map { entry -> entry.toDTO() }
    }


    fun update(req: UpdateEntryRequest, id: Long): EntryDTO {
        val userId = Context.userId ?: throw HttpException(HttpStatus.FORBIDDEN)

        val entry = entryJpaRepository.findByIdAndOwnerId(id, userId) ?: throw HttpException(HttpStatus.FORBIDDEN)

        var updated = false

        req.value?.let {
            entry.value = it
            updated = true
        }

        req.label?.let {
            entry.label = it
            updated = true
        }

        req.date?.let {
            entry.date = it
            updated = true
        }

        if (!req.categories.isNullOrEmpty() && req.categories != entry.categories.map { it.id }) {
            entry.categories = categoryService.findCategoriesByIds(req.categories)
            updated = true
        } else if (req.categories.isNullOrEmpty()) {
            entry.categories = mutableSetOf()
            updated = true
        }

        if (!updated) {
            return entry.toDTO()
        }

        return entryJpaRepository.save(entry).toDTO()
    }

    fun delete(id: Long) {
        val ownerId = Context.userId ?: throw HttpException(HttpStatus.FORBIDDEN)
        entryJpaRepository.deleteAllByIdAndOwnerId(id, ownerId)
    }
}