package io.github.afchamis21.finapp.http.dto

import io.github.afchamis21.finapp.domain.category.Category
import io.github.afchamis21.finapp.domain.category.CategoryType
import java.math.BigDecimal

data class CategoryDTO(
    val id: Long?,
    val label: String,
    val color: String,
    val type: CategoryType,
    val active: Boolean,
    val goal: BigDecimal?
)

fun Category.toDTO(): CategoryDTO = CategoryDTO(
    id = this.id,
    label = this.label,
    color = this.color,
    type = this.type,
    active = this.active,
    goal = this.goal
)