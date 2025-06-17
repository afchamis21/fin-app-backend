package io.github.afchamis21.finapp.service

import io.github.afchamis21.finapp.context.Context
import io.github.afchamis21.finapp.domain.category.Category
import io.github.afchamis21.finapp.exceptions.HttpException
import io.github.afchamis21.finapp.http.dto.CategoryDTO
import io.github.afchamis21.finapp.http.dto.toDTO
import io.github.afchamis21.finapp.http.request.category.CreateCategoryRequest
import io.github.afchamis21.finapp.http.request.category.UpdateCategoryRequest
import io.github.afchamis21.finapp.repo.jpa.CategoryJpaRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service

@Service
class CategoryService(private val userService: UserService, private val categoryJpaRepository: CategoryJpaRepository) {
    fun createCategory(req: CreateCategoryRequest): CategoryDTO {
        val user = userService.findCurrentUser()

        val category = Category(
            owner = user,
            type = req.type,
            color = req.color,
            label = req.label,
            active = true,
            goal = req.goal
        )

        return categoryJpaRepository.save(category).toDTO()
    }

    fun updateCategory(id: Long, req: UpdateCategoryRequest): CategoryDTO {
        val userId = Context.userId ?: throw HttpException(HttpStatus.FORBIDDEN)

        val category = categoryJpaRepository.findByIdAndOwnerId(id, userId) ?: throw HttpException(HttpStatus.FORBIDDEN)

        var updated = false
        req.label?.let {
            category.label = it
            updated = true
        }

        req.color?.let {
            category.color = it
            updated = true
        }

        req.type?.let {
            category.type = it
            updated = true
        }

        req.goal?.let {
            category.goal = it
            updated = true
        }

        if (!updated) {
            return category.toDTO()
        }

        return categoryJpaRepository.save(category).toDTO()
    }

    fun getCategoriesByOwner(active: Boolean?): List<CategoryDTO> {
        val userId = Context.userId ?: throw HttpException(HttpStatus.FORBIDDEN)
        val categories = when (active) {
            null -> categoryJpaRepository.findAllByOwnerId(userId)
            true, false -> categoryJpaRepository.findAllByOwnerIdAndActive(userId, active)
        }

        return categories.map { it.toDTO() }
    }

    fun deleteCategory(id: Long) {
        val userId = Context.userId ?: throw HttpException(HttpStatus.FORBIDDEN)
        categoryJpaRepository.deleteByIdAndOwnerId(id, userId)
    }

    fun updateStatus(id: Long, active: Boolean) {
        val userId = Context.userId ?: throw HttpException(HttpStatus.FORBIDDEN)
        return categoryJpaRepository.updateByIdAndOwnerIdSetActive(id, userId, active)
    }

    fun findCategoriesByIds(ids: List<Long>, currentUserOnly: Boolean = true): MutableSet<Category> {
        if (currentUserOnly) {
            return categoryJpaRepository.findAllByIdInAndOwnerId(
                ids,
                Context.userId ?: throw HttpException(HttpStatus.FORBIDDEN)
            )
        }

        return categoryJpaRepository.findAllByIdIn(ids)
    }
}