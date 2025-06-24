package io.github.afchamis21.finapp.category.service

import io.github.afchamis21.finapp.category.model.Category
import io.github.afchamis21.finapp.category.repo.CategoryJpaRepository
import io.github.afchamis21.finapp.category.request.CreateCategoryRequest
import io.github.afchamis21.finapp.category.request.UpdateCategoryRequest
import io.github.afchamis21.finapp.category.response.CategoryDTO
import io.github.afchamis21.finapp.category.response.toDTO
import io.github.afchamis21.finapp.config.logger
import io.github.afchamis21.finapp.exceptions.HttpException
import io.github.afchamis21.finapp.http.Context
import io.github.afchamis21.finapp.user.service.UserService
import jakarta.transaction.Transactional
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service

@Service
class CategoryService(
    private val userService: UserService,
    private val categoryJpaRepository: CategoryJpaRepository
) {
    private val log = logger()

    fun createCategory(req: CreateCategoryRequest): CategoryDTO {
        val user = userService.findCurrentUser()
        log.info("User ${user.id} is creating a new category with label '${req.label}'.")

        val category = Category(
            owner = user,
            type = req.type,
            color = req.color,
            label = req.label,
            active = true,
            goal = req.goal
        )

        val savedCategory = categoryJpaRepository.save(category)
        log.info("Successfully created category with ID ${savedCategory.id} for user ${user.id}.")
        return savedCategory.toDTO()
    }

    fun updateCategory(id: Long, req: UpdateCategoryRequest): CategoryDTO {
        val userId = Context.userId ?: throw HttpException(HttpStatus.FORBIDDEN)
        log.info("User $userId is attempting to update category $id.")

        val category = categoryJpaRepository.findByIdAndOwnerId(id, userId) ?: run {
            log.warn("Update failed: Category $id not found for user $userId.")
            throw HttpException(HttpStatus.NOT_FOUND, "Categoria não encontrada")
        }

        var updated = false
        req.label?.let {
            log.debug("Updating label for category $id.")
            category.label = it
            updated = true
        }

        req.color?.let {
            log.debug("Updating color for category $id.")
            category.color = it
            updated = true
        }

        req.type?.let {
            log.debug("Updating type for category $id.")
            category.type = it
            updated = true
        }

        req.goal?.let {
            log.debug("Updating goal for category $id.")
            category.goal = it
            updated = true
        }

        if (!updated) {
            log.info("No updatable fields provided for category $id. Returning current state.")
            return category.toDTO()
        }

        val updatedCategory = categoryJpaRepository.save(category)
        log.info("Successfully updated category $id for user $userId.")
        return updatedCategory.toDTO()
    }

    fun getCategoriesByOwner(active: Boolean?): List<CategoryDTO> {
        val userId = Context.userId ?: throw HttpException(HttpStatus.FORBIDDEN)
        log.debug("Fetching categories for user {} with active filter: {}.", userId, active ?: "ALL")

        val categories = when (active) {
            null -> categoryJpaRepository.findAllByOwnerId(userId)
            true, false -> categoryJpaRepository.findAllByOwnerIdAndActive(userId, active)
        }

        log.debug("Found ${categories.size} categories for user $userId.")
        return categories.map { it.toDTO() }
    }

    @Transactional
    fun deleteCategory(id: Long) {
        val userId = Context.userId ?: throw HttpException(HttpStatus.FORBIDDEN)
        log.info("User '{}' is attempting to delete category '{}'.", userId, id)

        val associationsCleared = categoryJpaRepository.clearEntryAssociations(id, userId)
        log.info(
            "Cleared {} entry association(s) for category '{}' owned by user '{}'.",
            associationsCleared, id, userId
        )

        val categoriesDeleted = categoryJpaRepository.deleteByIdAndOwnerId(id, userId)

        if (categoriesDeleted > 0) {
            log.info("SUCCESS: Successfully deleted category '{}' for user '{}'.", id, userId)
        } else {
            log.warn(
                "NO ACTION: Category '{}' was not found for user '{}' or was already deleted. " +
                        "No category row was deleted.",
                id, userId
            )
        }
    }

    @Transactional
    fun updateStatus(id: Long, active: Boolean) {
        val userId = Context.userId ?: throw HttpException(HttpStatus.FORBIDDEN)
        log.info("User $userId is attempting to set active=$active for category $id.")
        val updatedCount = categoryJpaRepository.updateByIdAndOwnerIdSetActive(id, userId, active)
        if (updatedCount > 0) {
            log.info("Successfully updated status for category $id.")
        } else {
            log.warn("Status update for category $id failed. Category not found or status was already set.")
        }
    }

    fun findCategoriesByIds(ids: List<Long>, currentUserOnly: Boolean = true): MutableSet<Category> {
        val userId = if (currentUserOnly) Context.userId ?: throw HttpException(HttpStatus.FORBIDDEN) else null
        log.debug("Fetching {} categories by IDs. Scoped to user: {}.", ids.size, userId ?: "ANY")

        val categories = if (currentUserOnly) {
            categoryJpaRepository.findAllByIdInAndOwnerId(ids, userId!!)
        } else {
            categoryJpaRepository.findAllByIdIn(ids)
        }

        log.debug("Found ${categories.size} of the requested ${ids.size} categories.")
        return categories
    }
}