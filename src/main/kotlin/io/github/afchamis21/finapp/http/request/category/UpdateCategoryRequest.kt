package io.github.afchamis21.finapp.http.request.category

import io.github.afchamis21.finapp.domain.category.CategoryType
import java.math.BigDecimal

data class UpdateCategoryRequest(
    var label: String?,
    var color: String?,
    var type: CategoryType?,
    var goal: BigDecimal?
)
