package io.github.afchamis21.finapp.category.request

import io.github.afchamis21.finapp.category.model.CategoryType
import java.math.BigDecimal

data class UpdateCategoryRequest(
    var label: String?,
    var color: String?,
    var type: CategoryType?,
    var goal: BigDecimal?
)
