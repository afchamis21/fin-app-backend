package io.github.afchamis21.finapp.category.response

import io.github.afchamis21.finapp.category.model.Category
import io.github.afchamis21.finapp.category.model.CategoryType
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