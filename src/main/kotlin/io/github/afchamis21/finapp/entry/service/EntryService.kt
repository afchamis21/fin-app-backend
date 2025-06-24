package io.github.afchamis21.finapp.entry.service

import io.github.afchamis21.finapp.category.service.CategoryService
import io.github.afchamis21.finapp.config.logger
import io.github.afchamis21.finapp.entry.dto.EntryDTO
import io.github.afchamis21.finapp.entry.dto.toDTO
import io.github.afchamis21.finapp.entry.model.Entry
import io.github.afchamis21.finapp.entry.repo.EntryJpaRepository
import io.github.afchamis21.finapp.entry.request.CreateEntryRequest
import io.github.afchamis21.finapp.entry.request.UpdateEntryRequest
import io.github.afchamis21.finapp.exceptions.HttpException
import io.github.afchamis21.finapp.http.Context
import io.github.afchamis21.finapp.user.service.UserService
import jakarta.transaction.Transactional
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class EntryService(
    private val userService: UserService,
    private val categoryService: CategoryService,
    private val entryJpaRepository: EntryJpaRepository,
) {
    private val log = logger()

    fun create(req: CreateEntryRequest): List<EntryDTO> {
        val user = userService.findCurrentUser()
        log.info("User ${user.id} is creating ${req.data.size} new entries.")

        val entries = req.data.map { data ->
            log.debug("Processing new entry data with label '${data.label}' for user ${user.id}.")
            val categories = when {
                data.categories.isEmpty() -> {
                    log.debug("No categories provided for new entry '${data.label}'.")
                    mutableSetOf()
                }

                else -> {
                    log.debug("Fetching ${data.categories.size} categories by ID for new entry '${data.label}'.")
                    categoryService.findCategoriesByIds(data.categories)
                }
            }

            Entry(
                label = data.label,
                value = data.value,
                date = data.date,
                categories = categories,
                owner = user
            )
        }

        val savedEntries = entryJpaRepository.saveAll(entries)
        log.info("Successfully saved ${savedEntries.size} new entries for user ${user.id}. IDs: ${savedEntries.map { it.id }}.")
        return savedEntries.map { it.toDTO() }
    }

    fun search(startDt: LocalDate, endDt: LocalDate): List<EntryDTO> {
        val user = userService.findCurrentUser()
        log.debug("User {} is searching for entries between {} and {}.", user.id, startDt, endDt)

        val entries = entryJpaRepository.findAllByOwnerAndDateBetween(user, startDt, endDt)
        log.debug("Found ${entries.size} entries for user ${user.id} in the given date range.")
        return entries.map { it.toDTO() }
    }


    fun update(req: UpdateEntryRequest, id: Long): EntryDTO {
        val userId = Context.userId ?: throw HttpException(HttpStatus.FORBIDDEN)
        log.info("User $userId is attempting to update entry $id.")

        val entry = entryJpaRepository.findByIdAndOwnerId(id, userId) ?: run {
            log.warn("Update failed: Entry $id not found for user $userId.")
            throw HttpException(HttpStatus.NOT_FOUND)
        }

        var updated = false
        log.debug("Checking fields to update for entry $id.")

        req.value?.let {
            entry.value = it
            updated = true
            log.debug("Entry $id: 'value' field will be updated.")
        }

        req.label?.let {
            entry.label = it
            updated = true
            log.debug("Entry $id: 'label' field will be updated.")
        }

        req.date?.let {
            entry.date = it
            updated = true
            log.debug("Entry $id: 'date' field will be updated.")
        }

        if (!req.categories.isNullOrEmpty()) {
            val newCategoryIds = req.categories.toSet()
            val existingCategoryIds = entry.categories.map { it.id }.toSet()
            if (newCategoryIds != existingCategoryIds) {
                log.debug("Entry $id: 'categories' field will be updated.")
                entry.categories = categoryService.findCategoriesByIds(req.categories)
                updated = true
            }
        } else if (req.categories != null && entry.categories.isNotEmpty()) {
            log.debug("Entry $id: 'categories' field will be cleared.")
            entry.categories = mutableSetOf()
            updated = true
        }

        if (!updated) {
            log.info("No fields were updated for entry $id as provided values matched existing ones or were null.")
            return entry.toDTO()
        }

        val updatedEntry = entryJpaRepository.save(entry)
        log.info("Successfully updated entry $id for user $userId.")
        return updatedEntry.toDTO()
    }

    @Transactional
    fun delete(id: Long) {
        val ownerId = Context.userId ?: throw HttpException(HttpStatus.FORBIDDEN)
        log.info("User $ownerId is attempting to delete entry $id.")
        entryJpaRepository.deleteAllByIdAndOwnerId(id, ownerId)
        log.info("Delete command issued for entry $id for user $ownerId.")
    }
}